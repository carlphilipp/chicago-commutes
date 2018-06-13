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

package fr.cph.chicago.core.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindDrawable
import butterknife.BindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.Constants.Companion.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.util.PositionUtil
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

@SuppressLint("Registered")
open class AbstractMapActivity : FragmentActivity(), EasyPermissions.PermissionCallbacks, GoogleMap.OnCameraIdleListener, OnMapReadyCallback {

    @BindView(android.R.id.content)
    lateinit var viewGroup: ViewGroup
    @BindView(R.id.map_container)
    lateinit var layout: LinearLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    lateinit var selectedMarker: Marker
    protected lateinit var googleMap: GoogleMap
    protected var refreshingInfoWindow = false

    protected open fun initData() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        selectedMarker.showInfoWindow()
        refreshingInfoWindow = false
    }

    protected fun centerMapOn(latitude: Double, longitude: Double, zoom: Int) {
        val latLng = LatLng(latitude, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom.toFloat()))
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

    override fun onCameraIdle() {
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setOnCameraIdleListener(this)
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PositionUtil.chicago, 10f))
        enableMyLocationOnMapIfAllowed()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        setLocationOnMap()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}

    @Throws(SecurityException::class)
    private fun setLocationOnMap() {
        this.googleMap.isMyLocationEnabled = true
    }
}
