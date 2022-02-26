package fr.cph.chicago.core.composable.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.TopBar
import fr.cph.chicago.core.composable.settingsViewModel
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.toComposeColor
import fr.cph.chicago.util.DebugView
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class TrainMapComposable : ComponentActivity() {

    private val snackbarHostState = mutableStateOf(SnackbarHostState())

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
                    snackbarHostState = snackbarHostState.value,
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
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar(viewModel.uiState.line.toStringWithLine()) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            var isMapLoaded by remember { mutableStateOf(false) }
            GoogleMapTrainMapView(
                viewModel = viewModel,
                onMapLoaded = { isMapLoaded = true },
            )
            // FIXME: refactor that into a component (see Nearby.kt)
            if (!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = !isMapLoaded,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
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
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)
    }
    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(uiState.moveCamera, uiState.moveCameraZoom))
        viewModel.resetMoveCamera()
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false),
        onMapLoaded = {
            onMapLoaded()
            viewModel.loadIcons()
        },
        onMapClick = {
            //mainViewModel.setShowNearbyDetails(false)
        }
    ) {
        TrainLineLayer(viewModel = viewModel)
        TrainsOnMapLayer(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
        )
    }
    DebugView(cameraPositionState = cameraPositionState)
}

@Composable
fun TrainLineLayer(
    viewModel: GoogleMapTrainViewModel,
) {
    Polyline(
        points = viewModel.uiState.points,
        color = viewModel.uiState.line.toComposeColor(),
        width = 9f,
    )
}

@Composable
fun TrainsOnMapLayer(
    viewModel: GoogleMapTrainViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.updateIconOnZoomChange(cameraPositionState.position.zoom)

    if (viewModel.uiState.trainIcon != null) {
        viewModel.uiState.trains.forEach { train ->
            Marker(
                position = train.position.toLatLng(),
                icon = viewModel.uiState.trainIcon,
                rotation = train.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                title = "To ${train.destName}",
                snippet = train.runNumber.toString(),
            )
        }
    }
}

data class GoogleMapTrainUiState(
    val line: TrainLine = TrainLine.NA,
    val points: List<LatLng> = listOf(),
    val trains: List<Train> = listOf(),

    val zoom: Float = defaultZoom,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val trainIcon: BitmapDescriptor? = null, // this has to be null as it needs google map to be loaded to init it
    val trainIconSmall: BitmapDescriptor? = null,
    val trainIconMedium: BitmapDescriptor? = null,
    val trainIconLarge: BitmapDescriptor? = null,
)

@HiltViewModel
class GoogleMapTrainViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapTrainUiState())
        private set

    fun initModel(line: TrainLine): GoogleMapTrainViewModel {
        uiState = uiState.copy(line = line)
        loadPositions(line)
        loadTrains(line)
        return this
    }

    fun loadIcons() {
        val trainBitmap = BitmapFactory.decodeResource(App.instance.resources, R.drawable.train)
        val bitmapDescSmall = createBitMapDescriptor(trainBitmap, 9)
        val bitmapDescMedium = createBitMapDescriptor(trainBitmap, 5)
        val bitmapDescLarge = createBitMapDescriptor(trainBitmap, 3)
        uiState = uiState.copy(
            trainIcon = bitmapDescSmall,
            trainIconSmall = bitmapDescSmall,
            trainIconMedium = bitmapDescMedium,
            trainIconLarge = bitmapDescLarge,
        )
    }

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

    fun resetMoveCamera() {
        uiState = uiState.copy(
            moveCamera = null,
            moveCameraZoom = null,
        )
    }

    private fun loadPositions(line: TrainLine) {
        trainService.readPatterns(line)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { trainStationPattern ->
                    Timber.i("Found ${trainStationPattern.size} patterns")
                    uiState = uiState.copy(
                        points = trainStationPattern.map { it.position.toLatLng() }
                    )

                },
                {
                    Timber.e(it, "Could not load train patterns")
                    // TODO handle exception
                }
            )
    }

    private fun loadTrains(line: TrainLine) {
        trainService.trainLocations(line.toTextString())
            .observeOn(Schedulers.computation())
            .subscribe(
                { trains: List<Train> ->
                    Timber.i("Found ${trains.size} trains")
                    uiState = uiState.copy(
                        trains = trains,
                    )
                    centerMapOnTrains()
                },
                {
                    Timber.e(it, "Could not load trains")
                    // TODO handle exception
                })
    }

    private fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
