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

package fr.cph.chicago.core.activity.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.model.marker.RefreshBusMarkers
import fr.cph.chicago.rx.BusFollowObserver
import fr.cph.chicago.rx.BusObserver
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusMapActivity : FragmentMapActivity() {

    companion object {
        private const val ZOOM_ONE_RESULT = 15
        private const val ZOOM_DEFAULT = 11
        private val busService: BusService = BusService
        private val util = Util
    }

    private var busMarkers: List<Marker> = listOf()
    private val busStationMarkers: MutableList<Marker> = mutableListOf()
    private val views: MutableMap<Marker, View> = mutableMapOf()
    private val status: MutableMap<Marker, Boolean> = mutableMapOf()
    private var busId: Int = 0
    private lateinit var busRouteId: String
    private lateinit var bounds: Array<String>
    private lateinit var refreshBusesBitmap: RefreshBusMarkers

    private var loadPattern = true

    override fun create(savedInstanceState: Bundle?) {
        super.create(savedInstanceState)
        if (savedInstanceState != null) {
            busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id))
            busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds)) ?: arrayOf()
        } else {
            busId = intent.getIntExtra(getString(R.string.bundle_bus_id), 0)
            busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            bounds = intent.getStringArrayExtra(getString(R.string.bundle_bus_bounds)) ?: arrayOf()
        }

        // Init data
        initData()

        // Init toolbar
        setToolbar()
    }

    public override fun onStop() {
        super.onStop()
        loadPattern = false
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener {
            busService.busForRouteId(busRouteId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(BusObserver(this@BusMapActivity, false, mapContainerLayout))
            false
        }

        util.setWindowsColor(this, toolbar, TrainLine.NA)
        toolbar.title = busRouteId
    }

    fun centerMapOnBus(result: List<Bus>) {
        val sizeIsOne = result.size == 1
        val position = if (sizeIsOne) result[0].position else MapUtil.getBestPosition(result.map { it.position })
        val zoom = if (sizeIsOne) ZOOM_ONE_RESULT else ZOOM_DEFAULT
        centerMapOn(position.latitude, position.longitude, zoom)
    }

    fun drawBuses(buses: List<Bus>) {
        cleanAllMarkers()
        val bitmapDesc = refreshBusesBitmap.currentDescriptor
        busMarkers = buses.map { bus ->
            val point = LatLng(bus.position.latitude, bus.position.longitude)
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title("To ${bus.destination}")
                    .snippet(bus.id.toString())
                    .icon(bitmapDesc)
                    .anchor(0.5f, 0.5f)
                    .rotation(bus.heading.toFloat())
                    .flat(true))
            marker
        }.onEach { marker ->
            val layoutInflater = this@BusMapActivity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.marker, mapContainerLayout, false)
            val title = view.findViewById<TextView>(R.id.title)
            title.text = marker.title
            views[marker] = view
        }
    }

    private fun cleanAllMarkers() {
        busMarkers.forEach { it.remove() }
    }

    private fun drawPattern(patterns: List<BusPattern>) {
        val index = intArrayOf(0)
        val red = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.red_marker_no_shade))
        val blue = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.blue_marker_no_shade))
        patterns.forEach { pattern ->
            val poly = PolylineOptions()
                .color(if (index[0] == 0) Color.RED else if (index[0] == 1) Color.BLUE else Color.YELLOW)
                .width(App.instance.lineWidthGoogleMap)
                .geodesic(true)
            // Potential null sent, if stream api change, it could fail
            pattern.busStopsPatterns
                .mapNotNull { patternPoint ->
                    val point = LatLng(patternPoint.position.latitude, patternPoint.position.longitude)
                    poly.add(point)
                    var markerOptions: MarkerOptions? = null
                    if ("S" == patternPoint.type) {
                        markerOptions = MarkerOptions()
                            .position(point)
                            .title(patternPoint.stopName)
                            .snippet(pattern.direction)
                            .icon(if (index[0] == 0) red else blue)
                    }
                    // Potential null sent, if stream api change, it could fail
                    markerOptions
                }
                .distinct()
                .map { markerOptions ->
                    val marker = googleMap.addMarker(markerOptions)
                    marker!!.isVisible = false
                    marker
                }
                .forEach { busStationMarkers.add(it!!) }
            googleMap.addPolyline(poly)
            index[0]++
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id))
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds)) ?: arrayOf()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(getString(R.string.bundle_bus_id), busId)
        if (::busRouteId.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId)
        if (::bounds.isInitialized) savedInstanceState.putStringArray(getString(R.string.bundle_bus_bounds), bounds)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCameraIdle() {
        refreshBusesBitmap.refreshBusAndStation(googleMap.cameraPosition, busMarkers, busStationMarkers)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        refreshBusesBitmap = RefreshBusMarkers()

        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                return if (marker.title.startsWith("To ")) {
                    val view = views[marker]
                    if (!refreshingInfoWindow) {
                        selectedMarker = marker
                        val busId = marker.snippet
                        busService.loadFollowBus(busId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(BusFollowObserver(this@BusMapActivity, mapContainerLayout, view!!, false))
                        status[marker] = false
                    }
                    view
                } else {
                    null
                }
            }
        })

        googleMap.setOnInfoWindowClickListener { marker ->
            if (marker.title.startsWith("To ")) {
                val view = views[marker]
                if (!refreshingInfoWindow) {
                    selectedMarker = marker
                    val runNumber = marker.snippet
                    val current = status[marker]
                    busService.loadFollowBus(runNumber)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(BusFollowObserver(this@BusMapActivity, mapContainerLayout, view!!, !current!!))
                    status[marker] = !current
                }
            }
        }
        loadActivityData()
    }

    @SuppressLint("CheckResult")
    private fun loadActivityData() {
        busService.busForRouteId(busRouteId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(BusObserver(this@BusMapActivity, true, mapContainerLayout))
        if (loadPattern) {
            Observable.fromCallable {
                val patterns: MutableList<BusPattern> = mutableListOf()
                if (busId == 0) {
                    // Search for directions
                    val busDirections = busService.loadBusDirectionsSingle(busRouteId).blockingGet()
                    bounds = busDirections.busDirections.map { busDirection -> busDirection.text }.toTypedArray()
                }
                busService.loadBusPattern(busRouteId, bounds).blockingGet().forEach { patterns.add(it) }
                patterns
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        if (result != null) {
                            drawPattern(result)
                        } else {
                            util.showNetworkErrorMessage(mapContainerLayout)
                        }
                    },
                    {
                        util.showNetworkErrorMessage(mapContainerLayout)
                    }
                )
        }
    }
}
