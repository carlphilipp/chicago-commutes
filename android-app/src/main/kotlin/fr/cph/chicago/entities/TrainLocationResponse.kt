package fr.cph.chicago.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class TrainLocationResponse(val ctatt: Ctatt) {
    data class Ctatt(
        val tmst: String,
        var errCd: String,
        var errNm: String? = null,
        var route: List<Route>? = null)


    data class Route(
        @JsonProperty("@name")
        val name: String,
        val train: List<Train> = listOf())

    data class Train(
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
