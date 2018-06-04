package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty
import fr.cph.chicago.core.model.AStation

data class Divvy(
    @JsonProperty("stationBeanList")
    val stations: List<DivvyStation>)

data class DivvyStation(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("stationName")
    val name: String,
    @JsonProperty("availableDocks")
    val availableDocks: Int,
    @JsonProperty("totalDocks")
    val totalDocks: Int,
    @JsonProperty("latitude")
    val latitude: Double,
    @JsonProperty("longitude")
    val longitude: Double,
    @JsonProperty("availableBikes")
    val availableBikes: Int,
    @JsonProperty("stAddress1")
    val stAddress1: String) : AStation
