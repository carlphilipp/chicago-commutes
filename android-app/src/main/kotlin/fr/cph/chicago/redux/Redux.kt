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
        loadTrainStationMiddleware
    )
)

fun reducer(action: Action, state: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = state ?: AppState()

    when (action) {
        is LoadFirstDataAction -> {
            state = state.copy(
                derp = Date(),
                highlightBackground = false,
                busRoutesError = action.busRoutesError,
                bikeStationsError = action.bikeStationsError,
                busRoutes = action.busRoutes,
                bikeStations = action.bikeStations
            )
        }
        is LoadLocalAndFavoritesDataAction -> {
            state = state.copy(
                derp = Date(),
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
                derp = Date(),
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
            state.trainArrivalsDTO.trainArrivalSparseArray.remove(action.trainStation.id)
            state.trainArrivalsDTO.trainArrivalSparseArray.put(action.trainStation.id, action.trainArrival)
            val newTrainArrivals = state.trainArrivalsDTO
            state = state.copy(
                derp = Date(),
                trainArrivalsDTO = newTrainArrivals,
                trainStationError = false,
                trainStationArrival = action.trainArrival
            )
        }
    }
    Log.e("Redux", "Reducer, return current state")
    return state
}
