package fr.cph.chicago.entities

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

    data class Error(val stpid: String, val msg: String)
}
