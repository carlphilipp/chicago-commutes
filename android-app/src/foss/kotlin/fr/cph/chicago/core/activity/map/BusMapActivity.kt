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

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import butterknife.BindString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
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
import com.mapbox.mapboxsdk.utils.ColorUtils
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.exception.CtaException
import fr.cph.chicago.rx.BusesFunction
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
//FIXME: to remove deprecated API, need https://github.com/mapbox/mapbox-plugins-android/issues/649
class BusMapActivity : FragmentMapActivity() {

    companion object {
        private val util = Util
        private val busService: BusService = BusService
    }

    @BindString(R.string.bundle_bus_id)
    lateinit var bundleBusId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bounds)
    lateinit var bundleBusBounds: String

    private var busId: Int = 0
    private lateinit var busRouteId: String
    private lateinit var bounds: Array<String>
    private val markerOptions = mutableListOf<MarkerOptions>()
    private var showStops = false

    override fun create(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            busId = savedInstanceState.getInt(bundleBusId)
            busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: StringUtils.EMPTY
            bounds = savedInstanceState.getStringArray(bundleBusBounds) ?: arrayOf()
        } else {
            busId = intent.getIntExtra(bundleBusId, 0)
            busRouteId = intent.getStringExtra(bundleBusRouteId) ?: StringUtils.EMPTY
            bounds = intent.getStringArrayExtra(bundleBusBounds) ?: arrayOf()
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
        util.setWindowsColor(this, toolbar, TrainLine.NA)
        toolbar.title = busRouteId
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busId = savedInstanceState.getInt(bundleBusId)
        busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: StringUtils.EMPTY
        bounds = savedInstanceState.getStringArray(bundleBusBounds) ?: arrayOf()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(bundleBusId, busId)
        if (::busRouteId.isInitialized) savedInstanceState.putString(bundleBusRouteId, busRouteId)
        if (::bounds.isInitialized) savedInstanceState.putStringArray(bundleBusBounds, bounds)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        this.map.addOnMapClickListener(this)

        this.map.addOnCameraIdleListener {
            if (this.map.cameraPosition.zoom >= 15 && !showStops) {
                this.map.addMarkers(markerOptions)
            } else if (this.map.markers.isNotEmpty()) {
                this.map.markers.forEach { this.map.removeMarker(it) }
            }
        }
    }

    override fun onMapStyleReady(style: Style) {
        style.addImage(IMAGE_BUS, BitmapFactory.decodeResource(resources, R.drawable.bus))

        style.addLayer(
            SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID)
                .withProperties(
                    iconImage(IMAGE_BUS),
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

        style.addLayer(
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
    }

    override fun onMapClick(point: LatLng): Boolean {
        val finalPoint = map.projection.toScreenLocation(point)
        val infoFeatures = map.queryRenderedFeatures(finalPoint, VEHICLE_INFO_LAYER_ID)
        if (infoFeatures.isNotEmpty()) {
            val feature = infoFeatures[0]
            clickOnVehicleInfo(feature)
        } else {
            val markerFeatures = map.queryRenderedFeatures(toRect(finalPoint), VEHICLE_LAYER_ID)
            if (markerFeatures.isNotEmpty()) {
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
        return true
    }

    override fun selectVehicle(feature: Feature) {
        super.selectVehicle(feature)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        busService.loadFollowBus(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, false))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, id, view) },
                { error ->
                    Timber.e(error)
                    util.showSnackBar(layout, R.string.message_no_data)
                    showProgress(false)
                })
    }

    private fun clickOnVehicleInfo(feature: Feature) {
        showProgress(true)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        busService.loadFollowBus(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, true))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, id, view) },
                { error ->
                    Timber.e(error)
                    util.showSnackBar(layout, R.string.message_no_data)
                    showProgress(false)
                })
    }

    private fun loadActivityData() {
        loadBuses()
        if (drawLine) {
            val index = intArrayOf(0)
            Observable.fromCallable {
                if (busId == 0) {
                    // Search for directions
                    val busDirections = busService.loadBusDirectionsSingle(busRouteId).blockingGet()
                    bounds = busDirections.busDirections.map { busDirection -> busDirection.text }.toTypedArray()
                }
                busService.loadBusPattern(busRouteId, bounds).blockingGet()
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
                                    .icon(if (index[0] != 0) blueIcon else redIcon)
                            }
                            Pair(latLng, marketOptions)
                        }

                        val lineOptions = LineOptions()
                            .withLatLngs(positions.map { it.first })
                            .withLineColor(ColorUtils.colorToRgbaString(if (index[0] == 0) Color.RED else if (index[0] == 1) Color.BLUE else Color.YELLOW))
                            .withLineWidth((application as App).lineWidthMapBox)
                        index[0]++
                        Pair(lineOptions, positions.mapNotNull { it.second })
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        result.map { pair -> pair.first }.forEach { drawPolyline(it) }
                        this.markerOptions.clear()
                        this.markerOptions.addAll(result.flatMap { pair -> pair.second })
                    },
                    { error ->
                        Timber.e(error)
                        util.handleConnectOrParserException(error, layout)
                    })
        }
    }

    private fun loadBuses() {
        busService.busForRouteId(busRouteId)
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
                        util.showSnackBar(layout, R.string.message_no_bus_found)
                    }
                },
                { error ->
                    Timber.e(error)
                    when (error) {
                        is CtaException -> util.showSnackBar(this.layout, R.string.message_error_could_not_load_path)
                        else -> util.handleConnectOrParserException(error, layout)
                    }
                })
    }

    private val blueIcon: Icon by lazy { IconFactory.getInstance(this@BusMapActivity).fromResource(R.drawable.blue_marker) }

    private val redIcon: Icon by lazy { IconFactory.getInstance(this@BusMapActivity).fromResource(R.drawable.red_marker) }
}
