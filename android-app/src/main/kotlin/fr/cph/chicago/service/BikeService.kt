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
import fr.cph.chicago.entity.DivvyResponse
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import fr.cph.chicago.util.Util
import io.reactivex.Single
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
        return singleFromCallable(Callable { loadAllBikeStations() })
    }

    fun findBikeStation(id: Int): Single<BikeStation> {
        return singleFromCallable(
            Callable { loadAllBikeStations().first { station -> station.id == id } })
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
        return BikeStation.buildDefaultBikeStationWithName(stationName)
    }

    private fun loadAllBikeStations(): List<BikeStation> {
        val bikeStationsInputStream = client.getBikeStations()
        return jsonParser
            .parse(bikeStationsInputStream, DivvyResponse::class.java)
            .stations
            .map { bikeStation ->
                BikeStation(
                    bikeStation.id,
                    bikeStation.name,
                    bikeStation.availableDocks,
                    bikeStation.availableBikes,
                    bikeStation.latitude,
                    bikeStation.longitude,
                    bikeStation.stAddress1)
            }
            .sortedWith(compareBy(BikeStation::name))
            .toMutableList()
    }
}
