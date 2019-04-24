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
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.containsIgnoreCase

object BikeService {

    private val client = DivvyClient
    private val jsonParser = JsonParser
    private val util = Util
    private val preferenceService = PreferenceService

    fun loadAllBikeStations(): List<BikeStation> {
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

    fun findBikeStation(id: Int): BikeStation {
        return loadAllBikeStations().first { station -> station.id == id }
    }

    fun searchBikeStations(query: String): List<BikeStation> {
        return mainStore.state.bikeStations
            .filter { station -> containsIgnoreCase(station.name, query) || containsIgnoreCase(station.address, query) }
            .distinct()
            .sortedWith(util.bikeStationComparator)
    }

    fun createEmptyBikeStation(bikeId: String): BikeStation {
        val stationName = preferenceService.getBikeRouteNameMapping(bikeId)
        return BikeStation.buildDefaultBikeStationWithName(stationName ?: StringUtils.EMPTY)
    }
}
