package fr.cph.chicago.core.composable.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.TopBar
import fr.cph.chicago.core.composable.common.LoadingBar
import fr.cph.chicago.core.composable.common.LoadingCircle
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.toComposeColor
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class TrainMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val line = if (savedInstanceState != null)
            savedInstanceState.getString(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        else
            intent.getStringExtra(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        val trainLine = TrainLine.fromXmlString(line)
        val viewModel = GoogleMapTrainViewModel().initModel(line = trainLine)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                TrainMapView(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapTrainViewModel,
) {
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    val scope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = viewModel.uiState.line.toStringWithLine(),
                showRefresh = true,
                onRefresh = { viewModel.reloadData() })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {


            GoogleMapTrainMapView(
                viewModel = viewModel,
                onMapLoaded = {
                    Timber.i("Map loaded")
                    isMapLoaded = true
                    // Google map must be loaded to be able to run these methods
                    viewModel.loadIcons()
                    viewModel.loadTrains()
                },
            )

            LoadingBar(
                show = viewModel.uiState.isLoading,
                color = viewModel.uiState.line.toComposeColor(),
            )

            LoadingCircle(show = !isMapLoaded)

            if (viewModel.uiState.showError) {
                ShowErrorMessageSnackBar(
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    showErrorMessage = viewModel.uiState.showError,
                    onComplete = { viewModel.showError(false) }
                )
            }
        }
    )
}

@Composable
fun GoogleMapTrainMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapTrainViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)
    }
    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")

        LaunchedEffect(key1 = uiState.moveCamera, block = {
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom));
            }
        })
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false),
        onMapLoaded = {
            onMapLoaded()

        },
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

    //DebugView(cameraPositionState = cameraPositionState)

    InfoWindowsDetailsTrain(
        showView = viewModel.uiState.trainEtas.isNotEmpty(),
        viewModel = viewModel
    )
}

@Composable
fun TrainLineLayer(
    viewModel: GoogleMapTrainViewModel,
) {
    Polyline(
        points = viewModel.uiState.polyLine,
        color = viewModel.uiState.line.toComposeColor(),
        width = 9f,
    )
}

@Composable
fun TrainStationsMarkers(
    viewModel: GoogleMapTrainViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.showHideStations(cameraPositionState.position.zoom)

    viewModel.uiState.stations.forEach { trainStation ->
        Marker(
            position = trainStation.stops[0].position.toLatLng(),
            icon = viewModel.uiState.stationIcon,
            title = trainStation.name,
            visible = viewModel.uiState.showStationIcon,
        )
    }
}

@Composable
fun TrainsOnMapLayer(
    viewModel: GoogleMapTrainViewModel,
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

// FIXME: Should be re-usable with bus
@Composable
fun InfoWindowsDetailsTrain(
    showView: Boolean,
    viewModel: GoogleMapTrainViewModel,
) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 50.dp, end = 50.dp, bottom = 50.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    enabled = true,
                    onClick = { viewModel.loadTrainEtas(viewModel.uiState.train, true) }
                ),
        ) {
            AnimatedVisibility(
                visible = showView,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "To: ${viewModel.uiState.train.destName}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    viewModel.uiState.trainEtas.forEachIndexed { index, trainEta ->
                        Timber.i("Found: ${trainEta.trainStation.name} ${trainEta.timeLeftDueDelay} index: ${index}")
                        if (index == viewModel.uiState.trainEtas.size - 1 && trainEta.trainStation.name == App.instance.getString(R.string.bus_all_results)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = trainEta.trainStation.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = trainEta.trainStation.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = trainEta.timeLeftDueDelay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GoogleMapTrainUiState(
    val isLoading: Boolean = false,
    val showError: Boolean = false,

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
class GoogleMapTrainViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapTrainUiState())
        private set

    fun initModel(line: TrainLine): GoogleMapTrainViewModel {
        uiState = uiState.copy(line = line)
        loadPositions()
        loadStations()
        return this
    }

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
        Timber.d("showHideStations $newZoom and ${uiState.zoom}")
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
        Timber.i("Center map on train")
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

    fun resetMoveCamera() {
        uiState = uiState.copy(
            moveCamera = null,
            moveCameraZoom = null,
        )
    }

    fun reloadData() {
        uiState = uiState.copy(isLoading = true)
        loadTrains()
        if (uiState.polyLine.isEmpty()) {
            loadPositions()
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
        trainService.trainEtas(train.runNumber.toString(), loadAll)
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

    private fun loadPositions() {
        trainService.readPatterns(uiState.line)
            .observeOn(Schedulers.computation())
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

    private fun loadStations() {
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

    private fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
