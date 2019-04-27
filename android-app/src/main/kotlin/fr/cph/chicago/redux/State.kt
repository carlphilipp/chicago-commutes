package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.StateType
import java.util.Date

data class AppState(
    val lastAction: Date = Date(), // Field to ensure the update of the state
    val lastUpdate: Date = Date(), // Field displayed in favorites
    val highlightBackground: Boolean = false,
    val error: Boolean? = null,
    val throwable: Throwable? = null,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),

    val busRoutesError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),

    val bikeStationsError: Boolean = false,
    val bikeStations: List<BikeStation> = listOf(),

    // Train Station activity state
    val trainStationError: Boolean = false,
    val trainStationErrorMessage: Int = R.string.message_something_went_wrong,
    val trainStationArrival: TrainArrival = TrainArrival.buildEmptyTrainArrival()
) : StateType
