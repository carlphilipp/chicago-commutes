package fr.cph.chicago.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class BusStopsResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(var stops: List<Stop>? = null, var error: List<Error>? = null)

    data class Stop(
        val stpid: String,
        val stpnm: String,
        val lat: Double,
        val lon: Double)

    data class Error(val rt: String, val dir: String, val msg: String)
}
