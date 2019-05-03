/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago.core.listener

import android.annotation.SuppressLint
import com.mapbox.mapboxsdk.annotations.Marker
import fr.cph.chicago.core.fragment.NearbyFragment
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Station
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusArrivalRouteDTO
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.rx.RxUtil

@SuppressLint("CheckResult")
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
        rxUtil.trainArrivals(trainTrainStation.id)
            .onErrorReturn { TrainArrival.buildEmptyTrainArrival() }
            .subscribe { result -> nearbyFragment.slidingUpAdapter.addTrainStation(result) }
    }

    private fun loadBusArrivals(busStop: BusStop) {
        nearbyFragment.slidingUpAdapter.updateTitleBus(busStop.name)
        rxUtil.busArrivals(busStop)
            .subscribe { result ->
                val busArrivalRouteDTO = BusArrivalRouteDTO(BusArrivalRouteDTO.busComparator)
                result.forEach { busArrivalRouteDTO.addBusArrival(it) }
                nearbyFragment.slidingUpAdapter.addBusArrival(busArrivalRouteDTO)
            }
    }

    private fun loadBikes(bikeStation: BikeStation) {
        nearbyFragment.slidingUpAdapter.updateTitleBike(bikeStation.name)
        rxUtil.bikeOneStation(bikeStation)
            .subscribe { result -> nearbyFragment.slidingUpAdapter.addBike(result) }

    }

    companion object {
        private val TAG = OnMarkerClickListener::class.java.simpleName
        private val rxUtil = RxUtil
    }
}
