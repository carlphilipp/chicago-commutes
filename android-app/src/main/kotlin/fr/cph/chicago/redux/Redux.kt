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
        is BusRoutesAndBikeStationAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = BusRoutesAndBikeStationAction(),
                busRoutesError = action.busRoutesError,
                bikeStationsError = action.bikeStationsError,
                busRoutes = action.busRoutes,
                bikeStations = action.bikeStations
            )
        }
        is BaseAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = BaseAction(),
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.trainArrivalsDTO,
                busArrivalsDTO = action.busArrivalsDTO
            )
        }
        is FavoritesAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = FavoritesAction(),
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = action.favoritesDTO.bikeStations,
                bikeStationsError = action.favoritesDTO.bikeError
            )
        }
        is TrainStationAction -> {
            if (action.error) {
                state = state.copy(
                    lastStateChange = Date(),
                    lastAction = TrainStationAction(),
                    trainStationError = true,
                    trainStationErrorMessage = action.errorMessage
                )
            } else {
                state.trainArrivalsDTO.trainsArrivals.remove(action.trainStation.id)
                state.trainArrivalsDTO.trainsArrivals.put(action.trainStation.id, action.trainArrival)
                val newTrainArrivals = state.trainArrivalsDTO
                state = state.copy(
                    lastStateChange = Date(),
                    lastAction = TrainStationAction(),
                    trainArrivalsDTO = newTrainArrivals,
                    trainStationError = false,
                    trainStationArrival = action.trainArrival
                )
            }
        }
        is BusStopArrivalsAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = BusStopArrivalsAction(),
                busStopError = action.error,
                busStopErrorMessage = action.errorMessage,
                busArrivalStopDTO = action.busArrivalStopDTO
            )
        }
        is BikeStationAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = BikeStationAction(),
                bikeStationsError = action.error,
                bikeStationsErrorMessage = action.errorMessage,
                bikeStations = if (action.error) mainStore.state.bikeStations else action.bikeStations
            )
        }
        is BusRoutesAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = BusRoutesAction(),
                busRoutes = if (action.error) state.busRoutes else action.busRoutes,
                busRoutesError = action.error,
                busRoutesErrorMessage = action.errorMessage
            )
        }
        is AlertAction -> {
            state = state.copy(
                lastStateChange = Date(),
                lastAction = AlertAction(),
                alertError = action.error,
                alertErrorMessage = action.errorMessage,
                alertsDTO = if (action.error) mainStore.state.alertsDTO else action.routesAlertsDTO
            )
        }
        else -> Log.w(TAG, "Action $action unknown")
    }
    return state
}

private const val TAG = "Redux"
