package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusStopsResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse3)

class BustimeResponse3(var stops: List<Stop>? = null, var error: List<Error2>? = null)

class Stop(
    val stpid: String,
    val stpnm: String,
    val lat: Double,
    val lon: Double)

class Error2(val rt: String, val dir: String, val msg: String)
