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
import android.view.View
import butterknife.BindString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.utils.BitmapGenerator
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.rx.TrainsFunction
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.Single
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

        initData()

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

    override fun centerMap() {
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

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)

        this.mapboxMap.addImage("image-train", BitmapFactory.decodeResource(resources, R.drawable.train))

        this.mapboxMap.addLayer(SymbolLayer(MARKER_LAYER_ID, SOURCE_ID)
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
    }

    private fun handleClickInfo(feature: Feature) {
        showProgress(true)
        val runNumber = feature.getStringProperty(PROPERTY_TITLE)
        observableUtil.createLoadTrainEtaObservable(runNumber, true)
            .observeOn(Schedulers.computation())
            .map(TrainsFunction(this@TrainMapActivity, feature))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view -> update(feature, runNumber, view) }
    }

    override fun setSelected(feature: Feature) {
        super.setSelected(feature)
        val runNumber = feature.getStringProperty(PROPERTY_TITLE)
        observableUtil.createLoadTrainEtaObservable(runNumber, false)
            .observeOn(Schedulers.computation())
            .map(TrainsFunction(this@TrainMapActivity, feature))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view -> update(feature, runNumber, view) }
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            // Load train location
            val featuresObs: Observable<FeatureCollection> = observableUtil.createTrainLocationObservable(line)
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
                val polylineObs: Observable<PolylineOptions> = observableUtil.createTrainPatternObservable(line)
                    .map { positions ->
                        PolylineOptions()
                            .width((application as App).lineWidthMapBox)
                            .color(TrainLine.fromXmlString(line).color)
                            .addAll(positions.map { position -> LatLng(position.latitude, position.longitude) })
                    }

                Observable.zip(featuresObs, polylineObs, BiFunction { collections: FeatureCollection, polyline: PolylineOptions ->
                    addFeatureCollection(collections)
                    drawPolyline(listOf(polyline))
                    if (collections.features() != null && collections.features()!!.isEmpty()) {
                        Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                    }
                    Any()
                }).subscribe({}, { Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data) })
            } else {
                featuresObs.subscribe({ featureCollection ->
                    if (featureCollection != null) {
                        addFeatureCollection(featureCollection)
                        if (featureCollection.features() != null && featureCollection.features()!!.isEmpty()) {
                            Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                        }
                    } else {
                        Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                    }
                }, {
                    Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                })
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }
}
