package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BusArrival
import java.util.*

/**
 * @author cpharmant
 */
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
