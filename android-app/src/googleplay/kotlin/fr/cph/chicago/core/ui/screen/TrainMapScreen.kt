package fr.cph.chicago.core.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModel
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
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.BottomSheet
import fr.cph.chicago.core.ui.common.BottomSheetScaffoldMaterial3
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.getDefaultPosition
import fr.cph.chicago.getZoom
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.DebugView
import fr.cph.chicago.util.GoogleMapUtil.createBitMapDescriptor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.InfoWindowsDetails
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TrainMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    settingsViewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Timber.d("Compose TrainMapScreen")
    val navController = LocalNavController.current
    var topBarTitle by remember { mutableStateOf(title) }
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
                viewModel.loadStations()
                viewModel.loadIcons()
                viewModel.loadTrains()
            }
        })
    }

    BottomSheetScaffoldMaterial3(
        //sheetState = viewModel.uiState.modalBottomSheetState,
        scaffoldState = viewModel.uiState.scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetContent = {
            BottomSheet(
                title = "Select a line",
                content = {
                    val selected = remember { mutableStateOf(viewModel.uiState.line) }
                    TrainLine.values()
                        .filter { trainLine -> trainLine != TrainLine.NA }
                        .forEach { line ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected.value = line
                                        topBarTitle = line.toStringWithLine()
                                        viewModel.switchTrainLine(scope, selected.value)
                                    },
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = line == selected.value,
                                    onClick = {
                                        selected.value = line
                                        topBarTitle = line.toStringWithLine()
                                        viewModel.switchTrainLine(scope, selected.value)
                                    })
                                Text(
                                    text = line.toStringWithLine(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                }
            )
        },
        content = {
            Column {
                Scaffold(
                    modifier = modifier,
                    snackbarHost = { SnackbarHostInsets(state = snackbarHostState) },
                    content = {
                        GoogleMapTrainMapView(
                            viewModel = viewModel,
                            settingsViewModel = settingsViewModel,
                            onMapLoaded = {
                                isMapLoaded = true
                            },
                        )

                        ConstraintLayout(
                            modifier = modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                )
                                .padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                        ) {
                            val (left, center, right, debug) = createRefs()
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

                            Text(
                                modifier = Modifier
                                    .constrainAs(center) {
                                        top.linkTo(anchor = parent.top)
                                        centerVerticallyTo(left)
                                        centerHorizontallyTo(parent)
                                    },
                                text = topBarTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Row(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .constrainAs(right) {
                                        end.linkTo(anchor = parent.end)
                                        width = Dimension.wrapContent
                                        centerVerticallyTo(left)
                                    }
                            ) {
                                IconButton(
                                    // modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
                                    //shape = RoundedCornerShape(16.0.dp),
                                    onClick = {
                                        scope.launch {
                                            viewModel.reloadData()
                                        }
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                    )
                                }
                                IconButton(
                                    //modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer),
                                    //shape = RoundedCornerShape(8.0.dp),
                                    onClick = {
                                        scope.launch {
                                            //viewModel.uiState.modalBottomSheetState.show()
                                            if (viewModel.uiState.scaffoldState.bottomSheetState.isCollapsed) {
                                                viewModel.uiState.scaffoldState.bottomSheetState.expand()
                                            } else {
                                                viewModel.uiState.scaffoldState.bottomSheetState.collapse()
                                            }
                                        }
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = null,
                                    )
                                }
                            }

                            if (settingsViewModel.uiState.showMapDebug) {
                                DebugView(
                                    modifier = Modifier.constrainAs(debug) {
                                        top.linkTo(anchor = left.bottom)
                                        end.linkTo(anchor = right.end)
                                    },
                                    cameraPositionState = viewModel.uiState.cameraPositionState
                                )
                            }
                        }

                        LoadingBar(
                            show = viewModel.uiState.isLoading,
                            color = viewModel.uiState.line.color,
                        )

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
    )
}

@Composable
fun GoogleMapTrainMapView(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
    viewModel: MapTrainViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isDarkTheme = when (settingsViewModel.uiState.theme) {
        Theme.AUTO -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    val style = if (isDarkTheme) R.raw.style_json_dark else R.raw.style_json_light

    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        LaunchedEffect(key1 = uiState.moveCamera, key2 = uiState.moveCameraZoom, block = {
            scope.launch {
                uiState.cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom))
            }
        })
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = uiState.cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, style),
        ),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false, zoomControlsEnabled = false),
        onMapLoaded = onMapLoaded,
    ) {
        TrainLineLayer(
            viewModel = viewModel,
        )

        TrainStationsMarkers(
            viewModel = viewModel,
            cameraPositionState = uiState.cameraPositionState,
        )

        TrainsOnMapLayer(
            viewModel = viewModel,
            cameraPositionState = uiState.cameraPositionState,
        )
    }

    InfoWindowsDetails(
        showView = viewModel.uiState.trainEtas.isNotEmpty(),
        destination = viewModel.uiState.train.destName,
        showAll = viewModel.uiState.trainLoadAll,
        results = viewModel.uiState.trainEtas.map { trainEta ->
            Pair(first = trainEta.trainStation.name, second = trainEta.timeLeftDueDelay)
        },
        onClick = {
            viewModel.loadTrainEtas(viewModel.uiState.train, true)
        }
    )
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
    viewModel.showHideStations(cameraPositionState.position.zoom)

    if (viewModel.uiState.stationIcon != null) {
        viewModel.uiState.stations.forEach { trainStation ->
            val markerState = rememberMarkerState()
            markerState.position = trainStation.stops[0].position.toLatLng()
            Marker(
                state = markerState,
                icon = viewModel.uiState.stationIcon,
                title = trainStation.name,
                visible = viewModel.uiState.showStationIcon,
            )
        }
    }
}

