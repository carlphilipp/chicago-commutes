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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindDrawable
import butterknife.BindView
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import fr.cph.chicago.Constants.Companion.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeFragmentMapActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

@SuppressLint("Registered")
abstract class FragmentMapActivity : ButterKnifeFragmentMapActivity(), EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    @BindView(android.R.id.content)
    protected lateinit var viewGroup: ViewGroup
    @BindView(R.id.map_container)
    protected lateinit var layout: LinearLayout
    @BindView(R.id.toolbar)
    protected lateinit var toolbar: Toolbar

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    protected lateinit var mapboxMap: MapboxMap
    protected lateinit var selectedMarker: Marker
    protected var refreshingInfoWindow = false

    protected open fun initData() {
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

    fun refreshInfoWindow() {
        refreshingInfoWindow = true
        //selectedMarker.showInfoWindow()
        refreshingInfoWindow = false
    }

    protected fun centerMapOn(latitude: Double, longitude: Double, zoom: Double) {
        val latLng = LatLng(latitude, longitude)
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    protected fun centerMapOn(latLng: LatLng, zoom: Double) {
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private fun enableMyLocationOnMapIfAllowed() {
        if (EasyPermissions.hasPermissions(applicationContext, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            setLocationOnMap()
        } else {
            EasyPermissions.requestPermissions(this, "Would you like to see your current location on the map?", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        //this.mapboxMap.setOnCameraIdleListener(this)
        //this.mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
        enableMyLocationOnMapIfAllowed()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        setLocationOnMap()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}

    @Throws(SecurityException::class)
    private fun setLocationOnMap() {
        //this.googleMap.isMyLocationEnabled = true
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
}
