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

package fr.cph.chicago.core.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import butterknife.BindString
import butterknife.BindView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.Constants.Companion.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import fr.cph.chicago.core.listener.OnMarkerClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.functions.Function3
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class NearbyFragment : Fragment(R.layout.fragment_nearby), EasyPermissions.PermissionCallbacks {

    @BindView(R.id.activity_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.sliding_layout)
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    @BindView(R.id.loading_layout_container)
    lateinit var layoutContainer: LinearLayout
    @BindView(R.id.search_area)
    lateinit var searchAreaButton: Button

    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String

    private val util: Util = Util
    private val googleMapUtil = GoogleMapUtil
    private val observableUtil: ObservableUtil = ObservableUtil

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleApiClient: GoogleApiClient
    lateinit var slidingUpAdapter: SlidingUpAdapter

    private lateinit var markerDataHolder: MarkerDataHolder
    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkTrainData(mainActivity)
        App.checkBusData(mainActivity)
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        slidingUpAdapter = SlidingUpAdapter(this)
        markerDataHolder = MarkerDataHolder()
        searchAreaButton.setOnClickListener { view ->
            view.visibility = View.INVISIBLE
            mapFragment.getMapAsync { googleMap ->
                googleMap.clear()
                markerDataHolder.clear()
                val target = googleMap.cameraPosition.target
                handleNearbyData(Position(target.latitude, target.longitude))
            }
        }
        showProgress(true)
    }

    override fun onStart() {
        super.onStart()
        googleApiClient = GoogleApiClient.Builder(mainActivity)
            .addApi(LocationServices.API)
            .build()
        val options = GoogleMapOptions()
        val camera = CameraPosition(GoogleMapUtil.chicago, 7f, 0f, 0f)
        options.camera(camera)
        mapFragment = SupportMapFragment.newInstance(options)
        mapFragment.retainInstance = true
        val fm = mainActivity.supportFragmentManager
        loadNearbyIfAllowed()
        fm.beginTransaction().replace(R.id.map, mapFragment).commit()
    }

    override fun onResume() {
        super.onResume()
        slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    private fun updateMarkersAndModel(
        busStops: List<BusStop>,
        trainTrainStation: List<TrainStation>,
        divvyStations: List<BikeStation>) {
        if (isAdded) {
            mapFragment.getMapAsync { googleMap ->
                with(googleMap) {
                    uiSettings.isMyLocationButtonEnabled = true
                    uiSettings.isZoomControlsEnabled = false
                    uiSettings.isMapToolbarEnabled = false
                }

                val bitmapDescriptorBus = createStop(context, R.drawable.bus_stop_icon)
                val bitmapDescriptorTrain = createStop(context, R.drawable.train_station_icon)
                val bitmapDescriptorBike = createStop(context, R.drawable.bike_station_icon)

                busStops
                    .forEach { busStop ->
                        val point = LatLng(busStop.position.latitude, busStop.position.longitude)
                        val markerOptions = MarkerOptions()
                            .position(point)
                            .title(busStop.name)
                            .icon(bitmapDescriptorBus)
                        val marker = googleMap.addMarker(markerOptions)
                        markerDataHolder.addData(marker, busStop)
                    }

                trainTrainStation
                    .forEach { station ->
                        val position = station.stopsPosition.firstOrNull()
                        if (position != null) {
                            val markerOptions = MarkerOptions()
                                .position(LatLng(position.latitude, position.longitude))
                                .title(station.name)
                                .icon(bitmapDescriptorTrain)
                            val marker = googleMap.addMarker(markerOptions)
                            markerDataHolder.addData(marker, station)
                        }
                    }

                divvyStations
                    .forEach { station ->
                        val markerOptions = MarkerOptions()
                            .position(LatLng(station.latitude, station.longitude))
                            .title(station.name)
                            .icon(bitmapDescriptorBike)
                        val marker = googleMap.addMarker(markerOptions)
                        markerDataHolder.addData(marker, station)
                    }

                showProgress(false)
                googleMap.setOnMarkerClickListener(OnMarkerClickListener(markerDataHolder, this@NearbyFragment))
                googleMap.setOnCameraMoveListener { searchAreaButton.visibility = View.VISIBLE }
            }
        }
    }

    private fun createStop(context: Context?, @DrawableRes icon: Int): BitmapDescriptor {
        return if (context != null) {
            val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
            val bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitMapBusStation)
            val shape = ContextCompat.getDrawable(context, icon)!!
            shape.setBounds(0, 0, px, bitMapBusStation.height)
            shape.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitMapBusStation)
        } else {
            BitmapDescriptorFactory.defaultMarker()
        }
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
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity)
            fusedLocationClient!!.lastLocation.addOnSuccessListener { location ->
                val position = if (location == null) Position() else Position(location.latitude, location.longitude)
                handleNearbyData(position)
            }
        } else {
            util.showNetworkErrorMessage(mainActivity.drawerLayout)
            showProgress(false)
        }
    }

    private fun handleNearbyData(position: Position) {
        val bikeStations = mainActivity.intent.extras?.getParcelableArrayList(bundleBikeStations)
            ?: listOf<BikeStation>()
        var finalPosition = position
        if (position.longitude == 0.0 && position.latitude == 0.0) {
            Log.w(TAG, "Could not get current user location")
            util.showSnackBar(mainActivity.drawer, R.string.message_cant_find_location)
            finalPosition = chicagoPosition
        }

        val trainStationAroundObservable = observableUtil.createTrainStationAroundObs(finalPosition)
        val busStopsAroundObservable = observableUtil.createBusStopsAroundObs(finalPosition)
        val bikeStationsObservable = observableUtil.createBikeStationAroundObs(finalPosition, bikeStations)
        Observable.zip(trainStationAroundObservable, busStopsAroundObservable, bikeStationsObservable, Function3 { trains: List<TrainStation>, buses: List<BusStop>, divvies: List<BikeStation> ->
            googleMapUtil.centerMap(mapFragment, finalPosition)
            updateMarkersAndModel(buses, trains, divvies)
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
