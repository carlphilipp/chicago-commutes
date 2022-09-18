package fr.cph.chicago.core.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import fr.cph.chicago.core.model.Position
import timber.log.Timber

@SuppressLint("MissingPermission")
fun getLastUserLocation(
    context: Context,
    callBackLoadLocation:(position: Position) -> Unit,
    callBackDefaultLocation: () -> Unit,
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            Timber.d("User location found: $location")
            val position = Position(location.latitude, location.longitude)
            callBackLoadLocation(position)
        } else {
            Timber.d("User location found not found")
            callBackDefaultLocation()
        }
    }
}
