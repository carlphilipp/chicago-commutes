/**
 * Copyright 2021 Carl-Philipp Harmant
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
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import java.math.BigInteger
import java.util.TreeMap
import org.apache.commons.lang3.StringUtils

data class BaseDTO(
    val trainArrivalsDTO: TrainArrivalDTO,
    val busArrivalsDTO: BusArrivalDTO,
    val trainFavorites: List<BigInteger>,
    val busFavorites: List<String>,
    val busRouteFavorites: List<String>,
    val bikeFavorites: List<BigInteger>
)

data class BusArrivalDTO(val busArrivals: List<BusArrival>, val error: Boolean)

data class TrainArrivalDTO(val trainsArrivals: MutableMap<BigInteger, TrainArrival>, val error: Boolean)

data class FirstLoadDTO(
    val busRoutesError: Boolean,
    val bikeStationsError: Boolean,
    val busRoutes: List<BusRoute>,
    val bikeStations: List<BikeStation>
)

data class FavoritesDTO(
    val trainArrivalDTO: TrainArrivalDTO,
    val busArrivalDTO: BusArrivalDTO,
    val bikeError: Boolean,
    val bikeStations: List<BikeStation>
)

data class BusFavoriteDTO(val routeId: String, val stopId: String, val bound: String)

data class BusDetailsDTO(
    val busRouteId: String = StringUtils.EMPTY,
    val bound: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    val stopId: Int = 0,
    val routeName: String = StringUtils.EMPTY,
    val stopName: String = StringUtils.EMPTY,
)

// destination -> list of bus arrival
class
BusArrivalStopDTO(val underlying: MutableMap<String, MutableList<BusArrival>> = ArrayMap()) : MutableMap<String, MutableList<BusArrival>> by underlying

class BusArrivalStopMappedDTO(private val underlying: TreeMap<String, MutableMap<String, MutableSet<BusArrival>>> = TreeMap()) : MutableMap<String, MutableMap<String, MutableSet<BusArrival>>> by underlying {
    // stop name => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        getOrPut(busArrival.stopName) { TreeMap() }
            .getOrPut(busArrival.routeDirection) { mutableSetOf() }
            .add(busArrival)
    }

    fun containsStopNameAndBound(stopName: String, bound: String): Boolean {
        return contains(stopName) && getValue(stopName).containsKey(bound)
    }
}

class BusArrivalRouteDTO(
    comparator: Comparator<String>,
    private val underlying: TreeMap<String, MutableMap<String, MutableSet<BusArrival>>> = TreeMap(comparator)
) : MutableMap<String, MutableMap<String, MutableSet<BusArrival>>> by underlying {
    // route => { bound => BusArrival }

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

    fun addBusArrival(busArrival: BusArrival) {
        getOrPut(busArrival.routeId, { TreeMap() })
            .getOrPut(busArrival.routeDirection, { mutableSetOf() })
            .add(busArrival)
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
