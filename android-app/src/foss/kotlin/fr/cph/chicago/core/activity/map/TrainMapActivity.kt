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
import android.os.Bundle
import android.util.Log
import butterknife.BindString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.step
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
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

    private fun centerMapOnLine() {
        val pair: Pair<LatLng, Double> = when (trainLine) {
            TrainLine.BLUE -> Pair(LatLng(41.895351487237065, -87.7658120473011), 9.712402653729297)
            TrainLine.BROWN -> Pair(LatLng(41.917109926603494, -87.66789627065805), 11.182480413976615)
            TrainLine.GREEN -> Pair(LatLng(41.83977485811293, -87.70335827378659), 10.112386603953428)
            TrainLine.ORANGE -> Pair(LatLng(41.836677288834935, -87.67880795158356), 10.763237680550047)
            TrainLine.PINK -> Pair(LatLng(41.86087730862362, -87.69142385608006), 10.78020744535134)
            TrainLine.PURPLE -> Pair(LatLng(41.97621387448969, -87.66136527647143), 10.267667342991793)
            TrainLine.RED -> Pair(LatLng(41.87368988294554, -87.64321964856293), 10.090815394650992)
            TrainLine.YELLOW -> Pair(LatLng(42.020033618572555, -87.7147765617836), 11.633041915241225)
            TrainLine.NA -> Pair(LatLng(41.8819, -87.6278), 11.0)
        }
        centerMapOn(pair.first, pair.second)
    }

    private fun drawTrains(trains: List<Train>) {
        val features = trains.map { train ->
            val feature = Feature.fromGeometry(Point.fromLngLat(train.position.longitude, train.position.latitude))
            feature.addNumberProperty("heading", train.heading)
            feature
        }
        val source = mapboxMap.getSource("marker-source") as GeoJsonSource?
        if (source == null) {
            mapboxMap.addSource(GeoJsonSource("marker-source", FeatureCollection.fromFeatures(features)))
        } else {
            source.setGeoJson(FeatureCollection.fromFeatures(features))
        }
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
        mapboxMap.uiSettings.isAttributionEnabled = false
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        mapboxMap.addOnCameraIdleListener {
            Log.d("DERP", "target: ${mapboxMap.cameraPosition.target} - zoom: ${mapboxMap.cameraPosition.zoom}")
        }

        mapboxMap.addImage("image-train", BitmapFactory.decodeResource(resources, R.drawable.train))
        val symbolLayer = SymbolLayer("marker-layer", "marker-source")
            .withProperties(
                iconImage("image-train"),
                iconRotate(get("heading")),
                iconSize(
                    step(zoom(), 0.05f,
                        stop(9, 0.10f),
                        stop(10.5, 0.15f),
                        stop(12, 0.2f),
                        stop(15, 0.3f)
                    )
                ),
                iconAllowOverlap(true),
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)
            )
        mapboxMap.addLayer(symbolLayer)
        centerMapOnLine()
        loadActivityData()
        mapboxMap.addOnMapClickListener { point: LatLng ->
            val finalPoint = mapboxMap.projection.toScreenLocation(point)
            //val features = mapboxMap.queryRenderedFeatures(finalPoint, "building")
            val features = mapboxMap.queryRenderedFeatures(finalPoint, *runNumbers.keys.map { "marker-layer" }.toTypedArray())
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
                    if (trains.isEmpty()) {
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
