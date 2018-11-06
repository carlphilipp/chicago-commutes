package fr.cph.chicago.core.listener

import android.util.Log
import com.mapbox.mapboxsdk.annotations.Marker
import fr.cph.chicago.core.fragment.NearbyFragment
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Station
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusArrivalRouteDTO
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.rx.ObservableUtil

class OnMarkerClickListener(private val markerDataHolder: MarkerDataHolder, private val nearbyFragment: NearbyFragment) : com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener {

    override fun onMarkerClick(marker: Marker): Boolean {
        nearbyFragment.showProgress(true)
        val station = markerDataHolder.getStation(marker)
        if (nearbyFragment.layoutContainer.childCount != 0) {
            nearbyFragment.layoutContainer.removeViewAt(0)
        }
        if (station != null) {
            loadArrivals(station)
        }
        return false
    }


    private fun loadArrivals(station: Station) {
        when (station) {
            is TrainStation -> loadTrainArrivals(station)
            is BusStop -> loadBusArrivals(station)
            is BikeStation -> loadBikes(station)
        }
    }

    private fun loadTrainArrivals(trainTrainStation: TrainStation) {
        nearbyFragment.slidingUpAdapter.updateTitleTrain(trainTrainStation.name)
        observableUtil.createTrainArrivalsObservable(trainTrainStation)
            .subscribe(
                { nearbyFragment.slidingUpAdapter.addTrainStation(it) },
                { error -> Log.e(TAG, error.message, error) }
            )
    }

    private fun loadBusArrivals(busStop: BusStop) {
        nearbyFragment.slidingUpAdapter.updateTitleBus(busStop.name)
        observableUtil.createBusArrivalsObservable(busStop)
            .subscribe(
                { result ->
                    val busArrivalRouteDTO = BusArrivalRouteDTO(BusArrivalRouteDTO.busComparator)
                    result.forEach { busArrivalRouteDTO.addBusArrival(it) }
                    nearbyFragment.slidingUpAdapter.addBusArrival(busArrivalRouteDTO)
                },
                { error -> Log.e(TAG, error.message, error) }
            )
    }

    private fun loadBikes(divvyStation: BikeStation) {
        nearbyFragment.slidingUpAdapter.updateTitleBike(divvyStation.name)
        observableUtil.createBikeStationsObservable(divvyStation)
            .subscribe(
                { nearbyFragment.slidingUpAdapter.addBike(it) },
                { error -> Log.e(TAG, error.message, error) }
            )
    }

    companion object {
        private val TAG = OnMarkerClickListener::class.java.simpleName
        private val observableUtil = ObservableUtil
    }
}
