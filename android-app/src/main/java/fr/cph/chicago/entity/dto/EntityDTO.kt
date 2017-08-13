package fr.cph.chicago.entity.dto

import android.util.SparseArray
import com.fasterxml.jackson.annotation.JsonProperty
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.TrainArrival
import java.util.*

class BusArrivalDTO(val busArrivals: List<BusArrival>? = null, val error: Boolean = false)

class TrainArrivalDTO(val trainArrivalSparseArray: SparseArray<TrainArrival>? = null, val error: Boolean = false)

class FirstLoadDTO(
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false,
    val busRoutes: List<BusRoute>? = null,
    val bikeStations: List<BikeStation>? = null)

class FavoritesDTO(
    var trainArrivalDTO: TrainArrivalDTO? = null,
    var busArrivalDTO: BusArrivalDTO? = null,
    var bikeError: Boolean = false,
    var bikeStations: List<BikeStation>? = null)

class DivvyDTO(@JsonProperty("stationBeanList") val stations: List<BikeStation>)

class BusFavoriteDTO(val routeId: String, val stopId: String, val bound: String)

class BusDetailsDTO {
    var busRouteId: String? = null
    var bound: String? = null
    var boundTitle: String? = null
    var stopId: String? = null
    var routeName: String? = null
    var stopName: String? = null
}

class BusArrivalStopMappedDTO : TreeMap<String, MutableMap<String, List<BusArrival>>>() {
    // stop name => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.stopName)) {
            val tempMap: MutableMap<String, List<BusArrival>>? = get(busArrival.stopName)
            if (tempMap!!.containsKey(busArrival.routeDirection)) {
                val arrivals: MutableList<BusArrival> = tempMap[busArrival.routeDirection]!!.toMutableList()
                arrivals.add(busArrival)
            } else {
                val arrivals = ArrayList<BusArrival>()
                arrivals.add(busArrival)
                tempMap.put(busArrival.routeDirection!!, arrivals)
            }
        } else {
            val tempMap = TreeMap<String, List<BusArrival>>()
            val arrivals = ArrayList<BusArrival>()
            arrivals.add(busArrival)
            tempMap.put(busArrival.routeDirection!!, arrivals)
            put(busArrival.stopName!!, tempMap)
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
                val arrivals: MutableList<BusArrival> = tempMap[busArrival.routeDirection.orEmpty()]!!.toMutableList()
                arrivals.add(busArrival)
            } else {
                val arrivals = ArrayList<BusArrival>()
                arrivals.add(busArrival)
                tempMap.put(busArrival.routeDirection!!, arrivals)
            }
        } else {
            val tempMap = TreeMap<String, List<BusArrival>>()
            val arrivals = ArrayList<BusArrival>()
            arrivals.add(busArrival)
            tempMap.put(busArrival.routeDirection!!, arrivals)
            put(busArrival.routeId.orEmpty(), tempMap)
        }
    }
}
