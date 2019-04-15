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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import butterknife.BindString
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainStationPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.model.marker.RefreshTrainMarkers
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.rx.TrainEtaObserver
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.MapUtil
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

    private val trainService = TrainService
    private val observableUtil: ObservableUtil = ObservableUtil
    private var views: MutableMap<Marker, View> = hashMapOf()

    private lateinit var line: String
    private lateinit var refreshTrainMarkers: RefreshTrainMarkers
    private var status: MutableMap<Marker, Boolean> = mutableMapOf()
    private var trainsMarker: List<Marker> = listOf()
    private val stationMarkers: MutableList<Marker> = mutableListOf()

    private var centerMap = true
    private var drawLine = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        App.checkTrainData(this)
        super.onCreate(savedInstanceState)
    }

    override fun create(savedInstanceState: Bundle?) {
        line = if (savedInstanceState != null)
            savedInstanceState.getString(bundleTrainLine) ?: ""
        else
            intent.getStringExtra(bundleTrainLine)

        // Init data
        initData()

        // Init toolbar
        setToolbar()
    }

    override fun initData() {
        super.initData()
        refreshTrainMarkers = RefreshTrainMarkers()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener {
            centerMap = false
            loadActivityData()
            false
        }

        val trainLine = TrainLine.fromXmlString(line)
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

    private fun centerMapOnTrain(result: List<Train>) {
        val position: Position
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
        centerMapOn(position.latitude, position.longitude, zoom)
    }

    private fun drawTrains(trains: List<Train>) {
        resetData()
        val bitmapDesc = refreshTrainMarkers.currentDescriptor
        trainsMarker = trains.map { (routeNumber, destName, _, position, heading) ->
            val point = LatLng(position.latitude, position.longitude)
            val title = "To $destName"
            val snippet = routeNumber.toString()
            googleMap.addMarker(MarkerOptions().position(point).title(title).snippet(snippet).icon(bitmapDesc).anchor(0.5f, 0.5f).rotation(heading.toFloat()).flat(true))
        }.onEach { marker ->
            val view = layoutInflater.inflate(R.layout.marker, viewGroup, false)
            val title2 = view.findViewById<TextView>(R.id.title)
            title2.text = marker.title
            views[marker] = view
        }
    }

    private fun resetData() {
        views.clear()
        trainsMarker.forEach { it.remove() }
    }

    private fun drawLine(positions: List<Position>) {
        val poly = PolylineOptions()
        poly.width((application as App).lineWidthGoogleMap)
        poly.geodesic(true).color(TrainLine.fromXmlString(line).color)
        positions
            .map { position -> LatLng(position.latitude, position.longitude) }
            .forEach { poly.add(it) }

        googleMap.addPolyline(poly)
        drawLine = false
    }

    override fun onCameraIdle() {
        refreshTrainMarkers.refreshTrainAndStation(googleMap.cameraPosition, trainsMarker, stationMarkers)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        Observable.fromCallable { BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, colorDrawable())) }
            .flatMapIterable { icon -> trainService.getStationsForLine(TrainLine.fromXmlString(line)).map { Pair(it, icon) } }
            .map { pair ->
                val station = pair.first
                val point = LatLng(station.stopsPosition[0].latitude, station.stopsPosition[0].longitude)
                MarkerOptions()
                    .position(point)
                    .title(station.name)
                    .icon(pair.second)
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { markerOptions ->
                val marker = googleMap.addMarker(markerOptions)
                marker.isVisible = false
                stationMarkers.add(marker)
            }

        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                return when {
                    stationMarkers.contains(marker) -> null
                    "" != marker.snippet -> {
                        val view = views[marker]
                        if (!refreshingInfoWindow) {
                            selectedMarker = marker
                            val runNumber = marker.snippet
                            observableUtil.createLoadTrainEtaObservable(runNumber, false)
                                .subscribe(TrainEtaObserver(view!!, this@TrainMapActivity))
                            status[marker] = false
                        }
                        view
                    }
                    else -> null
                }
            }
        })
        googleMap.setOnInfoWindowClickListener { marker ->
            if ("" != marker.snippet) {
                val view = views[marker]
                if (!refreshingInfoWindow) {
                    selectedMarker = marker
                    val runNumber = marker.snippet
                    val current = status[marker] ?: false
                    observableUtil.createLoadTrainEtaObservable(runNumber, !current)
                        .subscribe(TrainEtaObserver(view!!, this@TrainMapActivity))
                    status[marker] = !current
                }
            }
        }
        loadActivityData()
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            // Load train location
            val trainsObservable = observableUtil.createTrainLocationObservable(line)
            // Load pattern from local file
            val positionsObservable = observableUtil.createTrainPatternObservable(line)

            if (drawLine) {
                Observable.zip(trainsObservable, positionsObservable, BiFunction { trains: List<Train>, positions: List<TrainStationPattern> ->
                    drawTrains(trains)
                    drawLine(positions.map { it.position })
                    if (trains.isNotEmpty()) {
                        if (centerMap) {
                            centerMapOnTrain(trains)
                        }
                    } else {
                        Util.showSnackBar(this@TrainMapActivity.currentFocus!!, R.string.message_no_train_found)
                    }
                    Any()
                }).subscribe(
                    {},
                    { throwable ->
                        Util.showSnackBar(this@TrainMapActivity.currentFocus!!, R.string.message_error_while_loading_data)
                        Log.e(TAG, "Error not handled", throwable)
                    }
                )
            } else {
                trainsObservable.subscribe { trains ->
                    if (trains != null) {
                        drawTrains(trains)
                        if (trains.isEmpty()) {
                            Util.showSnackBar(this@TrainMapActivity.currentFocus!!, R.string.message_no_train_found)
                        }
                    } else {
                        Util.showSnackBar(this@TrainMapActivity.currentFocus!!, R.string.message_error_while_loading_data)
                    }
                }
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }

    private fun colorDrawable(): Int {
        return when (TrainLine.fromXmlString(line)) {
            TrainLine.BLUE -> R.drawable.blue_marker_no_shade
            TrainLine.BROWN -> R.drawable.brown_marker_no_shade
            TrainLine.GREEN -> R.drawable.green_marker_no_shade
            TrainLine.ORANGE -> R.drawable.orange_marker_no_shade
            TrainLine.PINK -> R.drawable.pink_marker_no_shade
            TrainLine.PURPLE -> R.drawable.purple_marker_no_shade
            TrainLine.RED -> R.drawable.red_marker_no_shade
            TrainLine.YELLOW -> R.drawable.yellow_marker
            TrainLine.NA -> R.drawable.red_marker_no_shade
        }
    }

    companion object {
        private val TAG = TrainMapActivity::class.java.simpleName
    }
}
