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
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.BusStop
import fr.cph.chicago.entity.Position
import fr.cph.chicago.parser.BusStopCsvParser
import fr.cph.chicago.repository.BusStopRepository
import io.realm.Realm

/**
 * Class that handle bus data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object BusData {

    private val DEFAULT_RANGE = 0.008
    private val TAG = BusData::class.java.simpleName

    lateinit var busRoutes: List<BusRoute>

    /**
     * Method that read bus stops from CSV
     */
    fun readBusStopsIfNeeded(context: Context) {
        if (BusStopRepository.isEmpty()) {
            Log.d(TAG, "Load bus stop from CSV")
            BusStopCsvParser.parse(context)
        }
    }

    /**
     * Get a route
     *
     * @param routeId the id of the bus route
     * @return a bus route
     */
    fun getRoute(routeId: String): BusRoute {
        return busRoutes
            .filter { (id) -> id == routeId }
            .getOrElse(0, { BusRoute("0", "error") })
    }

    /**
     * Get a list of bus stop within a a distance and position
     *
     * @param position the position
     * @return a list of bus stop
     */
    fun readNearbyStops(position: Position): List<BusStop> {
        return BusStopRepository.getStopsAround(position, DEFAULT_RANGE)
    }
}
