package fr.cph.chicago.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fr.cph.chicago.entity.BikeStation


class DivvyDTO {

    @JsonProperty("executionTime")
    val executionTime: String? = null
    @JsonProperty("stationBeanList")
    val stations: List<BikeStation>? = null
}
