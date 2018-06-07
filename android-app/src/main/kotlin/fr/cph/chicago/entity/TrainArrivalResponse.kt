package fr.cph.chicago.entity

data class TrainArrivalResponse(val ctatt: Ctatt2)

data class Ctatt2(
    val tmst: String,
    val errCd: String,
    val errNm: String? = null,
    val eta: List<Eta>? = null)

data class Eta(
    val staId: String,
    val stpId: String,
    val staNm: String,
    val stpDe: String,
    val rn: String,
    val rt: String,
    val destSt: String,
    val destNm: String,
    val trDr: String,
    val prdt: String,
    val arrT: String,
    val isApp: String,
    val isSch: String,
    val isDly: String,
    val isFlt: String,
    val lat: String? = null,
    val lon: String? = null,
    val heading: String? = null
)
