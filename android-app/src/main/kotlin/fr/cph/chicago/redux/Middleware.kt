package fr.cph.chicago.redux

import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.rx.RxUtil
import org.rekotlin.Middleware
import org.rekotlin.StateType

internal val loadLocalAndFavoritesDataMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadLocalAndFavoritesDataAction)?.let {
                RxUtil.createLocalAndFavoritesDataSingle()
                    .subscribe(
                        { favoritesDTO ->
                            next(LoadLocalAndFavoritesDataAction(
                                error = false,
                                trainArrivalsDTO = favoritesDTO.trainArrivalDTO,
                                busArrivalsDTO = favoritesDTO.busArrivalDTO))
                        },
                        { throwable ->
                            Log.e(TAG, throwable.message, throwable)
                            next(LoadLocalAndFavoritesDataAction(error = true, throwable = throwable))
                        }
                    )
            } ?: next(action)
        }
    }
}

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
                            Log.e(TAG, throwable.message, throwable)
                            next(action)
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val loadFavoritesDataMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadFavoritesDataAction)?.let {
                RxUtil.createAllDataSingle()
                    .subscribe(
                        { next(LoadFavoritesDataAction(favoritesDTO = it)) },
                        { throwable ->
                            Log.e(TAG, throwable.message, throwable)
                            next(LoadFavoritesDataAction(error = true, throwable = throwable))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val loadTrainStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadTrainStationAction)?.let {
                RxUtil.createTrainArrivalsSingle(action.trainStation)
                    .subscribe(
                        { trainArrival ->
                            next(LoadTrainStationAction(
                                trainStation = action.trainStation,
                                error = false,
                                trainArrival = trainArrival))
                        },
                        { throwable ->
                            Log.e(TAG, throwable.message, throwable)
                            val errorMessage = if (throwable is ConnectException)
                                R.string.message_connect_error
                            else
                                R.string.message_something_went_wrong
                            next(LoadTrainStationAction(
                                trainStation = action.trainStation,
                                error = true,
                                errorMessage = errorMessage)
                            )
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val loadBusStopArrivalsMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? LoadBusStopArrivalsAction)?.let {
                RxUtil.createBusArrivalObs(action.requestRt, action.busRouteId, action.requestStopId, action.busStopId, action.bound, action.boundTitle)
                    .subscribe(
                        { busArrivalStopDTO -> next(LoadBusStopArrivalsAction(busArrivalStopDTO = busArrivalStopDTO)) },
                        { throwable ->
                            val errorMessage = if (throwable is ConnectException)
                                R.string.message_connect_error
                            else
                                R.string.message_something_went_wrong
                            next(LoadBusStopArrivalsAction(
                                error = true,
                                errorMessage = errorMessage)
                            )
                        }
                    )
            } ?: next(action)
        }
    }
}


private const val TAG = "Middleware"
