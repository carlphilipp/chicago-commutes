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
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.utils.ColorUtils
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.BusBoundAdapter
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.utils.getCurrentStyle
import fr.cph.chicago.core.utils.setupMapbox
import fr.cph.chicago.exception.CtaException
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.foss.activity_bus_bound_mapbox.bellowLayout
import kotlinx.android.synthetic.foss.activity_bus_bound_mapbox.busFilter
import kotlinx.android.synthetic.foss.activity_bus_bound_mapbox.listView
import kotlinx.android.synthetic.foss.activity_bus_bound_mapbox.mapView
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val busService = BusService
        private val util = Util
        private val mapUtil = MapUtil
    }

    private var lineManager: LineManager? = null

    private lateinit var busRouteId: String
    private lateinit var busRouteName: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private lateinit var busBoundAdapter: BusBoundAdapter
    private var busStops: List<BusStop> = listOf()

    @SuppressLint("CheckResult")
    public override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this, getString(R.string.mapbox_token))
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bus_bound_mapbox)
            mapView?.onCreate(savedInstanceState)
            busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY
            bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
            boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY

            mapView?.getMapAsync(this)

            busBoundAdapter = BusBoundAdapter()
            listView.setOnItemClickListener { _, _, position, _ ->
                val busStop = busBoundAdapter.getItem(position) as BusStop
                val intent = Intent(applicationContext, BusStopActivity::class.java)

                val extras = with(Bundle()) {
                    putString(getString(R.string.bundle_bus_stop_id), busStop.id.toString())
                    putString(getString(R.string.bundle_bus_stop_name), busStop.name)
                    putString(getString(R.string.bundle_bus_route_id), busRouteId)
                    putString(getString(R.string.bundle_bus_route_name), busRouteName)
                    putString(getString(R.string.bundle_bus_bound), bound)
                    putString(getString(R.string.bundle_bus_bound_title), boundTitle)
                    putDouble(getString(R.string.bundle_bus_latitude), busStop.position.latitude)
                    putDouble(getString(R.string.bundle_bus_longitude), busStop.position.longitude)
                    this
                }

                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            listView.adapter = busBoundAdapter

            busFilter.addTextChangedListener(object : TextWatcher {
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

            toolbar.title = "$busRouteId - $boundTitle"

            toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_white_24dp)
            toolbar.setOnClickListener { finish() }

            busService.loadAllBusStopsForRouteBound(busRouteId, bound)
                .doOnSuccess { busStops ->
                    this.busStops = busStops
                    busBoundAdapter.updateBusStops(busStops)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { busBoundAdapter.notifyDataSetChanged() },
                    { error ->
                        Timber.e(error)
                        util.showOopsSomethingWentWrong(listView)
                    })
            // Preventing keyboard from moving background when showing up
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onMapReady(mapBox: MapboxMap) {
        setupMapbox(mapBox, resources.configuration)
        mapBox.setStyle(getCurrentStyle(resources.configuration)) { style ->
            lineManager = LineManager(this.mapView!!, mapBox, style)

            busService.loadBusPattern(busRouteId, bound)
                .observeOn(Schedulers.computation())
                .map { busPattern: BusPattern ->
                    val pair = mapUtil.getBounds(busPattern.busStopsPatterns.map { it.position })
                    val latLngBounds = LatLngBounds.Builder()
                        .include(LatLng(pair.first.latitude, pair.first.longitude))
                        .include(LatLng(pair.second.latitude, pair.second.longitude))
                        .build()
                    val lineOptions = LineOptions()
                        .withLatLngs(busPattern.busStopsPatterns.map { patternPoint -> LatLng(patternPoint.position.latitude, patternPoint.position.longitude) })
                        .withLineColor(ColorUtils.colorToRgbaString(Color.BLACK))
                        .withLineWidth((application as App).lineWidthMapBox)
                    Pair(latLngBounds, lineOptions)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { pair ->
                        mapBox.easeCamera(CameraUpdateFactory.newLatLngBounds(pair.first, 50), 500)
                        lineManager!!.create(pair.second)
                    },
                    { error ->
                        Timber.e(error)
                        when (error) {
                            is CtaException -> util.showSnackBar(bellowLayout, R.string.message_error_could_not_load_path)
                            else -> util.handleConnectOrParserException(error, bellowLayout)
                        }
                    })
        }
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId)
        savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName)
        savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound)
        savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle)
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
        lineManager?.onDestroy()
    }
}
