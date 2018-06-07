package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusStopsResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    class BustimeResponse(var stops: List<Stop>? = null, var error: List<Error>? = null)

    class Stop(
        val stpid: String,
        val stpnm: String,
        val lat: Double,
        val lon: Double)

    class Error(val rt: String, val dir: String, val msg: String)
}
