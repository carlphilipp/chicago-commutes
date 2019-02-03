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

package fr.cph.chicago.core.model.marker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.core.App

abstract class RefreshMarkers(@DrawableRes drawable: Int) {

    private val bitmapDescSmall: BitmapDescriptor
    private val bitmapDescMedium: BitmapDescriptor
    private val bitmapDescLarge: BitmapDescriptor
    var currentDescriptor: BitmapDescriptor

    init {
        val icon = BitmapFactory.decodeResource(App.instance.resources, drawable)
        bitmapDescSmall = createBitMapDescriptor(icon, 9)
        bitmapDescMedium = createBitMapDescriptor(icon, 5)
        bitmapDescLarge = createBitMapDescriptor(icon, 3)
        currentDescriptor = bitmapDescSmall
    }

    private fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun isIn(num: Float, sup: Float, inf: Float): Boolean {
        return num in inf..sup
    }

    fun refresh(position: CameraPosition, markers: List<Marker>) {
        var currentZoom = -1f
        if (position.zoom != currentZoom) {
            val oldZoom = currentZoom
            currentZoom = position.zoom

            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                markers.forEach { marker -> marker.setIcon(bitmapDescSmall) }
                currentDescriptor = bitmapDescSmall
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                markers.forEach { marker -> marker.setIcon(bitmapDescMedium) }
                currentDescriptor = bitmapDescMedium
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                markers.forEach { marker -> marker.setIcon(bitmapDescLarge) }
                currentDescriptor = bitmapDescLarge
            }
        }
    }
}
