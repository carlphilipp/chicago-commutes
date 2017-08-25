package fr.cph.chicago.core.listener

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.core.fragment.NearbyFragment
import fr.cph.chicago.entity.AStation
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusStop
import fr.cph.chicago.entity.Station
import fr.cph.chicago.entity.dto.BusArrivalRouteDTO
import fr.cph.chicago.marker.MarkerDataHolder
import fr.cph.chicago.rx.ObservableUtil

class OnMarkerClickListener(private val markerDataHolder: MarkerDataHolder, private val nearbyFragment: NearbyFragment) : GoogleMap.OnMarkerClickListener {

    override fun onMarkerClick(marker: Marker): Boolean {
        nearbyFragment.showProgress(true)
        val station = markerDataHolder.getStation(marker)
        if (nearbyFragment.layoutContainer.childCount != 0) {
            nearbyFragment.layoutContainer.removeViewAt(0)
        }
        //FIXME handle the case a null is returned
        loadArrivals(station!!)
        return false
    }


    private fun loadArrivals(station: AStation) {
        when (station) {
            is Station -> loadTrainArrivals(station)
            is BusStop -> loadBusArrivals(station)
            is BikeStation -> loadBikes(station)
        }
    }

    private fun loadTrainArrivals(trainStation: Station) {
        nearbyFragment.slidingUpAdapter.updateTitleTrain(trainStation.name)
        observableUtil.createTrainArrivalsObservable(nearbyFragment.context, trainStation)
            .subscribe({ nearbyFragment.slidingUpAdapter.addTrainStation(it) })
            { onError -> Log.e(TAG, onError.message, onError) }
    }

    private fun loadBusArrivals(busStop: BusStop) {
        nearbyFragment.slidingUpAdapter.updateTitleBus(busStop.name)
        observableUtil.createBusArrivalsObservable(nearbyFragment.context, busStop)
            .subscribe(
                { result ->
                    val busArrivalRouteDTO = BusArrivalRouteDTO(BusArrivalRouteDTO.busComparator)
                    result.forEach({ busArrivalRouteDTO.addBusArrival(it) })
                    nearbyFragment.slidingUpAdapter.addBusArrival(busArrivalRouteDTO)
                }
            ) { onError -> Log.e(TAG, onError.message, onError) }
    }

    private fun loadBikes(bikeStation: BikeStation) {
        nearbyFragment.slidingUpAdapter.updateTitleBike(bikeStation.name)
        observableUtil.createBikeStationsObservable(bikeStation)
            .subscribe({ nearbyFragment.slidingUpAdapter.addBike(it) })
            { onError -> Log.e(TAG, onError.message, onError) }
    }

    companion object {
        private val TAG = OnMarkerClickListener::class.java.simpleName
        private val observableUtil = ObservableUtil
    }
}
