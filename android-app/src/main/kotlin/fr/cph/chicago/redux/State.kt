package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.StateType
import java.util.Date

data class AppState(
    val lastStateChange: Date = Date(), // Field to ensure the update of the state
    val status: Status = Status.UNKNOWN,
    val lastFavoritesUpdate: Date = Date(), // Field displayed in favorites
    val forceRefreshFavorites: Boolean = false,

    // Trains and Buses arrivals
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
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
    val busStopError: Boolean = false,
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
    SUCCESS_HIGHLIGHT,
    FAILURE,
    FULL_FAILURE
}
