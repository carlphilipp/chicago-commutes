package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusDirectionResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse) {

    class BustimeResponse(val directions: List<Direction>? = null, val error: List<Error>? = null)

    class Direction(val dir: String)

    class Error(val rt: String, val msg: String)
}
