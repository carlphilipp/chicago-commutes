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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.Constants.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.databinding.ActivityMapBinding
import fr.cph.chicago.util.GoogleMapUtil
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

@SuppressLint("Registered")
abstract class FragmentMapActivity : FragmentActivity(), EasyPermissions.PermissionCallbacks, GoogleMap.OnCameraIdleListener, OnMapReadyCallback {

    protected lateinit var mapContainerLayout: LinearLayout
    protected lateinit var selectedMarker: Marker
    protected lateinit var googleMap: GoogleMap
    protected var refreshingInfoWindow = false
    protected lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            binding = ActivityMapBinding.inflate(layoutInflater)
            MapsInitializer.initialize(applicationContext)
            setContentView(binding.root)
            create(savedInstanceState)
        }
    }

    open fun create(savedInstanceState: Bundle?) {
        mapContainerLayout = binding.mapContainer
    }

    protected open fun initData() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    protected open fun setToolbar() {
        val toolbar = binding.included.toolbar
        toolbar.inflateMenu(R.menu.main)
        toolbar.elevation = 4f

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
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
        this.googleMap = with(googleMap) {
            setOnCameraIdleListener(this@FragmentMapActivity)
            moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
            this
        }
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
