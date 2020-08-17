/**
 * Copyright 2020 Carl-Philipp Harmant
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

package fr.cph.chicago.client

import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.math.BigInteger

const val REQUEST_ROUTE = "rt"
private const val REQUEST_DIR = "dir"
const val REQUEST_MAP_ID = "mapid"
private const val REQUEST_RUN_NUMBER = "runnumber"
const val REQUEST_STOP_ID = "stpid"
private const val REQUEST_VID = "vid"

fun emptyParams(): MultiValuedMap<String, String> {
    return ArrayListValuedHashMap()
}

fun busFollowParams(busId: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(2, 1)
    params.put(REQUEST_VID, busId)
    return params
}

fun busArrivalsParams(busRouteId: String, busStopId: BigInteger): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(2, 1)
    params.put(REQUEST_ROUTE, busRouteId)
    params.put(REQUEST_STOP_ID, busStopId.toString())
    return params
}

fun busArrivalsStopIdParams(busStopId: BigInteger): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_STOP_ID, busStopId.toString())
    return params
}

fun busVehiclesParams(busRouteId: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_ROUTE, busRouteId)
    return params
}

fun trainEtasParams(runNumber: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_RUN_NUMBER, runNumber)
    return params
}


fun trainLocationParams(line: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_ROUTE, line)
    return params
}

fun stationTrainParams(stationId: BigInteger): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_MAP_ID, stationId.toString())
    return params
}

fun busPatternParams(busRouteId: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_ROUTE, busRouteId)
    return params
}

fun busDirectionParams(busRouteId: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put(REQUEST_ROUTE, busRouteId)
    return params
}

fun allStopsParams(route: String, bound: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(2, 1)
    params.put(REQUEST_ROUTE, route)
    params.put(REQUEST_DIR, bound)
    return params
}

fun alertsParams(): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 2)
    params.put("type", "rail")
    params.put("type", "bus")
    return params
}

fun alertParams(id: String): MultiValuedMap<String, String> {
    val params = ArrayListValuedHashMap<String, String>(1, 1)
    params.put("routeid", id)
    return params
}

