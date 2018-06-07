package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusDirectionResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse4)

class BustimeResponse4(val directions: List<Direction>? = null, val error: List<Error3>? = null)

class Direction(val dir: String)

class Error3(val rt: String, val msg: String)
