package fr.cph.chicago.entity.dto

import java.util.ArrayList
import java.util.TreeMap

import fr.cph.chicago.entity.BusArrival

/**
 * @author cpharmant
 */
class BusArrivalRouteDTO : TreeMap<String, Map<String, List<BusArrival>>>() {
    // route => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        // FIXME kotlin
        /*if (containsKey(busArrival.routeId)) {
            val tempMap = get(busArrival.routeId)
            if (tempMap!!.containsKey(busArrival.routeDirection)) {
                val arrivals: List<BusArrival>? = tempMap[busArrival.routeDirection.orEmpty()]
                arrivals.add(busArrival)
            } else {
                val arrivals = ArrayList<BusArrival>()
                arrivals.add(busArrival)
                tempMap.put(busArrival.getRouteDirection(), arrivals)
            }
        } else {
            val tempMap = TreeMap<String, List<BusArrival>>()
            val arrivals = ArrayList<BusArrival>()
            arrivals.add(busArrival)
            tempMap.put(busArrival.getRouteDirection(), arrivals)
            put(busArrival.routeId.orEmpty(), tempMap)
        }*/
    }
}
