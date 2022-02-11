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

import java.util.Date
import java.util.UUID
import org.rekotlin.Action
import org.rekotlin.Store
import timber.log.Timber

val store = Store(
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
        alertMiddleware,
        addTrainFavorites,
        removeTrainFavorites,
        addBusFavorites,
        removeBusFavorites,
        addBikeFavorites,
        removeBikeFavorites
    )
)

fun reducer(action: Action, oldState: State?): State {
    // if no state has been provided, create the default state
    var state = oldState ?: State()

    when (action) {
        is ResetStateAction -> {
            state = state.copy(status = Status.UNKNOWN)
        }
        is UpdateStatus -> {
            state = state.copy(status = action.status)
        }
        is DefaultSettingsAction -> {
            state = state.copy(
                ctaTrainKey = action.ctaTrainKey,
                ctaBusKey = action.ctaBusKey,
                googleStreetKey = action.googleStreetKey)
        }
        is BaseAction -> {
            val status = when {
                action.localError -> Status.FULL_FAILURE
                action.trainArrivalsDTO.error || action.busArrivalsDTO.error -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                status = status,
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.trainArrivalsDTO,
                busArrivalsDTO = action.busArrivalsDTO,
                trainFavorites = action.trainFavorites,
                busFavorites = action.busFavorites,
                busRouteFavorites = action.busRouteFavorites,
                bikeFavorites = action.bikeFavorites,
                random = UUID.randomUUID().toString()
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
            val newStatus = if (state.status == Status.FAILURE_NO_SHOW && (status == Status.FAILURE || status == Status.FULL_FAILURE)) {
                Status.FAILURE_NO_SHOW
            } else {
                status
            }
            state = state.copy(
                status = newStatus,
                busRoutesStatus = busRoutesStatus,
                bikeStationsStatus = bikeStationsStatus,
                busRoutes = busRoutes,
                bikeStations = bikeStations,
                random = UUID.randomUUID().toString()
            )
        }
        is FavoritesAction -> {
            val status = when {
                action.favoritesDTO.trainArrivalDTO.error && action.favoritesDTO.busArrivalDTO.error && action.favoritesDTO.bikeError -> {
                    when {
                        action.favoritesDTO.trainArrivalDTO.trainsArrivals.isEmpty()
                            && action.favoritesDTO.busArrivalDTO.busArrivals.isEmpty()
                            && action.favoritesDTO.bikeStations.isEmpty() -> Status.FULL_FAILURE
                        else -> Status.FAILURE
                    }
                }
                action.favoritesDTO.trainArrivalDTO.error || action.favoritesDTO.busArrivalDTO.error || action.favoritesDTO.bikeError -> Status.FAILURE
                else -> Status.SUCCESS
            }
            val bikeStations = if (action.favoritesDTO.bikeError) state.bikeStations else action.favoritesDTO.bikeStations
            val bikeStationsStatus = when {
                action.favoritesDTO.bikeError && bikeStations.isEmpty() -> Status.FULL_FAILURE
                action.favoritesDTO.bikeError && bikeStations.isNotEmpty() -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                status = status,
                lastFavoritesUpdate = Date(),
                trainArrivalsDTO = action.favoritesDTO.trainArrivalDTO,
                busArrivalsDTO = action.favoritesDTO.busArrivalDTO,
                bikeStations = bikeStations,
                bikeStationsStatus = bikeStationsStatus,
                random = UUID.randomUUID().toString()
            )
        }
        is TrainStationAction -> {
            val trainStationStatus = when {
                action.error -> Status.FAILURE
                else -> Status.SUCCESS
            }
            if (trainStationStatus == Status.SUCCESS) {
                state.trainArrivalsDTO.trainsArrivals.remove(action.trainStationId)
                state.trainArrivalsDTO.trainsArrivals[action.trainStationId] = action.trainArrival
            }

            state = state.copy(
                trainArrivalsDTO = state.trainArrivalsDTO,
                trainStationStatus = trainStationStatus,
                trainStationArrival = action.trainArrival,
                random = UUID.randomUUID().toString()
            )
        }
        is BusStopArrivalsAction -> {
            val busStopStatus = when {
                action.error -> Status.FAILURE
                else -> Status.SUCCESS
            }
            state = state.copy(
                busStopStatus = busStopStatus,
                busStopErrorMessage = action.errorMessage,
                busArrivalStopDTO = action.busArrivalStopDTO,
                random = UUID.randomUUID().toString()
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
                bikeStationsStatus = bikeStationsStatus,
                bikeStationsErrorMessage = action.errorMessage,
                bikeStations = bikeStations,
                random = UUID.randomUUID().toString()
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
                busRoutes = if (action.error) state.busRoutes else action.busRoutes,
                busRoutesStatus = status,
                busRoutesErrorMessage = action.errorMessage,
                random = UUID.randomUUID().toString()
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
                alertStatus = status,
                alertErrorMessage = action.errorMessage,
                alertsDTO = alertsDTO,
                random = UUID.randomUUID().toString()
            )
        }
        is AddTrainFavoriteAction -> {
            state = state.copy(
                trainFavorites = action.trainFavorites,
                trainStationStatus = Status.ADD_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        is RemoveTrainFavoriteAction -> {
            state = state.copy(
                trainFavorites = action.trainFavorites,
                trainStationStatus = Status.REMOVE_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        is ResetTrainFavoriteAction -> {
            state = state.copy(
                trainStationStatus = Status.UNKNOWN,
                random = UUID.randomUUID().toString()
            )
        }
        is AddBusFavoriteAction -> {
            state = state.copy(
                busFavorites = action.busFavorites,
                busRouteFavorites = action.busRouteFavorites,
                busStopStatus = Status.ADD_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        is RemoveBusFavoriteAction -> {
            state = state.copy(
                busFavorites = action.busFavorites,
                busRouteFavorites = action.busRouteFavorites,
                busStopStatus = Status.REMOVE_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        is AddBikeFavoriteAction -> {
            state = state.copy(
                bikeFavorites = action.bikeFavorites,
                bikeStationsStatus = Status.ADD_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        is RemoveBikeFavoriteAction -> {
            state = state.copy(
                bikeFavorites = action.bikeFavorites,
                bikeStationsStatus = Status.REMOVE_FAVORITES,
                random = UUID.randomUUID().toString()
            )
        }
        else -> Timber.w("Action %s unknown", action)
    }
    return state
}
