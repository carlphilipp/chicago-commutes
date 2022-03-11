package fr.cph.chicago.core.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.core.ui.common.NearbyResult
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.ShowLocationNotFoundSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.util.GoogleMapUtil.getBitmapDescriptor
import fr.cph.chicago.util.toLatLng
import java.util.concurrent.TimeUnit
import timber.log.Timber

// FIXME: handle zoom right after permissions has been approved or denied
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    title: String
) {
    val scope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }
    // Show map after 5 seconds. This is needed because there is no callback from the sdk to know if the map can be loaded or not.
    // Meaning that we can have a situation where the onMapLoaded method is never triggered, while the map view has been populated
    // with some error messages from the google sdk like: "Play store needs to be updated"
    if (!isMapLoaded) {
        runWithDelay(5L, TimeUnit.SECONDS) {
            isMapLoaded = true
        }
    }

    NearbyLocationPermissionView(
        mainViewModel = mainViewModel,
        locationViewModel = locationViewModel,
    )

    Column {
        DisplayTopBar(
            title = title,
            viewModel = navigationViewModel,
        )

        Scaffold(
            modifier = modifier.fillMaxWidth(),
            snackbarHost = { SnackbarHostInsets(state = mainViewModel.uiState.snackbarHostState) },
            content = {
                NearbyGoogleMapView(
                    onMapLoaded = { isMapLoaded = true },
                    mainViewModel = mainViewModel,
                )

                LoadingCircle(show = !isMapLoaded)

                if (mainViewModel.uiState.nearbyShowLocationError) {

                    ShowLocationNotFoundSnackBar(
                        scope = scope,
                        snackbarHostState = mainViewModel.uiState.snackbarHostState,
                        showErrorMessage = mainViewModel.uiState.nearbyShowLocationError,
                        onComplete = { mainViewModel.setShowLocationError(false) }
                    )
                }
                if (mainViewModel.uiState.nearbyDetailsError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = mainViewModel.uiState.snackbarHostState,
                        showError = mainViewModel.uiState.nearbyDetailsError,
                        onComplete = { mainViewModel.setNearbyDetailsError(false) }
                    )
                }
            }
        )
    }
}

@Composable
fun NearbyGoogleMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    mainViewModel: MainViewModel,
) {
    val uiState = mainViewModel.uiState
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
    }
    //cameraPositionState.position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)

    Timber.d("Set location: ${uiState.nearbyMapCenterLocation.toLatLng()}")

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = uiState.nearbyIsMyLocationEnabled),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = uiState.nearbyIsMyLocationEnabled),
        onMapLoaded = {
            onMapLoaded()
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
            cameraPositionState.move(cameraUpdate)
        },
        onMapClick = {
            mainViewModel.setShowNearbyDetails(false)
        }
    ) {
        val bitmapDescriptorTrain = getBitmapDescriptor(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = getBitmapDescriptor(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = getBitmapDescriptor(context, R.drawable.bike_station_icon)

        uiState.nearbyTrainStations.forEach { trainStation ->
            Marker(
                position = trainStation.stops[0].position.toLatLng(),
                title = trainStation.name,
                icon = bitmapDescriptorTrain,
                onClick = {
                    mainViewModel.loadNearbyTrainDetails(trainStation = trainStation)
                    false
                }
            )
        }

        uiState.nearbyBusStops.forEach { busStop ->
            Marker(
                position = busStop.position.toLatLng(),
                title = busStop.name,
                icon = bitmapDescriptorBus,
                onClick = {
                    mainViewModel.loadNearbyBusDetails(busStop = busStop)
                    false
                }
            )
        }

        uiState.nearbyBikeStations.forEach { bikeStation ->
            Marker(
                position = LatLng(bikeStation.latitude, bikeStation.longitude),
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
                onClick = {
                    mainViewModel.loadNearbyBikeDetails(currentBikeStation = bikeStation)
                    false
                }
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SearchThisAreaButton(mainViewModel = mainViewModel, cameraPositionState = cameraPositionState)
        //DebugView(cameraPositionState)
    }

    MapStationDetailsView(
        showView = mainViewModel.uiState.nearbyDetailsShow,
        title = mainViewModel.uiState.nearbyDetailsTitle,
        image = mainViewModel.uiState.nearbyDetailsIcon,
        arrivals = mainViewModel.uiState.nearbyDetailsArrivals,
    )

}

@Composable
private fun SearchThisAreaButton(mainViewModel: MainViewModel, cameraPositionState: CameraPositionState) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
    ) {
        ElevatedButton(onClick = {
            mainViewModel.setMapCenterLocationAndLoadNearby(
                position = Position(latitude = cameraPositionState.position.target.latitude, longitude = cameraPositionState.position.target.longitude),
                zoom = cameraPositionState.position.zoom
            )
        }) {
            Text(text = stringResource(id = R.string.search_area))
        }
    }
}

@Composable
fun MapStationDetailsView(showView: Boolean, title: String, image: ImageVector, arrivals: NearbyResult) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 50.dp, end = 50.dp, bottom = 50.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            AnimatedVisibility(
                visible = showView,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)) {
                    HeaderCard(name = title, image = image, lastUpdate = arrivals.lastUpdate)
                    arrivals.arrivals.forEach { entry ->
                        Arrivals(
                            destination = entry.key.destination,
                            arrivals = entry.value,
                            trainLine = entry.key.trainLine,
                            direction = entry.key.direction
                        )
                    }
                }
            }
        }
    }
}
