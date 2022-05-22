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
import androidx.compose.ui.geometry.Offset
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
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.BottomSheetContent
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.TrainMapBottomSheet
import fr.cph.chicago.core.ui.common.defaultSheetPeekHeight
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.getDefaultPosition
import fr.cph.chicago.getZoom
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.CameraDebugView
import fr.cph.chicago.util.GoogleMapUtil.createBitMapDescriptor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
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

/**
 *      1. Show loading screen
 *      2. Load google map
 *      > 3.1. Play store is not available: show google map which handles the error message
 *      > 3.2 Show google map
 *      > 4. Load icons (can't be done before google map is loaded)
 *      > 5. Load patterns (can't be done before icons are loaded)
 *      > 6. Load stations (can't be done before icons are loaded)
 *      > 7. Load trains (can't be done before icons are loaded)
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrainMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Timber.d("Compose TrainMapScreen ${Thread.currentThread().name}")
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
            viewModel.loadTrainLine(
                scope = scope,
                trainLine = viewModel.uiState.line
            )
        })
    }

    BottomSheetScaffoldMaterial3(
        scaffoldState = viewModel.uiState.scaffoldState,
        sheetPeekHeight = defaultSheetPeekHeight,
        sheetContent = {
            TrainMapBottomSheet(
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
        snackbarHost = { SnackbarHostInsets(state = viewModel.uiState.snackbarHostState) },
        content = {
            Surface {
                GoogleMapTrainMapView(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    cameraPositionState = viewModel.uiState.cameraPositionState,
                    onMapLoaded = { viewModel.setMapLoaded() },
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

                LoadingBar(
                    show = viewModel.uiState.isLoading,
                    color = viewModel.uiState.line.color,
                )

                LoadingCircle(show = !viewModel.uiState.isMapLoaded)

                if (viewModel.uiState.showError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = viewModel.uiState.snackbarHostState,
                        showError = viewModel.uiState.showError,
                        onComplete = { viewModel.showError(false) }
                    )
                }
            }
        }
    )

    DisposableEffect(key1 = viewModel) {
        onDispose { viewModel.onStop() }
    }
}

@Composable
fun GoogleMapTrainMapView(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    viewModel: MapTrainViewModel,
    cameraPositionState: CameraPositionState,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        LaunchedEffect(key1 = uiState.moveCamera, key2 = uiState.moveCameraZoom, block = {
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
        TrainLineLayer(
            viewModel = viewModel,
        )

        TrainStationsMarkers(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
        )

        TrainsOnMapLayer(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
        )
    }
}

@Composable
fun TrainLineLayer(
    viewModel: MapTrainViewModel,
) {
    Polyline(
        points = viewModel.uiState.polyLine,
        color = viewModel.uiState.line.color,
        width = App.instance.lineWidthGoogleMap,
    )
}

@Composable
fun TrainStationsMarkers(
    viewModel: MapTrainViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.uiState.stationIcon?.run {
        viewModel.uiState.stations.forEach { trainStation ->
            val markerState = rememberMarkerState()
            markerState.position = trainStation.stops[0].position.toLatLng()
            Marker(
                state = markerState,
                icon = viewModel.uiState.stationIcon,
                title = trainStation.name,
                visible = cameraPositionState.position.zoom >= 14f,
            )
        }
    }
}

@Composable
fun TrainsOnMapLayer(
    viewModel: MapTrainViewModel,
    cameraPositionState: CameraPositionState,
) {
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }

    if (viewModel.uiState.trainIcon != null) {
        viewModel.uiState.trains.forEach { train ->
            val markerState = rememberMarkerState()
            markerState.position = train.position.toLatLng()
            Marker(
                title = "To ${train.destName}",
                state = markerState,
                icon = trainIcon(viewModel, cameraPositionState),
                rotation = train.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                onClick = {
                    job?.cancel()
                    scope.launch {
                        viewModel.loadTrainEtas(scope = scope, train = train)
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
    viewModel: MapTrainViewModel,
) {
    Column(
        modifier = modifier
            .padding(top = 10.dp)
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center
    ) {
        ColoredBox(color = viewModel.uiState.line.color)
        Text(text = "Current train line: ${viewModel.uiState.line}")
        Text(text = "Polyline size: ${viewModel.uiState.polyLine.size}")
        Text(text = "Trains size: ${viewModel.uiState.trains.size}")
        Text(text = "Stations size: ${viewModel.uiState.stations.size}")
        Text(text = "Trains Eta size: ${viewModel.uiState.trainEtas.size}")
    }
}

@Composable
private fun trainIcon(viewModel: MapTrainViewModel, cameraPositionState: CameraPositionState): BitmapDescriptor? {
    val zoom = cameraPositionState.position.zoom
    return when {
        zoom <= 12.3f -> viewModel.uiState.trainIconSmall
        zoom < 14.5f && zoom > 12.3f -> viewModel.uiState.trainIconMedium
        else -> viewModel.uiState.trainIconLarge
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class GoogleMapTrainUiState constructor(
    val isLoading: Boolean = false,
    val showError: Boolean = false,

    val line: TrainLine = TrainLine.NA,
    val polyLine: List<LatLng> = listOf(),
    val trains: List<Train> = listOf(),
    val stations: List<TrainStation> = listOf(),

    val isMapLoaded: Boolean = false,
    val cameraPositionState: CameraPositionState = CameraPositionState(position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)),
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val trainIcon: BitmapDescriptor? = null, // this has to be null as it needs google map to be loaded to init it
    val trainIconSmall: BitmapDescriptor? = null,
    val trainIconMedium: BitmapDescriptor? = null,
    val trainIconLarge: BitmapDescriptor? = null,
    val train: Train = Train(),
    val trainEtas: List<Pair<String, String>> = listOf(),
    val bottomSheetContentAndState: BottomSheetContent = BottomSheetContent.COLLAPSE,

    val stationIcon: BitmapDescriptor? = null,

    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    ),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@OptIn(ExperimentalMaterialApi::class)
class MapTrainViewModel constructor(
    private val trainService: TrainService = TrainService,
) : ViewModel() {

    var uiState by mutableStateOf(GoogleMapTrainUiState())
        private set

    fun setMapLoaded() {
        uiState = uiState.copy(isMapLoaded = true)
    }

    fun setTrainLine(trainLine: TrainLine) {
        uiState = uiState.copy(line = trainLine)
    }

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun onStop() {
        uiState = GoogleMapTrainUiState()
    }

    fun reloadData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            if (uiState.trainIcon == null) {
                googleMapIcons().subscribe(
                    { bitMapDescriptors ->
                        uiState = uiState.copy(
                            trainIcon = bitMapDescriptors[0],
                            trainIconSmall = bitMapDescriptors[0],
                            trainIconMedium = bitMapDescriptors[1],
                            trainIconLarge = bitMapDescriptors[2],
                            stationIcon = bitMapDescriptors[3],
                        )
                    },
                    { throwable -> Timber.e(throwable, "Could not load bitMapDescriptors") }
                )
            }
            if (uiState.stations.isEmpty()) {
                stations().subscribe(
                    { stations -> uiState = uiState.copy(stations = stations) },
                    { throwable -> Timber.e(throwable, "Could not load stations") }
                )
            }
            if (uiState.polyLine.isEmpty()) {
                patterns().subscribe(
                    { patterns -> uiState = uiState.copy(polyLine = patterns) },
                    { throwable -> Timber.e(throwable, "Could not load patterns") }
                )
            }
            trainService.trainLocations(uiState.line.toTextString())
                .subscribe(
                    { trains ->
                        uiState = uiState.copy(
                            isLoading = false,
                            trains = trains,
                        )
                    },
                    { throwable ->
                        Timber.e(throwable, "Error while loading train locations")
                        showError(true)
                        uiState = uiState.copy(isLoading = false)
                    }
                )
            // TODO: Reload bottom sheet data too
        }
    }

    fun loadTrainEtas(scope: CoroutineScope, train: Train) {
        uiState = uiState.copy(isLoading = true)
        trainService.trainEtas(train.runNumber.toString())
            .map { trainEtas ->
                trainEtas.map { trainEta ->
                    val timeLeftDueDelay = trainEta.timeLeftDueDelay
                    val value = if (timeLeftDueDelay.contains("min")) {
                        trainEta.timeLeftDueDelay.split(" min")[0]
                    } else {
                        trainEta.timeLeftDueDelay
                    }
                    Pair(
                        first = trainEta.trainStation.name,
                        second = value
                    )
                }
            }
            .observeOn(Schedulers.computation())
            .subscribe(
                { trainEtas ->
                    expandBottomSheet(
                        scope = scope,
                        runBefore = {
                            uiState = uiState.copy(
                                train = train,
                                trainEtas = trainEtas,
                                isLoading = false,
                                bottomSheetContentAndState = BottomSheetContent.EXPAND,
                            )
                        }
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Could not load train etas")
                    showError(true)
                    uiState = uiState.copy(isLoading = false)
                }
            )
    }

    fun resetDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(
                train = Train(),
                trainEtas = listOf(),
                bottomSheetContentAndState = BottomSheetContent.COLLAPSE,
            )
        }
    }

    fun loadTrainLine(scope: CoroutineScope, trainLine: TrainLine) {
        collapseBottomSheet(
            scope = scope,
            runBefore = {
                uiState = uiState.copy(
                    line = trainLine,
                    shouldMoveCamera = true,
                    polyLine = listOf(),
                    trains = listOf(),
                    stations = listOf(),
                    isLoading = true,
                    trainIcon = uiState.trainIconSmall,
                )
            },
            runAfter = {
                loadData()
            }
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

    private fun loadData() {
        viewModelScope.launch {
            val googleMapIcons = googleMapIcons()
            val patterns = patterns()
            val stations = stations()

            googleMapIcons
                .flatMap { bitMapDescriptors ->
                    Single.zip(patterns, stations) { patternsResult, stationsResult ->
                        uiState = uiState.copy(
                            isLoading = false,
                            stations = stationsResult,
                            polyLine = patternsResult,
                            trainIcon = bitMapDescriptors[0],
                            trainIconSmall = bitMapDescriptors[0],
                            trainIconMedium = bitMapDescriptors[1],
                            trainIconLarge = bitMapDescriptors[2],
                            stationIcon = bitMapDescriptors[3],
                        )
                    }
                }
                .subscribe(
                    {
                        if (uiState.shouldMoveCamera) {
                            centerMapOnLine()
                            uiState = uiState.copy(shouldMoveCamera = false)
                        }
                    }, { throwable ->
                        // Most likely should not happen, no network call
                        Timber.e(throwable, "Something went wrong")
                    }
                )

            trainService.trainLocations(uiState.line.toTextString())
                .subscribe(
                    { trains ->
                        uiState = uiState.copy(
                            isLoading = false,
                            trains = trains,
                        )
                    },
                    { throwable ->
                        Timber.e(throwable, "Error while loading train locations")
                        showError(true)
                        uiState = uiState.copy(isLoading = false)
                    }
                )
        }
    }

    private fun centerMapOnLine() {
        val line = uiState.line
        uiState = uiState.copy(
            moveCamera = line.getDefaultPosition().toLatLng(),
            moveCameraZoom = line.getZoom(),
        )
    }

    private fun googleMapIcons(): Single<List<BitmapDescriptor>> {
        return Single.fromCallable {
            val trainBitmap = BitmapFactory.decodeResource(App.instance.resources, R.drawable.train)
            val bitmapDescSmall = createBitMapDescriptor(trainBitmap, 9)
            val bitmapDescMedium = createBitMapDescriptor(trainBitmap, 5)
            val bitmapDescLarge = createBitMapDescriptor(trainBitmap, 3)
            val stationIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, colorDrawable()))
            listOf(bitmapDescSmall, bitmapDescMedium, bitmapDescLarge, stationIcon)
        }
    }

    private fun patterns(): Single<List<LatLng>> {
        return trainService.readPatterns(uiState.line)
            .map { trainStationPattern -> trainStationPattern.map { it.position.toLatLng() } }
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.io())
    }

    private fun stations(): Single<List<TrainStation>> {
        return Single.fromCallable { trainService.getStationsForLine(uiState.line) }
            .observeOn(Schedulers.computation())
    }

    private fun colorDrawable(): Int {
        return when (uiState.line) {
            TrainLine.BLUE -> R.drawable.blue_marker_no_shade
            TrainLine.BROWN -> R.drawable.brown_marker_no_shade
            TrainLine.GREEN -> R.drawable.green_marker_no_shade
            TrainLine.ORANGE -> R.drawable.orange_marker_no_shade
            TrainLine.PINK -> R.drawable.pink_marker_no_shade
            TrainLine.PURPLE -> R.drawable.purple_marker_no_shade
            TrainLine.RED -> R.drawable.red_marker_no_shade
            TrainLine.YELLOW -> R.drawable.yellow_marker_no_shade
            TrainLine.NA -> R.drawable.red_marker_no_shade
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
                    return MapTrainViewModel() as T
                }
            }
    }
}
