package fr.cph.chicago.core.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.core.ui.common.NearbyBottomSheet
import fr.cph.chicago.core.ui.common.NearbyResult
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.ShowLocationNotFoundSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.CameraDebugView
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.GoogleMapUtil.getBitmapDescriptor
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.mapStyle
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

// FIXME: handle zoom right after permissions has been approved or denied
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel,
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
    title: String
) {
    Timber.d("Compose NearbyScreen")
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState: CameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), GoogleMapUtil.defaultZoom)
            )
        )
    }

    LaunchedEffect(key1 = viewModel.uiState.nearbyDetailsShow, block = {
        scope.launch {
            if (viewModel.uiState.nearbyDetailsShow) {
                viewModel.uiState.scaffoldState.bottomSheetState.expand()
            }
        }
    })

    // Show map after 5 seconds. This is needed because there is no callback from the sdk to know if the map can be loaded or not.
    // Meaning that we can have a situation where the onMapLoaded method is never triggered, while the map view has been populated
    // with some error messages from the google sdk like: "Play store needs to be updated"
    if (!isMapLoaded) {
        runWithDelay(5L, TimeUnit.SECONDS) {
            isMapLoaded = true
        }
    }

    if (viewModel.uiState.findLocation) {
        NearbyLocationPermissionView(
            locationViewModel = locationViewModel,
            callBackLoadLocation = { position ->
                viewModel.setFindLocation(false)
                viewModel.setNearbyIsMyLocationEnabled(true)
                viewModel.setCurrentUserLocation(position)
                viewModel.loadNearbyStations(position)
            },
            callBackDefaultLocation = {
                viewModel.setDefaultUserLocation()
            }
        )
    }

    BottomSheetScaffoldMaterial3(
        modifier = Modifier.fillMaxWidth(),
        sheetPeekHeight = 120.dp,
        sheetContent = {
            NearbyBottomSheet(
                viewModel = viewModel,
                onBackClick = {
                    scope.launch {
                        if (viewModel.uiState.scaffoldState.bottomSheetState.isExpanded) {
                            viewModel.uiState.scaffoldState.bottomSheetState.collapse()
                        } else {
                            navController.navigateBack()
                        }
                    }
                },
                cameraPositionState = cameraPositionState,
            )
        },
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = modifier.fillMaxWidth(),
                    snackbarHost = { SnackbarHostInsets(state = viewModel.uiState.snackbarHostState) },
                    content = {
                        NearbyGoogleMapView(
                            onMapLoaded = { isMapLoaded = true },
                            viewModel = viewModel,
                            settingsViewModel = settingsViewModel,
                            cameraPositionState = cameraPositionState,
                        )

                        ConstraintLayout(
                            modifier = modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                                .padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                        ) {
                            val (left, cameraDebug, stateDebug) = createRefs()
                            FilledTonalButton(
                                modifier = Modifier.constrainAs(left) {
                                    start.linkTo(anchor = parent.start)
                                    width = Dimension.fillToConstraints
                                },
                                onClick = {
                                    scope.launch {
                                        navigationViewModel.uiState.drawerState.animateTo(androidx.compose.material3.DrawerValue.Open, TweenSpec(durationMillis = settingsViewModel.uiState.animationSpeed.openDrawerSlideDuration))
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = null,
                                )
                            }

                            if (settingsViewModel.uiState.showMapDebug) {
                                CameraDebugView(
                                    modifier = Modifier.constrainAs(cameraDebug) {
                                        top.linkTo(anchor = left.bottom)
                                    },
                                    cameraPositionState = cameraPositionState
                                )
                                StateDebugView(
                                    modifier = Modifier.constrainAs(stateDebug) {
                                        top.linkTo(anchor = cameraDebug.bottom)
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }

                        LoadingCircle(show = !isMapLoaded)

                        if (viewModel.uiState.nearbyShowLocationError) {
                            ShowLocationNotFoundSnackBar(
                                scope = scope,
                                snackbarHostState = viewModel.uiState.snackbarHostState,
                                showErrorMessage = viewModel.uiState.nearbyShowLocationError,
                                onComplete = { viewModel.setShowLocationError(false) }
                            )
                        }
                        if (viewModel.uiState.nearbyDetailsError) {
                            ShowErrorMessageSnackBar(
                                scope = scope,
                                snackbarHostState = viewModel.uiState.snackbarHostState,
                                showError = viewModel.uiState.nearbyDetailsError,
                                onComplete = { viewModel.setNearbyDetailsError(false) }
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun NearbyGoogleMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    viewModel: NearbyViewModel,
    settingsViewModel: SettingsViewModel,
    cameraPositionState: CameraPositionState,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var job: Job? by remember { mutableStateOf(null) }

    Timber.d("Set location: ${uiState.nearbyMapCenterLocation.toLatLng()}")

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = uiState.nearbyIsMyLocationEnabled,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, mapStyle(settingsViewModel)),
        ),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = uiState.nearbyIsMyLocationEnabled),
        onMapLoaded = {
            onMapLoaded()
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
            cameraPositionState.move(cameraUpdate)
        },
        onMapClick = {
            viewModel.setShowNearbyDetails(false)
        }
    ) {
        val bitmapDescriptorTrain = getBitmapDescriptor(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = getBitmapDescriptor(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = getBitmapDescriptor(context, R.drawable.bike_station_icon)

        uiState.nearbyTrainStations.forEach { trainStation ->
            val markerState = rememberMarkerState()
            markerState.position = trainStation.stops[0].position.toLatLng()
            Marker(
                state = markerState,
                title = trainStation.name,
                icon = bitmapDescriptorTrain,
                onClick = {
                    job?.cancel()
                    viewModel.loadNearbyTrainDetails(trainStation = trainStation)
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

        uiState.nearbyBusStops.forEach { busStop ->
            val markerState = rememberMarkerState()
            markerState.position = busStop.position.toLatLng()
            Marker(
                state = markerState,
                title = busStop.name,
                icon = bitmapDescriptorBus,
                onClick = {
                    job?.cancel()
                    viewModel.loadNearbyBusDetails(busStop = busStop)
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

        uiState.nearbyBikeStations.forEach { bikeStation ->
            val markerState = rememberMarkerState()
            markerState.position = LatLng(bikeStation.latitude, bikeStation.longitude)
            Marker(
                state = markerState,
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
                onClick = {
                    job?.cancel()
                    viewModel.loadNearbyBikeDetails(currentBikeStation = bikeStation)
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

// FIXME: To delete, keeping that for now until bottom sheet is done
@Composable
fun MapStationDetailsView(showView: Boolean, title: String, image: ImageVector, arrivals: NearbyResult) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 200.dp)
    ) {
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
                            color = entry.key.trainLine.color,
                            direction = entry.key.direction
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StateDebugView(
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel,
) {
    Column(
        modifier = modifier
            .padding(top = 10.dp)
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Latitude in state ${viewModel.uiState.nearbyMapCenterLocation.latitude}")
        Text(text = "Longitude in state ${viewModel.uiState.nearbyMapCenterLocation.longitude}")
        Text(text = "Train stations: ${viewModel.uiState.nearbyTrainStations.size}")
        Text(text = "Bus stops: ${viewModel.uiState.nearbyBusStops.size}")
        Text(text = "Bike stations: ${viewModel.uiState.nearbyBikeStations.size}")
    }
}

data class BottomSheetData(
    val bottomSheetState: BottomSheetDataState = BottomSheetDataState.HIDDEN,
    val trainArrivals: List<BottomSheetPagerData> = listOf(),
    val busArrivals: List<BottomSheetPagerData> = listOf(),
    val bikeStation: BikeStation = BikeStation.buildUnknownStation(),
)

enum class BottomSheetDataState {
    HIDDEN, TRAIN, BUS, BIKE
}

@OptIn(ExperimentalMaterialApi::class)
data class NearbyScreenUiState constructor(
    val findLocation: Boolean = true,
    val nearbyMapCenterLocation: Position = chicagoPosition,
    val nearbyTrainStations: List<TrainStation> = listOf(),
    val nearbyBusStops: List<BusStop> = listOf(),
    val nearbyBikeStations: List<BikeStation> = listOf(),
    val nearbyZoomIn: Float = 8f,
    val nearbyIsMyLocationEnabled: Boolean = false,
    val nearbyShowLocationError: Boolean = false,
    val nearbyDetailsShow: Boolean = false,
    val nearbyDetailsTitle: String = "",
    val nearbyDetailsIcon: ImageVector = Icons.Filled.Train,
    val bottomSheetData: BottomSheetData = BottomSheetData(),

    val nearbyDetailsError: Boolean = false,

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    )
)

@OptIn(ExperimentalMaterialApi::class)
class NearbyViewModel(
    private val trainService: TrainService = TrainService,
    private val busService: BusService = BusService,
    private val bikeService: BikeService = BikeService,
    private val mapUtil: MapUtil = MapUtil,
) : ViewModel() {
    var uiState by mutableStateOf(NearbyScreenUiState())
        private set

    fun setFindLocation(value: Boolean) {
        uiState = uiState.copy(findLocation = value)
    }

    fun setShowLocationError(value: Boolean) {
        uiState = uiState.copy(nearbyShowLocationError = value)
    }

    fun setNearbyDetailsError(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsError = value)
    }

    fun setShowNearbyDetails(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsShow = value)
    }

    fun setNearbyIsMyLocationEnabled(value: Boolean) {
        uiState = uiState.copy(nearbyIsMyLocationEnabled = value)
    }

    fun setDefaultUserLocation() {
        setFindLocation(false)
        setCurrentUserLocation(chicagoPosition)
        loadNearbyStations(chicagoPosition)
        setShowLocationError(true)
    }

    fun setMapCenterLocationAndLoadNearby(position: Position, zoom: Float) {
        setCurrentUserLocation(position, zoom)
        loadNearbyStations(position)
        setShowLocationError(false)
    }

    fun resetDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(
                nearbyDetailsShow = false,
                nearbyDetailsTitle = "",
                bottomSheetData = uiState.bottomSheetData.copy(
                    bottomSheetState = BottomSheetDataState.HIDDEN,
                    trainArrivals = listOf(),
                    busArrivals = listOf(),
                    bikeStation = BikeStation.buildUnknownStation(),
                )
            )
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

    fun setCurrentUserLocation(position: Position, zoom: Float = 16f) {
        uiState = uiState.copy(
            nearbyMapCenterLocation = position,
            nearbyZoomIn = zoom,
        )
    }

    fun loadNearbyStations(position: Position) {
        val trainStationAround = trainService.readNearbyStation(position = position)
        val busStopsAround = busService.busStopsAround(position = position)
        val bikeStationsAround = mapUtil.readNearbyStation(position = position, store.state.bikeStations)
        Single.zip(trainStationAround, busStopsAround, bikeStationsAround) { trains, buses, bikeStations ->
            uiState = uiState.copy(
                nearbyTrainStations = trains,
                nearbyBusStops = buses,
                nearbyBikeStations = bikeStations,
            )
            Any()
        }.subscribe({}, { error -> Timber.e(error) })
    }

    fun loadNearbyTrainDetails(trainStation: TrainStation) {
        trainService.loadStationTrainArrival(trainStation.id)
            .map { trainArrival ->
                trainArrival.trainEtas.filter { trainEta -> trainEta.trainStation.id == trainStation.id }
            }
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = trainStation.name,
                        bottomSheetData = uiState.bottomSheetData.copy(
                            bottomSheetState = BottomSheetDataState.TRAIN,
                            trainArrivals = it.map { trainEta ->
                                BottomSheetPagerData(
                                    title = trainEta.destName,
                                    content = trainEta.timeLeftDueDelayNoMinutes,
                                    subTitle = trainEta.stop.direction.toString(),
                                    backgroundColor = trainEta.routeName.color,
                                )
                            },
                        ),
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.Train,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading train arrivals")
                    setNearbyDetailsError(true)
                })
    }

    fun loadNearbyBusDetails(busStop: BusStop) {
        busService.loadBusArrivals(busStop)
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = busStop.name,
                        bottomSheetData = uiState.bottomSheetData.copy(
                            bottomSheetState = BottomSheetDataState.BUS,
                            busArrivals = it.map { busArrival ->
                                BottomSheetPagerData(
                                    title = busArrival.busDestination,
                                    content = busArrival.timeLeftDueDelayNoMinutes,
                                    subTitle = BusDirection.fromString(busArrival.routeDirection).shortLowerCase,
                                )
                            },
                        ),
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBus,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading bus arrivals")
                    setNearbyDetailsError(true)
                })
    }

    fun loadNearbyBikeDetails(currentBikeStation: BikeStation) {
        bikeService.findBikeStation(currentBikeStation.id)
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = currentBikeStation.name,
                        bottomSheetData = uiState.bottomSheetData.copy(
                            bottomSheetState = BottomSheetDataState.BIKE,
                            bikeStation = it,
                        ),
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBike,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading bus arrivals")
                    setNearbyDetailsError(true)
                })
    }
}
