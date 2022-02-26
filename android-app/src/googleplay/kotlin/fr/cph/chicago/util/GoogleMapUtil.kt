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

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.R
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.MapUtil.chicagoPosition

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

    fun createStop(context: Context?, @DrawableRes icon: Int): BitmapDescriptor {
        return if (context != null) {
            val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
            val bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitMapBusStation)
            val shape = ContextCompat.getDrawable(context, icon)!!
            shape.setBounds(0, 0, px, bitMapBusStation.height)
            shape.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitMapBusStation)
        } else {
            BitmapDescriptorFactory.defaultMarker()
        }
    }
}

@NonNull
fun Position.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}
