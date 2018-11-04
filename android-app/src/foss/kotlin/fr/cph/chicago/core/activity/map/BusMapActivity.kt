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
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.step
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import com.mapbox.mapboxsdk.style.layers.Property
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
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.BusesFunction
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.MapUtil
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

    private val observableUtil: ObservableUtil = ObservableUtil
    private val busService: BusService = BusService

    private var busId: Int = 0
    private lateinit var busRouteId: String
    private lateinit var bounds: Array<String>
    private var markerOptions = listOf<MarkerOptions>()
    private var showStops = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        App.checkBusData(this)
        super.onCreate(savedInstanceState)
    }

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

        initData()

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

    fun centerMapOnBus(result: List<Bus>) {
        val sizeIsOne = result.size == 1
        val position = if (sizeIsOne) result[0].position else MapUtil.getBestPosition(result.map { it.position })
        val zoom = if (sizeIsOne) 15 else 11 // FIXME magic numbers
        centerMapOn(position.latitude, position.longitude, zoom.toDouble())
    }

    override fun centerMap() {
        //TODO()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)

        this.mapboxMap.addImage("image-bus", BitmapFactory.decodeResource(resources, R.drawable.bus))

        this.mapboxMap.addLayer(SymbolLayer(MARKER_LAYER_ID, SOURCE_ID)
            .withProperties(
                iconImage("image-bus"),
                iconRotate(get("heading")),
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
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)))

        this.mapboxMap.addLayer(SymbolLayer(INFO_LAYER_ID, SOURCE_ID)
            .withProperties(
                // show image with id title based on the value of the title feature property
                iconImage("{title}"),
                // set anchor of icon to bottom-left
                iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),
                // offset icon slightly to match bubble layout
                iconOffset(arrayOf(-20.0f, -10.0f))
            )
            .withFilter(eq(get(PROPERTY_SELECTED), literal(true))))

        centerMap()
        loadActivityData()

        this.mapboxMap.addOnMapClickListener { point: LatLng ->
            val finalPoint = this.mapboxMap.projection.toScreenLocation(point)
            val infoFeatures = this.mapboxMap.queryRenderedFeatures(finalPoint, INFO_LAYER_ID)
            if (!infoFeatures.isEmpty()) {
                val feature = infoFeatures[0]
                handleClickInfo(feature)
            } else {
                val markerFeatures = this.mapboxMap.queryRenderedFeatures(toRect(finalPoint), MARKER_LAYER_ID)
                if (!markerFeatures.isEmpty()) {
                    val title = markerFeatures[0].getStringProperty(PROPERTY_TITLE)
                    val featureList = featureCollection!!.features()
                    for (i in featureList!!.indices) {
                        if (featureList[i].getStringProperty(PROPERTY_TITLE) == title) {
                            val feature = featureCollection!!.features()!![i]
                            setSelected(feature)
                        }
                    }
                } else {
                    deselectAll()
                    refreshSource()
                }
            }
        }

        this.mapboxMap.addOnCameraIdleListener {
            if (this.mapboxMap.cameraPosition.zoom >= 15 && !showStops) {
                this.mapboxMap.addMarkers(markerOptions)
            } else if (this.mapboxMap.markers.isNotEmpty()) {
                this.mapboxMap.markers.forEach { this.mapboxMap.removeMarker(it) }
            }
        }
    }

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
                        patterns.map { pattern ->
                            val positions = pattern.points.map { patternPoint ->
                                val latLng = LatLng(patternPoint.position.latitude, patternPoint.position.longitude)
                                var marketOptions: MarkerOptions? = null
                                if ("S" == patternPoint.type) {
                                    marketOptions = MarkerOptions()
                                        .position(latLng)
                                        .title(patternPoint.stopName)
                                        .snippet(pattern.direction)
                                    if (index[0] != 0) {
                                        marketOptions.icon(blueIcon)
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
                    .subscribe { result ->
                        if (result != null) {
                            drawPolyline(result.map { pair -> pair.first })
                            this.markerOptions = result.flatMap { pair -> pair.second }
                        } else {
                            Util.showNetworkErrorMessage(layout)
                        }
                    }
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }

    private fun handleClickInfo(feature: Feature) {
        showProgress(true)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        observableUtil.createFollowBusObservable(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, true))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view -> update(feature, id, view) }
    }

    override fun setSelected(feature: Feature) {
        super.setSelected(feature)
        val id = feature.getStringProperty(PROPERTY_TITLE)
        observableUtil.createFollowBusObservable(id)
            .observeOn(Schedulers.computation())
            .map(BusesFunction(this@BusMapActivity, feature, false))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view -> update(feature, id, view) }
    }

    private fun loadBuses() {
        observableUtil.createBusListObservable(busRouteId)
            .observeOn(Schedulers.computation())
            .map { buses ->
                val features = buses.map { bus ->
                    val feature = Feature.fromGeometry(Point.fromLngLat(bus.position.longitude, bus.position.latitude))
                    feature.addNumberProperty("heading", bus.heading)
                    feature.addStringProperty(PROPERTY_TITLE, bus.id.toString())
                    feature.addStringProperty(PROPERTY_DESTINATION, "To ${bus.destination}")
                    feature.addBooleanProperty(PROPERTY_FAVOURITE, false)
                    feature
                }
                FeatureCollection.fromFeatures(features)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ featureCollection ->
                addFeatureCollection(featureCollection)
                if (featureCollection.features() != null && featureCollection.features()!!.isEmpty()) {
                    Util.showMessage(this@BusMapActivity, R.string.message_no_bus_found)
                }
            }, {
                Util.showMessage(this@BusMapActivity, R.string.message_error_while_loading_data)
            })
    }

    private val blueIcon: Icon by lazy {
        IconFactory.getInstance(this@BusMapActivity).fromResource(R.drawable.blue_marker)
    }
}
