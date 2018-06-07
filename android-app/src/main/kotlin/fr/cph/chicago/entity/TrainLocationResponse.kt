package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class TrainLocationResponse(val ctatt: Ctatt) {
    class Ctatt(
        val tmst: String,
        var errCd: String,
        var errNm: String? = null,
        var route: List<Route>? = null)


    class Route(
        @JsonProperty("@name")
        val name: String,
        val train: List<Train> = listOf())

    class Train(
        var rn: String,
        var destSt: String,
        var destNm: String,
        var trDr: String,
        var nextStaId: String,
        var nextStpId: String,
        var nextStaNm: String,
        var prdt: String,
        var arrT: String,
        var isApp: String,
        var isDly: String,
        var lat: String,
        var lon: String,
        var heading: String)
}
