package fr.cph.chicago.redux

import org.rekotlin.Action
import org.rekotlin.Store
import java.util.Date

val mainStore = Store(
    reducer = ::reducer,
    state = null,
    middleware = listOf(
        loadLocalAndFavoritesDataMiddleware,
        loadFirstDataMiddleware,
        loadFavoritesDataMiddleware,
        loadTrainStationMiddleware,
        loadBusStopArrivalsMiddleware
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
                throwable = action.throwable,
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
                throwable = action.throwable,
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = action.favoritesDTO.bikeStations
            )
        }
        is LoadTrainStationAction -> {
            if (action.error) {
                state = state.copy(
                    lastAction = Date(),
                    trainStationError = true,
                    trainStationErrorMessage = action.errorMessage
                )
            } else {
                state.trainArrivalsDTO.trainArrivalSparseArray.remove(action.trainStation.id)
                state.trainArrivalsDTO.trainArrivalSparseArray.put(action.trainStation.id, action.trainArrival)
                val newTrainArrivals = state.trainArrivalsDTO
                state = state.copy(
                    lastAction = Date(),
                    trainArrivalsDTO = newTrainArrivals,
                    trainStationError = false,
                    trainStationArrival = action.trainArrival
                )
            }
        }
        is LoadBusStopArrivalsAction -> {
            state = state.copy(
                lastAction = Date(),
                busStopError = action.error,
                busStopErrorMessage = action.errorMessage,
                busArrivalStopDTO = action.busArrivalStopDTO
            )
        }
    }
    return state
}
