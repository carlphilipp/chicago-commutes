package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusRoute

class FirstLoadDTO(
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false,
    val busRoutes: List<BusRoute>? = null,
    val bikeStations: List<BikeStation>? = null)
