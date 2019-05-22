package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.apache.commons.lang3.StringUtils
import org.rekotlin.Action

data class ResetStateAction(val unit: Unit = Unit) : Action

data class ApiKeysAction(
    val ctaTrainKey: String,
    val ctaBusKey: String,
    val googleStreetKey: String
) : Action

data class BaseAction(
    val localError: Boolean = false,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),
    val trainFavorites: List<Int> = listOf(),
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf(),
    val bikeFavorites: List<Int> = listOf()
) : Action

data class FavoritesAction(
    val favoritesDTO: FavoritesDTO = FavoritesDTO(
        trainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
        busArrivalDTO = BusArrivalDTO(listOf(), false),
        bikeError = false,
        bikeStations = listOf())
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
    val trainStationId: Int = 0,
    val trainArrival: TrainArrival = TrainArrival(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bus stop activity
data class BusStopArrivalsAction(
    // input
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: Int = 0,
    val bound: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
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

data class AddTrainFavoriteAction(
    val id: Int = 0,
    val trainFavorites: List<Int> = listOf()
) : Action

data class RemoveTrainFavoriteAction(
    val id: Int = 0,
    val trainFavorites: List<Int> = listOf()
) : Action

data class AddBusFavoriteAction(
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    val busRouteName: String = StringUtils.EMPTY,
    val busStopName: String = StringUtils.EMPTY,
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf()
) : Action

data class RemoveBusFavoriteAction(
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf()
) : Action

data class AddBikeFavoriteAction(
    val id: Int = 0,
    val stationName: String = StringUtils.EMPTY,
    val bikeFavorites: List<Int> = listOf()
) : Action

data class RemoveBikeFavoriteAction(
    val id: Int = 0,
    val bikeFavorites: List<Int> = listOf()
) : Action
