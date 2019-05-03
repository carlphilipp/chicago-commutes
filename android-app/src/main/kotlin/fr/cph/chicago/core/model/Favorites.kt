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

package fr.cph.chicago.core.model

import android.os.Parcelable
import fr.cph.chicago.core.model.dto.BusArrivalStopMappedDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
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
object Favorites {

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val preferenceService = PreferenceService
    private val util = Util

    private var trainFavorites = listOf<Int>()
    private var busFavorites = listOf<String>()
    private var fakeBusFavorites = listOf<String>()
    private var bikeFavorites = listOf<Int>()

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
     * @return an object, trainStation or bus route
     */
    fun getObject(position: Int): Parcelable {
        return if (position < trainFavorites.size) {
            val stationId = trainFavorites[position]
            trainService.getStation(stationId)
        } else if (position < trainFavorites.size + fakeBusFavorites.size && position - trainFavorites.size < fakeBusFavorites.size) {
            val index = position - trainFavorites.size
            val routeId = fakeBusFavorites[index]
            busService.getBusRoute(routeId)
        } else {
            val index = position - (trainFavorites.size + fakeBusFavorites.size)
            store.state.bikeStations
                .filter { bikeStation -> bikeStation.id == bikeFavorites[index] }
                .getOrElse(0) { bikeService.createEmptyBikeStation(bikeFavorites[index]) }
        }
    }

    fun getTrainArrivalByLine(stationId: Int, trainLine: TrainLine): Map<String, String> {
        return store.state.trainArrivalsDTO.trainsArrivals
            .get(stationId, TrainArrival.buildEmptyTrainArrival())
            .getEtas(trainLine)
            .fold(TreeMap()) { accumulator, eta ->
                val stopNameData = eta.destName
                val timingData = eta.timeLeftDueDelay
                val value = if (accumulator.containsKey(stopNameData))
                    accumulator[stopNameData] + " " + timingData
                else
                    timingData
                accumulator[stopNameData] = value
                accumulator
            }
    }

    fun getBusArrivalsMapped(routeId: String): BusArrivalStopMappedDTO {
        val busArrivalDTO = BusArrivalStopMappedDTO()
        store.state.busArrivalsDTO.busArrivals
            .filter { (_, _, _, _, _, rId) -> rId == routeId }
            .filter { (_, _, _, stopId, _, _, routeDirection) -> isBusInFavorites(routeId, stopId, routeDirection) }
            .forEach { busArrivalDTO.addBusArrival(it) }

        // Put empty buses if one of the stop is missing from the response
        addNoServiceBusIfNeeded(busArrivalDTO, routeId)
        return busArrivalDTO
    }

    fun refreshFavorites() {
        trainFavorites = preferenceService.getTrainFavorites()
        busFavorites = preferenceService.getBusFavorites()
        fakeBusFavorites = calculateActualRouteNumberBusFavorites(busFavorites)
        bikeFavorites = if (store.state.bikeStations.isNotEmpty()) {
            preferenceService.getBikeFavorites()
                .flatMap { bikeStationId -> store.state.bikeStations.filter { station -> station.id == bikeStationId } }
                .sortedWith(util.bikeStationComparator)
                .map { station -> station.id }
        } else {
            preferenceService.getBikeFavorites()
        }
    }

    private fun calculateActualRouteNumberBusFavorites(busFavorites: List<String>): List<String> {
        return busFavorites
            .map { util.decodeBusFavorite(it) }
            .map { it.routeId }
            .distinct()
    }

    /**
     * Is in bus favorites
     *
     * @param routeId the route id
     * @param stopId  the stop id
     * @param bound   the bound
     * @return a boolean
     */
    private fun isBusInFavorites(routeId: String, stopId: Int, bound: String): Boolean {
        return busFavorites
            .map { util.decodeBusFavorite(it) }
            .any { (routeId1, stopId1, bound1) -> routeId == routeId1 && stopId.toString() == stopId1 && bound == bound1 }
    }

    private fun addNoServiceBusIfNeeded(busArrivalDTO: BusArrivalStopMappedDTO, routeId: String) {
        for (bus in busFavorites) {
            val (routeIdFav, stopId1, bound) = util.decodeBusFavorite(bus)
            if (routeIdFav == routeId) {
                val stopId = stopId1.toInt()

                val stopName = preferenceService.getBusStopNameMapping(stopId.toString())
                    ?: stopId.toString()

                // FIXME check if that logic works. I think it does not. In what case do we show that bus arrival?
                if (!busArrivalDTO.containsStopNameAndBound(stopName, bound)) {
                    val busArrival = BusArrival(Date(), "added bus", stopName, stopId, 0, routeIdFav, bound, StringUtils.EMPTY, Date(), false)
                    busArrivalDTO.addBusArrival(busArrival)
                }
            }
        }
    }
}
