package fr.cph.chicago.entities

import com.fasterxml.jackson.annotation.JsonProperty

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

    data class Error(val rt: String, val msg: String)
}

