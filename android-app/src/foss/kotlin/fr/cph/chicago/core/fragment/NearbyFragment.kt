/**
 * Copyright 2021 Carl-Philipp Harmant
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
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.Constants.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import fr.cph.chicago.core.listener.OnMarkerClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Station
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.core.utils.setupMapbox
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.Util
import fr.cph.chicago.util.merge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.foss.fragment_nearby_mapbox.loadingLayoutContainer
import kotlinx.android.synthetic.foss.fragment_nearby_mapbox.mapView
import kotlinx.android.synthetic.foss.fragment_nearby_mapbox.progressBar
import kotlinx.android.synthetic.foss.fragment_nearby_mapbox.searchAreaButton
import kotlinx.android.synthetic.foss.fragment_nearby_mapbox.slidingLayout
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.UUID

/**
 * Foss Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
//FIXME: to remove deprecated API, need https://github.com/mapbox/mapbox-plugins-android/issues/649
class NearbyFragment : Fragment(R.layout.fragment_nearby_mapbox), OnMapReadyCallback {

    companion object {
        private val util = Util
        private val mapUtil = MapUtil
        private val trainService = TrainService
        private val busService = BusService

        fun newInstance(sectionNumber: Int): NearbyFragment {
            return fragmentWithBundle(NearbyFragment(), sectionNumber) as NearbyFragment
        }
    }

    lateinit var loadingLayout: LinearLayout
    lateinit var slidingLayoutPanel: SlidingUpPanelLayout
    lateinit var slidingUpAdapter: SlidingUpAdapter
    private lateinit var markerDataHolder: MarkerDataHolder
    private lateinit var map: MapboxMap

    private var locationEngine: LocationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this.context!!, getString(R.string.mapbox_token))
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadingLayout = loadingLayoutContainer
        slidingLayoutPanel = slidingLayout
        slidingUpAdapter = SlidingUpAdapter(this)
        mapView?.getMapAsync(this)
        markerDataHolder = MarkerDataHolder()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = setupMapbox(mapboxMap, resources.configuration)
        map.addOnCameraMoveListener { searchAreaButton.visibility = View.VISIBLE }
        map.setOnMarkerClickListener(OnMarkerClickListener(markerDataHolder, this@NearbyFragment))

        searchAreaButton.setOnClickListener { view ->
            view.visibility = View.INVISIBLE
            markerDataHolder.clear()
            this.map.removeAnnotations()
            loadNearbyDataAroundPosition(
                position = Position(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude),
                zoomIn = false
            )
        }
        startGpsTask()
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private fun startGpsTask() {
        if (EasyPermissions.hasPermissions(context!!, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            val currentPosition = getCurrentPosition()
            loadNearbyDataAroundPosition(position = currentPosition, zoomIn = true)
        } else {
            EasyPermissions.requestPermissions(this, "To access that feature, we need to access your current location", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        }
    }

    @SuppressLint("CheckResult")
    private fun loadNearbyDataAroundPosition(position: Position, zoomIn: Boolean = false) {
        map.cameraPosition = CameraPosition.Builder()
            .zoom(if (zoomIn) 15.0 else map.cameraPosition.zoom)
            .target(LatLng(position.latitude, position.longitude))
            .build()

        val trainStations = trainService.readNearbyStation(position)
        val busStops = busService.busStopsAround(position)
        val bikeStations = mapUtil.readNearbyStation(position, store.state.bikeStations)
        Single.zip(
            trainStations.observeOn(Schedulers.computation()),
            busStops.observeOn(Schedulers.computation()),
            bikeStations.observeOn(Schedulers.computation()),
            { trains: List<Station>, buses, bikes -> merge(trains, buses, bikes) })
            .toObservable()
            .switchMap { stations -> generateMarkers(stations) }
            .observeOn(AndroidSchedulers.mainThread())
            .map { pair ->
                val markerOptions = pair.first
                val station = pair.second
                val marker: Marker = map.addMarker(markerOptions)
                markerDataHolder.addData(marker, station)
                station
            }
            .toList()
            .subscribe(
                {
                    showProgress(false)
                },
                { throwable ->
                    showProgress(false)
                    Timber.e(throwable)
                    displayErrorMessage()
                }
            )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentPosition(): Position {
        if (locationEngine == null) {
            locationEngine = LocationEngineProvider.getBestLocationEngine(this.context!!)
        }
        var lastPosition = chicagoPosition
        locationEngine!!.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult) {
                if (result.lastLocation != null) {
                    lastPosition = Position(result.lastLocation!!.latitude, result.lastLocation!!.longitude)
                    val locationComponent = map.locationComponent

                    val options = LocationComponentOptions.builder(this@NearbyFragment.context!!)
                        .trackingGesturesManagement(true)
                        .accuracyColor(ContextCompat.getColor(this@NearbyFragment.context!!, R.color.green))
                        .build()

                    map.getStyle { style ->
                        // Activate the component
                        locationComponent.activateLocationComponent(this@NearbyFragment.context!!, style)

                        // Apply the options to the LocationComponent
                        locationComponent.applyStyle(options)

                        // Enable to make component visible
                        locationComponent.isLocationComponentEnabled = true

                        // Set the component's camera mode
                        locationComponent.cameraMode = CameraMode.TRACKING
                        locationComponent.renderMode = RenderMode.COMPASS
                    }
                } else {
                    displayErrorMessage()
                }
            }

            override fun onFailure(exception: Exception) {
                // No need to log
                displayErrorMessage()
            }
        })
        return lastPosition
    }

    private fun generateMarkers(stations: List<Station>): Observable<Pair<MarkerOptions, Station>> {
        val bitmapBus = createStop(context!!, R.drawable.bus_stop_icon)
        val bitmapTrain = createStop(context!!, R.drawable.train_station_icon)
        val bitmapBike = createStop(context!!, R.drawable.bike_station_icon)

        return Single.fromCallable { stations }
            .flatMapObservable { preferences -> Observable.fromIterable(preferences) }
            .map { station ->
                val id = UUID.randomUUID().toString()
                val options = MarkerOptions()
                    .title(station.name)
                when (station) {
                    is TrainStation -> {
                        val position = station.stopsPosition.first()
                        options
                            .position(LatLng(position.latitude, position.longitude))
                            .icon(IconFactory.recreate(id, bitmapTrain))
                    }
                    is BusStop -> {
                        options
                            .position(LatLng(station.position.latitude, station.position.longitude))
                            .snippet(station.description)
                            .icon(IconFactory.recreate(id, bitmapBus))
                    }
                    is BikeStation -> {
                        options
                            .position(LatLng(station.latitude, station.longitude))
                            .icon(IconFactory.recreate(id, bitmapBike))
                    }
                }
                Pair(options, station)
            }
            .filter { pair -> pair.first.icon != null }
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

    private fun displayErrorMessage() {
        util.showSnackBar(loadingLayoutContainer, R.string.message_cant_find_location)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        slidingLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    private fun createStop(context: Context, @DrawableRes icon: Int): Bitmap {
        val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
        val bitMapStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitMapStation)
        val shape = ContextCompat.getDrawable(context, icon)!!
        shape.setBounds(0, 0, px, bitMapStation.height)
        shape.draw(canvas)
        return bitMapStation
    }
}
