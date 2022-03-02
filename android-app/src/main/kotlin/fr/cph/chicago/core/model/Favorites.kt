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

package fr.cph.chicago.core.model

import android.os.Parcelable
import androidx.compose.runtime.mutableStateOf
import fr.cph.chicago.core.composable.pojo.StopDirection
import fr.cph.chicago.core.model.dto.BusArrivalStopMappedDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.TimeUtil
import fr.cph.chicago.util.Util
import java.util.Calendar
import java.util.Date
import java.util.TreeMap
import kotlin.random.Random

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
    private val timeUtil = TimeUtil

    private var trainFavorites = listOf<String>()
    private var busFavorites = listOf<String>()
    private var busRouteFavorites = listOf<String>()
    private var bikeFavorites = listOf<String>()

    val time = mutableStateOf(LastUpdate(timeUtil.formatTimeDifference(store.state.lastFavoritesUpdate, Calendar.getInstance().time)))

    /**
     * Get the size of the current model
     *
     * @return a size
     */
    fun size(): Int {
        return trainFavorites.size + busRouteFavorites.size + bikeFavorites.size
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
        } else if (position < trainFavorites.size + busRouteFavorites.size && position - trainFavorites.size < busRouteFavorites.size) {
            val index = position - trainFavorites.size
            val routeId = busRouteFavorites[index]
            busService.getBusRoute(routeId)
        } else {
            val index = position - (trainFavorites.size + busRouteFavorites.size)
            store.state.bikeStations
                .filter { bikeStation -> bikeStation.id == bikeFavorites[index] }
                .getOrElse(0) { bikeService.createEmptyBikeStation(bikeFavorites[index]) }
        }
    }

    @Deprecated("Use getTrainArrivalByStopDirection instead")
    fun getTrainArrivalByLine(stationId: String, trainLine: TrainLine): Map<String, MutableList<String>> {
        return store.state.trainArrivalsDTO.trainsArrivals
            .getOrElse(stationId) { TrainArrival.buildEmptyTrainArrival() }
            .getEtas(trainLine)
            .fold(TreeMap()) { accumulator, eta ->
                val destinationName = eta.destName + "_" + eta.stop.direction.toString()
                val timeLeft = eta.timeLeftDueDelay
                if (accumulator.contains(destinationName)) {
                    val list: MutableList<String> = accumulator.getValue(destinationName)
                    list.add(timeLeft)
                    accumulator[destinationName] = list
                } else {
                    val list = mutableListOf(timeLeft)
                    accumulator[destinationName] = list
                }
                accumulator
            }
    }

    fun getTrainArrivalByStopDirection(stationId: String, trainLine: TrainLine): Map<StopDirection, MutableList<String>> {
        return store.state.trainArrivalsDTO.trainsArrivals
            .getOrElse(stationId) { TrainArrival.buildEmptyTrainArrival() }
            .getEtas(trainLine)
            .fold(TreeMap()) { accumulator, eta ->
                val stopDirection = StopDirection(destination = eta.destName, trainDirection = eta.stop.direction)
                val timeLeft = eta.timeLeftDueDelay
                if (accumulator.contains(stopDirection)) {
                    val list: MutableList<String> = accumulator.getValue(stopDirection)
                    list.add(timeLeft)
                    accumulator[stopDirection] = list
                } else {
                    val list = mutableListOf(timeLeft)
                    accumulator[stopDirection] = list
                }
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
        trainFavorites = store.state.trainFavorites
        busFavorites = store.state.busFavorites
        busRouteFavorites = store.state.busRouteFavorites
        // FIXME: Temporary fix to order list of favorites bikes
        bikeFavorites = store.state.bikeFavorites
            .map { id -> bikeService.createEmptyBikeStation(id) }
            .sortedBy { it.name }
            .map { it.id }
        refreshTime()
    }

    fun refreshTime() {
        time.value = LastUpdate(timeUtil.formatTimeDifference(store.state.lastFavoritesUpdate, Calendar.getInstance().time))
    }

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

                if (!busArrivalDTO.containsStopNameAndBound(stopName, bound)) {
                    val busArrival = BusArrival(Date(), "added bus", stopName, stopId, 0, routeIdFav, bound, "", Date(), false)
                    busArrivalDTO.addBusArrival(busArrival)
                }
            }
        }
    }
}

data class LastUpdate(val value: String, private val random: Int = Random.nextInt())
