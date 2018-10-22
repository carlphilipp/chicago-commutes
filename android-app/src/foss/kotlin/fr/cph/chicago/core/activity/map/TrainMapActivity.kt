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
import android.util.Log
import butterknife.BindString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotationAlignment
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.ArrayList
import java.util.UUID

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainMapActivity : FragmentMapActivity() {

    @BindString(R.string.bundle_train_line)
    lateinit var bundleTrainLine: String

    private val observableUtil: ObservableUtil = ObservableUtil

    private lateinit var line: String
    val trainLine: TrainLine by lazy {
        TrainLine.fromXmlString(line)
    }

    private var centerMap = true
    private var drawLine = true
    private val runNumbers = mutableMapOf<Int, Pair<String, LatLng>>()

    internal var featureMarker: Marker? = null

    private var selectedBuilding: com.mapbox.mapboxsdk.annotations.Polygon? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        App.checkTrainData(this)
        super.onCreate(savedInstanceState)
    }

    override fun create(savedInstanceState: Bundle?) {
        line = if (savedInstanceState != null)
            savedInstanceState.getString(bundleTrainLine) ?: ""
        else
            intent.getStringExtra(bundleTrainLine)

        mapView!!.getMapAsync(this)

        // Init toolbar
        setToolbar()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener { _ ->
            centerMap = false
            loadActivityData()
            false
        }
        Util.setWindowsColor(this, toolbar, trainLine)
        toolbar.title = trainLine.toStringWithLine()
    }

    public override fun onStop() {
        super.onStop()
        centerMap = false
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        line = savedInstanceState.getString(bundleTrainLine) ?: ""
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(bundleTrainLine, line)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun centerMapOnLine(result: List<Train>) {
        /*val position: Position
        val zoom: Int
        if (result.size == 1) {
            position = if (result[0].position.latitude == 0.0 && result[0].position.longitude == 0.0)
                MapUtil.chicagoPosition
            else
                result[0].position
            zoom = 15
        } else {
            position = MapUtil.getBestPosition(result.map { it.position })
            zoom = 11
        }
        centerMapOn(position.latitude, position.longitude, zoom)*/
        // FIXME: find a better way
        val position = MapUtil.getBestPosition(result.map { it.position })
        val zoom: Double = when (trainLine) {
            TrainLine.BLUE -> 9.7
            TrainLine.BROWN -> 11.0
            TrainLine.GREEN -> 10.0
            TrainLine.ORANGE -> 10.5
            TrainLine.PINK -> 10.5
            TrainLine.PURPLE -> 10.0
            TrainLine.RED -> 9.0
            TrainLine.YELLOW -> 9.0
            TrainLine.NA -> 9.0
        }
        centerMapOn(position.latitude, position.longitude, zoom)
    }


    private fun drawTrains(trains: List<Train>) {
        if (runNumbers.isEmpty()) {
            trains.forEach { train -> addTrainToMap(train) }
        } else {
            trains.forEach { train ->
                val marker = mapboxMap.getLayer("marker-layer-${train.runNumber}")
                if (marker != null) {
                    val pair = runNumbers[train.runNumber]
                    val newPosition = LatLng(train.position.latitude, train.position.longitude)
                    if (pair!!.second != newPosition) {
                        //val source = mapboxMap.getSourceAs<GeoJsonSource>("marker-source-${pair.first}")
                        //val features = source?.querySourceFeatures(Expression.literal("Text"))
                        //val point = features!!.get(0).geometry() as Point

                        val geoJsonSource = mapboxMap.getSource("marker-source-${pair.first}") as GeoJsonSource?
                        val feature = Feature.fromGeometry(Point.fromLngLat(train.position.longitude, train.position.latitude))
                        geoJsonSource?.setGeoJson(feature)

                        val symbolLayer = mapboxMap.getLayer("marker-layer-${train.runNumber}") as SymbolLayer
                        symbolLayer.setProperties(iconRotate(train.heading.toFloat()))
                    }
                } else {
                    addTrainToMap(train)
                }
            }
        }
    }

    private fun addTrainToMap(train: Train) {
        val feature = Feature.fromGeometry(Point.fromLngLat(train.position.longitude, train.position.latitude))
        val latLng = LatLng(train.position.latitude, train.position.longitude)
        val uuid = UUID.randomUUID().toString()
        //val marker = mapboxMap.addMarker(MarkerOptions().position(latLng).title(train.destName))
        runNumbers[train.runNumber] = Pair(uuid, latLng)

        mapboxMap.addSource(GeoJsonSource("marker-source-$uuid", feature))
        val symbolLayer = SymbolLayer("marker-layer-${train.runNumber}", "marker-source-$uuid")
            .withProperties(
                iconImage("image-train"),
                iconRotate(train.heading.toFloat()),
                iconSize(0.2f),
                iconAllowOverlap(true),
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)
            )
        mapboxMap.addLayer(symbolLayer)
    }

    private fun drawLine(positions: List<Position>) {
        val poly = PolylineOptions()
        poly.width((application as App).lineWidthMapBox)
        poly.color(TrainLine.fromXmlString(line).color)
        positions
            .map { position -> LatLng(position.latitude, position.longitude) }
            .forEach { poly.add(it) }

        mapboxMap.addPolyline(poly)
        drawLine = false
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.addImage("image-train", BitmapFactory.decodeResource(resources, R.drawable.train))
        loadActivityData()
        mapboxMap.addOnMapClickListener { point: LatLng ->
            val finalPoint = mapboxMap.projection.toScreenLocation(point)
            //val features = mapboxMap.queryRenderedFeatures(finalPoint, "building")
            val features = mapboxMap.queryRenderedFeatures(finalPoint, *runNumbers.keys.map { "marker-layer-$it" }.toTypedArray())
            if (features.size > 0) {
                val featureId = features[0].id()

                for (a in features.indices) {
                    if (featureId == features[a].id()) {
                        val pol = features[a].geometry()
                        if (pol is Point) {
                            val p = pol as Point
                            /*val list = ArrayList<LatLng>()
                            for (i in 0 until (features[a].geometry() as Point).coordinates()!!.size) {
                                for (j in 0 until (features[a].geometry() as Point).coordinates()!![i].size) {
                                    list.add(LatLng(
                                        (features[a].geometry() as Point).coordinates()!![i][j].latitude(),
                                        (features[a].geometry() as Point).coordinates()!![i][j].longitude()
                                    ))
                                }
                            }*/

                            val marker = mapboxMap.addMarker(
                                MarkerOptions()
                                    .title("derp")
                                    .position(point)
                                    .snippet("detail snippet")
                            )
                            marker.showInfoWindow(mapboxMap, mapView!!)

                            /*selectedBuilding = mapboxMap.addPolygon(PolygonOptions()
                                .addAll(list)
                                .fillColor(Color.parseColor("#8A8ACB"))
                            )*/
                        }
                    }
                }
            }
        }
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            // Load train location
            val trainsObservable = observableUtil.createTrainLocationObservable(line)
            // Load pattern from local file
            val positionsObservable = observableUtil.createTrainPatternObservable(line)

            if (drawLine) {
                Observable.zip(trainsObservable, positionsObservable, BiFunction { trains: List<Train>, positions: List<Position> ->
                    drawTrains(trains)
                    drawLine(positions)
                    if (trains.isNotEmpty()) {
                        if (centerMap) {
                            centerMapOnLine(trains)
                        }
                    } else {
                        Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                    }

                    Any()
                }).subscribe()
            } else {
                trainsObservable.subscribe { trains ->
                    if (trains != null) {
                        drawTrains(trains)
                        if (trains.isEmpty()) {
                            Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                        }
                    } else {
                        Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                    }
                }
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }
}
