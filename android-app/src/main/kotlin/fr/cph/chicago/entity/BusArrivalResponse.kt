package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class BusArrivalResponse(
    @JsonProperty("bustime-response")
    val bustimeResponse: BustimeResponse2)

class BustimeResponse2(var prd: List<Prd>? = null, var error: List<Error>? = null)

class Prd(
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

class Error(val stpid: String, val msg: String)
