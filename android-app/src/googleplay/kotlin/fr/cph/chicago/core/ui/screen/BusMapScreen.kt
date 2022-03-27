package fr.cph.chicago.core.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.GoogleMapUtil.createBitMapDescriptor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.InfoWindowsDetails
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Timber.d("Compose BusMapScreen")
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
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

    if (isMapLoaded) {
        LaunchedEffect(key1 = isMapLoaded, block = {
            scope.launch {
                viewModel.loadPatterns()
                viewModel.loadIcons()
                viewModel.loadBuses()
            }
        })
    }

    Column {
        DisplayTopBar(
            screen = Screen.BusMap,
            title = title,
            viewModel = navigationViewModel,
            onClickRightIcon = {
                viewModel.reloadData()
            }
        )

        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHostInsets(state = snackbarHostState) },
            content = {

                GoogleMapBusMapView(
                    viewModel = viewModel,
                    onMapLoaded = { isMapLoaded = true },
                )

                LoadingBar(show = viewModel.uiState.isLoading)

                LoadingCircle(show = !isMapLoaded)

                if (viewModel.uiState.showError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        showError = viewModel.uiState.showError,
                        onComplete = { viewModel.showError(false) }
                    )
                }
            }
        )
    }
}

@Composable
fun GoogleMapBusMapView(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")

        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                uiState.cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom))
            }
        })
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = uiState.cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false, zoomControlsEnabled = false),
        onMapLoaded = onMapLoaded,
    ) {
        BusLineLayer(
            viewModel = viewModel,
        )

        BusStopsMarkers(
            viewModel = viewModel,
            cameraPositionState = uiState.cameraPositionState,
        )

        BusOnMapLayer(
            viewModel = viewModel,
            cameraPositionState = uiState.cameraPositionState,
        )
    }

    //DebugView(cameraPositionState = cameraPositionState)

    InfoWindowsDetails(
        showView = viewModel.uiState.busArrivals.isNotEmpty(),
        destination = viewModel.uiState.detailsBus.destination,
        showAll = viewModel.uiState.detailsShowAll,
        results = viewModel.uiState.busArrivals.map { busArrival ->
            Pair(first = busArrival.stopName, second = busArrival.timeLeftDueDelay)
        },
        onClick = {
            viewModel.loadBusEta(viewModel.uiState.detailsBus, true)
        }
    )
}

@Composable
fun BusLineLayer(
    viewModel: MapBusViewModel,
) {
    viewModel.uiState.busPatterns.forEachIndexed { index, busPattern ->
        Polyline(
            points = busPattern.busStopsPatterns.map { it.position.toLatLng() },
            width = App.instance.lineWidthGoogleMap,
            color = viewModel.uiState.colors[index]
        )
    }
}

@Composable
fun BusStopsMarkers(
    viewModel: MapBusViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.showHideStops(cameraPositionState.position.zoom)

    if (viewModel.uiState.stopIcons.isNotEmpty()) {
        viewModel.uiState.busPatterns.forEachIndexed { index, busPattern ->
            busPattern.busStopsPatterns
                .filter { busStopPattern -> busStopPattern.type == "S" }
                .forEach { busStopPattern ->

                    Marker(
                        position = busStopPattern.position.toLatLng(),
                        icon = viewModel.uiState.stopIcons[index],
                        title = busStopPattern.stopName,
                        visible = viewModel.uiState.showStopIcon,
                        snippet = busPattern.direction,
                    )
                }
        }
    }
}

@Composable
fun BusOnMapLayer(
    viewModel: MapBusViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.updateIconOnZoomChange(cameraPositionState.position.zoom)

    if (viewModel.uiState.busIcon != null) {
        viewModel.uiState.buses.forEach { bus ->
            Marker(
                title = "To ${bus.destination}",
                position = bus.position.toLatLng(),
                icon = viewModel.uiState.busIcon,
                rotation = bus.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                onClick = {
                    viewModel.loadBusEta(bus, false)
                    false
                },
                onInfoWindowClose = {
                    viewModel.resetDetails()
                }
            )
        }
    }
}

data class GoogleMapBusUiState(
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val colors: List<Color> = TrainLine.values().map { it.color }.dropLast(1),
    val stopIcons: List<BitmapDescriptor> = listOf(),
    val cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)
    ),

    val busRouteId: String = "",

    val buses: List<Bus> = listOf(),
    val busPatterns: List<BusPattern> = listOf(),
    val busArrivals: List<BusArrival> = listOf(),

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val busIcon: BitmapDescriptor? = null,
    val busIconSmall: BitmapDescriptor? = null,
    val busIconMedium: BitmapDescriptor? = null,
    val busIconLarge: BitmapDescriptor? = null,
    val showStopIcon: Boolean = false,

    val detailsBus: Bus = Bus(),
    val detailsShowAll: Boolean = false,
)

