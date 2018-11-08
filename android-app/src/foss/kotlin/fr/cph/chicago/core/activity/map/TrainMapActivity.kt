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
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainStationPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.rx.TrainsFunction
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        App.checkTrainData(this)
        super.onCreate(savedInstanceState)
    }

    override fun create(savedInstanceState: Bundle?) {
        line = if (savedInstanceState != null)
            savedInstanceState.getString(bundleTrainLine) ?: ""
        else
            intent.getStringExtra(bundleTrainLine)

        initMap()

        setToolbar()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener {
            loadActivityData()
            false
        }
        Util.setWindowsColor(this, toolbar, trainLine)
        toolbar.title = trainLine.toStringWithLine()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        line = savedInstanceState.getString(bundleTrainLine) ?: ""
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(bundleTrainLine, line)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        this.map.addOnMapClickListener(this)

        this.map.addImage("image-train", BitmapFactory.decodeResource(resources, R.drawable.train))

        this.map.addLayer(SymbolLayer(MARKER_LAYER_ID, SOURCE_ID)
            .withProperties(
                iconImage("image-train"),
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
                iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP)))

        this.map.addLayer(SymbolLayer(INFO_LAYER_ID, SOURCE_ID)
            .withProperties(
                // show image with id title based on the value of the title feature property
                iconImage("{title}"),
                // set anchor of icon to bottom-left
                iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),
                // offset icon slightly to match bubble layout
                iconOffset(arrayOf(-20.0f, -10.0f))
            )
            .withFilter(eq(get(PROPERTY_SELECTED), literal(true))))

        loadActivityData()
    }

    override fun onMapClick(point: LatLng) {
        val finalPoint = this.map.projection.toScreenLocation(point)
        val infoFeatures = this.map.queryRenderedFeatures(finalPoint, INFO_LAYER_ID)
        if (!infoFeatures.isEmpty()) {
            val feature = infoFeatures[0]
            clickOnInfo(feature)
        } else {
            val markerFeatures = this.map.queryRenderedFeatures(toRect(finalPoint), MARKER_LAYER_ID)
            if (!markerFeatures.isEmpty()) {
                val title = markerFeatures[0].getStringProperty(PROPERTY_TITLE)
                val featureList = featureCollection!!.features()
                for (i in featureList!!.indices) {
                    if (featureList[i].getStringProperty(PROPERTY_TITLE) == title) {
                        val feature = featureCollection!!.features()!![i]
                        selectFeature(feature)
                    }
                }
            } else {
                deselectAll()
                refreshSource()
            }
        }
    }

    private fun clickOnInfo(feature: Feature) {
        showProgress(true)
        loadAndUpdateTrainArrivalFeature(feature, true)
    }

    override fun selectFeature(feature: Feature) {
        super.selectFeature(feature)
        loadAndUpdateTrainArrivalFeature(feature, false)
    }

    private fun loadAndUpdateTrainArrivalFeature(feature: Feature, loadAll: Boolean) {
        val runNumber = feature.getStringProperty(PROPERTY_TITLE)
        observableUtil.createLoadTrainEtaObservable(runNumber, loadAll)
            .observeOn(Schedulers.computation())
            .map(TrainsFunction(this@TrainMapActivity, feature))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, runNumber, view) },
                { error ->
                    Log.e(TAG, error.message, error)
                    Util.showMessage(layout, R.string.message_no_data)
                    showProgress(false)
                })
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            // Load train location
            val featuresObs = observableUtil.createTrainLocationObservable(line)
                .observeOn(Schedulers.computation())
                .map { trains: List<Train> ->
                    val features = trains.map { train ->
                        val feature = Feature.fromGeometry(Point.fromLngLat(train.position.longitude, train.position.latitude))
                        feature.addNumberProperty("heading", train.heading)
                        feature.addStringProperty(PROPERTY_TITLE, train.runNumber.toString())
                        feature.addStringProperty(PROPERTY_DESTINATION, "To ${train.destName}")
                        feature.addBooleanProperty(PROPERTY_FAVOURITE, false)
                        feature
                    }
                    FeatureCollection.fromFeatures(features)
                }

            if (drawLine) {
                // Load pattern from local file
                val polylineObs: Observable<Pair<PolylineOptions, List<TrainStationPattern>>> = observableUtil.createTrainPatternObservable(line)
                    .observeOn(Schedulers.computation())
                    .map { trainStationPatterns ->
                        val poly = PolylineOptions()
                            .width((application as App).lineWidthMapBox)
                            .color(TrainLine.fromXmlString(line).color)
                            .addAll(trainStationPatterns.map { trainStationPattern: TrainStationPattern ->
                                LatLng(trainStationPattern.position.latitude, trainStationPattern.position.longitude)
                            })
                        val mapPatterns = trainStationPatterns.filter { trainStationPattern -> trainStationPattern.stationName != null }
                        Pair(poly, mapPatterns)
                    }

                Observable.zip(
                    featuresObs.observeOn(AndroidSchedulers.mainThread()),
                    polylineObs.observeOn(AndroidSchedulers.mainThread()),
                    BiFunction { collections: FeatureCollection, pair: Pair<PolylineOptions, List<TrainStationPattern>> ->
                        addFeatureCollection(collections)
                        drawPolyline(listOf(pair.first))
                        drawStations(pair.second)
                        if (collections.features() != null && collections.features()!!.isEmpty()) {
                            Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                        }
                        pair.first.points
                    })
                    .subscribe(
                        { points -> centerMap(points) },
                        { error ->
                            Log.e(TAG, error.message, error)
                            Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                        })
            } else {
                featuresObs.subscribe(
                    { featureCollection ->
                        if (featureCollection != null) {
                            addFeatureCollection(featureCollection)
                            if (featureCollection.features() != null && featureCollection.features()!!.isEmpty()) {
                                Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                            }
                        } else {
                            Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                        }
                    },
                    { error ->
                        Log.e(TAG, error.message, error)
                        Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                    })
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }

    private fun drawStations(trainStationPatterns: List<TrainStationPattern>) {
        trainStationPatterns.forEach { trainStationPattern ->
            map.addMarker(MarkerOptions()
                .position(LatLng(trainStationPattern.position.latitude, trainStationPattern.position.longitude))
                .title(trainStationPattern.stationName)
            )
        }
    }

    companion object {
        private val TAG = TrainMapActivity::class.java.simpleName
    }
}
