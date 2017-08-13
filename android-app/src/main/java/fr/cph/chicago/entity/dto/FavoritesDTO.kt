package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BikeStation

class FavoritesDTO(
    var trainArrivalDTO: TrainArrivalDTO? = null,
    var busArrivalDTO: BusArrivalDTO? = null,
    var bikeError: Boolean = false,
    var bikeStations: List<BikeStation>? = null)

