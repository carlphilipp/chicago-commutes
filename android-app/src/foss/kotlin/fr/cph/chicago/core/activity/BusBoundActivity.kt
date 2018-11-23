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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import butterknife.BindDrawable
import butterknife.BindString
import butterknife.BindView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.BusBoundAdapter
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundActivity : ButterKnifeActivity(R.layout.activity_bus_bound_mapbox), OnMapReadyCallback {

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
    @BindView(R.id.mapView)
    @JvmField
    var mapView: MapView? = null

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    private val observableUtil: ObservableUtil = ObservableUtil
    private val util: Util = Util

    private lateinit var busRouteId: String
    private lateinit var busRouteName: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private lateinit var busBoundAdapter: BusBoundAdapter
    private var busStops: List<BusStop> = listOf()

    public override fun onCreate(savedInstanceState: Bundle?) {
        App.checkBusData(this)
        Mapbox.getInstance(this, App.decode(getString(R.string.mapbox_token)))
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("CheckResult")
    override fun create(savedInstanceState: Bundle?) {
        mapView!!.onCreate(savedInstanceState)
        busRouteId = intent.getStringExtra(bundleBusRouteId)
        busRouteName = intent.getStringExtra(bundleBusRouteName)
        bound = intent.getStringExtra(bundleBusBound)
        boundTitle = intent.getStringExtra(bundleBusBoundTitle)

        mapView?.getMapAsync(this)

        busBoundAdapter = BusBoundAdapter()
        listView.setOnItemClickListener { _, _, position, _ ->
            val busStop = busBoundAdapter.getItem(position) as BusStop
            val intent = Intent(applicationContext, BusStopActivity::class.java)

            val extras = Bundle()
            extras.putInt(bundleBusStopId, busStop.id)
            extras.putString(bundleBusStopName, busStop.name)
            extras.putString(bundleBusRouteId, busRouteId)
            extras.putString(bundleBusRouteName, busRouteName)
            extras.putString(bundleBusBound, bound)
            extras.putString(bundleBusBoundTitle, boundTitle)
            extras.putDouble(bundleBusLatitude, busStop.position.latitude)
            extras.putDouble(bundleBusLongitude, busStop.position.longitude)

            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        listView.adapter = busBoundAdapter

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
                busBoundAdapter.updateBusStops(busStopsFiltered)
                busBoundAdapter.notifyDataSetChanged()
            }
        })

        util.setWindowsColor(this, toolbar, TrainLine.NA)
        toolbar.title = "$busRouteId - $boundTitle"

        toolbar.navigationIcon = arrowBackWhite
        toolbar.setOnClickListener { finish() }

        observableUtil.createBusStopBoundObservable(busRouteId, bound)
            .subscribe(
                { onNext ->
                    busStops = onNext
                    busBoundAdapter.updateBusStops(onNext)
                    busBoundAdapter.notifyDataSetChanged()
                },
                { error ->
                    Log.e(TAG, error.message, error)
                    util.showOopsSomethingWentWrong(listView)
                })
        // Preventing keyboard from moving background when showing up
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onMapReady(mapBox: MapboxMap) {
        mapBox.uiSettings.isLogoEnabled = false
        mapBox.uiSettings.isAttributionEnabled = false
        mapBox.uiSettings.isRotateGesturesEnabled = false
        mapBox.uiSettings.isTiltGesturesEnabled = false
        observableUtil.createBusPatternObservable(busRouteId, bound)
            .observeOn(Schedulers.computation())
            .map { busPattern: BusPattern ->
                val pair = MapUtil.getBounds(busPattern.busStopsPatterns.map { it.position })
                val latLngBounds = LatLngBounds.Builder()
                    .include(LatLng(pair.first.latitude, pair.first.longitude))
                    .include(LatLng(pair.second.latitude, pair.second.longitude))
                    .build()
                val poly = PolylineOptions()
                    .addAll(busPattern.busStopsPatterns.map { patternPoint -> LatLng(patternPoint.position.latitude, patternPoint.position.longitude) })
                    .color(Color.BLACK)
                    .width(App.instance.lineWidthMapBox)
                Pair(latLngBounds, poly)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { pair ->
                    mapBox.easeCamera(CameraUpdateFactory.newLatLngBounds(pair.first, 50), 500)
                    mapBox.addPolyline(pair.second)
                },
                { error ->
                    Log.e(TAG, error.message, error)
                    util.handleConnectOrParserException(error, null, layout, layout)
                })
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: ""
        busRouteName = savedInstanceState.getString(bundleBusRouteName) ?: ""
        bound = savedInstanceState.getString(bundleBusBound) ?: ""
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle) ?: ""
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putString(bundleBusRouteName, busRouteName)
        savedInstanceState.putString(bundleBusBound, bound)
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        mapView?.onSaveInstanceState(savedInstanceState)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
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

    companion object {
        private val TAG = BusBoundActivity::class.java.simpleName
    }
}
