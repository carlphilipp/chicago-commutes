package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusPatternResponse(
    @JsonProperty("bustime-response")
    var bustimeResponse: BustimeResponse) {

    class BustimeResponse(val ptr: List<Ptr>? = null, val error: List<Error>? = null)

    class Ptr(
        val pid: Int,
        val ln: Int,
        val rtdir: String,
        val pt: List<Pt>)


    class Pt(
        val seq: Int,
        val lat: Double,
        var lon: Double,
        val typ: String,
        val stpid: String? = null,
        val stpnm: String? = null,
        var pdist: Int)

    class Error(val rt: String, val msg: String)
}

