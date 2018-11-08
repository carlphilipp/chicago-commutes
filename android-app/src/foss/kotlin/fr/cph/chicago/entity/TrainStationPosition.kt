package fr.cph.chicago.entity

import com.mapbox.mapboxsdk.geometry.LatLng

data class TrainStationPosition(
    val name: String,
    val topLeft: LatLng,
    val topRight: LatLng,
    val bottomLeft: LatLng,
    val bottomRight: LatLng)
