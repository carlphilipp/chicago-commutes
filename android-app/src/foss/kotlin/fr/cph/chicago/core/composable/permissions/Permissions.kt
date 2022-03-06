package fr.cph.chicago.core.composable.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import fr.cph.chicago.core.composable.common.LocationViewModel
import fr.cph.chicago.core.composable.viewmodel.MainViewModel
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.Util
import timber.log.Timber

@SuppressLint("MissingPermission")
@Composable
fun NearbyLocationPermissionView(
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    if (locationViewModel.requestPermission) {
        PermissionLocationView(context = context) { permissionAction ->
            when (permissionAction) {
                is PermissionAction.OnPermissionGranted -> {
                    Timber.d("Permission grant successful")
                    locationViewModel.requestPermission = false

                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    if (gpsEnabled || networkEnabled) {
                        refreshUserLocation(context, mainViewModel)
                    } else {
                        mainViewModel.setDefaultUserLocation()
                    }
                }
                is PermissionAction.OnPermissionDenied -> {
                    Timber.d("Permission grant denied")
                    locationViewModel.requestPermission = false
                    mainViewModel.setDefaultUserLocation()
                }
            }
        }
    } else {
        refreshUserLocation(context = context, mainViewModel = mainViewModel)
    }
}

@SuppressLint("MissingPermission")
private fun refreshUserLocation(context: Context, mainViewModel: MainViewModel) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val position = Position(location.latitude, location.longitude)
            mainViewModel.setNearbyIsMyLocationEnabled(true)
            mainViewModel.setCurrentUserLocation(position)
            mainViewModel.loadNearbyStations(position)

            locationManager.removeUpdates(this)
        }
    }

    locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, 0,
        0f, locationListener
    )
}

@Composable
private fun PermissionLocationView(
    context: Context,
    permissionAction: (PermissionAction) -> Unit
) {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionGranted = Util.checkIfPermissionGranted(context, permission)

    if (permissionGranted) {
        Timber.d("Permission already granted, exiting..")
        permissionAction(PermissionAction.OnPermissionGranted)
        return
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Timber.d("Permission provided by user")
            permissionAction(PermissionAction.OnPermissionGranted)
        } else {
            Timber.d("Permission denied by user")
            permissionAction(PermissionAction.OnPermissionDenied)
        }
    }

    Timber.d("Requesting permission for $permission")
    SideEffect {
        launcher.launch(permission)
    }
}

sealed class PermissionAction {
    object OnPermissionGranted : PermissionAction()
    object OnPermissionDenied : PermissionAction()
}
