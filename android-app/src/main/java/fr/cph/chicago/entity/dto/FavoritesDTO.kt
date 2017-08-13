package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BikeStation

class FavoritesDTO(
    val trainArrivalDTO: TrainArrivalDTO? = null,
    val busArrivalDTO: BusArrivalDTO? = null,
    val bikeError: Boolean = false,
    val bikeStations: List<BikeStation>? = null)

