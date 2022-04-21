package fr.cph.chicago.core.ui.common

import androidx.lifecycle.ViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.dto.BusArrivalRouteDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util
import java.util.TreeMap

class LocationViewModel : ViewModel() {
    var requestPermission: Boolean = true
}

class Arrival(
    val unit: String = "min",
    val destination: String,
    val value: String,
    val trainLine: TrainLine,
    val direction: TrainDirection,
)

// FIXME: To delete?
class NearbyResult(
    val lastUpdate: LastUpdate = LastUpdate(App.instance.getString(R.string.time_now)),
    val arrivals: TreeMap<NearbyDetailsArrivals, MutableList<String>> = TreeMap<NearbyDetailsArrivals, MutableList<String>>(),
    val arrivalsNew : List<Arrival> = listOf(),
) {
    companion object {
        @JvmName("toArrivalsTrain")
        fun toArrivals(trainEtas: List<TrainEta>): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            return trainEtas.fold(TreeMap<NearbyDetailsArrivals, MutableList<String>>()) { acc, cur ->
                val key = NearbyDetailsArrivals(cur.destName, cur.routeName, cur.stop.direction.toString())
                if (acc.containsKey(key)) {
                    acc[key]!!.add(cur.timeLeftDueDelay)
                } else {
                    acc[key] = mutableListOf(cur.timeLeftDueDelay)
                }
                acc
            }
        }

        @JvmName("toArrivalsTrainNewNearby")
        fun toArrivalsNewNearby(trainEtas: List<TrainEta>): List<Arrival> {
            return trainEtas
                .map { trainEta ->
                    val eta = if (trainEta.timeLeftDueDelay.contains(" min")) trainEta.timeLeftDueDelay.split(" min")[0] else trainEta.timeLeftDueDelay
                    Arrival(
                        unit = "min",
                        destination = trainEta.destName,
                        value = eta,
                        trainLine = trainEta.routeName,
                        direction = trainEta.stop.direction
                    )
                }
        }

        @JvmName("toArrivalsBus")
        fun toArrivals(busArrivals: List<BusArrival>): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            val busArrivalRouteDTO = BusArrivalRouteDTO(BusArrivalRouteDTO.busComparator)
            busArrivals.forEach { busArrivalRouteDTO.addBusArrival(it) }

            val result = TreeMap<NearbyDetailsArrivals, MutableList<String>>()

            busArrivalRouteDTO.forEach { entry ->
                val route = Util.trimBusStopNameIfNeeded(entry.key)
                entry.value.forEach { entryBound ->
                    val bound = entryBound.key
                    val arrivals = entryBound.value

                    val nearbyDetailsArrivals = NearbyDetailsArrivals(
                        destination = route,
                        trainLine = TrainLine.NA,
                        direction = BusDirection.fromString(bound).shortLowerCase,
                    )
                    result[nearbyDetailsArrivals] = arrivals.map { busArrival -> busArrival.timeLeftDueDelay }.toMutableList()
                }
            }
            return result
        }

        @JvmName("toArrivalsBike")
        fun toArrivals(bikeStation: BikeStation): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            val result = TreeMap<NearbyDetailsArrivals, MutableList<String>>()
            result[NearbyDetailsArrivals(
                destination = App.instance.getString(R.string.bike_available_bikes),
                trainLine = TrainLine.NA,
            )] = mutableListOf(bikeStation.availableBikes.toString())
            result[NearbyDetailsArrivals(
                destination = App.instance.getString(R.string.bike_available_docks),
                trainLine = TrainLine.NA
            )] = mutableListOf(bikeStation.availableDocks.toString())

            return result
        }

        @JvmName("toArrivalsBikeNewNearby")
        fun toArrivalsNewNearby(bikeStation: BikeStation): List<Arrival> {
            val availableBikes = Arrival(
                unit = "bikes",
                destination = "available",
                value = bikeStation.availableBikes.toString(),
                trainLine =  TrainLine.NA,
                direction = TrainDirection.UNKNOWN,
            )
            val availableDocks = Arrival(
                unit = "docks",
                destination = "available",
                value = bikeStation.availableDocks.toString(),
                trainLine =  TrainLine.NA,
                direction = TrainDirection.UNKNOWN,
            )

            return listOf(availableBikes, availableDocks)
        }
    }
}

data class NearbyDetailsArrivals(
    val destination: String,
    val trainLine: TrainLine,
    val direction: String? = null,
) : Comparable<NearbyDetailsArrivals> {
    override fun compareTo(other: NearbyDetailsArrivals): Int {
        val line = trainLine.toTextString().compareTo(other.trainLine.toTextString())
        return if (line == 0) {
            val station = destination.compareTo(other.destination)
            if (station == 0 && direction != null && other.direction != null) {
                direction.compareTo(other.direction)
            } else {
                station
            }
        } else {
            line
        }
    }
}
