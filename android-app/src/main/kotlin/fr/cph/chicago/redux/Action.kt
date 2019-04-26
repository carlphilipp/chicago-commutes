package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.Action

data class LoadLocalAndFavoritesDataAction(
    val error: Boolean = false,
    val throwable: Throwable? = null,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false)
) : Action

data class LoadFavoritesDataAction(
    val error: Boolean = false,
    val throwable: Throwable? = null,
    val favoritesDTO: FavoritesDTO = FavoritesDTO(
        trainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
        busArrivalDTO = BusArrivalDTO(listOf(), false),
        bikeError = false,
        bikeStations = listOf()
    )
) : Action

// Bus Routes + Bike stations
data class LoadFirstDataAction(
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),
    val bikeStations: List<BikeStation> = listOf()
) : Action

data class LoadTrainStationAction(
    val trainStation: TrainStation = TrainStation.buildEmptyStation(),
    val error: Boolean = false,
    val throwable: Throwable? = null,
    val trainArrival: TrainArrival = TrainArrival()
) : Action
