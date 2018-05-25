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

import fr.cph.chicago.client.DivvyClient
import fr.cph.chicago.entity.bike.DivvyStation
import fr.cph.chicago.parser.JsonParser
import io.reactivex.exceptions.Exceptions

object BikeService {

    private val client = DivvyClient
    private val jsonParser = JsonParser

    fun loadAllBikeStations(): List<DivvyStation> {
        try {
            val bikeStationsInputStream = client.getBikeStations()
            return jsonParser.parseStations(bikeStationsInputStream).sortedWith(compareBy(DivvyStation::name)).toMutableList()
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun findBikeStation(id: Int): DivvyStation {
        try {
            return loadAllBikeStations().first { (bikeId) -> bikeId == id }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }
}
