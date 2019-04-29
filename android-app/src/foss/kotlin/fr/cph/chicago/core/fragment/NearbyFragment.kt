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
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import butterknife.BindString
import butterknife.BindView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.Constants.Companion.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import fr.cph.chicago.core.listener.OnMarkerClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.rx.RxUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.functions.Function3
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.UUID

/**
 * Foss Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class NearbyFragment : Fragment(R.layout.fragment_nearby_mapbox), OnMapReadyCallback, LocationEngineListener, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.activity_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.sliding_layout)
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    @BindView(R.id.loading_layout_container)
    lateinit var layoutContainer: LinearLayout
    @BindView(R.id.search_area)
    lateinit var searchAreaButton: Button
    @BindView(R.id.mapView)
    @JvmField
    var mapView: MapView? = null
    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String

    lateinit var slidingUpAdapter: SlidingUpAdapter
    private lateinit var markerDataHolder: MarkerDataHolder

    private lateinit var map: MapboxMap
    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null
    private var locationOrigin: Location? = null

    private val util: Util = Util
    private val rxUtil: RxUtil = RxUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this.context!!, getString(R.string.mapbox_token))
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        slidingUpAdapter = SlidingUpAdapter(this)
        mapView?.getMapAsync(this)
        markerDataHolder = MarkerDataHolder()
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
        handleNearbyData(Position(0.0, 0.0))
    }

    @SuppressLint("MissingPermission")
    private fun startLoadingNearby() {
        if (util.isNetworkAvailable()) {
            showProgress(true)
            initLocationEngine()
            initLocationLayer()
            val position = if (locationOrigin == null) Position() else Position(locationOrigin!!.latitude, locationOrigin!!.longitude)
            handleNearbyData(position)
        } else {
            util.showNetworkErrorMessage(mainActivity.drawerLayout)
            showProgress(false)
        }
    }

    private fun handleNearbyData(position: Position) {
        val bikeStations = mainActivity.intent.extras?.getParcelableArrayList(bundleBikeStations)
            ?: listOf<BikeStation>()
        var chicago: Position? = null
        if (position.longitude == 0.0 && position.latitude == 0.0) {
            chicago = Position(chicagoPosition.latitude, chicagoPosition.longitude)
            util.showSnackBar(mainActivity.drawer, R.string.message_cant_find_location)
        }

        val finalPosition = chicago ?: position
        val trainStationAroundObservable = rxUtil.trainStationAround(finalPosition)
        val busStopsAroundObservable = rxUtil.busStopsAround(finalPosition)
        val bikeStationsObservable = rxUtil.bikeStationAround(finalPosition, bikeStations)
        Single.zip(trainStationAroundObservable, busStopsAroundObservable, bikeStationsObservable, Function3 { trains: List<TrainStation>, buses: List<BusStop>, divvies: List<BikeStation> ->
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(finalPosition.latitude, finalPosition.longitude))
                .zoom(15.0)
                .build()
            updateMarkersAndModel(buses, trains, divvies)
            Any()
        }).subscribe()
    }

    override fun onConnected() {
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.map = with(mapboxMap) {
            uiSettings.isLogoEnabled = false
            uiSettings.isAttributionEnabled = false
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isTiltGesturesEnabled = false
            addOnCameraMoveListener {
                searchAreaButton.visibility = View.VISIBLE
            }
            this
        }
        searchAreaButton.setOnClickListener { view ->
            view.visibility = View.INVISIBLE
            val target = this.map.cameraPosition.target
            val position = Position(target.latitude, target.longitude)
            markerDataHolder.clear()
            this.map.removeAnnotations()
            handleNearbyData(position)
        }
        loadNearbyIfAllowed()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            locationOrigin = location
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        if (locationEngine == null) {
            locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        }
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            locationOrigin = lastLocation
        } else {
            locationEngine!!.addLocationEngineListener(this)
        }
    }

    private fun initLocationLayer() {
        if (locationLayerPlugin == null) {
            locationLayerPlugin = LocationLayerPlugin(mapView!!, map, locationEngine)
        }
        locationLayerPlugin?.isLocationLayerEnabled = true
        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL
        lifecycle.addObserver(locationLayerPlugin!!)
    }

    private fun updateMarkersAndModel(
        busStops: List<BusStop>,
        trainStations: List<TrainStation>,
        bikeStations: List<BikeStation>) {

        val bitmapBus = createStop(context, R.drawable.bus_stop_icon)
        val bitmapTrain = createStop(context, R.drawable.train_station_icon)
        val bitmapBike = createStop(context, R.drawable.bike_station_icon)

        busStops.forEach { busStop ->
            val options = MarkerOptions()
                .position(LatLng(busStop.position.latitude, busStop.position.longitude))
                .title(busStop.name)
                .snippet(busStop.description)
            if (bitmapBus != null) {
                options.icon = IconFactory.recreate(UUID.randomUUID().toString(), bitmapBus)
            }
            val marker = map.addMarker(options)
            markerDataHolder.addData(marker, busStop)

        }
        trainStations.forEach { station ->
            val position = station.stopsPosition.firstOrNull()
            if (position != null) {
                val options = MarkerOptions()
                    .position(LatLng(position.latitude, position.longitude))
                    .title(station.name)
                if (bitmapTrain != null) {
                    options.icon = IconFactory.recreate(UUID.randomUUID().toString(), bitmapTrain)
                }
                val marker = map.addMarker(options)
                markerDataHolder.addData(marker, station)
            }
        }
        bikeStations.forEach { station ->
            val options = MarkerOptions()
                .position(LatLng(station.latitude, station.longitude))
                .title(station.name)
            if (bitmapBike != null) {
                options.icon = IconFactory.recreate(UUID.randomUUID().toString(), bitmapBike)
            }
            val marker = map.addMarker(options)
            markerDataHolder.addData(marker, station)
        }
        map.setOnMarkerClickListener(OnMarkerClickListener(markerDataHolder, this@NearbyFragment))
        showProgress(false)
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

    override fun onStart() {
        super.onStart()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine?.deactivate()
        mapView?.onDestroy()
    }

    private fun createStop(context: Context?, @DrawableRes icon: Int): Bitmap? {
        return if (context != null) {
            val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
            val bitMapStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitMapStation)
            val shape = ContextCompat.getDrawable(context, icon)!!
            shape.setBounds(0, 0, px, bitMapStation.height)
            shape.draw(canvas)
            bitMapStation
        } else {
            null
        }
    }

    companion object {
        fun newInstance(sectionNumber: Int): NearbyFragment {
            return fragmentWithBundle(NearbyFragment(), sectionNumber) as NearbyFragment
        }
    }
}
