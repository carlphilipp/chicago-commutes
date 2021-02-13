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
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import java.math.BigInteger
import java.util.Date
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StateType

data class State(
    // Can be useful to force an update
    val random: String = StringUtils.EMPTY,
    // Api keys
    val ctaTrainKey: String = StringUtils.EMPTY,
    val ctaBusKey: String = StringUtils.EMPTY,
    val googleStreetKey: String = StringUtils.EMPTY,

    val status: Status = Status.UNKNOWN,
    val lastFavoritesUpdate: Date = Date(), // Field displayed in favorites

    // Favorites
    val trainFavorites: List<BigInteger> = listOf(),
    val busFavorites: List<String> = listOf(),
    val busRouteFavorites: List<String> = listOf(),
    val bikeFavorites: List<BigInteger> = listOf(),

    // Trains and Buses arrivals
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(mutableMapOf(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),

    // Bus routes
    val busRoutesStatus: Status = Status.UNKNOWN,
    val busRoutesErrorMessage: Int = R.string.message_something_went_wrong,
    val busRoutes: List<BusRoute> = listOf(),

    // Bikes
    val bikeStationsStatus: Status = Status.UNKNOWN,
    val bikeStationsErrorMessage: Int = R.string.message_something_went_wrong,
    val bikeStations: List<BikeStation> = listOf(),

    // Train Station activity state
    val trainStationStatus: Status = Status.UNKNOWN,
    val trainStationErrorMessage: Int = R.string.message_something_went_wrong,
    val trainStationArrival: TrainArrival = TrainArrival.buildEmptyTrainArrival(),

    // Bus stop activity state
    val busStopStatus: Status = Status.UNKNOWN,
    val busStopErrorMessage: Int = R.string.message_something_went_wrong,
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(),

    // Alerts
    val alertStatus: Status = Status.UNKNOWN,
    val alertErrorMessage: Int = R.string.message_something_went_wrong,
    val alertsDTO: List<RoutesAlertsDTO> = listOf()
) : StateType

enum class Status {
    UNKNOWN,
    SUCCESS,
    FAILURE,
    FAILURE_NO_SHOW,
    FULL_FAILURE,
    ADD_FAVORITES,
    REMOVE_FAVORITES
}
