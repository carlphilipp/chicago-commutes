/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.data

import android.content.Context
import android.util.Log
import android.util.SparseArray
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.entity.Position
import fr.cph.chicago.entity.Station
import fr.cph.chicago.entity.Stop
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object TrainData {

    private val TAG = TrainData::class.java.simpleName
    private val DEFAULT_RANGE = 0.008
    // https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme
    private val TRAIN_FILE_PATH = "train_stops.csv"

    private val stations: SparseArray<Station> = SparseArray()
    private val stops: SparseArray<Stop> = SparseArray()
    private val parser: CsvParser
    private var stationsOrderByLineMap: MutableMap<TrainLine, MutableList<Station>> = mutableMapOf()

    init {
        val settings = CsvParserSettings()
        settings.format.setLineSeparator("\n")
        this.parser = CsvParser(settings)
    }

    /**
     * Read train data from CSV file.
     */
    fun read(context: Context) {
        if (stations.size() == 0 && stops.size() == 0) {
            var inputStreamReader: InputStreamReader? = null
            try {
                inputStreamReader = InputStreamReader(context.assets.open(TRAIN_FILE_PATH))
                val allRows = parser.parseAll(inputStreamReader)
                for (i in 1 until allRows.size) {
                    val row = allRows[i]
                    val stopId = Integer.parseInt(row[0]) // STOP_ID
                    val direction = TrainDirection.fromString(row[1]) // DIRECTION_ID
                    val stopName = row[2] // STOP_NAME
                    val stationName = row[3]// STATION_NAME
                    // String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
                    val parentStopId = Integer.parseInt(row[5])// MAP_ID (old PARENT_STOP_ID)
                    val ada = java.lang.Boolean.parseBoolean(row[6])// ADA
                    val lines = ArrayList<TrainLine>()
                    val red = java.lang.Boolean.parseBoolean(row[7])// Red
                    val blue = java.lang.Boolean.parseBoolean(row[8])// Blue
                    val green = java.lang.Boolean.parseBoolean(row[9])// G
                    val brown = java.lang.Boolean.parseBoolean(row[10])// Brn
                    val purple = java.lang.Boolean.parseBoolean(row[11])// P
                    val purpleExp = java.lang.Boolean.parseBoolean(row[12])// Pexp
                    val yellow = java.lang.Boolean.parseBoolean(row[13])// Y
                    val pink = java.lang.Boolean.parseBoolean(row[14])// Pink
                    val orange = java.lang.Boolean.parseBoolean(row[15])// Org
                    if (red) {
                        lines.add(TrainLine.RED)
                    }
                    if (blue) {
                        lines.add(TrainLine.BLUE)
                    }
                    if (brown) {
                        lines.add(TrainLine.BROWN)
                    }
                    if (green) {
                        lines.add(TrainLine.GREEN)
                    }
                    if (purple) {
                        // PURPLE_EXPRESS MOD
                        if (!lines.contains(TrainLine.PURPLE)) {
                            lines.add(TrainLine.PURPLE)
                        }
                    }
                    if (purpleExp) {
                        // PURPLE_EXPRESS MOD
                        if (!lines.contains(TrainLine.PURPLE)) {
                            lines.add(TrainLine.PURPLE)
                        }
                    }
                    if (yellow) {
                        lines.add(TrainLine.YELLOW)
                    }
                    if (pink) {
                        lines.add(TrainLine.PINK)
                    }
                    if (orange) {
                        lines.add(TrainLine.ORANGE)
                    }
                    val location = row[16]// Location
                    val locationTrunk = location.substring(1)
                    val coordinates = locationTrunk.substring(0, locationTrunk.length - 1).split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val longitude = java.lang.Double.parseDouble(coordinates[0])
                    val latitude = java.lang.Double.parseDouble(coordinates[1])

                    val station = Station(parentStopId, stationName, mutableListOf())
                    val stop = Stop(stopId, stopName, direction, Position(longitude, latitude), ada, lines)
                    stops.append(stopId, stop)

                    val currentStation = stations.get(parentStopId, null)
                    if (currentStation == null) {
                        station.stops = mutableListOf(stop)
                        stations.append(parentStopId, station)
                    } else {
                        currentStation.stops.add(stop)
                    }
                }
                stationsOrderByLineMap = sortStation()
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
            } finally {
                IOUtils.closeQuietly(inputStreamReader)
            }
        }
    }

    /**
     * Get all stations
     *
     * @return a map containing all the stations ordered line
     */
    val allStations: MutableMap<TrainLine, MutableList<Station>>
        get() = stationsOrderByLineMap


    /**
     * Get a list of station for a given line
     *
     * @param line the train line
     * @return a list of station
     */

    fun getStationsForLine(line: TrainLine): List<Station>? {
        return stationsOrderByLineMap[line]
    }

    /**
     * get a station
     *
     * @param id the id of the station
     * @return the station
     */
    fun getStation(id: Int): Station { // FIXME should be Station? ??
        val station = stations.get(id)
        return station ?: Station.buildEmptyStation()
    }

    val isStopsNull: Boolean
        get() = stops == null

    /**
     * Get a stop
     *
     * @param id the id of the stop
     * @return a stop
     */
    fun getStop(id: Int): Stop {
        return stops.get(id, Stop.buildEmptyStop())
    }

    /**
     * Read near by station
     *
     * @param position the position
     * @return a list of station
     */
    fun readNearbyStation(position: Position): List<Station> {
        val latitude = position.latitude
        val longitude = position.longitude

        val latMax = latitude + DEFAULT_RANGE
        val latMin = latitude - DEFAULT_RANGE
        val lonMax = longitude + DEFAULT_RANGE
        val lonMin = longitude - DEFAULT_RANGE

        val nearByStations = ArrayList<Station>()
        for (i in 0 until stations.size()) {
            val station = stations.valueAt(i)
            station.stopsPosition
                .filter { stopPosition ->
                    val trainLatitude = stopPosition.latitude
                    val trainLongitude = stopPosition.longitude
                    trainLatitude in latMin..latMax && trainLongitude <= lonMax && trainLongitude >= lonMin
                }
                .getOrElse(0, { Position() })
                .also {
                    if (position.latitude != 0.0 && position.longitude != 0.0) {
                        nearByStations.add(station)
                    }
                }
        }
        return nearByStations
    }

    fun readPattern(context: Context, line: TrainLine): List<Position> {
        var inputStreamReader: InputStreamReader? = null
        try {
            inputStreamReader = InputStreamReader(context.assets.open("train_pattern/" + line.toTextString() + "_pattern.csv"))
            val allRows = parser.parseAll(inputStreamReader)
            return allRows
                .map { row ->
                    val longitude = java.lang.Double.parseDouble(row[0])
                    val latitude = java.lang.Double.parseDouble(row[1])
                    val position = Position()
                    position.latitude = latitude
                    position.longitude = longitude
                    position
                }
                .toMutableList()
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            return ArrayList()
        } finally {
            IOUtils.closeQuietly(inputStreamReader)
        }
    }

    private fun sortStation(): MutableMap<TrainLine, MutableList<Station>> {
        val result = TreeMap<TrainLine, MutableList<Station>>()
        for (i in 0 until stations.size()) {
            val station = stations.valueAt(i)
            val trainLines = station.lines
            for (trainLine in trainLines) {
                if (result.containsKey(trainLine)) {
                    val stations = result[trainLine]
                    stations!!.add(station)
                    Collections.sort(stations)
                } else {
                    val stations = ArrayList<Station>()
                    result.put(trainLine, stations)
                    stations.add(station)
                }
            }
        }
        return result
    }
}
