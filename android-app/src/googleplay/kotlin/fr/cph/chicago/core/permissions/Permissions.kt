package fr.cph.chicago.core.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.util.GoogleMapUtil
import timber.log.Timber

@Composable
fun NearbyLocationPermissionView(
    locationViewModel: LocationViewModel,
) {
    if (locationViewModel.requestPermission) {
        locationViewModel.requestPermission = false
        PermissionLocationView { permissionAction ->
            when (permissionAction) {
                is PermissionAction.OnPermissionGranted -> {
                    Timber.d("Permission grant successful")
                    locationViewModel.isAllowed = true
                }
                is PermissionAction.OnPermissionDenied -> {
                    Timber.d("Permission grant denied")
                    locationViewModel.isAllowed = false
                }
            }
        }
    }
}

@Composable
private fun PermissionLocationView(
    permissionAction: (PermissionAction) -> Unit
) {
    val context = LocalContext.current
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
