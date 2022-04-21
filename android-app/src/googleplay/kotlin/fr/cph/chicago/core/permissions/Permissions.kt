package fr.cph.chicago.core.permissions

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
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.util.GoogleMapUtil
import timber.log.Timber

// TODO: Investigate https://google.github.io/accompanist/permissions/
@Composable
fun NearbyLocationPermissionView(
    locationViewModel: LocationViewModel,
    callBackLoadLocation:(position: Position) -> Unit,
    callBackDefaultLocation: () -> Unit,
) {
    val context = LocalContext.current
    Timber.i("requestPermission, ${locationViewModel.requestPermission}")
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
                            refreshUserLocation(
                                context = context,
                                callBackLoadLocation = callBackLoadLocation,
                                callBackDefaultLocation = callBackDefaultLocation,
                            )
                        } else {
                            callBackDefaultLocation()
                        }
                    }
                    gpsSettingTask.addOnFailureListener {
                        callBackDefaultLocation()
                    }
                }
                is PermissionAction.OnPermissionDenied -> {
                    Timber.d("Permission grant denied")
                    locationViewModel.requestPermission = false
                    callBackDefaultLocation()
                }
            }
        }
    } else {
        refreshUserLocation(
            context = context,
            callBackLoadLocation = callBackLoadLocation,
            callBackDefaultLocation = callBackDefaultLocation,
        )
    }
}

@SuppressLint("MissingPermission")
private fun refreshUserLocation(
    context: Context,
    callBackLoadLocation:(position: Position) -> Unit,
    callBackDefaultLocation: () -> Unit,
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val position = Position(location.latitude, location.longitude)
            callBackLoadLocation(position)
        } else {
            callBackDefaultLocation()
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
