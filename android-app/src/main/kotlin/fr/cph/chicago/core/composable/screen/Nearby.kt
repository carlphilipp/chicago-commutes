package fr.cph.chicago.core.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.toLatLng
import fr.cph.chicago.util.MapUtil.createStop
import java.util.TreeMap

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
                onMapLoaded = { isMapLoaded = true },
                mainViewModel = mainViewModel,
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
    mainViewModel: MainViewModel,
) {
    val uiState = mainViewModel.uiState
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
    }
    cameraPositionState.position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)

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
        val bitmapDescriptorTrain = createStop(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = createStop(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = createStop(context, R.drawable.bike_station_icon)

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
            )
        }

        uiState.nearbyBikeStations.forEach { bikeStation ->
            Marker(
                position = LatLng(bikeStation.latitude, bikeStation.longitude),
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
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
        DebugView(cameraPositionState)
    }

    MapStationDetailsView(
        showView = mainViewModel.uiState.nearbyDetailsShow,
        title = mainViewModel.uiState.nearbyDetailsTitle,
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
            Text(text = "Search this area")
        }
    }
}

@Composable
private fun MapStationDetailsView(showView: Boolean, title: String, arrivals: NearbyResult) {
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
                Column(modifier = Modifier.padding(10.dp)) {
                    HeaderCard(name = title, image = Icons.Filled.Train, lastUpdate = arrivals.lastUpdate)
                    arrivals.arrivals.forEach { entry ->
                        Arrivals(
                            destination = entry.key.stationName,
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

@Composable
private fun DebugView(cameraPositionState: CameraPositionState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        val moving = if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
    }
}

class LocationViewModel : ViewModel() {
    var requestPermission: Boolean = true
}

class NearbyResult(
    val lastUpdate: LastUpdate = LastUpdate("now"),
    trainEtas: List<TrainEta> = listOf(),
) {
    val arrivals = trainEtas.fold(TreeMap<NearbyDetailsArrivals, MutableList<String>>()) { acc, cur ->
        val key = NearbyDetailsArrivals(cur.destName, cur.routeName, cur.stop.direction.toString())
        if (acc.containsKey(key)) {
            acc[key]!!.add(cur.timeLeftDueDelay)
        } else {
            acc[key] = mutableListOf(cur.timeLeftDueDelay)
        }
        acc
    }
}

data class NearbyDetailsArrivals(
    val stationName: String,
    val trainLine: TrainLine,
    val direction: String,
) : Comparable<NearbyDetailsArrivals> {
    override fun compareTo(other: NearbyDetailsArrivals): Int {
        val line = trainLine.toTextString().compareTo(other.trainLine.toTextString())
        return if (line == 0) {
            val station = stationName.compareTo(other.stationName)
            if (station == 0) {
                direction.compareTo(other.direction)
            } else {
                station
            }
        } else {
            line
        }
    }
}
