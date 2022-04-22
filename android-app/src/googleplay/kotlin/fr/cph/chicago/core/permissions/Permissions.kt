package fr.cph.chicago.core.permissions

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NearbyLocationPermissionView(
    onPermissionsResult: (Map<String, Boolean>) -> Unit
) {
    Timber.d("Compose NearbyLocationPermissionView ${Thread.currentThread().name}")
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        onPermissionsResult = onPermissionsResult
    )

    if (locationPermissionsState.allPermissionsGranted) {
        Timber.d("Permission grant successful")
        onPermissionsResult(
            mapOf(
                Manifest.permission.ACCESS_COARSE_LOCATION to true,
                Manifest.permission.ACCESS_FINE_LOCATION to true,
            )
        )
    } else {
        SideEffect {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}
