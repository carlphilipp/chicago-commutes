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
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util
import java.util.TreeMap

class LocationViewModel : ViewModel() {
    var requestPermission: Boolean = true
}

class NearbyResult(
    val lastUpdate: LastUpdate = LastUpdate(App.instance.getString(R.string.time_now)),
    val arrivals: TreeMap<NearbyDetailsArrivals, MutableList<String>> = TreeMap<NearbyDetailsArrivals, MutableList<String>>()
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
