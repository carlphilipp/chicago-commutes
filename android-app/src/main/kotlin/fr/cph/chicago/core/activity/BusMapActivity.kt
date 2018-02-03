/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import butterknife.BindString
import butterknife.ButterKnife
import com.annimon.stream.Stream
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import fr.cph.chicago.Constants.Companion.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.Companion.BUSES_DIRECTION_URL
import fr.cph.chicago.Constants.Companion.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.Companion.BUSES_VEHICLES_URL
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.Bus
import fr.cph.chicago.entity.BusPattern
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.marker.RefreshBusMarkers
import fr.cph.chicago.rx.BusFollowObserver
import fr.cph.chicago.rx.BusObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.Util
import java.util.*

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusMapActivity : AbstractMapActivity() {

    @BindString(R.string.bundle_bus_id)
    lateinit var bundleBusId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bounds)
    lateinit var bundleBusBounds: String
    @BindString(R.string.analytics_bus_map)
    lateinit var analyticsBusMap: String
    @BindString(R.string.request_rt)
    lateinit var requestRt: String

    private val observableUtil: ObservableUtil = ObservableUtil
    private val busService: BusService = BusService

    private var busMarkers: MutableList<Marker>? = null
    private var busStationMarkers: MutableList<Marker>? = null
    private var views: MutableMap<Marker, View>? = null
    private var status: MutableMap<Marker, Boolean>? = null

    private var busId: Int? = null
    private var busRouteId: String? = null
    private var bounds: Array<String> = arrayOf()
    private var refreshBusesBitmap: RefreshBusMarkers? = null

    private var loadPattern = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkBusData(this)
        if (!this.isFinishing) {
            MapsInitializer.initialize(applicationContext)
            setContentView(R.layout.activity_map)
            ButterKnife.bind(this)

            if (savedInstanceState != null) {
                busId = savedInstanceState.getInt(bundleBusId)
                busRouteId = savedInstanceState.getString(bundleBusRouteId)
                bounds = savedInstanceState.getStringArray(bundleBusBounds)
            } else {
                val extras = intent.extras
                busId = extras!!.getInt(bundleBusId)
                busRouteId = extras.getString(bundleBusRouteId)
                bounds = extras.getStringArray(bundleBusBounds)
            }

            // Init data
            initData()

            // Init toolbar
            setToolbar()

            // Google analytics
            Util.trackScreen(analyticsBusMap)
        }
    }

    public override fun onStop() {
        super.onStop()
        loadPattern = false
    }

    override fun initData() {
        super.initData()
        busMarkers = ArrayList()
        busStationMarkers = ArrayList()
        views = HashMap()
        status = HashMap()
        refreshBusesBitmap = RefreshBusMarkers()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener { item ->
            Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_VEHICLES_URL)
            observableUtil.createBusListObservable(busId!!, busRouteId!!).subscribe(BusObserver(this@BusMapActivity, false, layout))
            false
        }

        Util.setWindowsColor(this, toolbar, TrainLine.NA)
        toolbar.title = busRouteId
    }

    fun centerMapOnBus(result: List<Bus>) {
        val sizeIsOne = result.size == 1
        val position = if (sizeIsOne) result[0].position else Bus.getBestPosition(result)
        val zoom = if (sizeIsOne) 15 else 11 // FIXME magic numbers
        centerMapOn(position.latitude, position.longitude, zoom)
    }

    fun drawBuses(buses: List<Bus>) {
        cleanAllMarkers()
        val bitmapDescr = refreshBusesBitmap!!.currentDescriptor
        Stream.of(buses).forEach { bus ->
            val point = LatLng(bus.position.latitude, bus.position.longitude)
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title("To " + bus.destination)
                    .snippet(bus.id.toString() + "")
                    .icon(bitmapDescr)
                    .anchor(0.5f, 0.5f)
                    .rotation(bus.heading.toFloat())
                    .flat(true)
            )
            busMarkers!!.add(marker)

            val layoutInflater = this@BusMapActivity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.marker, viewGroup, false)
            val title = view.findViewById<TextView>(R.id.title)
            title.text = marker.title

            views!![marker] = view
        }
    }

    private fun cleanAllMarkers() {
        Stream.of(busMarkers!!).forEach({ it.remove() })
        busMarkers!!.clear()
    }

    private fun drawPattern(patterns: List<BusPattern>) {
        val index = intArrayOf(0)
        val red = BitmapDescriptorFactory.defaultMarker()
        val blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        Stream.of(patterns).forEach { pattern ->
            val poly = PolylineOptions()
                .color(if (index[0] == 0) Color.RED else if (index[0] == 1) Color.BLUE else Color.YELLOW)
                .width((application as App).lineWidth).geodesic(true)
            Stream.of(pattern.points)
                .map<Marker> { patternPoint ->
                    val point = LatLng(patternPoint.position.latitude, patternPoint.position.longitude)
                    poly.add(point)
                    var marker: Marker? = null
                    if ("S" == patternPoint.type) {
                        marker = googleMap.addMarker(MarkerOptions()
                            .position(point)
                            .title(patternPoint.stopName)
                            .snippet(pattern.direction)
                            .icon(if (index[0] == 0) red else blue)
                        )
                        marker!!.isVisible = false
                    }
                    // Potential null sent, if stream api change, it could fail
                    marker
                }
                .filter { marker -> marker != null }
                .forEach({ busStationMarkers!!.add(it) })
            googleMap.addPolyline(poly)
            index[0]++
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busId = savedInstanceState.getInt(bundleBusId)
        busRouteId = savedInstanceState.getString(bundleBusRouteId)
        bounds = savedInstanceState.getStringArray(bundleBusBounds)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState!!.putInt(bundleBusId, busId!!)
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putStringArray(bundleBusBounds, bounds)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCameraIdle() {
        refreshBusesBitmap!!.refreshBusAndStation(googleMap.cameraPosition, busMarkers!!, busStationMarkers!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                if (marker.title.startsWith("To ")) {
                    val view = views!![marker]
                    if (!refreshingInfoWindow) {
                        selectedMarker = marker
                        val busId = marker.snippet
                        Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL)
                        observableUtil.createFollowBusObservable(busId)
                            .subscribe(BusFollowObserver(this@BusMapActivity, layout, view!!, false))
                        status!![marker] = false
                    }
                    return view
                } else {
                    return null
                }
            }
        })

        googleMap.setOnInfoWindowClickListener { marker ->
            if (marker.title.startsWith("To ")) {
                val view = views!![marker]
                if (!refreshingInfoWindow) {
                    selectedMarker = marker
                    val runNumber = marker.snippet
                    val current = status!![marker]
                    Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL)
                    observableUtil.createFollowBusObservable(runNumber)
                        .subscribe(BusFollowObserver(this@BusMapActivity, layout, view!!, !current!!))
                    status!![marker] = !current
                }
            }
        }
        loadActivityData()
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_VEHICLES_URL)
            observableUtil.createBusListObservable(busId!!, busRouteId!!).subscribe(BusObserver(this@BusMapActivity, true, layout))
            if (loadPattern) {
                LoadPattern().execute()
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }

    private inner class LoadPattern : AsyncTask<Void, Void, MutableList<BusPattern>>() {

        override fun doInBackground(vararg params: Void): MutableList<BusPattern> {
            val patterns: MutableList<BusPattern> = mutableListOf()
            if (busId == 0) {
                // Search for directions
                val busDirections = busService.loadBusDirections(busRouteId!!)
                bounds = busDirections.busDirections.map { busDirection -> busDirection.text }.toTypedArray()
                Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_DIRECTION_URL)
            }
            Stream.of(busService.loadBusPattern(busRouteId!!, bounds)).forEach({ patterns.add(it) })
            Util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_PATTERN_URL)
            return patterns
        }

        override fun onPostExecute(result: MutableList<BusPattern>?) {
            if (result != null) {
                drawPattern(result)
            } else {
                Util.showNetworkErrorMessage(layout)
            }
        }
    }

    companion object {

        private val TAG = BusMapActivity::class.java.simpleName
    }
}
