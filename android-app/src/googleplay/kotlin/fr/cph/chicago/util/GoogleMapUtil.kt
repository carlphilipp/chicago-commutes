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

package fr.cph.chicago.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.MapUtil.chicagoPosition
import timber.log.Timber

object GoogleMapUtil {

    val chicago: LatLng by lazy {
        LatLng(chicagoPosition.latitude, chicagoPosition.longitude)
    }

    @Throws(SecurityException::class)
    fun centerMap(mapFragment: SupportMapFragment, zoomIn: Boolean, position: Position?) {
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            val latLng = if (position != null) LatLng(position.latitude, position.longitude) else chicago
            val cameraUpdate = if (zoomIn) CameraUpdateFactory.newLatLngZoom(latLng, 16f) else CameraUpdateFactory.newLatLng(latLng)
            googleMap.moveCamera(cameraUpdate)
        }
    }

    fun checkIfPermissionGranted(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }

    fun shouldShowPermissionRationale(context: Context, permission: String): Boolean {
        val activity = context as Activity?
        if (activity == null)
            Timber.d("Activity is null")

        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity!!,
            permission
        )
    }
}
