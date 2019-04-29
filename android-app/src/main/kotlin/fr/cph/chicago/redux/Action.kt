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
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.Action

data class BaseAction(
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false)
) : Action

data class FavoritesAction(
    val favoritesDTO: FavoritesDTO = FavoritesDTO(
        trainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
        busArrivalDTO = BusArrivalDTO(listOf(), false),
        bikeError = false,
        bikeStations = listOf()
    )
) : Action

// Bus Routes
data class BusRoutesAction(
    val busRoutes: List<BusRoute> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bus Routes + Bike stations
data class BusRoutesAndBikeStationAction(
    val busRoutes: List<BusRoute> = listOf(),
    val bikeStations: List<BikeStation> = listOf(),
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false
) : Action

// Train station activity
data class TrainStationAction(
    val trainStation: TrainStation = TrainStation.buildEmptyStation(),
    val trainArrival: TrainArrival = TrainArrival(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bus stop activity
data class BusStopArrivalsAction(
    // input
    val requestRt: String = "",
    val busRouteId: String = "",
    val requestStopId: String = "",
    val busStopId: Int = 0,
    val bound: String = "",
    val boundTitle: String = "",
    // output
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bike station
data class BikeStationAction(
    val bikeStations: List<BikeStation> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

data class AlertAction(
    val routesAlertsDTO: List<RoutesAlertsDTO> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action
