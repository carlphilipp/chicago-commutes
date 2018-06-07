package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty


class BusPositionResponse(@JsonProperty("bustime-response") var bustimeResponse: BustimeResponse6)

class BustimeResponse6(val vehicle: List<Vehicle>? = null, val error: List<Error5>? = null)

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

class Error5(val rt: String, val msg: String)
