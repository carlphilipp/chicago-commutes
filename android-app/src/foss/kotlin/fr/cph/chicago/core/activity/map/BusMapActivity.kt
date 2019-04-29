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
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import butterknife.BindString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.step
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM_LEFT
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotationAlignment
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.BusesFunction
import fr.cph.chicago.rx.RxUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusMapActivity : FragmentMapActivity() {

    @BindString(R.string.bundle_bus_id)
    lateinit var bundleBusId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bounds)
    lateinit var bundleBusBounds: String

    private val rxUtil: RxUtil = RxUtil
    private val busService: BusService = BusService

    private var busId: Int = 0
    private lateinit var busRouteId: String
    private lateinit var bounds: Array<String>
    private var markerOptions = listOf<MarkerOptions>()
    private var showStops = false

    override fun create(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            busId = savedInstanceState.getInt(bundleBusId)
            busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: ""
            bounds = savedInstanceState.getStringArray(bundleBusBounds) ?: arrayOf()
        } else {
            busId = intent.getIntExtra(bundleBusId, 0)
            busRouteId = intent.getStringExtra(bundleBusRouteId)
            bounds = intent.getStringArrayExtra(bundleBusBounds)
        }

        initMap()

        setToolbar()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener {
            loadBuses()
            false
        }
        Util.setWindowsColor(this, toolbar, TrainLine.NA)
        toolbar.title = busRouteId
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busId = savedInstanceState.getInt(bundleBusId)
        busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: ""
        bounds = savedInstanceState.getStringArray(bundleBusBounds) ?: arrayOf()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(bundleBusId, busId)
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putStringArray(bundleBusBounds, bounds)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        this.map.addOnMapClickListener(this)

        this.map.addImage("image-bus", BitmapFactory.decodeResource(resources, R.drawable.bus))

        this.map.addLayer(
            SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID)
                .withProperties(
                    iconImage("image-bus"),
                    iconRotate(get(PROPERTY_HEADING)),
                    iconSize(
                        step(zoom(), 0.05f,
                            stop(9, 0.10f),
                            stop(10.5, 0.15f),
                            stop(12, 0.2f),
                            stop(15, 0.3f),
                            stop(17, 0.5f)
                        )
                    ),
                    iconAllowOverlap(true),
                    iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP)))

        this.map.addLayer(
            SymbolLayer(VEHICLE_INFO_LAYER_ID, VEHICLE_SOURCE_ID)
                .withProperties(
                    // show image with id title based on the value of the title feature property
                    iconImage("{title}"),
                    // set anchor of icon to bottom-left
                    iconAnchor(ICON_ANCHOR_BOTTOM_LEFT),
                    // offset icon slightly to match bubble layout
                    iconOffset(arrayOf(-20.0f, -10.0f)),
                    iconAllowOverlap(true)
                )
                .withFilter(eq(get(PROPERTY_SELECTED), literal(true))))

        loadActivityData()

        this.map.addOnCameraIdleListener {
            if (this.map.cameraPosition.zoom >= 15 && !showStops) {
                this.map.addMarkers(markerOptions)
            } else if (this.map.markers.isNotEmpty()) {
                this.map.markers.forEach { this.map.removeMarker(it) }
            }
        }
    }

    override fun onMapClick(point: LatLng) {
        val finalPoint = map.projection.toScreenLocation(point)
        val infoFeatures = map.queryRenderedFeatures(finalPoint, VEHICLE_INFO_LAYER_ID)
        if (!infoFeatures.isEmpty()) {
            val feature = infoFeatures[0]
            clickOnVehicleInfo(feature)
        } else {
            val markerFeatures = map.queryRenderedFeatures(toRect(finalPoint), VEHICLE_LAYER_ID)
            if (!markerFeatures.isEmpty()) {
                val title = markerFeatures[0].getStringProperty(PROPERTY_TITLE)
                val featureList = vehicleFeatureCollection!!.features()
                for (i in featureList!!.indices) {
                    if (featureList[i].getStringProperty(PROPERTY_TITLE) == title) {
                        val feature = vehicleFeatureCollection!!.features()!![i]
                        selectVehicle(feature)
                    }
                }
            } else {
                deselectAll()
                refreshVehicles()
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun selectVehicle(feature: Feature) {
        super.selectVehicle(feature)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        rxUtil.followBus(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, false))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, id, view) },
                { error ->
                    Log.e(TAG, error.message, error)
                    Util.showSnackBar(layout, R.string.message_no_data)
                    showProgress(false)
                })
    }

    @SuppressLint("CheckResult")
    private fun clickOnVehicleInfo(feature: Feature) {
        showProgress(true)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        rxUtil.followBus(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, true))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, id, view) },
                { error ->
                    Log.e(TAG, error.message, error)
                    Util.showSnackBar(layout, R.string.message_no_data)
                    showProgress(false)
                })
    }

    @SuppressLint("CheckResult")
    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            loadBuses()
            if (drawLine) {
                val index = intArrayOf(0)
                Observable.fromCallable {
                    val patterns: MutableList<BusPattern> = mutableListOf()
                    if (busId == 0) {
                        // Search for directions
                        val busDirections = busService.loadBusDirections(busRouteId)
                        bounds = busDirections.busDirections.map { busDirection -> busDirection.text }.toTypedArray()
                    }
                    busService.loadBusPattern(busRouteId, bounds).forEach { patterns.add(it) }
                    patterns
                }
                    .observeOn(Schedulers.computation())
                    .map { patterns ->
                        centerMap(patterns.flatMap { it.busStopsPatterns.map { patternPoint -> LatLng(patternPoint.position.latitude, patternPoint.position.longitude) } })
                        patterns.map { pattern ->
                            val positions = pattern.busStopsPatterns.map { patternPoint ->
                                val latLng = LatLng(patternPoint.position.latitude, patternPoint.position.longitude)
                                var marketOptions: MarkerOptions? = null
                                if ("S" == patternPoint.type) {
                                    marketOptions = MarkerOptions()
                                        .position(latLng)
                                        .title(patternPoint.stopName)
                                        .snippet(pattern.direction)
                                    if (index[0] != 0) {
                                        marketOptions.icon(blueIcon)
                                    } else {
                                        marketOptions.icon(redIcon)
                                    }
                                }
                                Pair(latLng, marketOptions)
                            }

                            val poly = PolylineOptions()
                                .color(if (index[0] == 0) Color.RED else if (index[0] == 1) Color.BLUE else Color.YELLOW)
                                .width((application as App).lineWidthMapBox)
                                .addAll(positions.map { it.first })

                            index[0]++
                            Pair<PolylineOptions, List<MarkerOptions>>(
                                poly,
                                positions
                                    .map { it.second }
                                    .filter { markerOptions -> markerOptions != null }
                                    .map { markerOptions -> markerOptions!! }
                            )
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { result ->
                            if (result != null) {
                                drawPolyline(result.map { pair -> pair.first })
                                this.markerOptions = result.flatMap { pair -> pair.second }
                            } else {
                                Util.showSnackBar(layout, R.string.message_no_pattern_found)
                            }
                        },
                        { error ->
                            Log.e(TAG, error.message, error)
                            Util.showSnackBar(layout, R.string.message_no_pattern_found)
                        })
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }

    @SuppressLint("CheckResult")
    private fun loadBuses() {
        rxUtil.busForRouteId(busRouteId)
            .observeOn(Schedulers.computation())
            .map { buses ->
                val features = buses.map { bus ->
                    with(Feature.fromGeometry(Point.fromLngLat(bus.position.longitude, bus.position.latitude))) {
                        addNumberProperty(PROPERTY_HEADING, bus.heading)
                        addStringProperty(PROPERTY_TITLE, bus.id.toString())
                        addStringProperty(PROPERTY_DESTINATION, "To ${bus.destination}")
                        addBooleanProperty(PROPERTY_FAVOURITE, false)
                        this
                    }
                }
                FeatureCollection.fromFeatures(features)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { featureCollection: FeatureCollection ->
                    addVehicleFeatureCollection(featureCollection)
                    if (featureCollection.features() != null && featureCollection.features()!!.isEmpty()) {
                        Util.showSnackBar(layout, R.string.message_no_bus_found)
                    }
                },
                { error ->
                    Log.e(TAG, error.message, error)
                    Util.showSnackBar(layout, R.string.message_error_while_loading_data)
                })
    }

    private val blueIcon: Icon by lazy {
        IconFactory.getInstance(this@BusMapActivity).fromResource(R.drawable.blue_marker)
    }

    private val redIcon: Icon by lazy {
        IconFactory.getInstance(this@BusMapActivity).fromResource(R.drawable.red_marker)
    }

    companion object {
        private val TAG = BusMapActivity::class.java.simpleName
    }
}
