package fr.cph.chicago.redux

import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.exception.BaseException
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.MixedService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import org.rekotlin.Middleware
import org.rekotlin.StateType
import timber.log.Timber

private val mixedService = MixedService
private val trainService = TrainService
private val busService = BusService
private val bikeService = BikeService
private val alertService = AlertService
private val preferenceService = PreferenceService

internal val baseMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BaseAction)?.let {
                mixedService.local()
                    .flatMap { localDTO ->
                        if (localDTO.busLocalError || localDTO.trainLocalError) {
                            throw BaseException()
                        }
                        val trainFavorites = preferenceService.getTrainFavorites()
                        val busFavorites = preferenceService.getBusFavorites().subscribeOn(Schedulers.io())
                        val bikeFavorites = preferenceService.getBikeFavorites().subscribeOn(Schedulers.io())
                        Single.zip(
                            mixedService.baseArrivals().observeOn(Schedulers.computation()),
                            trainFavorites.observeOn(Schedulers.computation()),
                            busFavorites.observeOn(Schedulers.computation()),
                            bikeFavorites.observeOn(Schedulers.computation()),
                            Function4 { favoritesDTO: FavoritesDTO, favoritesTrains: List<Int>, favoritesBuses: List<String>, favoritesBikes: List<Int> ->
                                val trainArrivals = if (favoritesDTO.trainArrivalDTO.error)
                                    TrainArrivalDTO(store.state.trainArrivalsDTO.trainsArrivals, true)
                                else
                                    favoritesDTO.trainArrivalDTO
                                val busArrivals = if (favoritesDTO.busArrivalDTO.error)
                                    BusArrivalDTO(store.state.busArrivalsDTO.busArrivals, true)
                                else
                                    favoritesDTO.busArrivalDTO
                                val favoritesBusRoute = busService.extractBusRouteFavorites(favoritesBuses)
                                BaseAction(
                                    trainArrivalsDTO = trainArrivals,
                                    busArrivalsDTO = busArrivals,
                                    trainFavorites = favoritesTrains,
                                    busFavorites = favoritesBuses,
                                    busRouteFavorites = favoritesBusRoute,
                                    bikeFavorites = favoritesBikes)
                            })
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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
                mixedService.busRoutesAndBikeStation()
                    .map { (busRoutesError, bikeStationsError, busRoutes, bikeStations) ->
                        BusRoutesAndBikeStationAction(
                            busRoutesError = busRoutesError,
                            bikeStationsError = bikeStationsError,
                            busRoutes = busRoutes,
                            bikeStations = bikeStations)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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
                busService.busRoutes()
                    .observeOn(Schedulers.computation())
                    .map { busRoutes -> BusRoutesAction(error = false, busRoutes = busRoutes) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
                        { throwable ->
                            Timber.e(throwable)
                            next(BusRoutesAction(error = true, errorMessage = buildErrorMessage(throwable)))
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
                mixedService.favorites()
                    .observeOn(Schedulers.computation())
                    .map { favoritesDTO ->
                        val trainArrivals = if (favoritesDTO.trainArrivalDTO.error)
                            TrainArrivalDTO(store.state.trainArrivalsDTO.trainsArrivals, true)
                        else
                            favoritesDTO.trainArrivalDTO
                        val busArrivals = if (favoritesDTO.busArrivalDTO.error)
                            BusArrivalDTO(store.state.busArrivalsDTO.busArrivals, true)
                        else
                            favoritesDTO.busArrivalDTO
                        val newFavorites = FavoritesDTO(
                            trainArrivalDTO = trainArrivals,
                            busArrivalDTO = busArrivals,
                            bikeStations = if (favoritesDTO.bikeError) store.state.bikeStations else favoritesDTO.bikeStations,
                            bikeError = favoritesDTO.bikeError)
                        FavoritesAction(favoritesDTO = newFavorites)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val trainStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? TrainStationAction)?.let {
                trainService.loadStationTrainArrival(action.trainStationId)
                    .observeOn(Schedulers.computation())
                    .map { trainArrival ->
                        TrainStationAction(
                            trainStationId = action.trainStationId,
                            error = false,
                            trainArrival = trainArrival)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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
                busService.loadBusArrivals(action.requestRt, action.busRouteId, action.requestStopId, action.busStopId, action.bound, action.boundTitle)
                    .observeOn(Schedulers.computation())
                    .map { busArrivalStopDTO -> BusStopArrivalsAction(busArrivalStopDTO = busArrivalStopDTO) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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
                bikeService.allBikeStations()
                    .observeOn(Schedulers.computation())
                    .map { bikeStations -> BikeStationAction(bikeStations = bikeStations) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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
                alertService.alerts()
                    .observeOn(Schedulers.computation())
                    .map { routesAlertsDTO -> AlertAction(routesAlertsDTO = routesAlertsDTO) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newAction -> next(newAction) },
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

internal val addTrainFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AddTrainFavoriteAction)?.let {
                preferenceService.addToTrainFavorites(action.id)
                    .map { favorites -> AddTrainFavoriteAction(id = action.id, trainFavorites = favorites) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val removeTrainFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveTrainFavoriteAction)?.let {
                preferenceService.removeFromTrainFavorites(action.id)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> RemoveTrainFavoriteAction(id = action.id, trainFavorites = favorites) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val addBusFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AddBusFavoriteAction)?.let {
                PreferenceService.addToBusFavorites(
                    busRouteId = action.busRouteId,
                    busStopId = action.busStopId,
                    bound = action.boundTitle,
                    busRouteName = action.busRouteName,
                    busStopName = action.busStopName)
                    .observeOn(Schedulers.computation())
                    .map { favorites ->
                        val favoritesBusRoute = busService.extractBusRouteFavorites(favorites)
                        AddBusFavoriteAction(
                            busRouteId = action.busRouteId,
                            busStopId = action.busStopId,
                            boundTitle = action.boundTitle,
                            busFavorites = favorites,
                            busRouteFavorites = favoritesBusRoute)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val removeBusFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveBusFavoriteAction)?.let {
                PreferenceService.removeFromBusFavorites(
                    busRouteId = action.busRouteId,
                    busStopId = action.busStopId,
                    bound = action.boundTitle)
                    .observeOn(Schedulers.computation())
                    .map { favorites ->
                        val favoritesBusRoute = busService.extractBusRouteFavorites(favorites)
                        RemoveBusFavoriteAction(
                            busRouteId = action.busRouteId,
                            busStopId = action.busStopId,
                            boundTitle = action.boundTitle,
                            busFavorites = favorites,
                            busRouteFavorites = favoritesBusRoute)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val addBikeFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AddBikeFavoriteAction)?.let {
                PreferenceService.addToBikeFavorites(action.id, action.stationName)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> AddBikeFavoriteAction(id = action.id, bikeFavorites = favorites) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
            } ?: next(action)
        }
    }
}

internal val removeBikeFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveBikeFavoriteAction)?.let {
                PreferenceService.removeFromBikeFavorites(action.id)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> RemoveBikeFavoriteAction(id = action.id, bikeFavorites = favorites) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { newAction -> next(newAction) }
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
