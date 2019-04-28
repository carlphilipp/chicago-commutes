package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.Action
import org.rekotlin.ReKotlinInit
import org.rekotlin.StateType
import java.util.*

data class AppState(
    val lastStateChange: Date = Date(), // Field to ensure the update of the state
    val lastAction: Action = ReKotlinInit(), // Not sure this is a smart idea.
    val lastFavoritesUpdate: Date = Date(), // Field displayed in favorites
    val error: Boolean? = null,

    // Trains and Buses arrivals
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),

    // Bus routes
    val busRoutesError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),

    // Bikes
    val bikeStationsError: Boolean = false,
    val bikeStationsErrorMessage: Int = R.string.message_something_went_wrong,
    val bikeStations: List<BikeStation> = listOf(),

    // Train Station activity state
    val trainStationError: Boolean = false,
    val trainStationErrorMessage: Int = R.string.message_something_went_wrong,
    val trainStationArrival: TrainArrival = TrainArrival.buildEmptyTrainArrival(),

    // Bus stop activity state
    val busStopError: Boolean = false,
    val busStopErrorMessage: Int = R.string.message_something_went_wrong,
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO()
) : StateType
