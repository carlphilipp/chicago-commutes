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
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.GoogleMapUtil.createBitMapDescriptor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.InfoWindowsDetails
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Timber.d("Compose TrainMapScreen")
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

    Column {
        DisplayTopBar(
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

                GoogleMapTrainMapView(
                    viewModel = viewModel,
                    onMapLoaded = {
                        isMapLoaded = true
                    },
                )

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

@Composable
fun GoogleMapTrainMapView(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()

    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
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

    //DebugView(cameraPositionState = cameraPositionState)

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
            Marker(
                position = trainStation.stops[0].position.toLatLng(),
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
            Marker(
                title = "To ${train.destName}",
                position = train.position.toLatLng(),
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

data class GoogleMapTrainUiState(
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
)

@HiltViewModel
class MapTrainViewModel @Inject constructor(
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

    fun centerMapOnTrains() {
        val position: Position
        val zoom: Float
        if (uiState.trains.size == 1) {
            position = if (uiState.trains[0].position.latitude == 0.0 && uiState.trains[0].position.longitude == 0.0)
                chicagoPosition
            else
                uiState.trains[0].position
            zoom = 15f
        } else {
            position = MapUtil.getBestPosition(uiState.trains.map { it.position })
            zoom = 11f
        }
        uiState = uiState.copy(
            moveCamera = LatLng(position.latitude, position.longitude),
            moveCameraZoom = zoom,
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
                        centerMapOnTrains()
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
