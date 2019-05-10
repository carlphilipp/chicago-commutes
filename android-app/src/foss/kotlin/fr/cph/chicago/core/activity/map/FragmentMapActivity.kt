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
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import butterknife.BindDrawable
import butterknife.BindView
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeFragmentMapActivity
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.utils.BitmapGenerator
import fr.cph.chicago.util.MapUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("Registered")
abstract class FragmentMapActivity : ButterKnifeFragmentMapActivity(), OnMapReadyCallback, OnMapClickListener {

    @BindView(R.id.activity_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.map_container)
    protected lateinit var layout: LinearLayout
    @BindView(R.id.toolbar)
    protected lateinit var toolbar: Toolbar

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    protected lateinit var map: MapboxMap
    protected var drawLine = true

    private var vehicleSource: GeoJsonSource? = null
    protected var vehicleFeatureCollection: FeatureCollection? = null

    private var stationSource: GeoJsonSource? = null
    protected var stationFeatureCollection: FeatureCollection? = null

    protected open fun initMap() {
        mapView?.getMapAsync(this)
    }

    protected open fun setToolbar() {
        toolbar.inflateMenu(R.menu.main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }

        toolbar.navigationIcon = arrowBackWhite
        toolbar.setOnClickListener { finish() }
    }

    protected fun refreshVehicles() {
        vehicleSource?.setGeoJson(vehicleFeatureCollection)
    }

    protected fun refreshStations() {
        stationSource?.setGeoJson(stationFeatureCollection)
    }

    protected fun centerMap(points: List<LatLng>) {
        Single.defer { Single.just(points) }
            .map { latLngs -> mapUtil.getBounds(latLngs.map { latLng -> Position(latLng.latitude, latLng.longitude) }) }
            .map { pair ->
                val latLngBounds = LatLngBounds.Builder()
                    .include(LatLng(pair.first.latitude, pair.first.longitude))
                    .include(LatLng(pair.second.latitude, pair.second.longitude))
                    .build()
                latLngBounds
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { latLngBounds -> map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 500) }
    }

    protected open fun selectVehicle(feature: Feature) {
        showProgress(true)
        deselectAll()
    }

    protected fun toRect(point: PointF): RectF {
        return RectF(
            point.x - DEFAULT_EXTRAPOLATION,
            point.y + DEFAULT_EXTRAPOLATION,
            point.x + DEFAULT_EXTRAPOLATION,
            point.y - DEFAULT_EXTRAPOLATION)
    }

    protected fun addVehicleFeatureCollection(featureCollection: FeatureCollection) {
        vehicleFeatureCollection = featureCollection
        vehicleSource = map.getSource(VEHICLE_SOURCE_ID) as GeoJsonSource?
        if (vehicleSource == null) {
            vehicleSource = GeoJsonSource(VEHICLE_SOURCE_ID, featureCollection)
            map.addSource(vehicleSource!!)
        } else {
            vehicleSource!!.setGeoJson(featureCollection)
        }
    }

    protected fun addStationFeatureCollection(featureCollection: FeatureCollection) {
        stationFeatureCollection = featureCollection
        stationSource = map.getSource(STATION_SOURCE_ID) as GeoJsonSource?
        if (stationSource == null) {
            stationSource = GeoJsonSource(STATION_SOURCE_ID, featureCollection)
            map.addSource(stationSource!!)
        } else {
            stationSource!!.setGeoJson(featureCollection)
        }
    }

    protected fun drawPolyline(polylines: List<PolylineOptions>) {
        map.addPolylines(polylines)
        drawLine = false
    }

    protected fun deselectAll() {
        vehicleFeatureCollection?.features()?.forEach { feature -> feature.properties()?.addProperty(PROPERTY_SELECTED, false) }
        stationFeatureCollection?.features()?.forEach { feature -> feature.properties()?.addProperty(PROPERTY_SELECTED, false) }
        refreshStations()
        refreshVehicles()
    }

    protected fun showProgress(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 50
        } else {
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("CheckResult")
    protected fun update(feature: Feature, id: String, view: View) {
        Single.defer { Single.just(BitmapGenerator.generate(view)) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bitmap ->
                map.addImage(id, bitmap)
                feature.properties()?.addProperty(PROPERTY_SELECTED, true)
                refreshVehicles()
                showProgress(false)
            }
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = with(map) {
            uiSettings.isLogoEnabled = false
            uiSettings.isAttributionEnabled = false
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isTiltGesturesEnabled = false
            this
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    companion object {
        protected const val DEFAULT_EXTRAPOLATION = 100

        private val mapUtil = MapUtil
    }
}
