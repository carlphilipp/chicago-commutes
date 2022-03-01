/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.repository

import android.util.SparseArray
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.TrainStationPattern
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util
import java.io.IOException
import java.io.InputStreamReader
import java.util.TreeMap
import timber.log.Timber

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object TrainRepository {

    // https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme
    private const val TRAIN_FILE_PATH = "train_stops.csv"

    private val util = Util
    private val parser: CsvParser

    init {
        val settings = CsvParserSettings()
        settings.format.setLineSeparator("\n")
        parser = CsvParser(settings)
    }

    private val inMemoryData: Triple<Map<String, TrainStation>, SparseArray<Stop>, Map<TrainLine, List<TrainStation>>> by lazy {
        loadInMemoryStationsAndStops()
    }

    val stations: Map<String, TrainStation>
        get() = inMemoryData.first

    private val stops: SparseArray<Stop>
        get() = inMemoryData.second

    /**
     * Get all stations
     *
     * @return a map containing all the stations ordered line
     */
    val allStations: Map<TrainLine, List<TrainStation>>
        get() = inMemoryData.third

    val blueLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.BLUE)
    }

    val brownLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.BROWN)
    }

    val greenLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.GREEN)
    }

    val orangeLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.ORANGE)
    }

    val pinkLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.PINK)
    }

    val purpleLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.PURPLE)
    }

    val redLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.RED)
    }

    val yellowLinePatterns: List<TrainStationPattern> by lazy {
        readPattern(TrainLine.YELLOW)
    }

    /**
     * Get a train station
     *
     * @param id the id of the train station
     * @return the train station
     */
    fun getStation(id: String): TrainStation {
        return stations[id] ?: TrainStation.buildEmptyStation()
    }

    /**
     * Get a stop
     *
     * @param id the id of the stop
     * @return a stop
     */
    fun getStop(id: Int): Stop {
        return stops.get(id, Stop.buildEmptyStop())
    }

    private fun loadInMemoryStationsAndStops(): Triple<Map<String, TrainStation>, SparseArray<Stop>, Map<TrainLine, List<TrainStation>>> {
        val stations = mutableMapOf<String, TrainStation>()
        val stops = SparseArray<Stop>()
        var stationsOrderByLine = TreeMap<TrainLine, List<TrainStation>>()

        var inputStreamReader: InputStreamReader? = null
        try {
            inputStreamReader = InputStreamReader(App.instance.resources.assets.open(TRAIN_FILE_PATH))
            val allRows = parser.parseAll(inputStreamReader)
            for (i in 1 until allRows.size) {
                val row: Array<String> = allRows[i]
                val stopId = row[0].toInt() // STOP_ID
                val direction = TrainDirection.fromString(row[1]) // DIRECTION_ID
                val stopName = row[2] // STOP_NAME
                val stationName = row[3]// STATION_NAME
                // String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
                val parentStopId = row[5]// MAP_ID (old PARENT_STOP_ID)
                val ada = row[6].toBoolean()// ADA
                val red = row[7].toBoolean()// Red
                val blue = row[8].toBoolean()// Blue
                val green = row[9].toBoolean()// G
                val brown = row[10].toBoolean()// Brn
                val purple = row[11].toBoolean()// P
                val purpleExp = row[12].toBoolean()// Pexp
                val yellow = row[13].toBoolean()// Y
                val pink = row[14].toBoolean()// Pink
                val orange = row[15].toBoolean()// Org

                val lines = mutableSetOf<TrainLine>()
                if (red) lines.add(TrainLine.RED)
                if (blue) lines.add(TrainLine.BLUE)
                if (brown) lines.add(TrainLine.BROWN)
                if (green) lines.add(TrainLine.GREEN)
                if (purple || purpleExp) lines.add(TrainLine.PURPLE)
                if (yellow) lines.add(TrainLine.YELLOW)
                if (pink) lines.add(TrainLine.PINK)
                if (orange) lines.add(TrainLine.ORANGE)

                val location = row[16]// Location
                val locationTrunk = location.substring(1)
                val coordinates = locationTrunk
                    .substring(0, locationTrunk.length - 1)
                    .split(", ".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val latitude = coordinates[0].toDouble()
                val longitude = coordinates[1].toDouble()

                val station = TrainStation(parentStopId, stationName, listOf())
                val stop = Stop(stopId, stopName, direction, Position(latitude, longitude), ada, lines)
                stops.append(stopId, stop)

                val currentStation = stations[parentStopId]
                if (currentStation == null) {
                    station.stops = mutableListOf(stop)
                    stations[parentStopId] = station
                } else {
                    (currentStation.stops as MutableList).add(stop)
                }
                Timber.d("Load %s", station)
            }
            stationsOrderByLine = sortStation(stations)
        } catch (e: IOException) {
            Timber.e(e, "Error while reading csv file")
        } finally {
            util.closeQuietly(inputStreamReader)
        }
        return Triple(stations, stops, stationsOrderByLine)
    }

    private fun readPattern(line: TrainLine): List<TrainStationPattern> {
        var inputStreamReader: InputStreamReader? = null
        return try {
            inputStreamReader = InputStreamReader(App.instance.resources.assets.open("train_pattern/" + line.toTextString() + "_pattern.csv"))
            parser.parseAll(inputStreamReader)
                .map { row ->
                    val longitude = row[0].toDouble()
                    val latitude = row[1].toDouble()
                    var name: String? = null
                    if (row.size == 3) {
                        name = row[2]
                    }
                    TrainStationPattern(name, Position(latitude, longitude))
                }
        } catch (e: IOException) {
            Timber.e(e, "Error while reading train pattern csv file")
            listOf()
        } finally {
            util.closeQuietly(inputStreamReader)
        }
    }

    private fun sortStation(trainStationNotSorted: Map<String, TrainStation>): TreeMap<TrainLine, List<TrainStation>> {
        return trainStationNotSorted
            .map { it.value }
            .fold(TreeMap()) { accumulator, station ->
                // Accumulate train station in the map
                station.lines.forEach { trainLine -> (accumulator.getOrPut(trainLine, { mutableListOf() }) as MutableList).add(station) }
                // Sort stations by name
                accumulator.values.forEach { stations -> (stations as MutableList).sortBy { it.name } }
                accumulator
            }
    }
}
