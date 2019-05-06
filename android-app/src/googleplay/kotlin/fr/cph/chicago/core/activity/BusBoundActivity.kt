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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import butterknife.BindDrawable
import butterknife.BindString
import butterknife.BindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.BusBoundAdapter
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundActivity : ButterKnifeActivity(R.layout.activity_bus_bound) {

    @BindView(R.id.bellow)
    lateinit var layout: LinearLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.bus_filter)
    lateinit var filter: EditText
    @BindView(R.id.list)
    lateinit var listView: ListView

    @BindString(R.string.bundle_bus_stop_id)
    lateinit var bundleBusStopId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bound)
    lateinit var bundleBusBound: String
    @BindString(R.string.bundle_bus_bound_title)
    lateinit var bundleBusBoundTitle: String
    @BindString(R.string.bundle_bus_stop_name)
    lateinit var bundleBusStopName: String
    @BindString(R.string.bundle_bus_route_name)
    lateinit var bundleBusRouteName: String
    @BindString(R.string.bundle_bus_latitude)
    lateinit var bundleBusLatitude: String
    @BindString(R.string.bundle_bus_longitude)
    lateinit var bundleBusLongitude: String

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var busRouteId: String
    private lateinit var busRouteName: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private lateinit var adapter: BusBoundAdapter
    private var busStops: List<BusStop> = listOf()

    @SuppressLint("CheckResult")
    override fun create(savedInstanceState: Bundle?) {
        busRouteId = intent.getStringExtra(bundleBusRouteId)
        busRouteName = intent.getStringExtra(bundleBusRouteName)
        bound = intent.getStringExtra(bundleBusBound)
        boundTitle = intent.getStringExtra(bundleBusBoundTitle)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        adapter = BusBoundAdapter()
        listView.setOnItemClickListener { _, _, position, _ ->
            val busStop = adapter.getItem(position) as BusStop
            val intent = Intent(applicationContext, BusStopActivity::class.java)

            val extras = with(Bundle()) {
                putInt(bundleBusStopId, busStop.id)
                putString(bundleBusStopName, busStop.name)
                putString(bundleBusRouteId, busRouteId)
                putString(bundleBusRouteName, busRouteName)
                putString(bundleBusBound, bound)
                putString(bundleBusBoundTitle, boundTitle)
                putDouble(bundleBusLatitude, busStop.position.latitude)
                putDouble(bundleBusLongitude, busStop.position.longitude)
                this
            }

            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        listView.adapter = adapter

        filter.addTextChangedListener(object : TextWatcher {
            private var busStopsFiltered: MutableList<BusStop> = mutableListOf()

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                busStopsFiltered = mutableListOf()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                busStops
                    .filter { busStop -> StringUtils.containsIgnoreCase(busStop.name, s) }
                    .forEach { busStopsFiltered.add(it) }
            }

            override fun afterTextChanged(s: Editable) {
                adapter.updateBusStops(busStopsFiltered)
                adapter.notifyDataSetChanged()
            }
        })

        toolbar.title = "$busRouteId - $boundTitle"

        toolbar.navigationIcon = arrowBackWhite
        toolbar.setOnClickListener { finish() }

        busService.loadAllBusStopsForRouteBound(busRouteId, bound)
            .subscribe(
                { onNext ->
                    busStops = onNext
                    adapter.updateBusStops(onNext)
                    adapter.notifyDataSetChanged()
                },
                { onError ->
                    Timber.e(onError, "Error while getting bus stops for route bound")
                    util.showOopsSomethingWentWrong(listView)
                })

        // Preventing keyboard from moving background when showing up
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    public override fun onResume() {
        super.onResume()
        mapFragment.getMapAsync { googleMap ->
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(GoogleMapUtil.chicago, 7f, 0f, 0f)))
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isZoomControlsEnabled = false
                uiSettings.isMapToolbarEnabled = false
            }
            busService.loadBusPattern(busRouteId, bound)
                .subscribe(
                    { busPattern ->
                        if (busPattern.direction != "error") {
                            val center = busPattern.busStopsPatterns.size / 2
                            val position = busPattern.busStopsPatterns[center].position
                            if (position.latitude == 0.0 && position.longitude == 0.0) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
                            } else {
                                val latLng = LatLng(position.latitude, position.longitude)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(9f), 500, null)
                            }
                            drawPattern(googleMap, busPattern)
                        } else {
                            util.showSnackBar(this.layout, R.string.message_error_could_not_load_path)
                        }
                    },
                    { onError ->
                        util.handleConnectOrParserException(onError, layout)
                        Timber.e(onError, "Error while getting bus patterns")
                    })
        }
    }

    private fun drawPattern(googleMap: GoogleMap, pattern: BusPattern) {
        val poly = PolylineOptions()
            .geodesic(true)
            .color(Color.BLACK)
            .width(App.instance.lineWidthGoogleMap)
            .addAll(pattern.busStopsPatterns.map { patternPoint -> LatLng(patternPoint.position.latitude, patternPoint.position.longitude) })
        googleMap.addPolyline(poly)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: StringUtils.EMPTY
        busRouteName = savedInstanceState.getString(bundleBusRouteName) ?: StringUtils.EMPTY
        bound = savedInstanceState.getString(bundleBusBound) ?: StringUtils.EMPTY
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle) ?: StringUtils.EMPTY
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::busRouteId.isInitialized) savedInstanceState.putString(bundleBusRouteId, busRouteId)
        if (::busRouteName.isInitialized) savedInstanceState.putString(bundleBusRouteName, busRouteName)
        if (::bound.isInitialized) savedInstanceState.putString(bundleBusBound, bound)
        if (::boundTitle.isInitialized) savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        super.onSaveInstanceState(savedInstanceState)
    }

    companion object {
        private val busService = BusService
        private val util: Util = Util
    }
}
