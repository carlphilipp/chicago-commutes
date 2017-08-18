package fr.cph.chicago.entity.dto

import android.util.SparseArray
import com.fasterxml.jackson.annotation.JsonProperty
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.TrainArrival
import java.util.*

data class BusArrivalDTO(val busArrivals: List<BusArrival>, val error: Boolean)

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

data class DivvyDTO(@JsonProperty("stationBeanList") val stations: List<BikeStation>)

data class BusFavoriteDTO(val routeId: String, val stopId: String, val bound: String)

data class BusDetailsDTO(
    val busRouteId: String,
    val bound: String,
    val boundTitle: String,
    val stopId: String,
    val routeName: String,
    val stopName: String
)

class BusArrivalStopMappedDTO : TreeMap<String, MutableMap<String, List<BusArrival>>>() {
    // stop name => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.stopName)) {
            val tempMap: MutableMap<String, List<BusArrival>>? = get(busArrival.stopName)
            if (tempMap!!.containsKey(busArrival.routeDirection)) {
                val arrivals: MutableList<BusArrival> = tempMap[busArrival.routeDirection]!!.toMutableList()
                arrivals.add(busArrival)
            } else {
                tempMap.put(busArrival.routeDirection, mutableListOf(busArrival))
            }
        } else {
            val tempMap = TreeMap<String, List<BusArrival>>()
            val arrivals = mutableListOf(busArrival)
            tempMap.put(busArrival.routeDirection, arrivals)
            put(busArrival.stopName, tempMap)
        }
    }

    fun containsStopNameAndBound(stopName: String, bound: String): Boolean {
        return containsKey(stopName) && get(stopName)!!.containsKey(bound)
    }
}

class BusArrivalRouteDTO : TreeMap<String, MutableMap<String, List<BusArrival>>>() {
    // route => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.routeId)) {
            val tempMap: MutableMap<String, List<BusArrival>>? = get(busArrival.routeId)
            if (tempMap!!.containsKey(busArrival.routeDirection)) {
                val arrivals: MutableList<BusArrival> = tempMap[busArrival.routeDirection]!!.toMutableList()
                arrivals.add(busArrival)
            } else {
                val arrivals = mutableListOf(busArrival)
                tempMap.put(busArrival.routeDirection, arrivals)
            }
        } else {
            val tempMap = TreeMap<String, List<BusArrival>>()
            tempMap.put(busArrival.routeDirection, mutableListOf(busArrival))
            put(busArrival.routeId, tempMap)
        }
    }
}
