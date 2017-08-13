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

package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BusArrival
import java.util.*

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
