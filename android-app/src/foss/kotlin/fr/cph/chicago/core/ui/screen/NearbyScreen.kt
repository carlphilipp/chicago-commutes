package fr.cph.chicago.core.ui.screen

import android.preference.PreferenceManager
import androidx.appcompat.content.res.AppCompatResources
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
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.ui.common.*
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.core.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import timber.log.Timber
import java.util.concurrent.TimeUnit

// FIXME: handle zoom right after permissions has been approved or denied
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    title: String,
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
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
            screen = Screen.Nearby,
            title = title,
            viewModel = navigationViewModel,
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

        it.controller.setZoom(mainViewModel.uiState.nearbyZoomIn.toDouble())
        val center = mainViewModel.uiState.nearbyMapCenterLocation
        it.controller.setCenter(GeoPoint(center.latitude, center.longitude))

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                // TODO: remove this workaround after making sure the app view is not
                // recomposed at each station/stop marker click.
                updateMapCenter(mainViewModel, it)
                mainViewModel.setShowNearbyDetails(false)

                return false
            }

            override fun longPressHelper(point: GeoPoint): Boolean {
                return false
            }

        }
        it.overlays.add(0, MapEventsOverlay(mapEventsReceiver))

        it.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                val point = it.projection.fromPixels(event.x, event.y)
                /*mainViewModel.setCurrentUserLocation(
                    Position(point.latitude, point.longitude),
                    it.zoomLevelDouble.toFloat()
                )*/

                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                /*mainViewModel.setCurrentUserLocation(
                    Position(it.mapCenter.latitude, it.mapCenter.longitude),
                    event.zoomLevel.toFloat()
                )*/

                return false
            }
        })

        uiState.nearbyTrainStations.forEach { trainStation ->
            val trainPosition = trainStation.stops[0].position
            Marker(it).apply {
                position = GeoPoint(trainPosition.latitude, trainPosition.longitude)
                title = trainStation.name
                icon = AppCompatResources.getDrawable(context, R.drawable.train_station_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(mainViewModel, it)
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
                icon = AppCompatResources.getDrawable(context, R.drawable.bus_stop_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(mainViewModel, it)
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
                icon = AppCompatResources.getDrawable(context, R.drawable.bike_station_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(mainViewModel, it)
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
        SearchThisAreaButton(mainViewModel = mainViewModel)
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
private fun SearchThisAreaButton(mainViewModel: MainViewModel) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
    ) {
        ElevatedButton(onClick = {
            mainViewModel.loadNearby()
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
                            direction = entry.key.direction
                        )
                    }
                }
            }
        }
    }
}

fun updateMapCenter(mainViewModel: MainViewModel, mapView: MapView) {
    mainViewModel.setCurrentUserLocation(
        Position(mapView.mapCenter.latitude, mapView.mapCenter.longitude),
        mapView.zoomLevelDouble.toFloat()
    )
}