@HiltViewModel
class MapBusViewModel @Inject constructor(
    busRouteId: String,
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapBusUiState(busRouteId = busRouteId))
        private set

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun reloadData() {
        uiState = uiState.copy(isLoading = true)
        loadBuses()
        if (uiState.busPatterns.isEmpty()) {
            loadPatterns()
        }
    }

    fun loadBusEta(bus: Bus, showAll: Boolean) {
        uiState = uiState.copy(
            isLoading = true,
            detailsBus = bus,
            detailsShowAll = showAll,
        )
        busService.loadFollowBus(bus.id.toString())
            .observeOn(Schedulers.computation())
            .subscribe(
                { busArrivals ->
                    uiState = uiState.copy(
                        busArrivals = busArrivals,
                        isLoading = false,
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Could not load bus eta")
                    uiState = uiState.copy(
                        isLoading = false,
                        showError = true,
                    )
                }
            )
    }

    fun resetDetails() {
        uiState = uiState.copy(
            busArrivals = listOf(),
        )
    }

    private fun centerMapOnBuses() {
        val position: Position
        val zoom: Float
        if (uiState.buses.size == 1) {
            position = if (uiState.buses[0].position.latitude == 0.0 && uiState.buses[0].position.longitude == 0.0)
                chicagoPosition
            else
                uiState.buses[0].position
            zoom = 15f
        } else {
            position = MapUtil.getBestPosition(uiState.buses.map { it.position })
            zoom = 11f
        }
        uiState = uiState.copy(
            moveCamera = LatLng(position.latitude, position.longitude),
            moveCameraZoom = zoom,
        )
    }

    fun resetMoveCamera() {
        uiState = uiState.copy(
            moveCamera = null,
            moveCameraZoom = null,
        )
    }

    fun loadIcons() {
        Single.fromCallable {
            val busBitmap = BitmapFactory.decodeResource(App.instance.resources, R.drawable.bus)
            val bitmapDescSmall = createBitMapDescriptor(busBitmap, 9)
            val bitmapDescMedium = createBitMapDescriptor(busBitmap, 5)
            val bitmapDescLarge = createBitMapDescriptor(busBitmap, 3)
            listOf(
                bitmapDescSmall,
                bitmapDescMedium,
                bitmapDescLarge,
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.blue_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.brown_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.green_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.orange_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.pink_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.purple_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.red_marker_no_shade)),
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.yellow_marker_no_shade)),
            )
        }
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe { result ->
                uiState = uiState.copy(
                    busIcon = result[0],
                    busIconSmall = result[0],
                    busIconMedium = result[1],
                    busIconLarge = result[2],
                    stopIcons = listOf(
                        result[3],
                        result[4],
                        result[5],
                        result[6],
                        result[7],
                        result[8],
                        result[9],
                        result[10],
                    ),
                )
            }
    }

    fun showHideStops(newZoom: Float) {
        Timber.d("showHideStations $newZoom and ${uiState.zoom}")
        if (newZoom >= 14f && !uiState.showStopIcon) {
            uiState = uiState.copy(
                showStopIcon = true
            )
        }
        if (newZoom < 14f && uiState.showStopIcon) {
            uiState = uiState.copy(
                showStopIcon = false
            )
        }
    }

    fun updateIconOnZoomChange(newZoom: Float) {
        if (newZoom != uiState.zoom) {
            val oldZoom = uiState.zoom
            if (isIn(newZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                uiState = uiState.copy(
                    busIcon = uiState.busIconSmall,
                    zoom = newZoom,
                )
            } else if (isIn(newZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                uiState = uiState.copy(
                    busIcon = uiState.busIconMedium,
                    zoom = newZoom,
                )
            } else if (isIn(newZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                uiState = uiState.copy(
                    busIcon = uiState.busIconLarge,
                    zoom = newZoom,
                )
            }
        }
    }

    fun loadPatterns() {
        Observable.fromCallable {
            val patterns: MutableList<BusPattern> = mutableListOf()
            busService.loadBusPattern(uiState.busRouteId).blockingGet().forEach { patterns.add(it) }
            patterns
        }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    uiState = uiState.copy(
                        busPatterns = result,
                        isLoading = false,
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Could not load route pattern")
                    uiState = uiState.copy(
                        isLoading = false,
                        showError = true,
                    )
                }
            )
    }

    fun loadBuses() {
        busService.busForRouteId(uiState.busRouteId)
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { buses ->
                    uiState = uiState.copy(
                        buses = buses,
                        isLoading = false,
                    )
                    if (uiState.shouldMoveCamera) {
                        centerMapOnBuses()
                        uiState = uiState.copy(shouldMoveCamera = false)
                    }
                },
                { throwable ->
                    Timber.e(throwable, "Could not load buses")
                    uiState = uiState.copy(
                        isLoading = false,
                        showError = true,
                    )
                }
            )
    }
}
