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
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import java.math.BigInteger
import org.apache.commons.lang3.StringUtils
import org.rekotlin.Action

data class ResetStateAction(val unit: Unit = Unit) : Action

data class UpdateStatus(val status: Status) : Action

data class DefaultSettingsAction(
    val ctaTrainKey: String,
    val ctaBusKey: String,
    val googleStreetKey: String
) : Action

data class BaseAction(
    val localError: Boolean = false,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(mutableMapOf(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),
    val trainFavorites: List<BigInteger> = listOf(),
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf(),
    val bikeFavorites: List<BigInteger> = listOf()
) : Action

data class FavoritesAction(
    val favoritesDTO: FavoritesDTO = FavoritesDTO(
        trainArrivalDTO = TrainArrivalDTO(mutableMapOf(), false),
        busArrivalDTO = BusArrivalDTO(listOf(), false),
        bikeError = false,
        bikeStations = listOf()
    )
) : Action

// Bus Routes
data class BusRoutesAction(
    val busRoutes: List<BusRoute> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bus Routes + Bike stations
data class BusRoutesAndBikeStationAction(
    val busRoutes: List<BusRoute> = listOf(),
    val bikeStations: List<BikeStation> = listOf(),
    val busRoutesError: Boolean = false,
    val bikeStationsError: Boolean = false
) : Action

data class ResetBusRoutesFavoritesAction(val unit: Unit = Unit) : Action

// Train station activity
data class TrainStationAction(
    val trainStationId: BigInteger = BigInteger.ZERO,
    val trainArrival: TrainArrival = TrainArrival(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bus stop activity
data class BusStopArrivalsAction(
    // input
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: BigInteger = BigInteger.ZERO,
    val bound: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    // output
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

// Bike station
data class BikeStationAction(
    val bikeStations: List<BikeStation> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

data class AlertAction(
    val routesAlertsDTO: List<RoutesAlertsDTO> = listOf(),
    val error: Boolean = false,
    val errorMessage: Int = R.string.message_something_went_wrong
) : Action

data class AddTrainFavoriteAction(
    val id: BigInteger = BigInteger.ZERO,
    val trainFavorites: List<BigInteger> = listOf()
) : Action

data class RemoveTrainFavoriteAction(
    val id: BigInteger = BigInteger.ZERO,
    val trainFavorites: List<BigInteger> = listOf()
) : Action

data class ResetTrainFavoriteAction(val unit: Unit = Unit) : Action

data class AddBusFavoriteAction(
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    val busRouteName: String = StringUtils.EMPTY,
    val busStopName: String = StringUtils.EMPTY,
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf()
) : Action

data class RemoveBusFavoriteAction(
    val busRouteId: String = StringUtils.EMPTY,
    val busStopId: String = StringUtils.EMPTY,
    val boundTitle: String = StringUtils.EMPTY,
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf()
) : Action

data class AddBikeFavoriteAction(
    val id: BigInteger = BigInteger.ZERO,
    val stationName: String = StringUtils.EMPTY,
    val bikeFavorites: List<BigInteger> = listOf()
) : Action

data class RemoveBikeFavoriteAction(
    val id: BigInteger = BigInteger.ZERO,
    val bikeFavorites: List<BigInteger> = listOf()
) : Action
