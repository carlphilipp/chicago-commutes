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

import fr.cph.chicago.client.DivvyClient
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.entity.DivvyStationInformation
import fr.cph.chicago.entity.DivvyStationStatus
import fr.cph.chicago.entity.StationInformationResponse
import fr.cph.chicago.entity.StationStatusResponse
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import timber.log.Timber
import java.util.concurrent.Callable

object BikeService {

    private val client = DivvyClient
    private val jsonParser = JsonParser
    private val util = Util
    private val preferenceService = PreferenceService

    fun allBikeStations(): Single<List<BikeStation>> {
        return loadAllBikeStations()
    }

    fun findBikeStation(id: Int): Single<BikeStation> {
        return loadAllBikeStations()
            .toObservable()
            .flatMapIterable { station -> station }
            .filter { station -> station.id == id }
            .firstOrError()
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load bike stations")
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun searchBikeStations(query: String): Single<List<BikeStation>> {
        return Single
            .fromCallable {
                store.state.bikeStations
                    .filter { station -> containsIgnoreCase(station.name, query) || containsIgnoreCase(station.address, query) }
                    .distinct()
                    .sortedWith(util.bikeStationComparator)
            }
            .subscribeOn(Schedulers.computation())
    }

    fun createEmptyBikeStation(bikeStationId: Int): BikeStation {
        val stationName = preferenceService.getBikeRouteNameMapping(bikeStationId)
        return BikeStation.buildDefaultBikeStationWithName(stationName, bikeStationId)
    }

    private fun loadAllBikeStations(): Single<List<BikeStation>> {
        val informationSingle = singleFromCallable(Callable { loadStationsInformation() })
        val statusSingle = singleFromCallable(Callable { loadStationsStatus() })
        return Singles.zip(informationSingle, statusSingle, zipper = { info, stat ->
            val res = mutableListOf<BikeStation>()
            for ((key, stationInfo) in info) {
                val stationStatus = stat[key] ?: DivvyStationStatus("", 0, 0)
                res.add(BikeStation(
                    id = stationInfo.station_id.toInt(),
                    name = stationInfo.name,
                    availableDocks = stationStatus.num_docks_available,
                    availableBikes = stationStatus.num_bikes_available,
                    latitude = stationInfo.lat,
                    longitude = stationInfo.lon,
                    address = stationInfo.name))
            }
            res.sortedWith(compareBy(BikeStation::name))
        })
    }

    private fun loadStationsInformation(): Map<String, DivvyStationInformation> {
        val stationsInformationIs = client.getStationsInformation()
        return jsonParser
            .parse(stationsInformationIs, StationInformationResponse::class.java)
            .data.stations.associateBy { it.station_id }
    }

    private fun loadStationsStatus(): Map<String, DivvyStationStatus> {
        val stationsStatusIs = client.getStationsStatus()
        return jsonParser
            .parse(stationsStatusIs, StationStatusResponse::class.java)
            .data.stations.associateBy { it.station_id }
    }
}
