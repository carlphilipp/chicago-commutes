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

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.BusBoundAdapter
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_bus_bound.bellowLayout
import kotlinx.android.synthetic.main.activity_bus_bound.busFilter
import kotlinx.android.synthetic.main.activity_bus_bound.listView
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundActivity : AppCompatActivity() {

    companion object {
        private val busService = BusService
        private val util: Util = Util
    }

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var busRouteId: String
    private lateinit var busRouteName: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private lateinit var adapter: BusBoundAdapter
    private var busStops: List<BusStop> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bus_bound)
            busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY
            bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
            boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY

            mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            adapter = BusBoundAdapter()
            listView.setOnItemClickListener { _, _, position, _ ->
                val busStop = adapter.getItem(position) as BusStop
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
            listView.adapter = adapter

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
                    adapter.updateBusStops(busStopsFiltered)
                    adapter.notifyDataSetChanged()
                }
            })

            toolbar.title = "$busRouteId - $boundTitle"

            toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_white_24dp)
            toolbar.setOnClickListener { finish() }

            busService.loadAllBusStopsForRouteBound(busRouteId, bound)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        busStops = result
                        adapter.updateBusStops(result)
                        adapter.notifyDataSetChanged()
                    },
                    { throwable ->
                        Timber.e(throwable, "Error while getting bus stops for route bound")
                        util.showOopsSomethingWentWrong(listView)
                    })

            // Preventing keyboard from moving background when showing up
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        if (result.direction != "error") {
                            val center = result.busStopsPatterns.size / 2
                            val position = result.busStopsPatterns[center].position
                            if (position.latitude == 0.0 && position.longitude == 0.0) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
                            } else {
                                val latLng = LatLng(position.latitude, position.longitude)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(9f), 500, null)
                            }
                            drawPattern(googleMap, result)
                        } else {
                            util.showSnackBar(bellowLayout, R.string.message_error_could_not_load_path)
                        }
                    },
                    { throwable ->
                        util.handleConnectOrParserException(throwable, bellowLayout)
                        Timber.e(throwable, "Error while getting bus patterns")
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
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::busRouteId.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId)
        if (::busRouteName.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName)
        if (::bound.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound)
        if (::boundTitle.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle)
        super.onSaveInstanceState(savedInstanceState)
    }
}
