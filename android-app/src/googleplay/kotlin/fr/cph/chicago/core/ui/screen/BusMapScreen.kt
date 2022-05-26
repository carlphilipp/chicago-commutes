package fr.cph.chicago.core.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.google.maps.android.compose.rememberMarkerState
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.BottomSheetStatus
import fr.cph.chicago.core.ui.common.BusBottomSheet
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.defaultSheetPeekHeight
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.CameraDebugView
import fr.cph.chicago.util.GoogleMapUtil.createBitMapDescriptor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BusMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Timber.d("Compose BusMapScreen ${Thread.currentThread().name}")
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    // Show map after 5 seconds. This is needed because there is no callback from the sdk to know if the map can be loaded or not.
    // Meaning that we can have a situation where the onMapLoaded method is never triggered, while the map view has been populated
    // with some error messages from the google sdk like: "Play store needs to be updated"
    if (!viewModel.uiState.isMapLoaded) {
        runWithDelay(5L, TimeUnit.SECONDS) {
            viewModel.setMapLoaded()
        }
    }

    if (viewModel.uiState.isMapLoaded) {
        LaunchedEffect(key1 = viewModel.uiState.isMapLoaded, block = {
            scope.launch {
                viewModel.loadPatterns()
                viewModel.loadIcons()
                viewModel.loadBuses()
            }
        })
    }

    BottomSheetScaffoldMaterial3(
        scaffoldState = viewModel.uiState.scaffoldState,
        sheetPeekHeight = defaultSheetPeekHeight,
        sheetContent = {
            // FIXME: Handle bus stops size and also DUE vs minutes
            BusBottomSheet(
                viewModel = viewModel,
                onBackClick = {
                    scope.launch {
                        if (viewModel.uiState.scaffoldState.bottomSheetState.isExpanded) {
                            viewModel.uiState.scaffoldState.bottomSheetState.collapse()
                        } else {
                            navController.navigateBack()
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHostInsets(state = snackbarHostState) },
        content = {
            Surface {
                GoogleMapBusMapView(
                    viewModel = viewModel,
                    onMapLoaded = { viewModel.setMapLoaded() },
                    cameraPositionState = viewModel.uiState.cameraPositionState,
                )

                ConstraintLayout(
                    modifier = modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                        .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp),
                ) {
                    val (left, right, cameraDebug, stateDebug) = createRefs()
                    FilledTonalButton(
                        modifier = Modifier.constrainAs(left) {
                            start.linkTo(anchor = parent.start)
                            width = Dimension.fillToConstraints
                        },
                        onClick = { navController.navigateBack() },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }

                    FilledTonalButton(
                        modifier = Modifier.constrainAs(right) {
                            end.linkTo(anchor = parent.end)
                            width = Dimension.fillToConstraints
                        },
                        onClick = {
                            viewModel.reloadData()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                        )
                    }

                    if (settingsViewModel.uiState.showMapDebug) {
                        CameraDebugView(
                            modifier = Modifier.constrainAs(cameraDebug) {
                                top.linkTo(anchor = left.bottom)
                            },
                            cameraPositionState = viewModel.uiState.cameraPositionState
                        )
                        StateDebugView(
                            modifier = Modifier.constrainAs(stateDebug) {
                                top.linkTo(anchor = cameraDebug.bottom)
                            },
                            viewModel = viewModel
                        )
                    }
                }

                LoadingBar(show = viewModel.uiState.isLoading)

                LoadingCircle(show = !viewModel.uiState.isMapLoaded)

                if (viewModel.uiState.showError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        showError = viewModel.uiState.showError,
                        onComplete = { viewModel.showError(false) }
                    )
                }
            }
        })

    DisposableEffect(key1 = viewModel) {
        onDispose { viewModel.onStop() }
    }
}

@Composable
fun GoogleMapBusMapView(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    cameraPositionState: CameraPositionState,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")
        LaunchedEffect(key1 = uiState.moveCamera, key2 = uiState.moveCameraZoom, block = {
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom))
            }
        })
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false, zoomControlsEnabled = false),
        onMapLoaded = onMapLoaded,
    ) {
        BusLineLayer(
            viewModel = viewModel,
        )

        BusStopsMarkers(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
        )

        BusOnMapLayer(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
        )
    }
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
                    val markerState = rememberMarkerState()
                    markerState.position = busStopPattern.position.toLatLng()
                    Marker(
                        state = markerState,
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
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }
    viewModel.updateIconOnZoomChange(cameraPositionState.position.zoom)

    if (viewModel.uiState.busIcon != null) {
        viewModel.uiState.buses.forEach { bus ->
            val markerState = rememberMarkerState()
            markerState.position = bus.position.toLatLng()
            Marker(
                title = "To ${bus.destination}",
                state = markerState,
                icon = viewModel.uiState.busIcon,
                rotation = bus.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                onClick = {
                    job?.cancel()
                    scope.launch {
                        viewModel.loadBusEta(scope, bus, false)
                    }
                    false
                },
                onInfoWindowClose = {
                    job = scope.launch {
                        viewModel.collapseBottomSheet(
                            scope = scope,
                            runAfter = { viewModel.resetDetails() }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun StateDebugView(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
) {
    Column(
        modifier = modifier
            .padding(top = 10.dp)
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Current bus route id: ${viewModel.uiState.busRouteId}")
        Text(text = "Buses size: ${viewModel.uiState.buses.size}")
        Text(text = "Buses arrivals size: ${viewModel.uiState.busData.size}")
        Text(text = "Buses patterns size: ${viewModel.uiState.busPatterns.size}")
        Text(text = "Bottom bar state: ${viewModel.uiState.bottomSheetStatus.name}")
        Text(text = "Current bus selected: ${viewModel.uiState.detailsBus.id}")
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class GoogleMapBusUiState constructor(
    // data
    val busRouteId: String = "",
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val showStopIcon: Boolean = false,
    val colors: List<Color> = TrainLine.values().map { it.color }.dropLast(1),
    val stopIcons: List<BitmapDescriptor> = listOf(),
    val buses: List<Bus> = listOf(),
    val busPatterns: List<BusPattern> = listOf(),
    val busData: List<BottomSheetPagerData> = listOf(),
    val detailsBus: Bus = Bus(),
    val detailsShowAll: Boolean = false,

    // map
    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,
    val isMapLoaded: Boolean = false,
    val cameraPositionState: CameraPositionState = CameraPositionState(position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)),

    // bitmap descriptor
    val busIcon: BitmapDescriptor? = null,
    val busIconSmall: BitmapDescriptor? = null,
    val busIconMedium: BitmapDescriptor? = null,
    val busIconLarge: BitmapDescriptor? = null,

    // bottom sheet
    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    ),
    val bottomSheetStatus: BottomSheetStatus = BottomSheetStatus.COLLAPSE,
)

@OptIn(ExperimentalMaterialApi::class)
class MapBusViewModel constructor(
    busRouteId: String,
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapBusUiState(busRouteId = busRouteId))
        private set

    fun setMapLoaded() {
        uiState = uiState.copy(isMapLoaded = true)
    }

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun reloadData() {
        // TODO: reload current selected bus so data at the bottom is reloaded
        uiState = uiState.copy(isLoading = true)
        loadBuses()
        if (uiState.busPatterns.isEmpty()) {
            loadPatterns()
        }
    }

    fun loadBusEta(scope: CoroutineScope, bus: Bus, showAll: Boolean) {
        uiState = uiState.copy(
            isLoading = true,
            detailsBus = bus,
            detailsShowAll = showAll,
        )
        busService.loadFollowBus(bus.id.toString())
            .observeOn(Schedulers.computation())
            .subscribe(
                { busArrivals ->
                    expandBottomSheet(
                        scope = scope,
                        runBefore = {
                            uiState = uiState.copy(
                                busData = busArrivals.map { busArrival ->
                                    BottomSheetPagerData(
                                        title = busArrival.stopName,
                                        content = busArrival.timeLeftDueDelayNoMinutes,
                                        subTitle = BusDirection.fromString(busArrival.routeDirection).shortLowerCase,
                                    )
                                },
                                isLoading = false,
                                bottomSheetStatus = BottomSheetStatus.EXPAND,
                            )
                        }
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

    private fun expandBottomSheet(
        scope: CoroutineScope,
        runBefore: () -> Unit = {},
        runAfter: () -> Unit = {},
    ) {
        viewModelScope.launch {
            runBefore()
            val job = scope.launch {
                if (uiState.scaffoldState.bottomSheetState.isCollapsed) {
                    uiState.scaffoldState.bottomSheetState.expand()
                }
            }
            job.join()
            runAfter()
        }
    }

    fun collapseBottomSheet(
        scope: CoroutineScope,
        runBefore: () -> Unit = {},
        runAfter: () -> Unit = {},
    ) {
        viewModelScope.launch {
            runBefore()
            val job = scope.launch {
                if (uiState.scaffoldState.bottomSheetState.isExpanded) {
                    uiState.scaffoldState.bottomSheetState.collapse()
                }
            }
            job.join()
            runAfter()
        }
    }

    fun resetDetails() {
        uiState = uiState.copy(
            busData = listOf(),
            bottomSheetStatus = BottomSheetStatus.COLLAPSE,
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

    fun onStop() {
        uiState = GoogleMapBusUiState(busRouteId = uiState.busRouteId)
    }
}
