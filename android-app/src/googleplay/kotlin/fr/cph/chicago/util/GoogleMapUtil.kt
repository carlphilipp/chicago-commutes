package fr.cph.chicago.util

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.MapUtil.chicagoPosition

object GoogleMapUtil {

    val chicago: LatLng by lazy {
        LatLng(chicagoPosition.latitude, chicagoPosition.longitude)
    }

    @Throws(SecurityException::class)
    fun centerMap(mapFragment: SupportMapFragment, position: Position?) {
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            if (position != null) {
                val latLng = LatLng(position.latitude, position.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
            }
        }
    }
}
