/**
 * Copyright 2019 Carl-Philipp Harmant
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

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.TRAIN_FOLLOW
import fr.cph.chicago.client.CtaRequestType.TRAIN_LOCATION
import fr.cph.chicago.client.stationTrainParams
import fr.cph.chicago.client.trainEtasParams
import fr.cph.chicago.client.trainLocationParams
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.TrainStationPattern
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.entity.TrainArrivalResponse
import fr.cph.chicago.entity.TrainLocationResponse
import fr.cph.chicago.parseNotNull
import fr.cph.chicago.repository.TrainRepository
import fr.cph.chicago.rx.RxUtil.handleListError
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Callable
import kotlin.collections.component1
import kotlin.collections.component2

object TrainService {

    private const val DEFAULT_RANGE = 0.008

    private val trainRepository = TrainRepository
    private val preferencesService = PreferenceService
    private val ctaClient = CtaClient
    private val simpleDateFormatTrain: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    fun loadFavoritesTrain(): Single<TrainArrivalDTO> {
        return singleFromCallable(
            Callable {
                val trainParams = preferencesService.getFavoritesTrainParams()
                var trainArrivals = SparseArray<TrainArrival>()
                for ((key, value) in trainParams.asMap()) {
                    if ("mapid" == key) {
                        val list = value as MutableList<String>
                        if (list.size < 5) {
                            trainArrivals = getTrainArrivals(trainParams).blockingGet()
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
                                val temp = getTrainArrivals(paramsTemp).blockingGet()
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
                trainArrivals
            })
            .observeOn(Schedulers.computation())
            .map { favoriteTrains -> TrainArrivalDTO(favoriteTrains, false) }
    }

    fun loadLocalTrainData(): Single<Boolean> {
        // Force loading train from CSV toi avoid doing it later
        return Single
            .fromCallable { trainRepository.stations }
            .map { it.size() == 0 }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not create local train data")
                true
            }
    }

    fun loadStationTrainArrival(stationId: Int): Single<TrainArrival> {
        return getTrainArrivals(stationTrainParams(stationId))
            .observeOn(Schedulers.computation())
            .map { trainArrivals -> trainArrivals.get(stationId, TrainArrival.buildEmptyTrainArrival()) }
            .subscribeOn(Schedulers.io())
    }

    fun trainEtas(runNumber: String, loadAll: Boolean): Single<List<TrainEta>> {
        return ctaClient.get(TRAIN_FOLLOW, trainEtasParams(runNumber), TrainArrivalResponse::class.java)
            .map { trainArrivalResponse ->
                val arrivals = getTrainArrivalsInternal(trainArrivalResponse)
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
                trainEta.toList()
            }
            .onErrorReturn(handleListError())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun trainLocations(line: String): Single<List<Train>> {
        return ctaClient.get(TRAIN_LOCATION, trainLocationParams(line), TrainLocationResponse::class.java)
            .map { trainLocationResponse ->
                if (trainLocationResponse.ctatt.route == null) {
                    val error = trainLocationResponse.ctatt.errNm
                    Timber.e(error)
                    listOf()
                } else {
                    trainLocationResponse.ctatt.route!!
                        .flatMap { route -> route.train }
                        .map { route -> Train(route.rn.toInt(), route.destNm, route.approaching.toBoolean(), Position(route.lat.toDouble(), route.lon.toDouble()), route.heading.toInt()) }
                }
            }
    }

    fun getStation(id: Int): TrainStation {
        return trainRepository.getStation(id)
    }

    fun readPatterns(line: TrainLine): Single<List<TrainStationPattern>> {
        return singleFromCallable(
            Callable {
                when (line) {
                    TrainLine.BLUE -> trainRepository.blueLinePatterns
                    TrainLine.BROWN -> trainRepository.brownLinePatterns
                    TrainLine.GREEN -> trainRepository.greenLinePatterns
                    TrainLine.ORANGE -> trainRepository.orangeLinePatterns
                    TrainLine.PINK -> trainRepository.pinkLinePatterns
                    TrainLine.PURPLE -> trainRepository.purpleLinePatterns
                    TrainLine.RED -> trainRepository.redLinePatterns
                    TrainLine.YELLOW -> trainRepository.yellowLinePatterns
                    else -> listOf()
                }
            })
            .onErrorReturn(handleListError())
    }

    fun readNearbyStation(position: Position): Single<List<TrainStation>> {
        return singleFromCallable(Callable {
            val latMax = position.latitude + DEFAULT_RANGE
            val latMin = position.latitude - DEFAULT_RANGE
            val lonMax = position.longitude + DEFAULT_RANGE
            val lonMin = position.longitude - DEFAULT_RANGE

            (0 until trainRepository.stations.size())
                .map { trainRepository.stations.valueAt(it) }
                .filter {
                    it.stopsPosition.any { stopPosition ->
                        val trainLatitude = stopPosition.latitude
                        val trainLongitude = stopPosition.longitude
                        trainLatitude in latMin..latMax && trainLongitude <= lonMax && trainLongitude >= lonMin
                    }
                }
        })
    }

    fun getStationsForLine(line: TrainLine): List<TrainStation> {
        return if (trainRepository.allStations.containsKey(line))
            trainRepository.allStations[line]!!
        else {
            Timber.e("%s not found", line)
            // Fallback to blue
            trainRepository.allStations[TrainLine.BLUE]!!
        }
    }

    fun searchStations(query: String): Single<List<TrainStation>> {
        return Single
            .fromCallable {
                trainRepository.allStations.entries
                    .flatMap { mutableEntry -> mutableEntry.value }
                    .filter { station -> StringUtils.containsIgnoreCase(station.name, query) }
                    .distinct()
                    .sorted()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    private fun getTrainArrivals(params: MultiValuedMap<String, String>): Single<SparseArray<TrainArrival>> {
        return ctaClient.get(TRAIN_ARRIVALS, params, TrainArrivalResponse::class.java)
            .map { trainArrivalResponse -> getTrainArrivalsInternal(trainArrivalResponse) }
    }

    private fun getTrainArrivalsInternal(trainArrivalResponse: TrainArrivalResponse): SparseArray<TrainArrival> {
        val result = SparseArray<TrainArrival>()
        if (trainArrivalResponse.ctatt.eta == null) {
            val error = trainArrivalResponse.ctatt.errNm
            Timber.e("Error: %s", error)
            return result
        }
        trainArrivalResponse
            .ctatt
            .eta
            .map { eta ->
                val station = getStation(eta.staId.toInt())
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
                    predictionDate = simpleDateFormatTrain.parseNotNull(eta.prdt),
                    arrivalDepartureDate = simpleDateFormatTrain.parseNotNull(eta.arrT),
                    isApp = eta.isApp.toBoolean(),
                    isDly = eta.isDly.toBoolean())
                trainEta
            }
            .filter { (station, stop, line) -> preferencesService.getTrainFilter(station.id, line, stop.direction) }
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
}
