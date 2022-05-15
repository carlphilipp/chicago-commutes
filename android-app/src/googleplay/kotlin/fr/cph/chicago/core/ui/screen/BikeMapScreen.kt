package fr.cph.chicago.core.ui.screen

import android.graphics.BitmapFactory
import android.os.Bundle
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.BikeBottomSheet
import fr.cph.chicago.core.ui.common.BottomSheetContent
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.defaultSheetPeekHeight
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.util.CameraDebugView
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.mapStyle
import fr.cph.chicago.util.toLatLng
import fr.cph.chicago.util.toPosition
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BikeMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    settingsViewModel: SettingsViewModel,
    title: String,
) {
    Timber.d("Compose BikeMapScreen $title")
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState: CameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(MapUtil.chicagoPosition.toLatLng(), defaultZoom)
            )
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

    if (isMapLoaded) {
        LaunchedEffect(key1 = isMapLoaded, block = {
            scope.launch {
                viewModel.searchBikeStationInState()
                viewModel.loadIcon()
            }
        })
    }

    BottomSheetScaffoldMaterial3(
        scaffoldState = viewModel.uiState.scaffoldState,
        sheetPeekHeight = defaultSheetPeekHeight,
        sheetContent = {
            BikeBottomSheet(
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
                BikeGoogleBusMapView(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { isMapLoaded = true },
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
                            top.linkTo(anchor = parent.top)
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
                            viewModel.reloadData(position = cameraPositionState.position.target.toPosition())
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

                LoadingBar(show = viewModel.uiState.isRefreshing)

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
        }
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

@Composable
private fun BikeGoogleBusMapView(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    settingsViewModel: SettingsViewModel,
    cameraPositionState: CameraPositionState,
    onMapLoaded: () -> Unit,
) {
    Timber.d("Compose BikeGoogleBusMapView with location ${viewModel.uiState.moveCamera}")
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var jobOnClose: Job? by remember { mutableStateOf(null) }

    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")

        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom))
            }
        })
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, mapStyle(settingsViewModel)),
        ),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false, zoomControlsEnabled = false),
        onMapLoaded = onMapLoaded,
    ) {
        viewModel.uiState.bikeStationAround.values.forEach { bikeStation ->
            BikeStationMarker(
                show = true,
                viewModel = viewModel,
                bikeStation = bikeStation,
                onInfoWindowClick = {
                    jobOnClose?.cancel()
                    viewModel.expandBottomSheet(
                        scope = scope,
                        runBefore = {
                            viewModel.onInfoWindowClose(BottomSheetContent.EXPAND, bikeStation.name)
                            viewModel.refreshBottomSheet(bikeStation)
                        }
                    )
                    false
                },
                onInfoWindowClose = {
                    jobOnClose = scope.launch {
                        viewModel.collapseBottomSheet(
                            scope = scope,
                            runBefore = {
                                viewModel.onInfoWindowClose(BottomSheetContent.COLLAPSE, "Bikes")
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun BikeStationMarker(
    show: Boolean,
    viewModel: MapBikesViewModel,
    bikeStation: BikeStation,
    onInfoWindowClick: (Marker) -> Boolean,
    onInfoWindowClose: (Marker) -> Unit,
) {
    val markerState = rememberMarkerState()
    val scope = rememberCoroutineScope()
    markerState.position = Position(latitude = bikeStation.latitude, longitude = bikeStation.longitude).toLatLng()
    if (bikeStation.id == viewModel.uiState.bikeStationSelected.id) {
        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                markerState.showInfoWindow()
            }
        })
    }
    Marker(
        state = markerState,
        icon = viewModel.uiState.bikeStationIcon,
        title = bikeStation.name,
        visible = show,
        onClick = onInfoWindowClick,
        onInfoWindowClose = onInfoWindowClose,
    )
}


@Composable
fun StateDebugView(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
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
        Text(text = "Bike station selected id: ${viewModel.uiState.bikeStationSelected.id}")
        Text(text = "Bike station selected name: ${viewModel.uiState.bikeStationSelected.name}")
        Text(text = "Bike around: ${viewModel.uiState.bikeStationAround.size}")
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class GoogleMapBikeUiState constructor(
    val isMapLoading: Boolean = false,
    val showError: Boolean = false,
    val isRefreshing: Boolean = false,

    val id: String = "",
    val bikeStationSelected: BikeStation = BikeStation.buildDefaultBikeStationWithName(id = "", name = "Bikes"),
    val bikeStationAround: Map<String, BikeStation> = mapOf(),
    val bikeStationBottomSheet: List<BottomSheetPagerData> = listOf(),

    val bikeStationIcon: BitmapDescriptor? = null,

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    ),

    val bottomSheetContentAndState: BottomSheetContent = BottomSheetContent.EXPAND,
    val bottomSheetTitle: String = "Bikes",
)

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class MapBikesViewModel @Inject constructor(
    private val bikeService: BikeService = BikeService,
    private val mapUtil: MapUtil = MapUtil,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(GoogleMapBikeUiState())
        private set

    fun setId(id: String) {
        uiState = uiState.copy(id = id)
    }

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun searchBikeStationInState() {
        viewModelScope.launch {
            loadData(
                Single.fromCallable { bikeService.getAllBikeStationsFromState() }
                    .map { stations ->
                        if (stations.containsKey(uiState.id)) {
                            Position(stations[uiState.id]!!.latitude, stations[uiState.id]!!.longitude)
                        } else {
                            MapUtil.chicagoPosition
                        }
                    }
                    .flatMap { position -> mapUtil.readNearbyStation(position = position, store.state.bikeStations) }
            )
        }
    }

    fun reloadData(position: Position) {
        loadData(
            bikeService.allBikeStations()
                .flatMap { bikeStations -> mapUtil.readNearbyStation(position = position, bikeStations) }
        )
    }

    private fun loadData(pipe: Single<Map<String, BikeStation>>) {
        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true)

            pipe
                .subscribeOn(Schedulers.computation())
                .subscribe(
                    { bikeStations ->
                        val bikeStation = if (bikeStations.containsKey(uiState.id)) bikeStations[uiState.id]!! else bikeService.createEmptyBikeStation(uiState.id)
                        uiState = uiState.copy(
                            bikeStationSelected = bikeStation,
                            bottomSheetTitle = bikeStation.name,
                            bikeStationBottomSheet = listOf(
                                BottomSheetPagerData(
                                    title = "Bikes",
                                    content = bikeStation.availableBikes.toString(),
                                    bottom = "available",
                                ),
                                BottomSheetPagerData(
                                    title = "Docks",
                                    content = bikeStation.availableDocks.toString(),
                                    bottom = "available",
                                )
                            ),
                            bikeStationAround = bikeStations,
                            moveCamera = LatLng(bikeStation.latitude, bikeStation.longitude),
                            moveCameraZoom = 14f,
                            isRefreshing = false,
                        )
                    },
                    {
                        Timber.e(it, "Could not load bike station")
                    }
                )
        }
    }

    fun refreshBottomSheet(bikeStation: BikeStation) {
        viewModelScope.launch {
            uiState = uiState.copy(
                bikeStationSelected = bikeStation,
                bottomSheetTitle = bikeStation.name,
                bikeStationBottomSheet = listOf(
                    BottomSheetPagerData(
                        title = "Bikes",
                        content = bikeStation.availableBikes.toString(),
                        bottom = "available",
                    ),
                    BottomSheetPagerData(
                        title = "Docks",
                        content = bikeStation.availableDocks.toString(),
                        bottom = "available",
                    )
                ),
            )
        }
    }

    fun loadIcon() {
        Single.fromCallable {
            BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.blue_marker_no_shade))
        }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(bikeStationIcon = it)
                },
                {
                    Timber.e(it, "Could not load bike station icon")
                }
            )
    }

    override fun newState(state: State) {
        Timber.d("MapBikesViewModel new state ${state.bikeStationsStatus} thread: ${Thread.currentThread().name}")
        when (state.bikeStationsStatus) {
            Status.SUCCESS -> {
                searchBikeStationInState()
            }
            Status.FULL_FAILURE, Status.FAILURE -> {
                showError(true)
            }
            else -> Timber.d("Status not handled")
        }
        uiState = uiState.copy(isRefreshing = false)
    }

    fun onInfoWindowClose(state: BottomSheetContent, title: String) {
        uiState = uiState.copy(
            bottomSheetContentAndState = state,
            bottomSheetTitle = title,
        )
    }

    fun expandBottomSheet(
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

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
        uiState = GoogleMapBikeUiState()
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
                    return MapBikesViewModel() as T
                }
            }
    }
}
