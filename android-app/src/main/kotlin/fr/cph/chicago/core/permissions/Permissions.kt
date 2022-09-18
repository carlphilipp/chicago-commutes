package fr.cph.chicago.core.permissions

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import fr.cph.chicago.core.location.getLastUserLocation
import fr.cph.chicago.core.ui.screen.NearbyViewModel
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

fun onPermissionsResult(context: Context, viewModel: NearbyViewModel): (Map<String, Boolean>) -> Unit {
    return { result ->
        val allowed =
            result.getOrElse(
                key = Manifest.permission.ACCESS_COARSE_LOCATION,
                defaultValue = { false }
            )
                &&
                result.getOrElse(
                    key = Manifest.permission.ACCESS_FINE_LOCATION,
                    defaultValue = { false }
                )
        if (allowed) {
            getLastUserLocation(
                context = context,
                callBackLoadLocation = { position ->
                    viewModel.setNearbyIsMyLocationEnabled(true)
                    viewModel.setCurrentUserLocation(position)
                    viewModel.loadNearbyStations(position)
                },
                callBackDefaultLocation = {
                    viewModel.setDefaultUserLocation()
                }
            )
        } else {
            viewModel.setDefaultUserLocation()
        }
    }

}
