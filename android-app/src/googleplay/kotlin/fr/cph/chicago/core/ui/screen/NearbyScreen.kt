package fr.cph.chicago.core.ui.screen

import android.Manifest
import android.os.Bundle
import androidx.compose.animation.core.TweenSpec
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
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
import fr.cph.chicago.core.location.getLastUserLocation
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.ui.common.BottomSheetData
import fr.cph.chicago.core.ui.common.BottomSheetDataState
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.NearbyBottomSheet
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
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.getBitmapDescriptor
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.mapStyle
import fr.cph.chicago.util.toLatLng
import fr.cph.chicago.util.toPosition
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Timber.d("Compose NearbyScreen ${Thread.currentThread().name}")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val navController = LocalNavController.current
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState: CameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)
            )
        )
    }
    val onPermissionsResult: (Map<String, Boolean>) -> Unit by remember {
        mutableStateOf(
            { result ->
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
        )
    }

    // Show map after 5 seconds. This is needed because there is no callback from the sdk to know if the map can be loaded or not.
    // Meaning that we can have a situation where the onMapLoaded method is never triggered, while the map view has been populated
    // with some error messages from the google sdk like: "Play store needs to be updated"
    if (!isMapLoaded) {
        runWithDelay(5L, TimeUnit.SECONDS) {
            isMapLoaded = true
        }
    }

    if (viewModel.uiState.moveCamera != null && viewModel.uiState.moveCameraZoom != null && isMapLoaded) {
        LaunchedEffect(key1 = viewModel.uiState.moveCamera, key2 = viewModel.uiState.moveCameraZoom, block = {
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(viewModel.uiState.moveCamera!!.toLatLng(), viewModel.uiState.moveCameraZoom!!))
            }
        })
    }

    NearbyLocationPermissionView(onPermissionsResult = onPermissionsResult)

    BottomSheetScaffoldMaterial3(
        scaffoldState = viewModel.uiState.scaffoldState,
        sheetPeekHeight = 0.dp,
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
            )
        },
        snackbarHost = { SnackbarHostInsets(state = snackbarHostState) },
        content = {
            Column {
                Scaffold(
                    modifier = modifier,
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
                                .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp),
                        ) {
                            val (left, right, cameraDebug, stateDebug) = createRefs()
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

                            FilledTonalButton(
                                modifier = Modifier.constrainAs(right) {
                                    end.linkTo(anchor = parent.end)
                                    width = Dimension.fillToConstraints
                                },
                                onClick = {
                                    scope.launch {
                                        viewModel.loadNearby(position = cameraPositionState.position.target.toPosition())
                                        when (viewModel.uiState.bottomSheetData.bottomSheetState) {
                                            BottomSheetDataState.TRAIN -> viewModel.loadNearbyTrainDetails(trainStation = viewModel.uiState.bottomSheetData.trainStation)
                                            BottomSheetDataState.BUS -> viewModel.loadNearbyBusDetails(busStop = viewModel.uiState.bottomSheetData.busStop)
                                            BottomSheetDataState.BIKE -> viewModel.loadNearbyBikeDetails(currentBikeStation = viewModel.uiState.bottomSheetData.bikeStation)
                                            else -> {}
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
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

                        if (viewModel.uiState.showLocationError) {
                            ShowLocationNotFoundSnackBar(
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                showErrorMessage = viewModel.uiState.showLocationError,
                                onComplete = { viewModel.setShowLocationError(false) }
                            )
                        }
                        if (viewModel.uiState.nearbyDetailsError) {
                            ShowErrorMessageSnackBar(
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                showError = viewModel.uiState.nearbyDetailsError,
                                onComplete = { viewModel.setNearbyDetailsError(false) }
                            )
                        }
                    }
                )
            }
        }
    )

    DisposableEffect(key1 = viewModel) {
        onDispose { viewModel.onStop() }
    }
}

