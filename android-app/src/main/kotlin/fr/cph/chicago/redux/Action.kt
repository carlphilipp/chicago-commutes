package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
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

// Bus Routes
data class LoadBusRoutesAction(
    val error: Boolean = false,
    val busRoutes: List<BusRoute> = listOf()
) : Action

// Bike stations
data class LoadBikesAction(
    val bikeStationsError: Boolean = false,
    val bikeStations: List<BikeStation> = listOf()
) : Action

// Bus Routes + Bike stations
data class LoadFirstDataAction(
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),
    val bikeStations: List<BikeStation> = listOf()
) : Action

// Train station activity
data class LoadTrainStationAction(
    val trainStation: TrainStation = TrainStation.buildEmptyStation(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong,
    val trainArrival: TrainArrival = TrainArrival()
) : Action

// Bus stop activity
data class LoadBusStopArrivalsAction(
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong,
    val requestRt: String = "",
    val busRouteId: String = "",
    val requestStopId: String = "",
    val busStopId: Int = 0,
    val bound: String = "",
    val boundTitle: String = "",
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO()
) : Action

// Bike station activity
data class LoadBikeStationAction(
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong,
    val bikeStations: List<BikeStation> = listOf()
) : Action

data class HighlightBackgroundDone(val unit: Unit = Unit) : Action
