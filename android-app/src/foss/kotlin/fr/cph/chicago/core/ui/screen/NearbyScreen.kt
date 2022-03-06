package fr.cph.chicago.core.ui.screen

import android.preference.PreferenceManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import fr.cph.chicago.R
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.ui.common.*
import fr.cph.chicago.core.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import timber.log.Timber
import java.util.concurrent.TimeUnit

// FIXME: handle zoom right after permissions has been approved or denied
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel,
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

    Scaffold(
        modifier = modifier.fillMaxWidth(),
        snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            NearbyOsmdroidMapView(
                onMapLoaded = { isMapLoaded = true },
                mainViewModel = mainViewModel
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

@Composable
fun NearbyOsmdroidMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    mainViewModel: MainViewModel,
) {
    val uiState = mainViewModel.uiState
    val context = LocalContext.current

    /*
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
    }*/
    //cameraPositionState.position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)

    Timber.d("Set location: ${uiState.nearbyMapCenterLocation}")

    Configuration.getInstance().load(
        context.applicationContext,
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    )

    AndroidView(factory = ::MapView, modifier = modifier) {
        it.setTileSource(TileSourceFactory.MAPNIK)
        it.isTilesScaledToDpi = true

        it.setMultiTouchControls(true)
        RotationGestureOverlay(it).apply {
            isEnabled = true
            it.overlays.add(this)
        }

        it.controller.setZoom(17.0)
        val center = mainViewModel.uiState.nearbyMapCenterLocation
        it.controller.setCenter(GeoPoint(center.latitude, center.longitude))

        it.setOnClickListener { // TODO: check that it behaves as map click
            mainViewModel.setShowNearbyDetails(false)
        }

        uiState.nearbyTrainStations.forEach { trainStation ->
            val trainPosition = trainStation.stops[0].position
            Marker(it).apply {
                position = GeoPoint(trainPosition.latitude, trainPosition.longitude)
                title = trainStation.name
                setDefaultIcon() // TODO: icon = bitmapDescriptorTrain
                setOnMarkerClickListener { _, _ ->
                    mainViewModel.loadNearbyTrainDetails(trainStation = trainStation)
                    false
                }
                it.overlays.add(this)
            }
        }

        uiState.nearbyBusStops.forEach { busStop ->
            val busPosition = busStop.position
            Marker(it).apply {
                position = GeoPoint(busPosition.latitude, busPosition.longitude)
                title = busStop.name
                setDefaultIcon() // TODO: icon = bitmapDescriptorBus
                setOnMarkerClickListener { _, _ ->
                    mainViewModel.loadNearbyBusDetails(busStop = busStop)
                    false
                }
                it.overlays.add(this)
            }
        }

        uiState.nearbyBikeStations.forEach { bikeStation ->
            Marker(it).apply {
                position = GeoPoint(bikeStation.latitude, bikeStation.longitude)
                title = bikeStation.name
                setDefaultIcon() // TODO: icon = bitmapDescriptorBike
                setOnMarkerClickListener { _, _ ->
                    mainViewModel.loadNearbyBikeDetails(currentBikeStation = bikeStation)
                    false
                }
                it.overlays.add(this)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        //val position = Position(mapView.mapCenter.latitude, mapView.mapCenter.longitude)
        //SearchThisAreaButton(mainViewModel = mainViewModel, position, mapView.zoomLevelDouble.toFloat())
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
private fun SearchThisAreaButton(mainViewModel: MainViewModel, position: Position, zoom: Float) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
    ) {
        ElevatedButton(onClick = {
            mainViewModel.setMapCenterLocationAndLoadNearby(
                position = position,
                zoom = zoom
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

