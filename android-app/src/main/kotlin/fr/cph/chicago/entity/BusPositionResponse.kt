package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty


class BusPositionResponse(@JsonProperty("bustime-response") var bustimeResponse: BustimeResponse) {
    class BustimeResponse(val vehicle: List<Vehicle>? = null, val error: List<Error>? = null)

    class Vehicle(
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

    class Error(val rt: String, val msg: String)
}
