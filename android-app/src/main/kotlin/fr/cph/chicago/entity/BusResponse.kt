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

package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class BusArrivalResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(var prd: List<Prd>? = null, var error: List<Error>? = null)

    data class Prd(
        val tmstmp: String,
        val typ: String,
        val stpnm: String,
        val stpid: String,
        val vid: String,
        val dstp: Int,
        val rt: String,
        val rtdd: String,
        val rtdir: String,
        val des: String,
        val prdtm: String,
        val tablockid: String,
        val tatripid: String,
        val dly: Boolean,
        val prdctdn: String,
        val zone: String)
}

data class BusDirectionResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(val directions: List<Direction>? = null, val error: List<Error>? = null)

    data class Direction(val dir: String)
}

data class BusPatternResponse(
    @JsonProperty("bustime-response")
    var bustimeResponse: BustimeResponse) {

    data class BustimeResponse(val ptr: List<Ptr>? = null, val error: List<Error>? = null)

    data class Ptr(
        val pid: Int,
        val ln: Int,
        val rtdir: String,
        val pt: List<Pt>)


    data class Pt(
        val seq: Int,
        val lat: Double,
        var lon: Double,
        val typ: String,
        val stpid: String? = null,
        val stpnm: String? = null,
        var pdist: Int)
}

data class BusPositionResponse(@JsonProperty("bustime-response") var bustimeResponse: BustimeResponse) {
    data class BustimeResponse(val vehicle: List<Vehicle>? = null, val error: List<Error>? = null)

    data class Vehicle(
        val vid: String,
        val tmstmp: String,
        val lat: String,
        val lon: String,
        val hdg: String,
        val pid: Int,
        val rt: String,
        val des: String,
        val pdist: Int,
        val dly: Boolean,
        val tatripid: String,
        val tablockid: String,
        val zone: String
    )
}

data class BusRoutesResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(
        var routes: List<Route>)

    data class Route(
        @JsonProperty("rt")
        val routeId: String,
        @JsonProperty("rtnm")
        val routeName: String)

}

data class BusStopsResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(var stops: List<Stop>? = null, var error: List<Error>? = null)

    data class Stop(
        val stpid: String,
        val stpnm: String,
        val lat: Double,
        val lon: Double)
}

class Error : HashMap<String, Any>() {
    fun noServiceScheduled() : Boolean {
        if(!this.containsKey("msg")) {
            return false
        }
        return "No service scheduled" == get("msg") || "No arrival times" == get("msg")
    }
}
