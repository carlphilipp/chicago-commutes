package fr.cph.chicago.core.composable

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute

val isRefreshing = mutableStateOf(false)
var busRoutes = mutableStateListOf<BusRoute>()
var bikeStations = mutableStateListOf<BikeStation>()
