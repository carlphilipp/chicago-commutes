/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import butterknife.BindString
import butterknife.BindView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.Constants.Companion.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.functions.Function3
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Foss Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class NearbyFragment : Fragment(R.layout.fragment_nearby_mapbox), EasyPermissions.PermissionCallbacks {

    @BindView(R.id.activity_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.sliding_layout)
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    @BindView(R.id.loading_layout_container)
    lateinit var layoutContainer: LinearLayout
    @BindView(R.id.search_area)
    lateinit var searchAreaButton: Button
    @BindView(R.id.mapView)
    lateinit var mapView: MapView
    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String

    lateinit var slidingUpAdapter: SlidingUpAdapter
    lateinit var locationEngine: LocationEngine

    private val util: Util = Util
    private val observableUtil: ObservableUtil = ObservableUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiY2FybHBoaWxpcHAiLCJhIjoiY2puMmRlZXFsMGlxYjNxbzhsc2Rjb2JvayJ9.hTDfu1EZG4Qgy8_9P_eDdw")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView() {
        slidingUpAdapter = SlidingUpAdapter(this)
        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        locationEngine.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine.activate()
        //val lastLocation = locationEngine.getLastLocation()
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.cameraPosition = CameraPosition.Builder()
                .target(LatLng(MapUtil.chicagoPosition.latitude, MapUtil.chicagoPosition.longitude))
                .zoom(10.0)
                .build()
            mapboxMap.addOnCameraMoveListener {
                searchAreaButton.visibility = View.VISIBLE
                searchAreaButton.setOnClickListener { view ->
                    view.visibility = View.INVISIBLE
                    /*googleMap.clear()
                    markerDataHolder.clear()
                    val target = googleMap.cameraPosition.target
                    handleNearbyData(Position(target.latitude, target.longitude))*/
                }
            }
            showProgress(false)
        }
    }

    override fun onStart() {
        super.onStart()
        loadNearbyIfAllowed()
    }

    fun showProgress(show: Boolean) {
        if (isAdded) {
            if (show) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 50
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private fun loadNearbyIfAllowed() {
        if (EasyPermissions.hasPermissions(context!!, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            startLoadingNearby()
        } else {
            EasyPermissions.requestPermissions(this, "To access that feature, we need to access your current location", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startLoadingNearby()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        showProgress(false)
    }

    @SuppressLint("MissingPermission")
    private fun startLoadingNearby() {
        if (util.isNetworkAvailable()) {
            showProgress(true)
            // TODO get current location
            val lastLocation = locationEngine.lastLocation

            val position = Position(lastLocation.latitude, lastLocation.longitude)
            handleNearbyData(position)
        } else {
            util.showNetworkErrorMessage(mainActivity)
            showProgress(false)
        }
    }

    private fun handleNearbyData(position: Position) {
        val bikeStations = mainActivity.intent.extras?.getParcelableArrayList(bundleBikeStations)
            ?: listOf<BikeStation>()
        if (position.longitude == 0.0 && position.latitude == 0.0) {
            Log.w(TAG, "Could not get current user location")
            util.showSnackBar(mainActivity, R.string.message_cant_find_location, Snackbar.LENGTH_LONG)
        }

        val finalPosition = chicagoPosition
        val trainStationAroundObservable = observableUtil.createTrainStationAroundObservable(finalPosition)
        val busStopsAroundObservable = observableUtil.createBusStopsAroundObservable(finalPosition)
        val bikeStationsObservable = observableUtil.createBikeStationAroundObservable(finalPosition, bikeStations)
        Observable.zip(trainStationAroundObservable, busStopsAroundObservable, bikeStationsObservable, Function3 { trains: List<TrainStation>, buses: List<BusStop>, divvies: List<BikeStation> ->
            //googleMapUtil.centerMap(mapFragment, finalPosition)
            //updateMarkersAndModel(buses, trains, divvies)
            mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(MapUtil.chicagoPosition.latitude, MapUtil.chicagoPosition.longitude))
                    .zoom(15.0)
                    .build()
            }
            Any()
        }).subscribe()
    }

    companion object {

        private val TAG = NearbyFragment::class.java.simpleName

        fun newInstance(sectionNumber: Int): NearbyFragment {
            return Fragment.fragmentWithBundle(NearbyFragment(), sectionNumber) as NearbyFragment
        }
    }
}