@Composable
fun TrainsOnMapLayer(
    viewModel: MapTrainViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.updateIconOnZoomChange(cameraPositionState.position.zoom)

    if (viewModel.uiState.trainIcon != null) {
        viewModel.uiState.trains.forEach { train ->

            // It seems that it does not get refreshed when the state change.
            // I think it's normal as it's an image that is displayed in the
            // original SDK. This won't probably be fixed. Instead, we can
            // just display a floating box like in nearby
            // Reference in case it's getting fixed: https://github.com/googlemaps/android-maps-compose/issues/46
            val markerState = rememberMarkerState()
            markerState.position = train.position.toLatLng()
            Marker(
                title = "To ${train.destName}",
                state = markerState,
                icon = viewModel.uiState.trainIcon,
                rotation = train.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                onClick = {
                    viewModel.loadTrainEtas(train, false)
                    false
                },
                onInfoWindowClose = {
                    viewModel.resetDetails()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class GoogleMapTrainUiState constructor(
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)
    ),

    val line: TrainLine = TrainLine.NA,
    val polyLine: List<LatLng> = listOf(),
    val trains: List<Train> = listOf(),
    val stations: List<TrainStation> = listOf(),

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val trainIcon: BitmapDescriptor? = null, // this has to be null as it needs google map to be loaded to init it
    val trainIconSmall: BitmapDescriptor? = null,
    val trainIconMedium: BitmapDescriptor? = null,
    val trainIconLarge: BitmapDescriptor? = null,
    val train: Train = Train(),
    val trainEtas: List<TrainEta> = listOf(),
    val trainLoadAll: Boolean = false,

    val stationIcon: BitmapDescriptor? = null,
    val showStationIcon: Boolean = false,

    // FIXME: this should not be in use ?
    val modalBottomSheetState: ModalBottomSheetState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        isSkipHalfExpanded = true,
    ),
    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    )
)

@OptIn(ExperimentalMaterialApi::class)
class MapTrainViewModel constructor(
    line: TrainLine,
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapTrainUiState(line = line))
        private set

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun loadIcons() {
        Single.fromCallable {
            val trainBitmap = BitmapFactory.decodeResource(App.instance.resources, R.drawable.train)
            val bitmapDescSmall = createBitMapDescriptor(trainBitmap, 9)
            val bitmapDescMedium = createBitMapDescriptor(trainBitmap, 5)
            val bitmapDescLarge = createBitMapDescriptor(trainBitmap, 3)
            val stationIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, colorDrawable()))
            listOf(bitmapDescSmall, bitmapDescMedium, bitmapDescLarge, stationIcon)
        }
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe { result ->
                uiState = uiState.copy(
                    trainIcon = result[0],
                    trainIconSmall = result[0],
                    trainIconMedium = result[1],
                    trainIconLarge = result[2],
                    stationIcon = result[3],
                )
            }
    }

    // FIXME: It looks like a BS algo, that should be more simple than that
    fun updateIconOnZoomChange(newZoom: Float) {
        if (newZoom != uiState.zoom) {
            val oldZoom = uiState.zoom
            if (isIn(newZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconSmall,
                    zoom = newZoom,
                )
            } else if (isIn(newZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconMedium,
                    zoom = newZoom,
                )
            } else if (isIn(newZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconLarge,
                    zoom = newZoom,
                )
            }
        }
    }

    fun showHideStations(newZoom: Float) {
        if (newZoom >= 14f && !uiState.showStationIcon) {
            uiState = uiState.copy(
                showStationIcon = true
            )
        }
        if (newZoom < 14f && uiState.showStationIcon) {
            uiState = uiState.copy(
                showStationIcon = false
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

    fun reloadData() {
        uiState = uiState.copy(isLoading = true)
        loadTrains()
        if (uiState.polyLine.isEmpty()) {
            loadPatterns()
        }
    }

    fun loadTrains() {
        trainService.trainLocations(uiState.line.toTextString())
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { trains: List<Train> ->
                    uiState = uiState.copy(
                        trains = trains,
                        isLoading = false,
                    )
                    if (uiState.shouldMoveCamera) {
                        centerMapOnLine()
                        uiState = uiState.copy(shouldMoveCamera = false)
                    }
                },
                {
                    Timber.e(it, "Could not load trains")
                    showError(true)
                    uiState = uiState.copy(isLoading = false)
                }
            )
    }

    fun loadTrainEtas(train: Train, loadAll: Boolean) {
        uiState = uiState.copy(isLoading = true)
        trainService.trainEtas(train.runNumber.toString())
            .observeOn(Schedulers.computation())
            .subscribe(
                { trainEtas ->
                    uiState = uiState.copy(
                        train = train,
                        trainEtas = trainEtas,
                        trainLoadAll = loadAll,
                        isLoading = false,
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
        uiState = uiState.copy(
            train = Train(),
            trainEtas = listOf(),
            trainLoadAll = false,
        )
    }

    fun loadPatterns() {
        trainService.readPatterns(uiState.line)
            .observeOn(Schedulers.computation())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { trainStationPattern ->
                    uiState = uiState.copy(
                        polyLine = trainStationPattern.map { it.position.toLatLng() }
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Could not load train patterns")
                    showError(true)
                    uiState = uiState.copy(isLoading = false)
                }
            )
    }

    fun loadStations() {
        Single.fromCallable { trainService.getStationsForLine(uiState.line) }
            .observeOn(Schedulers.computation())
            .subscribe(
                { trainStations ->
                    uiState = uiState.copy(
                        stations = trainStations,
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Could not load stations")
                    showError(true)
                    uiState = uiState.copy(isLoading = false)
                }
            )
    }

    fun switchTrainLine(scope: CoroutineScope, trainLine: TrainLine) {
        scope.launch {
            uiState = uiState.copy(
                line = trainLine,
                shouldMoveCamera = true,
                polyLine = listOf(),
                trains = listOf(),
                stations = listOf(),
            )
            uiState.modalBottomSheetState.hide()
            while (uiState.modalBottomSheetState.isVisible) {
                // wait for the animation to finish
            }
            uiState = uiState.copy(isLoading = true)
            loadPatterns()
            loadStations()
            loadTrains()
            loadIcons()
        }
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
}
