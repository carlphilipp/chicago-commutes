package fr.cph.chicago.redux

import android.util.Log
import org.rekotlin.Action
import org.rekotlin.Store
import java.util.*

val mainStore = Store(
    reducer = ::reducer,
    state = null,
    middleware = listOf(
        loadLocalAndFavoritesDataMiddleware,
        loadFirstDataMiddleware,
        loadFavoritesDataMiddleware,
        loadTrainStationMiddleware,
        loadBusStopArrivalsMiddleware,
        loadBikeStationMiddleware,
        loadBusRoutesMiddleware
    )
)

fun reducer(action: Action, oldState: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = oldState ?: AppState()

    when (action) {
        is LoadFirstDataAction -> {
            state = state.copy(
                lastAction = Date(),
                highlightBackground = false,
                busRoutesError = action.busRoutesError,
                bikeStationsError = action.bikeStationsError,
                busRoutes = action.busRoutes,
                bikeStations = action.bikeStations
            )
        }
        is LoadLocalAndFavoritesDataAction -> {
            state = state.copy(
                lastAction = Date(),
                lastUpdate = Date(),
                highlightBackground = false,
                error = action.error,
                trainArrivalsDTO = action.trainArrivalsDTO,
                busArrivalsDTO = action.busArrivalsDTO
            )
        }
        is LoadFavoritesDataAction -> {
            state = state.copy(
                lastAction = Date(),
                lastUpdate = Date(),
                highlightBackground = true,
                error = action.error,
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = action.favoritesDTO.bikeStations
            )
        }
        is LoadTrainStationAction -> {
            if (action.error) {
                state = state.copy(
                    lastAction = Date(),
                    highlightBackground = false,
                    trainStationError = true,
                    trainStationErrorMessage = action.errorMessage
                )
            } else {
                state.trainArrivalsDTO.trainArrivalSparseArray.remove(action.trainStation.id)
                state.trainArrivalsDTO.trainArrivalSparseArray.put(action.trainStation.id, action.trainArrival)
                val newTrainArrivals = state.trainArrivalsDTO
                state = state.copy(
                    lastAction = Date(),
                    highlightBackground = false,
                    trainArrivalsDTO = newTrainArrivals,
                    trainStationError = false,
                    trainStationArrival = action.trainArrival
                )
            }
        }
        is LoadBusStopArrivalsAction -> {
            state = state.copy(
                lastAction = Date(),
                highlightBackground = false,
                busStopError = action.error,
                busStopErrorMessage = action.errorMessage,
                busArrivalStopDTO = action.busArrivalStopDTO
            )
        }
        is LoadBikeStationAction -> {
            state = state.copy(
                lastAction = Date(),
                highlightBackground = false,
                bikeStationsError = action.error,
                bikeStationsErrorMessage = action.errorMessage,
                bikeStations = action.bikeStations
            )
        }
        is LoadBusRoutesAction -> {
            state = state.copy(
                lastAction = Date(),
                highlightBackground = false,
                busRoutes = action.busRoutes,
                busRoutesError = action.error
            )
        }
        is HighlightBackgroundDone -> {
            state = state.copy(highlightBackground = false)
        }
        else -> Log.w(TAG, "Action $action unknown")

    }
    return state
}

private const val TAG = "Redux"
