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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.BubbleLayout
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
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.utils.BitmapGenerator
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import fr.cph.chicago.rx.TrainsFunction
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.foss.activity_map_mapbox.mapContainer
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.concurrent.Callable

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
//FIXME: to remove deprecated API, need https://github.com/mapbox/mapbox-plugins-android/issues/649
class TrainMapActivity : FragmentMapActivity() {

    companion object {
        private val trainService = TrainService
        private val util = Util
    }

    private lateinit var line: String
    val trainLine: TrainLine by lazy {
        TrainLine.fromXmlString(line)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        line = if (savedInstanceState != null)
            savedInstanceState.getString(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        else
            intent.getStringExtra(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY

        initMap()

        setToolbar()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener {
            loadActivityData()
            false
        }
        util.setWindowsColor(this, toolbar, trainLine)
        toolbar.title = trainLine.toStringWithLine()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        line = savedInstanceState.getString(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(getString(R.string.bundle_train_line), line)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        this.map.addOnMapClickListener(this)
    }

    override fun onMapStyleReady(style: Style) {
        val imageTrain = singleFromCallable(Callable { BitmapFactory.decodeResource(resources, R.drawable.train) }, Schedulers.computation())
        val stationMarker = singleFromCallable(Callable { BitmapFactory.decodeResource(resources, colorDrawable()) }, Schedulers.computation())

        Singles.zip(imageTrain, stationMarker, zipper = { bitmapTrain, bitmapStation ->
            style.addImage(IMAGE_TRAIN, bitmapTrain)
            style.addImage(IMAGE_STATION, bitmapStation)
        }).subscribe()

        style.addLayer(
            SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID)
                .withProperties(
                    iconImage(IMAGE_TRAIN),
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

        style.addLayerAbove(
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
                .withFilter(eq(get(PROPERTY_SELECTED), literal(true))), VEHICLE_LAYER_ID)

        style.addLayerBelow(
            SymbolLayer(STATION_LAYER_ID, STATION_SOURCE_ID)
                .withProperties(
                    iconImage(IMAGE_STATION),
                    iconSize(
                        step(zoom(), 0f,
                            stop(13, 0.6f),
                            stop(15, 1f),
                            stop(17, 1.3f)
                        )
                    ),
                    iconAllowOverlap(true)), VEHICLE_LAYER_ID)

        style.addLayerAbove(
            SymbolLayer(STATION_INFO_LAYER_ID, STATION_SOURCE_ID)
                .withProperties(
                    // show image with id title based on the value of the title feature property
                    iconImage("{title}"),
                    // set anchor of icon to bottom-left
                    iconAnchor(ICON_ANCHOR_BOTTOM_LEFT),
                    iconSize(
                        step(zoom(), 0f,
                            stop(13, 1f)
                        )
                    ),
                    // offset icon slightly to match bubble layout
                    iconOffset(arrayOf(-20.0f, -24.0f)),
                    iconAllowOverlap(true)
                )
                .withFilter(eq(get(PROPERTY_SELECTED), literal(true))), VEHICLE_LAYER_ID)
        loadActivityData()
    }

    override fun onMapClick(point: LatLng): Boolean {
        val finalPoint = map.projection.toScreenLocation(point)
        val vehicleInfoFeatures = map.queryRenderedFeatures(finalPoint, VEHICLE_INFO_LAYER_ID)
        if (vehicleInfoFeatures.isNotEmpty()) {
            val feature = vehicleInfoFeatures[0]
            clickOnVehicleInfo(feature)
        } else {
            val rectangle = toRect(finalPoint)
            val markerFeatures = map.queryRenderedFeatures(rectangle, VEHICLE_LAYER_ID)
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
                val stationsInfoFeatures = map.queryRenderedFeatures(rectangle, STATION_LAYER_ID)
                if (stationsInfoFeatures.isNotEmpty()) {
                    val title = stationsInfoFeatures[0].getStringProperty(PROPERTY_TITLE)
                    val featureList = stationFeatureCollection!!.features()
                    for (i in featureList!!.indices) {
                        if (featureList[i].getStringProperty(PROPERTY_TITLE) == title) {
                            val feature = stationFeatureCollection!!.features()!![i]
                            selectStation(feature)
                        }
                    }
                } else {
                    deselectAll()
                    refreshVehicles()
                }
            }
        }
        return false
    }

    override fun selectVehicle(feature: Feature) {
        super.selectVehicle(feature)
        loadAndUpdateTrainArrivalFeature(feature, false)
    }

    private fun selectStation(feature: Feature) {
        deselectAll()
        feature.properties()?.addProperty(PROPERTY_SELECTED, true)
        refreshStations()
    }

    private fun clickOnVehicleInfo(feature: Feature) {
        showProgress(true)
        loadAndUpdateTrainArrivalFeature(feature, true)
    }

    @SuppressLint("CheckResult")
    private fun loadAndUpdateTrainArrivalFeature(feature: Feature, loadAll: Boolean) {
        val runNumber = feature.getStringProperty(PROPERTY_TITLE)
        trainService.trainEtas(runNumber, loadAll)
            .observeOn(Schedulers.computation())
            .map(TrainsFunction(this@TrainMapActivity, feature))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { view -> update(feature, runNumber, view) },
                { error ->
                    Timber.e(error)
                    util.showSnackBar(mapContainer, R.string.message_no_data)
                    showProgress(false)
                })
    }

    @SuppressLint("CheckResult")
    private fun loadActivityData() {
        // Load train location
        val featuresTrains = trainService.trainLocations(line)
            .observeOn(Schedulers.computation())
            .map { trains: List<Train> ->
                val features = trains.map { train ->
                    val feature = Feature.fromGeometry(Point.fromLngLat(train.position.longitude, train.position.latitude))
                    feature.addNumberProperty(PROPERTY_HEADING, train.heading)
                    feature.addStringProperty(PROPERTY_TITLE, train.runNumber.toString())
                    feature.addStringProperty(PROPERTY_DESTINATION, "To ${train.destName}")
                    feature.addBooleanProperty(PROPERTY_FAVOURITE, false)
                    feature
                }
                FeatureCollection.fromFeatures(features)
            }

        if (drawLine) {
            // Load pattern from local file
            val patterns = trainService.readPatterns(TrainLine.fromXmlString(line))
                .observeOn(Schedulers.computation())
                .map { trainStationPatterns ->
                    val latLngs = trainStationPatterns.map { stationPattern -> LatLng(stationPattern.position.latitude, stationPattern.position.longitude) }
                    val lineOptions = LineOptions()
                        .withLatLngs(latLngs)
                        .withLineColor(ColorUtils.colorToRgbaString(TrainLine.fromXmlString(line).color))
                        .withLineWidth((application as App).lineWidthMapBox)
                    val features = trainStationPatterns
                        .filter { trainStationPattern -> trainStationPattern.stationName != null }
                        .map { trainStationPattern ->
                            val feature = Feature.fromGeometry(Point.fromLngLat(trainStationPattern.position.longitude, trainStationPattern.position.latitude))
                            feature.addStringProperty(PROPERTY_TITLE, trainStationPattern.stationName)
                            feature
                        }
                    val featureCollection = FeatureCollection.fromFeatures(features)
                    Pair(lineOptions, featureCollection)
                }

            Singles.zip(
                featuresTrains.observeOn(AndroidSchedulers.mainThread()),
                patterns.observeOn(AndroidSchedulers.mainThread()),
                zipper = { featuresTrain, pair ->
                    addVehicleFeatureCollection(featuresTrain)
                    addStationFeatureCollection(pair.second)
                    addStationOnMap(pair.second)
                    drawPolyline(pair.first)
                    if (featuresTrain.features() != null && featuresTrain.features()!!.isEmpty()) {
                        util.showSnackBar(mapContainer, R.string.message_no_train_found)
                    }
                    pair.first.latLngs
                })
                .subscribe(
                    { points -> centerMap(points) },
                    { error ->
                        Timber.e(error)
                        util.showSnackBar(mapContainer, R.string.message_error_while_loading_data)
                    })
        } else {
            featuresTrains
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { featureCollection ->
                        addVehicleFeatureCollection(featureCollection)
                        if (featureCollection.features() != null && featureCollection.features()!!.isEmpty()) {
                            util.showSnackBar(mapContainer, R.string.message_no_train_found)
                        }
                    },
                    { error ->
                        Timber.e(error)
                        util.showSnackBar(mapContainer, R.string.message_error_while_loading_data)
                    })
        }
    }

    @SuppressLint("CheckResult")
    private fun addStationOnMap(stationInfoFeatureCollection: FeatureCollection) {
        Single.defer {
            val bitmaps = stationInfoFeatureCollection.features()?.map { feature ->
                val inflater = LayoutInflater.from(this@TrainMapActivity)
                val bubbleLayout = inflater.inflate(R.layout.marker_mapbox, null) as BubbleLayout // FIXME: Do not pass null

                val title = feature.getStringProperty(PROPERTY_TITLE)
                val titleTextView = bubbleLayout.findViewById<TextView>(R.id.title)
                titleTextView.text = title

                val bitmap = BitmapGenerator.generate(bubbleLayout)
                Pair(title, bitmap)
            }?.toMap()
            Single.just(bitmaps)
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bitmaps -> map.style!!.addImages(bitmaps as HashMap<String, Bitmap>) },
                { error -> Timber.e(error) }
            )
    }

    private fun colorDrawable(): Int {
        return when (trainLine) {
            TrainLine.BLUE -> R.drawable.blue_marker
            TrainLine.BROWN -> R.drawable.brown_marker
            TrainLine.GREEN -> R.drawable.green_marker
            TrainLine.ORANGE -> R.drawable.orange_marker
            TrainLine.PINK -> R.drawable.pink_marker
            TrainLine.PURPLE -> R.drawable.purple_marker
            TrainLine.RED -> R.drawable.red_marker
            TrainLine.YELLOW -> R.drawable.yellow_marker
            TrainLine.NA -> R.drawable.red_marker
        }
    }
}
