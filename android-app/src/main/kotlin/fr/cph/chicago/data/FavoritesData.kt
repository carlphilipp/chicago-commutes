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

package fr.cph.chicago.data

import android.os.Parcelable
import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalStopMappedDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import java.util.Date
import java.util.TreeMap

/**
 * Vehicle Arrival. Hold data for favorites adapter.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
object FavoritesData {

    private val trainService = TrainService
    private val busService = BusService
    private val preferenceService = PreferenceService
    private val util = Util

    private val trainArrivals: SparseArray<TrainArrival> = SparseArray()
    private val busArrivals: MutableList<BusArrival> = mutableListOf()
    private val divvyStations: MutableList<BikeStation> = mutableListOf()
    private var trainFavorites: List<Int> = listOf()
    private var busFavorites: List<String> = listOf()
    private var fakeBusFavorites: List<String> = listOf()
    private val bikeFavorites: MutableList<String> = mutableListOf()

    /**
     * Get the size of the current model
     *
     * @return a size
     */
    fun size(): Int {
        return trainFavorites.size + fakeBusFavorites.size + bikeFavorites.size
    }

    /**
     * Get the object depending on position
     *
     * @param position the position
     * @return an object, station or bus route
     */
    fun getObject(position: Int): Parcelable {
        if (position < trainFavorites.size) {
            val stationId = trainFavorites[position]
            return trainService.getStation(stationId)
        } else if (position < trainFavorites.size + fakeBusFavorites.size && position - trainFavorites.size < fakeBusFavorites.size) {
            val index = position - trainFavorites.size
            val routeId = fakeBusFavorites[index]
            val busDataRoute = busService.getBusRoute(routeId)
            return if (busDataRoute.name != "error") {
                busDataRoute
            } else {
                // Get name in the preferences if null
                val routeName = preferenceService.getBusRouteNameMapping(routeId)
                BusRoute(routeId, routeName ?: "")
            }
        } else {
            val index = position - (trainFavorites.size + fakeBusFavorites.size)
            return divvyStations
                .filter { (id) -> Integer.toString(id) == bikeFavorites[index] }
                .getOrElse(0, { createEmptyBikeStation(index) })
        }
    }

    fun getTrainArrivalByLine(stationId: Int, trainLine: TrainLine): Map<String, String> {
        return trainArrivals
            .get(stationId, TrainArrival.buildEmptyTrainArrival())
            .getEtas(trainLine)
            .fold(TreeMap(), { accumulator, eta ->
                val stopNameData = eta.destName
                val timingData = eta.timeLeftDueDelay
                val value = if (accumulator.containsKey(stopNameData))
                    accumulator[stopNameData] + " " + timingData
                else
                    timingData
                accumulator[stopNameData] = value
                accumulator
            })
    }

    fun getBusArrivalsMapped(routeId: String): BusArrivalStopMappedDTO {
        val busArrivalDTO = BusArrivalStopMappedDTO()
        busArrivals
            .filter { (_, _, _, _, _, routeId) -> routeId == routeId }
            .filter { (_, _, _, stopId, _, _, routeDirection) -> isInFavorites(routeId, stopId, routeDirection) }
            .forEach({ busArrivalDTO.addBusArrival(it) })

        // Put empty buses if one of the stop is missing from the response
        addNoServiceBusIfNeeded(busArrivalDTO, routeId)
        return busArrivalDTO
    }

    fun refreshFavorites() {
        trainFavorites = preferenceService.getTrainFavorites()
        busFavorites = preferenceService.getBusFavorites()
        fakeBusFavorites = calculateActualRouteNumberBusFavorites(busFavorites)
        bikeFavorites.clear()
        val bikeFavoritesTemp = preferenceService.getBikeFavorites()
        if (divvyStations.isNotEmpty()) {
            bikeFavoritesTemp
                .flatMap { bikeStationId -> divvyStations.filter { (id) -> Integer.toString(id) == bikeStationId } }
                .sortedWith(util.DIVVY_COMPARATOR_BY_NAME)
                .map { (id) -> Integer.toString(id) }
                .forEach({ bikeFavorites.add(it) })
        } else {
            bikeFavorites.addAll(bikeFavoritesTemp)
        }
    }

    fun updateTrainArrivals(trainArrivals: SparseArray<TrainArrival>) {
        this.trainArrivals.clear()
        for (i in 0 until trainArrivals.size()) {
            val key = trainArrivals.keyAt(i)
            val obj = trainArrivals.get(key)
            this.trainArrivals.append(key, obj)
        }
    }

    fun updateBusArrivals(busArrivals: List<BusArrival>) {
        this.busArrivals.clear()
        this.busArrivals.addAll(busArrivals)
    }

    fun updateBikeStations(divvyStations: List<BikeStation>) {
        this.divvyStations.clear()
        this.divvyStations.addAll(divvyStations)
    }

    private fun createEmptyBikeStation(index: Int): BikeStation {
        val stationName = preferenceService.getBikeRouteNameMapping(bikeFavorites[index])
        return BikeStation.buildDefaultBikeStationWithName(stationName ?: StringUtils.EMPTY)
    }

    private fun calculateActualRouteNumberBusFavorites(busFavorites: List<String>): List<String> {
        return busFavorites
            .map { util.decodeBusFavorite(it) }
            .map { it.routeId }
            .distinct()
    }

    /**
     * Is in favorites
     *
     * @param routeId the route id
     * @param stopId  the stop id
     * @param bound   the bound
     * @return a boolean
     */
    private fun isInFavorites(routeId: String, stopId: Int, bound: String): Boolean {
        return busFavorites
            .map { util.decodeBusFavorite(it) }
            // TODO: Is that correct ? maybe remove stopId
            .filter { (routeId1, stopId1, bound1) -> routeId == routeId1 && Integer.toString(stopId) == stopId1 && bound == bound1 }
            .isNotEmpty()
    }

    private fun addNoServiceBusIfNeeded(busArrivalDTO: BusArrivalStopMappedDTO, routeId: String) {
        for (bus in busFavorites) {
            val (routeIdFav, stopId1, bound) = util.decodeBusFavorite(bus)
            if (routeIdFav == routeId) {
                val stopId = Integer.valueOf(stopId1)

                var stopName = preferenceService.getBusStopNameMapping(stopId.toString())
                stopName = if (stopName != null) stopName else stopId.toString()

                // FIXME check if that logic works. I think it does not. In what case do we show that bus arrival?
                if (!busArrivalDTO.containsStopNameAndBound(stopName, bound)) {
                    val busArrival = BusArrival(Date(), "added bus", stopName, stopId, 0, routeIdFav, bound, StringUtils.EMPTY, Date(), false)
                    busArrivalDTO.addBusArrival(busArrival)
                }
            }
        }
    }
}
