package fr.cph.chicago.redux

import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.exception.BaseException
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.rx.RxUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import org.rekotlin.Middleware
import org.rekotlin.StateType
import timber.log.Timber

internal val baseMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BaseAction)?.let {
                RxUtil.local()
                    .flatMap { localDTO ->
                        if (localDTO.busLocalError || localDTO.trainLocalError) {
                            throw BaseException()
                        }
                        RxUtil.baseFavorites()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { favoritesDTO ->
                            val trainArrivals = if (favoritesDTO.trainArrivalDTO.error)
                                TrainArrivalDTO(mainStore.state.trainArrivalsDTO.trainsArrivals, true)
                            else
                                favoritesDTO.trainArrivalDTO
                            val busArrivals = if (favoritesDTO.busArrivalDTO.error)
                                BusArrivalDTO(mainStore.state.busArrivalsDTO.busArrivals, true)
                            else
                                favoritesDTO.busArrivalDTO
                            next(BaseAction(trainArrivalsDTO = trainArrivals, busArrivalsDTO = busArrivals))
                        },
                        { throwable ->
                            Timber.e(throwable)
                            next(BaseAction(localError = true))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val busRoutesAndBikeStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BusRoutesAndBikeStationAction)?.let {
                RxUtil.busRoutesAndBikeStation()
                    .subscribe(
                        { (busRoutesError, bikeStationsError, busRoutes, bikeStations) ->
                            next(BusRoutesAndBikeStationAction(
                                busRoutesError = busRoutesError,
                                bikeStationsError = bikeStationsError,
                                busRoutes = busRoutes,
                                bikeStations = bikeStations))
                        },
                        { throwable ->
                            Timber.e(throwable)
                            next(action)
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val busRoutesMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BusRoutesAction)?.let {
                RxUtil.busRoutes()
                    .subscribe(
                        { busRoutes -> next(BusRoutesAction(error = false, busRoutes = busRoutes)) },
                        { throwable ->
                            Timber.e(throwable)
                            next(
                                BusRoutesAction(
                                    error = true,
                                    errorMessage = buildErrorMessage(throwable)))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val favoritesMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? FavoritesAction)?.let {
                RxUtil.favorites()
                    .subscribe { favoritesDTO ->
                        val trainArrivals = if (favoritesDTO.trainArrivalDTO.error)
                            TrainArrivalDTO(mainStore.state.trainArrivalsDTO.trainsArrivals, true)
                        else
                            favoritesDTO.trainArrivalDTO
                        val busArrivals = if (favoritesDTO.busArrivalDTO.error)
                            BusArrivalDTO(mainStore.state.busArrivalsDTO.busArrivals, true)
                        else
                            favoritesDTO.busArrivalDTO
                        val newFavorites = FavoritesDTO(
                            trainArrivalDTO = trainArrivals,
                            busArrivalDTO = busArrivals,
                            bikeStations = if (favoritesDTO.bikeError) mainStore.state.bikeStations else favoritesDTO.bikeStations,
                            bikeError = favoritesDTO.bikeError)
                        next(FavoritesAction(favoritesDTO = newFavorites))
                    }
            } ?: next(action)
        }
    }
}

internal val trainStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? TrainStationAction)?.let {
                RxUtil.trainArrivals(action.trainStationId)
                    .subscribe(
                        { trainArrival ->
                            next(TrainStationAction(
                                trainStationId = action.trainStationId,
                                error = false,
                                trainArrival = trainArrival))
                        },
                        { throwable ->
                            Timber.e(throwable)
                            next(TrainStationAction(
                                trainStationId = action.trainStationId,
                                error = true,
                                errorMessage = buildErrorMessage(throwable)))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val busStopArrivalsMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BusStopArrivalsAction)?.let {
                RxUtil.busArrivalsForStop(action.requestRt, action.busRouteId, action.requestStopId, action.busStopId, action.bound, action.boundTitle)
                    .subscribe(
                        { busArrivalStopDTO -> next(BusStopArrivalsAction(busArrivalStopDTO = busArrivalStopDTO)) },
                        { throwable ->
                            Timber.e(throwable)
                            next(BusStopArrivalsAction(
                                error = true,
                                errorMessage = buildErrorMessage(throwable)))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val bikeStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BikeStationAction)?.let {
                RxUtil.bikeAllStations()
                    .subscribe(
                        { bikeStations -> next(BikeStationAction(bikeStations = bikeStations)) },
                        { throwable ->
                            Timber.e(throwable)
                            next(BikeStationAction(
                                error = true,
                                errorMessage = buildErrorMessage(throwable)))
                        }
                    )
            } ?: next(action)
        }
    }
}

internal val alertMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AlertAction)?.let {
                RxUtil.alerts()
                    .subscribe(
                        { routesAlertsDTO -> next(AlertAction(routesAlertsDTO = routesAlertsDTO)) },
                        { throwable ->
                            Timber.e(throwable)
                            next(AlertAction(
                                error = true,
                                errorMessage = buildErrorMessage(throwable)))
                        }
                    )
            } ?: next(action)
        }
    }
}

private fun buildErrorMessage(throwable: Throwable): Int {
    return if (throwable is ConnectException)
        R.string.message_connect_error
    else
        R.string.message_something_went_wrong
}
