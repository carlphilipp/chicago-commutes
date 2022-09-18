package fr.cph.chicago.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import fr.cph.chicago.core.model.Position

@SuppressLint("MissingPermission")
fun getLastUserLocation(
    context: Context,
    callBackLoadLocation: (position: Position) -> Unit,
    callBackDefaultLocation: () -> Unit,
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    var position: Position? = null

    if (gpsEnabled) {
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (gpsLocation != null) {
            position = Position(gpsLocation.latitude, gpsLocation.longitude)
        }
    }
    if (networkEnabled && position == null) {
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (networkLocation != null) {
            position = Position(networkLocation.latitude, networkLocation.longitude)
        }
    }
    when (position) {
        null -> callBackDefaultLocation()
        else -> callBackLoadLocation(position)
    }
}
