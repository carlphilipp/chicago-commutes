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

package fr.cph.chicago.repository

import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.repository.entity.BusStopDb
import io.realm.Realm

object BusRepository {

    private const val DEFAULT_RANGE = 0.008

    // FIXME: No state should be allowed here
    var busRouteError: Boolean = false

    var inMemoryBusRoutes: List<BusRoute> = listOf()

    fun isEmpty(): Boolean {
        return inMemoryBusRoutes.isEmpty()
    }

    fun saveBusRoutes(busRoutes: List<BusRoute>) {
        inMemoryBusRoutes = busRoutes
    }

    fun getBusRoute(routeId: String): BusRoute {
        return this.inMemoryBusRoutes
            .filter { (id) -> id == routeId }
            .getOrElse(0) { BusRoute("0", "error") }
    }

    fun hasBusStopsEmpty(): Boolean {
        val realm = Realm.getDefaultInstance()
        return realm.use {
            it.where(BusStopDb::class.java).findFirst() == null
        }
    }

    fun saveBusStops(busStops: List<BusStop>) {
        val realm = Realm.getDefaultInstance()
        realm.use { r ->
            r.executeTransaction {
                busStops
                    .map { busStop -> BusStopDb(busStop) }
                    .forEach { busStopDb -> r.copyToRealm(busStopDb) }
            }
        }
    }

    fun getBusStopsAround(position: Position): List<BusStop> {
        return getBusStopsAround(position, DEFAULT_RANGE)
    }

    /**
     * Get a list of bus stop within a a distance and position
     *
     * @param position the position
     * @return a list of bus stop
     */
    private fun getBusStopsAround(position: Position, range: Double): List<BusStop> {
        val realm = Realm.getDefaultInstance()

        val latitude = position.latitude
        val longitude = position.longitude

        val latMax = latitude + range
        val latMin = latitude - range
        val lonMax = longitude + range
        val lonMin = longitude - range

        return realm.use {
            it.where(BusStopDb::class.java)
                // TODO use between when child object is supported by Realm
                .greaterThan("position.latitude", latMin)
                .lessThan("position.latitude", latMax)
                .greaterThan("position.longitude", lonMin)
                .lessThan("position.longitude", lonMax)
                .sort("name")
                .findAll()
                .map { currentBusStop ->
                    BusStop(
                        id = currentBusStop.id,
                        name = currentBusStop.name,
                        description = currentBusStop.description,
                        position = Position(
                            latitude = currentBusStop.position!!.latitude,
                            longitude = currentBusStop.position!!.longitude
                        )
                    )
                }
        }
    }
}
