package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class BusDirectionResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    data class BustimeResponse(val directions: List<Direction>? = null, val error: List<Error>? = null)

    data class Direction(val dir: String)

    data class Error(val rt: String, val msg: String)
}
