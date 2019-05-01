package fr.cph.chicago.redux

import android.util.Log
import org.rekotlin.Action
import org.rekotlin.Store
import java.util.Date

val mainStore = Store(
    reducer = ::reducer,
    state = null,
    middleware = listOf(
        baseMiddleware,
        busRoutesAndBikeStationMiddleware,
        favoritesMiddleware,
        trainStationMiddleware,
        busStopArrivalsMiddleware,
        bikeStationMiddleware,
        busRoutesMiddleware,
        alertMiddleware
    )
)

fun reducer(action: Action, oldState: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = oldState ?: AppState()

    when (action) {
        is BaseAction -> {
            val status = when {
                action.localError -> Status.FULL_FAILURE
                action.trainArrivalsDTO.error || action.busArrivalsDTO.error -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                status = status,
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.trainArrivalsDTO,
                busArrivalsDTO = action.busArrivalsDTO
            )
        }
        is BusRoutesAndBikeStationAction -> {
            val status = when {
                state.trainArrivalsDTO.error && state.busArrivalsDTO.error && action.bikeStationsError -> Status.FULL_FAILURE
                state.trainArrivalsDTO.error || state.busArrivalsDTO.error || action.bikeStationsError -> Status.FAILURE
                else -> Status.SUCCESS
            }
            val busRoutes = if (action.busRoutesError) state.busRoutes else action.busRoutes
            val busRoutesStatus = when {
                action.busRoutesError && busRoutes.isEmpty() -> Status.FULL_FAILURE
                action.busRoutesError && busRoutes.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            val bikeStations = if (action.bikeStationsError) state.bikeStations else action.bikeStations
            val bikeStationsStatus = when {
                action.bikeStationsError && bikeStations.isEmpty() -> Status.FULL_FAILURE
                action.bikeStationsError && bikeStations.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                status = status,
                busRoutesStatus = busRoutesStatus,
                bikeStationsStatus = bikeStationsStatus,
                busRoutes = busRoutes,
                bikeStations = bikeStations
            )
        }
        is FavoritesAction -> {
            val status = when {
                action.favoritesDTO.trainArrivalDTO.error && action.favoritesDTO.busArrivalDTO.error && action.favoritesDTO.bikeError -> {
                    Status.FULL_FAILURE
                }
                action.favoritesDTO.trainArrivalDTO.error || action.favoritesDTO.busArrivalDTO.error || action.favoritesDTO.bikeError -> {
                    Status.FAILURE
                }
                else -> Status.SUCCESS
            }
            val bikeStations = if (action.favoritesDTO.bikeError) state.bikeStations else action.favoritesDTO.bikeStations
            val bikeStationsStatus = when {
                action.favoritesDTO.bikeError && bikeStations.isEmpty() -> Status.FULL_FAILURE
                action.favoritesDTO.bikeError && bikeStations.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                status = status,
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = bikeStations,
                bikeStationsStatus = bikeStationsStatus
            )
        }
        is TrainStationAction -> {
            val trainStationStatus = when {
                action.error -> Status.FAILURE
                else -> Status.SUCCESS
            }
            if (trainStationStatus == Status.SUCCESS) {
                state.trainArrivalsDTO.trainsArrivals.remove(action.trainStationId)
                state.trainArrivalsDTO.trainsArrivals.put(action.trainStationId, action.trainArrival)
            }

            state = state.copy(
                lastStateChange = Date(),
                trainArrivalsDTO = state.trainArrivalsDTO,
                trainStationStatus = trainStationStatus,
                trainStationArrival = action.trainArrival
            )
        }
        is BusStopArrivalsAction -> {
            state = state.copy(
                lastStateChange = Date(),
                busStopError = action.error,
                busStopErrorMessage = action.errorMessage,
                busArrivalStopDTO = action.busArrivalStopDTO
            )
        }
        is BikeStationAction -> {
            val bikeStations = if (action.error) state.bikeStations else action.bikeStations
            val bikeStationsStatus = when {
                action.error && bikeStations.isEmpty() -> Status.FULL_FAILURE
                action.error && bikeStations.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                bikeStationsStatus = bikeStationsStatus,
                bikeStationsErrorMessage = action.errorMessage,
                bikeStations = bikeStations
            )
        }
        is BusRoutesAction -> {
            val busRoutes = if (action.error) state.busRoutes else action.busRoutes
            val status = when {
                action.error && busRoutes.isEmpty() -> Status.FULL_FAILURE
                action.error && busRoutes.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                busRoutes = if (action.error) state.busRoutes else action.busRoutes,
                busRoutesStatus = status,
                busRoutesErrorMessage = action.errorMessage
            )
        }
        is AlertAction -> {
            val alertsDTO = if (action.error) state.alertsDTO else action.routesAlertsDTO
            val status = when {
                action.error && alertsDTO.isEmpty() -> Status.FULL_FAILURE
                action.error && alertsDTO.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                lastStateChange = Date(),
                alertStatus = status,
                alertErrorMessage = action.errorMessage,
                alertsDTO = alertsDTO
            )
        }
        else -> Log.w(TAG, "Action $action unknown")
    }
    return state
}

private const val TAG = "Redux"
