package fr.cph.chicago.redux

import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import org.rekotlin.StateType
import java.util.*

data class AppState(
    val derp : Date = Date(), // field to unsure the update of the state
    val lastUpdate: Date = Date(),
    val highlightBackground: Boolean = false,
    val error: Boolean? = null,
    val throwable: Throwable? = null,
    val trainArrivalsDTO: TrainArrivalDTO = TrainArrivalDTO(SparseArray(), false),
    val busArrivalsDTO: BusArrivalDTO = BusArrivalDTO(listOf(), false),

    val busRoutesError: Boolean = false,
    val busRoutes: List<BusRoute> = listOf(),

    val bikeStationsError: Boolean = false,
    val bikeStations: List<BikeStation> = listOf(),

    val trainStationError: Boolean = false,
    val trainStationArrival: TrainArrival = TrainArrival.buildEmptyTrainArrival()
) : StateType
