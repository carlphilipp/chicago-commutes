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

package fr.cph.chicago.core.model.dto

import android.util.ArrayMap
import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import org.apache.commons.lang3.StringUtils
import java.util.TreeMap

data class BusArrivalDTO(val busArrivals: List<BusArrival> = listOf(), val error: Boolean = true)

data class TrainArrivalDTO(val trainArrivalSparseArray: SparseArray<TrainArrival>, val error: Boolean)

data class FirstLoadDTO(
    val busRoutesError: Boolean,
    val bikeStationsError: Boolean,
    val busRoutes: List<BusRoute>,
    val bikeStations: List<BikeStation>)

data class FavoritesDTO(
    val trainArrivalDTO: TrainArrivalDTO,
    val busArrivalDTO: BusArrivalDTO,
    val bikeError: Boolean,
    val bikeStations: List<BikeStation>)

data class BusFavoriteDTO(val routeId: String, val stopId: String, val bound: String)

data class BusDetailsDTO(
    val busRouteId: String,
    val bound: String,
    val boundTitle: String,
    val stopId: String,
    val routeName: String,
    val stopName: String
)

// destination -> list of bus arrival
class BusArrivalStopDTO(private val map: ArrayMap<String, List<BusArrival>> = ArrayMap()) : Map<String, List<BusArrival>> {

    override val entries: Set<Map.Entry<String, List<BusArrival>>>
        get() = map.entries

    override val keys: Set<String>
        get() = map.keys

    override val size: Int
        get() = map.size

    override val values: Collection<List<BusArrival>>
        get() = map.values

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: List<BusArrival>): Boolean {
        return map.containsValue(value)
    }

    override fun get(key: String): List<BusArrival>? {
        return map[key]
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun put(key: String, value: List<BusArrival>) {
        map[key] = value
    }
}

class BusArrivalStopMappedDTO : TreeMap<String, MutableMap<String, MutableList<BusArrival>>>() {
    // stop name => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.stopName)) {
            val tempMap = get(busArrival.stopName)!!
            if (tempMap.containsKey(busArrival.routeDirection)) {
                val list = tempMap[busArrival.routeDirection]!!
                if (!list.contains(busArrival)) list.add(busArrival)
            } else {
                tempMap[busArrival.routeDirection] = mutableListOf(busArrival)
            }
        } else {
            val tempMap = TreeMap<String, MutableList<BusArrival>>()
            val arrivals = mutableListOf(busArrival)
            tempMap[busArrival.routeDirection] = arrivals
            put(busArrival.stopName, tempMap)
        }
    }

    fun containsStopNameAndBound(stopName: String, bound: String): Boolean {
        return containsKey(stopName) && get(stopName)!!.containsKey(bound)
    }
}

class BusArrivalRouteDTO(comparator: Comparator<String>) : TreeMap<String, MutableMap<String, MutableList<BusArrival>>>(comparator) {
    // route => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.routeId)) {
            val tempMap = get(busArrival.routeId)!!
            if (tempMap.containsKey(busArrival.routeDirection)) {
                tempMap[busArrival.routeDirection]!!.add(busArrival)
            } else {
                tempMap[busArrival.routeDirection] = mutableListOf(busArrival)
            }
        } else {
            val tempMap = TreeMap<String, MutableList<BusArrival>>()
            tempMap[busArrival.routeDirection] = mutableListOf(busArrival)
            put(busArrival.routeId, tempMap)
        }
    }

    companion object {

        private val busRouteIdRegex = Regex("[^0-9]+")

        val busComparator: Comparator<String> = Comparator { key1: String, key2: String ->
            if (key1.matches(busRouteIdRegex) && key2.matches(busRouteIdRegex)) {
                key1.toInt().compareTo(key2.toInt())
            } else {
                key1.replace(busRouteIdRegex, StringUtils.EMPTY).toInt().compareTo(key2.replace(busRouteIdRegex, StringUtils.EMPTY).toInt())
            }
        }
    }
}

data class RoutesAlertsDTO(
    val id: String,
    val routeName: String,
    val routeBackgroundColor: String,
    val routeTextColor: String,
    val routeStatus: String,
    val routeStatusColor: String,
    val alertType: AlertType
)

enum class AlertType {
    TRAIN, BUS
}

data class RouteAlertsDTO(
    val id: String,
    val headLine: String,
    val description: String,
    val impact: String,
    val severityScore: Int,
    val start: String,
    val end: String
)
