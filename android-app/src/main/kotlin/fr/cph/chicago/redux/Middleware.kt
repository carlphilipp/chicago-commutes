/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.redux

import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.MixedService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.schedulers.Schedulers
import org.rekotlin.Middleware
import org.rekotlin.StateType
import timber.log.Timber

private val mixedService by lazy { MixedService }
private val trainService by lazy { TrainService }
private val busService by lazy { BusService }
private val bikeService by lazy { BikeService }
private val alertService by lazy { AlertService }
private val preferenceService by lazy { PreferenceService }

internal val baseMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? BaseAction)?.let {
                mixedService.baseData()
                    .map { baseDTO ->
                        BaseAction(
                            trainArrivalsDTO = baseDTO.trainArrivalsDTO,
                            busArrivalsDTO = baseDTO.busArrivalsDTO,
                            trainFavorites = baseDTO.trainFavorites,
                            busFavorites = baseDTO.busFavorites,
                            busRouteFavorites = baseDTO.busRouteFavorites,
                            bikeFavorites = baseDTO.bikeFavorites,
                        )
                    }
                    .observeOn(Schedulers.computation())
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
                            bikeStations = bikeStations
                        )
                    }
                    .observeOn(Schedulers.computation())
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
                    .map { busRoutes -> BusRoutesAction(error = false, busRoutes = busRoutes) }
                    .observeOn(Schedulers.computation())
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
                            bikeError = favoritesDTO.bikeError
                        )
                        FavoritesAction(favoritesDTO = newFavorites)
                    }
                    .observeOn(Schedulers.computation())
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val trainStationMiddleware: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? TrainStationAction)?.let {
                trainService.loadStationTrainArrival(action.trainStationId)
                    .map { trainArrival ->
                        TrainStationAction(
                            trainStationId = action.trainStationId,
                            error = false,
                            trainArrival = trainArrival
                        )
                    }
                    .observeOn(Schedulers.computation())
                    .subscribe(
                        { newAction -> next(newAction) },
                        { throwable ->
                            Timber.e(throwable)
                            next(
                                TrainStationAction(
                                    trainStationId = action.trainStationId,
                                    error = true,
                                    errorMessage = buildErrorMessage(throwable)
                                )
                            )
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
                busService.loadBusArrivals(action.busRouteId, action.busStopId, action.bound, action.boundTitle)
                    .map { busArrivalStopDTO -> BusStopArrivalsAction(busArrivalStopDTO = busArrivalStopDTO) }
                    .observeOn(Schedulers.computation())
                    .subscribe(
                        { newAction -> next(newAction) },
                        { throwable ->
                            Timber.e(throwable)
                            next(
                                BusStopArrivalsAction(
                                    error = true,
                                    errorMessage = buildErrorMessage(throwable)
                                )
                            )
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
                    .map { bikeStations -> BikeStationAction(bikeStations = bikeStations) }
                    .observeOn(Schedulers.computation())
                    .subscribe(
                        { newAction -> next(newAction) },
                        { throwable ->
                            Timber.e(throwable)
                            next(BikeStationAction(error = true, errorMessage = buildErrorMessage(throwable)))
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
                    .map { routesAlertsDTO -> AlertAction(routesAlertsDTO = routesAlertsDTO) }
                    .observeOn(Schedulers.computation())
                    .subscribe(
                        { newAction -> next(newAction) },
                        { throwable ->
                            Timber.e(throwable)
                            next(
                                AlertAction(
                                    error = true,
                                    errorMessage = buildErrorMessage(throwable)
                                )
                            )
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
                preferenceService.addTrainStationToFavorites(action.id)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> AddTrainFavoriteAction(id = action.id, trainFavorites = favorites) }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val removeTrainFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveTrainFavoriteAction)?.let {
                preferenceService.removeTrainFromFavorites(action.id)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> RemoveTrainFavoriteAction(id = action.id, trainFavorites = favorites) }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val addBusFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AddBusFavoriteAction)?.let {
                preferenceService.addBusToFavorites(
                    busRouteId = action.busRouteId,
                    busStopId = action.busStopId,
                    bound = action.boundTitle,
                    busRouteName = action.busRouteName,
                    busStopName = action.busStopName
                )
                    .observeOn(Schedulers.computation())
                    .map { favorites ->
                        val favoritesBusRoute = busService.extractBusRouteFavorites(favorites)
                        AddBusFavoriteAction(
                            busRouteId = action.busRouteId,
                            busStopId = action.busStopId,
                            boundTitle = action.boundTitle,
                            busFavorites = favorites,
                            busRouteFavorites = favoritesBusRoute
                        )
                    }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val removeBusFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveBusFavoriteAction)?.let {
                preferenceService.removeBusFromFavorites(
                    busRouteId = action.busRouteId,
                    busStopId = action.busStopId,
                    bound = action.boundTitle
                )
                    .observeOn(Schedulers.computation())
                    .map { favorites ->
                        val favoritesBusRoute = busService.extractBusRouteFavorites(favorites)
                        RemoveBusFavoriteAction(
                            busRouteId = action.busRouteId,
                            busStopId = action.busStopId,
                            boundTitle = action.boundTitle,
                            busFavorites = favorites,
                            busRouteFavorites = favoritesBusRoute
                        )
                    }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val addBikeFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? AddBikeFavoriteAction)?.let {
                preferenceService.addBikeToFavorites(action.id, action.stationName)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> AddBikeFavoriteAction(id = action.id, bikeFavorites = favorites) }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
            } ?: next(action)
        }
    }
}

internal val removeBikeFavorites: Middleware<StateType> = { _, _ ->
    { next ->
        { action ->
            (action as? RemoveBikeFavoriteAction)?.let {
                preferenceService.removeBikeFromFavorites(action.id)
                    .observeOn(Schedulers.computation())
                    .map { favorites -> RemoveBikeFavoriteAction(id = action.id, bikeFavorites = favorites) }
                    .subscribe({ newAction -> next(newAction) }, { error -> Timber.e(error) })
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
