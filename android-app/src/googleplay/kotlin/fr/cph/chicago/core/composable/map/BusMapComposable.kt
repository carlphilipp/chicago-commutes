package fr.cph.chicago.core.composable.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.composable.TopBar
import fr.cph.chicago.core.composable.common.LoadingBar
import fr.cph.chicago.core.composable.common.LoadingCircle
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.mainViewModel
import fr.cph.chicago.core.composable.settingsViewModel
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.DebugView
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject

class BusMapComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val busId: Int
        val busRouteId: String
        val bounds: Array<String>
        if (savedInstanceState != null) {
            //  busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id))
            busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds)) ?: arrayOf()
        } else {
            //busId = intent.getIntExtra(getString(R.string.bundle_bus_id), 0)
            busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
            bounds = intent.getStringArrayExtra(getString(R.string.bundle_bus_bounds)) ?: arrayOf()
        }

        val viewModel = GoogleMapBusViewModel().initModel(
            //busId = busId,
            busRouteId = busRouteId,
            bounds = bounds,
        )

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                BusMapView(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapBusViewModel,
) {
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    val scope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = viewModel.uiState.busRouteId,
                showRefresh = true,
                onRefresh = { viewModel.reloadData() })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
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
                    showErrorMessage = viewModel.uiState.showError,
                    onComplete = { viewModel.showError(false) }
                )
            }
        }
    )
}

@Composable
fun GoogleMapBusMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapBusViewModel,
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

    DebugView(cameraPositionState = cameraPositionState)

    /*InfoWindowsDetails(
        showView = viewModel.uiState.trainEtas.isNotEmpty(),
        viewModel = viewModel
    )*/
}

@Composable
fun BusLineLayer(
    viewModel: GoogleMapBusViewModel,
) {
    viewModel.uiState.busPatterns.forEachIndexed { index, busPattern ->
        Polyline(
            points = busPattern.busStopsPatterns.map { it.position.toLatLng() },
            width = 7f,
            color = viewModel.uiState.colors[index]
        )
    }
}

@Composable
fun BusStopsMarkers(
    viewModel: GoogleMapBusViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.showHideStops(cameraPositionState.position.zoom)

    viewModel.uiState.busPatterns.forEachIndexed { index, busPattern ->
        busPattern.busStopsPatterns
            .filter { busStopPattern -> busStopPattern.type == "S" }
            .forEach { busStopPattern ->
                if(viewModel.uiState.stopIcons.isNotEmpty()) {
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
    viewModel: GoogleMapBusViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.updateIconOnZoomChange(cameraPositionState.position.zoom)

/*    if (viewModel.uiState.trainIcon != null) {
        viewModel.uiState.trains.forEach { train ->
            Marker(
                title = "To ${train.destName}",
                position = train.position.toLatLng(),
                icon = viewModel.uiState.trainIcon,
                rotation = train.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                onClick = {
                    viewModel.loadTrainEtas(train, false)
                    false
                },
                onInfoWindowClose = {
                    viewModel.resetDetails()
                }
            )
        }
    }*/
}

data class GoogleMapBusUiState(
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val colors: List<Color> = TrainLine.values().map { Color(it.color) }.dropLast(1),
    val stopIcons: List<BitmapDescriptor> = listOf(),

    //val busId: Int = 0,
    val busRouteId: String = StringUtils.EMPTY,

    val busPatterns: List<BusPattern> = listOf(),

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val busIcon: BitmapDescriptor? = null,
    val busIconSmall: BitmapDescriptor? = null,
    val busIconMedium: BitmapDescriptor? = null,
    val busIconLarge: BitmapDescriptor? = null,
    val showStopIcon: Boolean = false,
)

@HiltViewModel
class GoogleMapBusViewModel @Inject constructor(
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapBusUiState())
        private set

    fun initModel(busRouteId: String, bounds: Array<String>): GoogleMapBusViewModel {
        uiState = uiState.copy(
            busRouteId = busRouteId,
        )
        loadPatterns()
        return this
    }

    fun loadPatterns() {
        Observable.fromCallable {
            val patterns: MutableList<BusPattern> = mutableListOf()

            // Search for directions
/*            val busDirections = busService.loadBusDirectionsSingle(uiState.busRouteId).blockingGet()
            bounds = busDirections.busDirections.map { busDirection -> busDirection.text }.toTypedArray()*/

            busService.loadBusPattern(uiState.busRouteId, arrayOf<String>()).blockingGet().forEach { patterns.add(it) }
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
                    Timber.e(throwable, "Could not load train etas")
                    showError(true)
                    uiState = uiState.copy(isLoading = false)
                }
            )
    }

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun reloadData() {
        uiState = uiState.copy(isLoading = true)
        loadBuses()
        if (uiState.busPatterns.isEmpty()) {
            //loadPositions()
        }
    }

    fun loadBuses() {
        busService.busForRouteId(uiState.busRouteId)
            .observeOn(Schedulers.computation())
            .subscribe(
                {

                },
                {

                }
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
            .subscribeOn(Schedulers.io())
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

    private val blueIcon: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(App.instance.resources, R.drawable.blue_marker_no_shade))
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
/*        if (newZoom != uiState.zoom) {
            val oldZoom = uiState.zoom
            if (GoogleMapUtil.isIn(newZoom, 12.9f, 11f) && !GoogleMapUtil.isIn(oldZoom, 12.9f, 11f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconSmall,
                    zoom = newZoom,
                )
            } else if (GoogleMapUtil.isIn(newZoom, 14.9f, 13f) && !GoogleMapUtil.isIn(oldZoom, 14.9f, 13f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconMedium,
                    zoom = newZoom,
                )
            } else if (GoogleMapUtil.isIn(newZoom, 21f, 15f) && !GoogleMapUtil.isIn(oldZoom, 21f, 15f)) {
                uiState = uiState.copy(
                    trainIcon = uiState.trainIconLarge,
                    zoom = newZoom,
                )
            }
        }*/
    }

    private fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
