package fr.cph.chicago.core.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.ShowLocationNotFoundSnackBar
import fr.cph.chicago.core.composable.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.toLatLng
import fr.cph.chicago.util.MapUtil.createStop
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nearby(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel,
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    NearbyLocationPermissionView(
        mainViewModel = mainViewModel,
        locationViewModel = locationViewModel,
    )

    Scaffold(
        modifier = modifier.fillMaxWidth(),
        snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            GoogleMapView(
                onMapLoaded = {
                    isMapLoaded = true
                },
                trainStations = mainViewModel.uiState.nearbyTrainStations,
                busStops = mainViewModel.uiState.nearbyBusStops,
                bikeStations = mainViewModel.uiState.nearbyBikeStations,
                mapPosition = mainViewModel.uiState.nearbyUserCurrentLocation,
                zoomIn = mainViewModel.uiState.nearbyZoomIn,
                isMyLocationEnabled = mainViewModel.uiState.nearbyIsMyLocationEnabled,
            )
            if (!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = !isMapLoaded,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
            if (mainViewModel.uiState.nearbyShowLocationError) {
                mainViewModel.setShowLocationError(false)
                ShowLocationNotFoundSnackBar(
                    scope = scope,
                    snackbarHostState = mainViewModel.uiState.snackbarHostState,
                    showErrorMessage = mainViewModel.uiState.nearbyShowLocationError
                )
            }
        }
    )
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    trainStations: List<TrainStation>,
    busStops: List<BusStop>,
    bikeStations: List<BikeStation>,
    mapPosition: Position,
    zoomIn: Float,
    isMyLocationEnabled: Boolean,
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition.toLatLng(), zoomIn)
    }
    cameraPositionState.position = CameraPosition.fromLatLngZoom(mapPosition.toLatLng(), zoomIn)

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = isMyLocationEnabled),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = isMyLocationEnabled),
        onMapLoaded = {
            onMapLoaded()
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapPosition.toLatLng(), zoomIn)
            cameraPositionState.move(cameraUpdate)
        },
    ) {
        val bitmapDescriptorTrain = createStop(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = createStop(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = createStop(context, R.drawable.bike_station_icon)

        trainStations.forEach { trainStation ->
            Marker(
                position = trainStation.stops[0].position.toLatLng(),
                title = trainStation.name,
                icon = bitmapDescriptorTrain,
            )
        }

        busStops.forEach { busStop ->
            Marker(
                position = busStop.position.toLatLng(),
                title = busStop.name,
                icon = bitmapDescriptorBus,
            )
        }

        bikeStations.forEach { bikeStation ->
            Marker(
                position = LatLng(bikeStation.latitude, bikeStation.longitude),
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
            )
        }
    }
    Column {
        DebugView(cameraPositionState)
    }
}

@Composable
private fun DebugView(cameraPositionState: CameraPositionState) {
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        val moving =
            if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
    }
}

class LocationViewModel : ViewModel() {
    var requestPermission: Boolean = true
}
