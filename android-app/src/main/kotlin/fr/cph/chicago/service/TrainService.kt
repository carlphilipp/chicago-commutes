/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.service

import android.util.Log
import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.TRAIN_FOLLOW
import fr.cph.chicago.client.CtaRequestType.TRAIN_LOCATION
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.entity.TrainArrivalResponse
import fr.cph.chicago.entity.TrainLocationResponse
import fr.cph.chicago.repository.TrainRepository
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TrainService {

    private val TAG = TrainService::class.java.simpleName
    private val trainRepository = TrainRepository
    private val preferencesService = PreferenceService
    private val ctaClient = CtaClient
    private val simpleDateFormatTrain: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    fun loadFavoritesTrain(): SparseArray<TrainArrival> {
        val trainParams = preferencesService.getFavoritesTrainParams()
        var trainArrivals = SparseArray<TrainArrival>()
        for ((key, value) in trainParams.asMap()) {
            if ("mapid" == key) {
                val list = value as MutableList<String>
                if (list.size < 5) {
                    trainArrivals = getTrainArrivals(trainParams)
                } else {
                    val size = list.size
                    var start = 0
                    var end = 4
                    while (end < size + 1) {
                        val subList = list.subList(start, end)
                        val paramsTemp = ArrayListValuedHashMap<String, String>()
                        for (sub in subList) {
                            paramsTemp.put(key, sub)
                        }
                        val temp = getTrainArrivals(paramsTemp)
                        for (j in 0..temp.size() - 1) {
                            trainArrivals.put(temp.keyAt(j), temp.valueAt(j))
                        }
                        start = end
                        if (end + 3 >= size - 1 && end != size) {
                            end = size
                        } else {
                            end += 3
                        }
                    }
                }
            }
        }

        // Apply filters
        var index = 0
        while (index < trainArrivals.size()) {
            val trainArrival = trainArrivals.valueAt(index++)
            val etas = trainArrival.trainEtas
            trainArrival.trainEtas = etas
                .filter { (station, stop, line) -> preferencesService.getTrainFilter(station.id, line, stop.direction) }
                .sorted()
                .toMutableList()
        }
        return trainArrivals
    }

    fun loadLocalTrainData(): SparseArray<TrainStation> {
        // Force loading train from CSV toi avoid doing it later
        return trainRepository.stations
    }

    fun loadStationTrainArrival(stationId: Int): TrainArrival {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put(App.instance.applicationContext.getString(R.string.request_map_id), stationId.toString())
        val map = getTrainArrivals(params)
        return map.get(stationId, TrainArrival.buildEmptyTrainArrival())
    }

    fun loadTrainEta(runNumber: String, loadAll: Boolean): List<TrainEta> {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put(App.instance.applicationContext.getString(R.string.request_runnumber), runNumber)

        val content = ctaClient.get(TRAIN_FOLLOW, params, TrainArrivalResponse::class.java)
        val arrivals = getTrainArrivalsInternal(content)

        var trainEta = mutableListOf<TrainEta>()
        var index = 0
        while (index < arrivals.size()) {
            val (etas) = arrivals.valueAt(index++)
            if (etas.size != 0) {
                trainEta.add(etas[0])
            }
        }
        trainEta.sort()

        if (!loadAll && trainEta.size > 7) {
            trainEta = trainEta.subList(0, 6)
            val currentDate = Calendar.getInstance().time
            val fakeStation = TrainStation(0, App.instance.getString(R.string.bus_all_results), ArrayList())
            // Add a fake TrainEta cell to alert the user about the fact that only a part of the result is displayed
            val eta = TrainEta.buildFakeEtaWith(fakeStation, currentDate, currentDate, false, false)
            trainEta.add(eta)
        }
        return trainEta
    }

    fun getTrainLocation(line: String): List<Train> {
        val connectParam = ArrayListValuedHashMap<String, String>(1, 1)
        connectParam.put(App.instance.applicationContext.getString(R.string.request_rt), line)
        val result = ctaClient.get(TRAIN_LOCATION, connectParam, TrainLocationResponse::class.java)
        if (result.ctatt.route == null) {
            val error = result.ctatt.errNm
            Log.e(TAG, error)
            return listOf()
        }
        return result.ctatt.route!!
            .flatMap { route -> route.train }
            .map { route -> Train(route.rn.toInt(), route.destNm, route.isApp.toBoolean(), Position(route.lat.toDouble(), route.lon.toDouble()), route.heading.toInt()) }
    }

    fun setStationError(value: Boolean) {
        trainRepository.error = value
    }

    fun getStationError(): Boolean {
        return trainRepository.error
    }

    fun getStation(id: Int): TrainStation {
        return trainRepository.getStation(id)
    }

    fun readPatterns(line: TrainLine): List<Position> {
        return when (line) {
            TrainLine.BLUE -> trainRepository.blueLinePatterns
            TrainLine.BROWN -> trainRepository.brownLinePatterns
            TrainLine.GREEN -> trainRepository.greenLinePatterns
            TrainLine.ORANGE -> trainRepository.orangeLinePatterns
            TrainLine.PINK -> trainRepository.pinkLinePatterns
            TrainLine.PURPLE -> trainRepository.purpleLinePatterns
            TrainLine.RED -> trainRepository.redLinePatterns
            TrainLine.YELLOW -> trainRepository.yellowLinePatterns
            TrainLine.NA -> throw RuntimeException("NA not available")
        }
    }

    fun readNearbyStation(position: Position): List<TrainStation> {
        return trainRepository.readNearbyStation(position)
    }

    fun getStationsForLine(line: TrainLine): List<TrainStation> {
        return trainRepository.allStations[line]!!
    }

    fun searchStations(query: String): List<TrainStation> {
        return getAllStations().entries
            .flatMap { mutableEntry -> mutableEntry.value }
            .filter { station -> StringUtils.containsIgnoreCase(station.name, query) }
            .distinct()
            .sorted()
    }

    private fun getTrainArrivals(params: MultiValuedMap<String, String>): SparseArray<TrainArrival> {
        val trainArrivalResponse = ctaClient.get(TRAIN_ARRIVALS, params, TrainArrivalResponse::class.java)
        return getTrainArrivalsInternal(trainArrivalResponse)
    }

    private fun getTrainArrivalsInternal(trainArrivalResponse: TrainArrivalResponse): SparseArray<TrainArrival> {
        val result = SparseArray<TrainArrival>()
        if (trainArrivalResponse.ctatt.eta == null) {
            val error = trainArrivalResponse.ctatt.errNm
            Log.e(TAG, error)
            return result
        }
        trainArrivalResponse.ctatt.eta.map { eta ->
            val station = getStation(eta.staId.toInt())
            // FIXME: potential issue with name here
            //station.name = eta.staNm
            val stop = getStop(eta.stpId.toInt())
            stop.description = eta.stpDe
            val routeName = TrainLine.fromXmlString(eta.rt)
            val destinationName =
                if ("See train".equals(eta.destNm, ignoreCase = true) && stop.description.contains("Loop") && routeName == TrainLine.GREEN ||
                    "See train".equals(eta.destNm, ignoreCase = true) && stop.description.contains("Loop") && routeName == TrainLine.BROWN ||
                    "Loop, Midway".equals(eta.destNm, ignoreCase = true) && routeName == TrainLine.BROWN)
                    "Loop"
                else
                    eta.destNm

            val trainEta = TrainEta(
                trainStation = station,
                stop = stop,
                routeName = routeName,
                destName = destinationName,
                predictionDate = simpleDateFormatTrain.parse(eta.prdt),
                arrivalDepartureDate = simpleDateFormatTrain.parse(eta.arrT),
                isApp = eta.isApp.toBoolean(),
                isDly = eta.isDly.toBoolean())
            trainEta
        }
            .forEach {
                if (result.indexOfKey(it.trainStation.id) < 0) {
                    result.append(it.trainStation.id, TrainArrival.buildEmptyTrainArrival().addEta(it))
                } else {
                    result.get(it.trainStation.id).addEta(it)
                }
            }
        return result
    }

    private fun getStop(id: Int): Stop {
        return trainRepository.getStop(id)
    }

    private fun getAllStations(): Map<TrainLine, List<TrainStation>> {
        return trainRepository.allStations
    }
}
