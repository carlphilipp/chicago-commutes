package fr.cph.chicago.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Divvy(
    @JsonProperty("stationBeanList")
    val stations: List<DivvyStation>)

data class DivvyStation(
    val id: Int,
    @JsonProperty("stationName")
    val name: String,
    val availableDocks: Int,
    val totalDocks: Int,
    val latitude: Double,
    val longitude: Double,
    val statusValue: String,
    val statusKey: Int,
    val status: String,
    val availableBikes: Int,
    val stAddress1: String,
    val stAddress2: String,
    val city: String,
    val postalCode: String,
    val location: String,
    val altitude: String,
    val testStation: Boolean,
    val lastCommunicationTime: String,
    val landMark: String,
    @JsonProperty("is_renting")
    val isRenting: Boolean)