@Composable
fun NearbyGoogleMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    viewModel: NearbyViewModel,
    settingsViewModel: SettingsViewModel,
    cameraPositionState: CameraPositionState,
) {
    Timber.d("Compose NearbyGoogleMapView with location ${viewModel.uiState.moveCamera?.toLatLng()}")
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var jobOnClose: Job? by remember { mutableStateOf(null) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = uiState.isMyLocationEnabled,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, mapStyle(settingsViewModel)),
        ),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = uiState.isMyLocationEnabled),
        onMapLoaded = onMapLoaded,
    ) {
        // FIXME: load that in a different scope
        val bitmapDescriptorTrain = getBitmapDescriptor(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = getBitmapDescriptor(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = getBitmapDescriptor(context, R.drawable.bike_station_icon)

        uiState.trainStations.forEach { trainStation ->
            val markerState = rememberMarkerState()
            markerState.position = trainStation.stops[0].position.toLatLng()
            Marker(
                state = markerState,
                title = trainStation.name,
                icon = bitmapDescriptorTrain,
                onClick = {
                    jobOnClose?.cancel()
                    viewModel.loadNearbyTrainDetails(trainStation = trainStation)
                    false
                },
                onInfoWindowClose = {
                    jobOnClose = scope.launch {
                        viewModel.resetDetails()
                    }
                }
            )
        }

        uiState.busStops.forEach { busStop ->
            val markerState = rememberMarkerState()
            markerState.position = busStop.position.toLatLng()
            Marker(
                state = markerState,
                title = busStop.name,
                icon = bitmapDescriptorBus,
                onClick = {
                    jobOnClose?.cancel()
                    viewModel.loadNearbyBusDetails(busStop = busStop)
                    false
                },
                onInfoWindowClose = {
                    jobOnClose = scope.launch {
                        viewModel.resetDetails()
                    }
                }
            )
        }

        uiState.bikeStations.forEach { bikeStation ->
            val markerState = rememberMarkerState()
            markerState.position = LatLng(bikeStation.latitude, bikeStation.longitude)
            Marker(
                state = markerState,
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
                onClick = {
                    jobOnClose?.cancel()
                    viewModel.loadNearbyBikeDetails(currentBikeStation = bikeStation)
                    false
                },
                onInfoWindowClose = {
                    jobOnClose = scope.launch {
                        viewModel.resetDetails()
                    }
                }
            )
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
        Text(text = "Latitude in state ${viewModel.uiState.moveCamera?.latitude}")
        Text(text = "Longitude in state ${viewModel.uiState.moveCamera?.longitude}")
        Text(text = "Zoom in state ${viewModel.uiState.moveCameraZoom}")
        Text(text = "Train stations: ${viewModel.uiState.trainStations.size}")
        Text(text = "Bus stops: ${viewModel.uiState.busStops.size}")
        Text(text = "Bike stations: ${viewModel.uiState.bikeStations.size}")
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class NearbyScreenUiState constructor(
    val trainStations: List<TrainStation> = listOf(),
    val busStops: List<BusStop> = listOf(),
    val bikeStations: List<BikeStation> = listOf(),
    val moveCamera: Position? = null,
    val moveCameraZoom: Float? = null,
    val isMyLocationEnabled: Boolean = false,
    val showLocationError: Boolean = false,
    val bottomSheetData: BottomSheetData = BottomSheetData(),
    val nearbyDetailsError: Boolean = false,
    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded),
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

    fun onStop() {
        uiState = NearbyScreenUiState()
    }

    fun setShowLocationError(value: Boolean) {
        uiState = uiState.copy(showLocationError = value)
    }

    fun setNearbyDetailsError(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsError = value)
    }

    fun setNearbyIsMyLocationEnabled(value: Boolean) {
        uiState = uiState.copy(isMyLocationEnabled = value)
    }

    fun loadNearby(position: Position) {
        viewModelScope.launch {
            loadNearbyStations(position)
            setShowLocationError(false)
        }
    }

    fun resetDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(
                bottomSheetData = uiState.bottomSheetData.copy(
                    title = "",
                    bottomSheetState = BottomSheetDataState.HIDDEN,
                    bikeStation = BikeStation.buildUnknownStation(),
                )
            )
        }
    }

    fun setCurrentUserLocation(position: Position, zoom: Float = 16f) {
        Timber.d("Set position $position and zoom $zoom")
        uiState = uiState.copy(
            moveCamera = position,
            moveCameraZoom = zoom,
        )
    }

    fun setDefaultUserLocation() {
        Timber.d("Set default user location")
        viewModelScope.launch {
            setCurrentUserLocation(chicagoPosition)
            loadNearbyStations(chicagoPosition)
            setShowLocationError(true)
        }
    }

    fun loadNearbyStations(position: Position) {
        viewModelScope.launch {
            val trainStationAround = trainService.readNearbyStation(position = position)
            val busStopsAround = busService.busStopsAround(position = position)
            val bikeStationsAround = mapUtil.readNearbyStation(position = position, store.state.bikeStations)
            Single.zip(trainStationAround, busStopsAround, bikeStationsAround) { trains, buses, bikeStations ->
                uiState = uiState.copy(
                    trainStations = trains,
                    busStops = buses,
                    bikeStations = bikeStations,
                )
                Any()
            }.subscribe({}, { error -> Timber.e(error) })
        }
    }

    fun loadNearbyTrainDetails(trainStation: TrainStation) {
        viewModelScope.launch {
            trainService.loadStationTrainArrival(trainStation.id)
                .map { trainArrival ->
                    trainArrival.trainEtas.filter { trainEta -> trainEta.trainStation.id == trainStation.id }
                }
                .observeOn(Schedulers.computation())
                .subscribe(
                    {
                        uiState = uiState.copy(
                            bottomSheetData = uiState.bottomSheetData.copy(
                                trainStation = trainStation,
                                title = trainStation.name,
                                icon = Icons.Filled.Train,
                                bottomSheetState = BottomSheetDataState.TRAIN,
                                data = it.map { trainEta ->
                                    BottomSheetPagerData(
                                        title = trainEta.destName,
                                        content = trainEta.timeLeftDueDelayNoMinutes,
                                        subTitle = trainEta.stop.direction.toString(),
                                        titleColor = trainEta.routeName.textColor,
                                        backgroundColor = trainEta.routeName.color,
                                    )
                                },
                            ),
                        )
                    },
                    { onError ->
                        Timber.e(onError, "Error while loading train arrivals")
                        setNearbyDetailsError(true)
                    })
        }
    }

    fun loadNearbyBusDetails(busStop: BusStop) {
        viewModelScope.launch {
            busService.loadBusArrivals(busStop)
                .observeOn(Schedulers.computation())
                .subscribe(
                    {
                        uiState = uiState.copy(
                            bottomSheetData = uiState.bottomSheetData.copy(
                                busStop = busStop,
                                title = busStop.name,
                                icon = Icons.Filled.DirectionsBus,
                                bottomSheetState = BottomSheetDataState.BUS,
                                data = it.map { busArrival ->
                                    BottomSheetPagerData(
                                        title = busArrival.busDestination,
                                        content = busArrival.timeLeftDueDelayNoMinutes,
                                        subTitle = BusDirection.fromString(busArrival.routeDirection).shortLowerCase,
                                    )
                                },
                            ),
                        )
                    },
                    { onError ->
                        Timber.e(onError, "Error while loading bus arrivals")
                        setNearbyDetailsError(true)
                    })
        }
    }

    fun loadNearbyBikeDetails(currentBikeStation: BikeStation) {
        viewModelScope.launch {
            bikeService.findBikeStation(currentBikeStation.id)
                .observeOn(Schedulers.computation())
                .subscribe(
                    {
                        uiState = uiState.copy(
                            bottomSheetData = uiState.bottomSheetData.copy(
                                title = currentBikeStation.name,
                                icon = Icons.Filled.DirectionsBike,
                                bottomSheetState = BottomSheetDataState.BIKE,
                                bikeStation = it,
                                data = listOf(
                                    BottomSheetPagerData(
                                        title = "Bikes",
                                        content = it.availableBikes.toString(),
                                        bottom = "available",
                                    ),
                                    BottomSheetPagerData(
                                        title = "Docks",
                                        content = it.availableDocks.toString(),
                                        bottom = "available",
                                    )
                                )
                            ),
                        )
                    },
                    { onError ->
                        Timber.e(onError, "Error while loading bus arrivals")
                        setNearbyDetailsError(true)
                    })
        }
    }

    companion object {
        fun provideFactory(
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return NearbyViewModel() as T
                }
            }
    }
}
