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

package fr.cph.chicago.core.model.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.camera.CameraPosition
import fr.cph.chicago.core.App
import java.util.UUID

abstract class RefreshMarkers(@DrawableRes drawable: Int) {

    private val bitmapDescSmall: Icon
    private val bitmapDescMedium: Icon
    private val bitmapDescLarge: Icon
    var currentDescriptor: Icon

    init {
        bitmapDescSmall = IconFactory.recreate(UUID.randomUUID().toString(), createBitMapDescriptor(drawable, 100, App.instance)) // 9
        bitmapDescMedium = IconFactory.recreate(UUID.randomUUID().toString(), createBitMapDescriptor(drawable, 50, App.instance)) // 5
        bitmapDescLarge = IconFactory.recreate(UUID.randomUUID().toString(), createBitMapDescriptor(drawable, 10, App.instance)) // 3
        currentDescriptor = bitmapDescSmall
    }

    private fun createBitMapDescriptor(@DrawableRes drawable: Int, size: Int, context: Context): Bitmap {
        val bitMapStation = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitMapStation)
        val shape = ContextCompat.getDrawable(context, drawable)!!
        shape.setBounds(0, 0, size, bitMapStation.height)
        shape.draw(canvas)
        return bitMapStation
    }

    fun isIn(num: Double, sup: Float, inf: Float): Boolean {
        return num in inf..sup
    }

    fun refresh(position: CameraPosition, markers: List<Marker>) {
        var currentZoom = -1.0
        if (position.zoom != currentZoom) {
            val oldZoom = currentZoom
            currentZoom = position.zoom

            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                markers.forEach { marker -> marker.icon = bitmapDescSmall }
                currentDescriptor = bitmapDescSmall
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                markers.forEach { marker -> marker.icon = bitmapDescMedium }
                currentDescriptor = bitmapDescMedium
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                markers.forEach { marker -> marker.icon = bitmapDescLarge }
                currentDescriptor = bitmapDescLarge
            }
        }
    }
}
