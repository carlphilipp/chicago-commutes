package fr.cph.chicago.redux

import android.util.Log
import android.util.SparseArray
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.rx.RxUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.rekotlin.Action
import org.rekotlin.Middleware
import org.rekotlin.StateType
import org.rekotlin.Store
import java.util.Calendar

data class AppState(
    val highlightBackground: Boolean = false,
    val error: Boolean? = null,
    val throwable: Throwable? = null,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),

    val busRoutesError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),

    val bikeStationsError: Boolean = false,
    val bikeStations: List<BikeStation> = listOf()
) : StateType

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

internal val loadLocalAndFavoritesDataMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadLocalAndFavoritesDataAction)?.let {
                // FIXME: To put that in ObservableUtils
                // Train local data
                val trainLocalData = RxUtil.createLocalTrainDataObs()

                // Bus local data
                val busLocalData = RxUtil.createLocalBusDataObs()

                // Train online favorites
                val trainOnlineFavorites = RxUtil.createFavoritesTrainArrivalsObs()

                // Bus online favorites
                val busOnlineFavorites = RxUtil.createFavoritesBusArrivalsObs()

                // Run local first and then online: Ensure that local data is loaded first
                Single.zip(trainLocalData, busLocalData, BiFunction { _: Any, _: Any -> true })
                    .doAfterTerminate {
                        Observable.zip(trainOnlineFavorites, busOnlineFavorites, BiFunction { trainArrivalsDTO: TrainArrivalDTO, busArrivalsDTO: BusArrivalDTO ->
                            TrainService.setTrainStationError(false)
                            BusService.setBusRouteError(false)
                            App.instance.lastUpdate = Calendar.getInstance().time
                            FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf())
                        })
                            .subscribe(
                                { favoritesDTO ->
                                    next(LoadLocalAndFavoritesDataAction(
                                        error = false,
                                        trainArrivalsDTO = favoritesDTO.trainArrivalDTO,
                                        busArrivalsDTO = favoritesDTO.busArrivalDTO))
                                },
                                { error ->
                                    Log.e(TAG, error.message, error)
                                    next(LoadLocalAndFavoritesDataAction(error = true, throwable = error))
                                }
                            )
                    }.subscribe()

            } ?: next(action)
        }
    }
}

internal val loadFavoritesDataMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadFavoritesDataAction)?.let {
                RxUtil.createAllDataObs()
                    .subscribe(
                        { next(LoadFavoritesDataAction(favoritesDTO = it)) },
                        { onError ->
                            Log.e(TAG, onError.message, onError)
                            next(LoadFavoritesDataAction(error = true, throwable = onError))
                        }
                    )
            } ?: next(action)
        }
    }
}

// Bus Routes + Bike stations
data class LoadFirstDataAction(
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),
    val bikeStations: List<BikeStation> = listOf()
) : Action

internal val loadFirstDataMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadFirstDataAction)?.let {
                RxUtil.createOnFirstLoadObs()
                    .subscribe(
                        { (busRoutesError, bikeStationsError, busRoutes, bikeStations) ->
                            next(LoadFirstDataAction(
                                busRoutesError = busRoutesError,
                                bikeStationsError = bikeStationsError,
                                busRoutes = busRoutes,
                                bikeStations = bikeStations)
                            )
                        },
                        { throwable ->
                            next(action)
                        }
                    )
            } ?: next(action)
        }
    }
}

val mainStore = Store(
    reducer = ::reducer,
    state = null,
    middleware = listOf(
        loadLocalAndFavoritesDataMiddleware,
        loadFirstDataMiddleware,
        loadFavoritesDataMiddleware
    )
)

fun reducer(action: Action, state: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = state ?: AppState()

    when (action) {
        is LoadFirstDataAction -> {
            state = state.copy(
                highlightBackground = false,
                busRoutesError = action.busRoutesError,
                bikeStationsError = action.bikeStationsError,
                busRoutes = action.busRoutes,
                bikeStations = action.bikeStations
            )
        }
        is LoadLocalAndFavoritesDataAction -> {
            state = state.copy(
                highlightBackground = false,
                error = action.error,
                throwable = action.throwable,
                trainArrivalsDTO = action.trainArrivalsDTO,
                busArrivalsDTO = action.busArrivalsDTO
            )
        }
        is LoadFavoritesDataAction -> {
            state = state.copy(
                highlightBackground = true,
                error = action.error,
                throwable = action.throwable,
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = action.favoritesDTO.bikeStations
            )
        }
    }
    return state
}

private val TAG = "Redux"
