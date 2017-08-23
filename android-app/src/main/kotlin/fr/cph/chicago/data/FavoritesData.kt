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
import android.os.Parcelable
import android.util.SparseArray
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.TrainArrival
import fr.cph.chicago.entity.dto.BusArrivalStopMappedDTO
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Vehicle Arrival. Hold data for favorites adapter.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
object FavoritesData {

    var trainArrivals: SparseArray<TrainArrival> = SparseArray()
    var busArrivals: List<BusArrival> = listOf()
    var bikeStations: List<BikeStation> = listOf()
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
    fun getObject(position: Int, context: Context): Parcelable {
        if (position < trainFavorites.size) {
            val stationId = trainFavorites[position]
            return TrainData.getStation(stationId)
        } else if (position < trainFavorites.size + fakeBusFavorites.size && position - trainFavorites.size < fakeBusFavorites.size) {
            val index = position - trainFavorites.size
            val routeId = fakeBusFavorites[index]
            val busDataRoute = BusData.getRoute(routeId)
            if (busDataRoute.name != "error") {
                return busDataRoute
            } else {
                // Get name in the preferences if null
                val routeName = PreferenceRepository.getBusRouteNameMapping(context, routeId)
                return BusRoute(routeId, routeName ?: "")
            }
        } else {
            val index = position - (trainFavorites.size + fakeBusFavorites.size)
            return bikeStations
                .filter { (id) -> Integer.toString(id) == bikeFavorites[index] }
                .getOrElse(0, { createEmptyBikeStation(index, context) })
        }
    }

    private fun createEmptyBikeStation(index: Int, context: Context): BikeStation {
        val stationName = PreferenceRepository.getBikeRouteNameMapping(context, bikeFavorites[index])
        return BikeStation.buildDefaultBikeStationWithName(stationName ?: StringUtils.EMPTY)
    }

    /**
     * Get the train arrival
     *
     * @param stationId the station id
     * @return a train arrival
     */
    private fun getTrainArrival(stationId: Int): TrainArrival {
        return trainArrivals.get(stationId, TrainArrival.buildEmptyTrainArrival())
    }

    fun getTrainArrivalByLine(stationId: Int, trainLine: TrainLine): Map<String, String> {
        val etas = getTrainArrival(stationId).getEtas(trainLine)
        return etas.fold(TreeMap(), { accumulator, eta ->
            val stopNameData = eta.destName
            val timingData = eta.timeLeftDueDelay
            val value = if (accumulator.containsKey(stopNameData))
                accumulator[stopNameData] + " " + timingData
            else
                timingData
            accumulator.put(stopNameData, value)
            accumulator
        })
    }

    /**
     * Get bus arrival mapped
     *
     * @param routeId the route id
     * @return a nice map
     */
    fun getBusArrivalsMapped(routeId: String, context: Context): BusArrivalStopMappedDTO {
        // TODO check why (and if?) this method is called several time
        val busArrivalDTO = BusArrivalStopMappedDTO()
        busArrivals
            .filter { (_, _, _, _, _, routeId1) -> routeId1 == routeId }
            .filter { (_, _, _, stopId, _, _, routeDirection) -> isInFavorites(routeId, stopId, routeDirection) }
            .forEach({ busArrivalDTO.addBusArrival(it) })

        // Put empty buses if one of the stop is missing from the response
        addNoServiceBusIfNeeded(busArrivalDTO, routeId, context)
        return busArrivalDTO
    }

    private fun addNoServiceBusIfNeeded(busArrivalDTO: BusArrivalStopMappedDTO, routeId: String, context: Context) {
        for (bus in busFavorites) {
            val (routeIdFav, stopId1, bound) = Util.decodeBusFavorite(bus)
            if (routeIdFav == routeId) {
                val stopId = Integer.valueOf(stopId1)

                var stopName = PreferenceRepository.getBusStopNameMapping(context, stopId.toString())
                stopName = if (stopName != null) stopName else stopId.toString()

                // FIXME check if that logic works. I think it does not. In what case do we show that bus arrival?
                if (!busArrivalDTO.containsStopNameAndBound(stopName, bound)) {
                    val busArrival = BusArrival(Date(), "added bus", stopName, stopId, 0, routeIdFav, bound, StringUtils.EMPTY, Date(), false)
                    busArrivalDTO.addBusArrival(busArrival)
                }
            }
        }
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
            .map { Util.decodeBusFavorite(it) }
            // TODO: Is that correct ? maybe remove stopId
            .filter { (routeId1, stopId1, bound1) -> routeId == routeId1 && Integer.toString(stopId) == stopId1 && bound == bound1 }
            .isNotEmpty()
    }

    fun setFavorites(context: Context) {
        trainFavorites = PreferenceRepository.getTrainFavorites(context)
        busFavorites = PreferenceRepository.getBusFavorites(context)
        fakeBusFavorites = calculateActualRouteNumberBusFavorites()
        bikeFavorites.clear()
        val bikeFavoritesTemp = PreferenceRepository.getBikeFavorites(context)
        if (bikeStations.isNotEmpty()) {
            bikeFavoritesTemp
                .flatMap { bikeStationId -> bikeStations.filter { (id) -> Integer.toString(id) == bikeStationId } }
                .sortedWith(Util.BIKE_COMPARATOR_NAME)
                .map { (id) -> Integer.toString(id) }
                .forEach({ bikeFavorites.add(it) })
        } else {
            bikeFavorites.addAll(bikeFavoritesTemp)
        }
    }

    private fun calculateActualRouteNumberBusFavorites(): List<String> {
        return busFavorites
            .map { Util.decodeBusFavorite(it) }
            .map { it.routeId }
            .distinct()
            .toList()
    }
}
