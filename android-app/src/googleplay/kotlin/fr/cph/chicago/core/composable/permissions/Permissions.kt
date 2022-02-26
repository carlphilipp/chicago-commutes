package fr.cph.chicago.core.composable.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.LocationViewModel
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.GoogleMapUtil
import timber.log.Timber

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

                    val locationRequest = LocationRequest.create().apply {
                        interval = 1000
                        fastestInterval = 1000
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    }

                    val client: SettingsClient = LocationServices.getSettingsClient(context)
                    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
                        .Builder()
                        .addLocationRequest(locationRequest)

                    val gpsSettingTask: Task<LocationSettingsResponse> =
                        client.checkLocationSettings(builder.build())

                    gpsSettingTask.addOnSuccessListener { locationSettingsResponse ->
                        val settingsStates = locationSettingsResponse.locationSettingsStates
                        if (settingsStates!!.isLocationPresent && settingsStates.isLocationUsable) {
                            refreshUserLocation(context = context, mainViewModel = mainViewModel)
                        } else {
                            mainViewModel.setDefaultUserLocation()
                        }
                    }
                    gpsSettingTask.addOnFailureListener {
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
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val position = Position(location.latitude, location.longitude)
            mainViewModel.setNearbyIsMyLocationEnabled(true)
            mainViewModel.setCurrentUserLocation(position)
            mainViewModel.loadNearbyStations(position)
        } else {
            mainViewModel.setDefaultUserLocation()
        }
    }
}

@Composable
private fun PermissionLocationView(
    context: Context,
    permissionAction: (PermissionAction) -> Unit
) {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionGranted = GoogleMapUtil.checkIfPermissionGranted(context, permission)

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
