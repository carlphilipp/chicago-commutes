package fr.cph.chicago.marker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.core.App

abstract class RefreshMarkers(@DrawableRes drawable: Int) {

    val bitmapDescrSmall: BitmapDescriptor
    val bitmapDescrMedium: BitmapDescriptor
    val bitmapDescrLarge: BitmapDescriptor
    var currentDescriptor: BitmapDescriptor? = null

    init {
        val icon = BitmapFactory.decodeResource(App.instance.resources, drawable)
        bitmapDescrSmall = createBitMapDescriptor(icon, 9)
        bitmapDescrMedium = createBitMapDescriptor(icon, 5)
        bitmapDescrLarge = createBitMapDescriptor(icon, 3)
        currentDescriptor = bitmapDescrSmall
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
                markers.forEach { marker -> marker.setIcon(bitmapDescrSmall) }
                currentDescriptor = bitmapDescrSmall
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                markers.forEach { marker -> marker.setIcon(bitmapDescrMedium) }
                currentDescriptor = bitmapDescrMedium
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                markers.forEach { marker -> marker.setIcon(bitmapDescrLarge) }
                currentDescriptor = bitmapDescrLarge
            }
        }
    }
}
