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
import androidx.compose.ui.graphics.vector.ImageVector
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
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.dto.BusArrivalRouteDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.toLatLng
import fr.cph.chicago.util.MapUtil.createStop
import fr.cph.chicago.util.Util
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
        DebugView(cameraPositionState)
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
            Text(text = "Search this area")
        }
    }
}

@Composable
private fun MapStationDetailsView(showView: Boolean, title: String, image: ImageVector, arrivals: NearbyResult) {
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
    val arrivals: TreeMap<NearbyDetailsArrivals, MutableList<String>> = TreeMap<NearbyDetailsArrivals, MutableList<String>>()
) {
    companion object {
        @JvmName("toArrivalsTrain")
        fun toArrivals(trainEtas: List<TrainEta>): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            return trainEtas.fold(TreeMap<NearbyDetailsArrivals, MutableList<String>>()) { acc, cur ->
                val key = NearbyDetailsArrivals(cur.destName, cur.routeName, cur.stop.direction.toString())
                if (acc.containsKey(key)) {
                    acc[key]!!.add(cur.timeLeftDueDelay)
                } else {
                    acc[key] = mutableListOf(cur.timeLeftDueDelay)
                }
                acc
            }
        }

        @JvmName("toArrivalsBus")
        fun toArrivals(busArrivals: List<BusArrival>): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            val busArrivalRouteDTO = BusArrivalRouteDTO(BusArrivalRouteDTO.busComparator)
            busArrivals.forEach { busArrivalRouteDTO.addBusArrival(it) }

            val result = TreeMap<NearbyDetailsArrivals, MutableList<String>>()

            busArrivalRouteDTO.forEach { entry ->
                val route = Util.trimBusStopNameIfNeeded(entry.key) // FIXME
                entry.value.forEach { entryBound ->
                    val bound = entryBound.key
                    val arrivals = entryBound.value

                    val nearbyDetailsArrivals = NearbyDetailsArrivals(
                        destination = route,
                        trainLine = TrainLine.NA,
                        direction = BusDirection.fromString(bound).shortLowerCase,
                    )
                    result[nearbyDetailsArrivals] = arrivals.map { busArrival -> busArrival.timeLeftDueDelay }.toMutableList()
                }
            }
            return result
        }

        @JvmName("toArrivalsBike")
        fun toArrivals(bikeStation: BikeStation): TreeMap<NearbyDetailsArrivals, MutableList<String>> {
            val result = TreeMap<NearbyDetailsArrivals, MutableList<String>>()
            result[NearbyDetailsArrivals(
                destination = "Available bikes",
                trainLine = TrainLine.NA,
            )] = mutableListOf(bikeStation.availableBikes.toString())
            result[NearbyDetailsArrivals(
                destination = "Available docks",
                trainLine = TrainLine.NA
            )] = mutableListOf(bikeStation.availableDocks.toString())

            return result
        }
    }
}

data class NearbyDetailsArrivals(
    val destination: String,
    val trainLine: TrainLine,
    val direction: String? = null,
) : Comparable<NearbyDetailsArrivals> {
    override fun compareTo(other: NearbyDetailsArrivals): Int {
        val line = trainLine.toTextString().compareTo(other.trainLine.toTextString())
        return if (line == 0) {
            val station = destination.compareTo(other.destination)
            if (station == 0 && direction != null && other.direction != null) {
                direction.compareTo(other.direction)
            } else {
                station
            }
        } else {
            line
        }
    }
}
