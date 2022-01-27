/**
 * Copyright 2021 Carl-Philipp Harmant
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
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import fr.cph.chicago.R
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.utils.BitmapGenerator
import fr.cph.chicago.core.utils.getCurrentStyle
import fr.cph.chicago.core.utils.setupMapbox
import fr.cph.chicago.databinding.ActivityMapMapboxBinding
import fr.cph.chicago.util.MapUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@SuppressLint("Registered")
abstract class FragmentMapActivity : FragmentActivity(), OnMapReadyCallback, OnMapClickListener {

    companion object {
        protected const val DEFAULT_EXTRAPOLATION = 100
        private val mapUtil = MapUtil
    }

    protected lateinit var binding: ActivityMapMapboxBinding
    protected lateinit var map: MapboxMap
    private var lineManager: LineManager? = null
    protected var drawLine = true

    private var vehicleSource: GeoJsonSource? = null
    protected var vehicleFeatureCollection: FeatureCollection? = null

    private var stationSource: GeoJsonSource? = null
    protected var stationFeatureCollection: FeatureCollection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            Mapbox.getInstance(this, getString(R.string.mapbox_token))
            binding = ActivityMapMapboxBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.mapView.onCreate(savedInstanceState)
        }
    }

    protected open fun initMap() {
        binding.mapView.getMapAsync(this)
    }

    protected open fun setToolbar() {
        val toolbar = binding.included.toolbar
        toolbar.inflateMenu(R.menu.main)
        toolbar.elevation = 4f

        toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_back_white_24dp, theme)
        toolbar.setOnClickListener { finish() }
    }

    protected fun refreshVehicles() {
        vehicleSource?.setGeoJson(vehicleFeatureCollection)
    }

    protected fun refreshStations() {
        stationSource?.setGeoJson(stationFeatureCollection)
    }

    @SuppressLint("CheckResult")
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
            .subscribe(
                { latLngBounds -> map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 500) },
                { error -> Timber.e(error) })
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

        vehicleSource = map.style!!.getSource(VEHICLE_SOURCE_ID) as GeoJsonSource?
        if (vehicleSource == null) {
            vehicleSource = GeoJsonSource(VEHICLE_SOURCE_ID, featureCollection)
            map.style!!.addSource(vehicleSource!!)
        } else {
            vehicleSource!!.setGeoJson(featureCollection)
        }
    }

    protected fun addStationFeatureCollection(featureCollection: FeatureCollection) {
        stationFeatureCollection = featureCollection
        stationSource = map.style!!.getSource(STATION_SOURCE_ID) as GeoJsonSource?
        if (stationSource == null) {
            stationSource = GeoJsonSource(STATION_SOURCE_ID, featureCollection)
            map.style!!.addSource(stationSource!!)
        } else {
            stationSource!!.setGeoJson(featureCollection)
        }
    }

    protected fun drawPolyline(lineOptions: LineOptions) {
        lineManager?.create(lineOptions)
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
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.progress = 50
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("CheckResult")
    protected fun update(feature: Feature, id: String, view: View) {
        Single.defer { Single.just(BitmapGenerator.generate(view)) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bitmap ->
                    map.style!!.addImage(id, bitmap)
                    feature.properties()?.addProperty(PROPERTY_SELECTED, true)
                    refreshVehicles()
                    showProgress(false)
                },
                { error -> Timber.e(error) }
            )
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = setupMapbox(map, resources.configuration)
        this.map.setStyle(getCurrentStyle(resources.configuration)) { style ->
            lineManager = LineManager(this.binding.mapView, this.map, style)
            onMapStyleReady(style)
        }
    }

    abstract fun onMapStyleReady(style: Style)

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        lineManager?.onDestroy()
    }
}
