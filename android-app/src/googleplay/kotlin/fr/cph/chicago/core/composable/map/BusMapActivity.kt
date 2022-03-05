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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import fr.cph.chicago.core.composable.RefreshTopBar
import fr.cph.chicago.core.composable.common.LoadingBar
import fr.cph.chicago.core.composable.common.LoadingCircle
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.GoogleMapUtil.isIn
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

class BusMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val busRouteId = if (savedInstanceState != null) {
            savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: ""
        } else {
            intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: ""
        }

        val viewModel = GoogleMapBusViewModel().initModel(
            busRouteId = busRouteId,
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
            RefreshTopBar(
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
                    showError = viewModel.uiState.showError,
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

    //DebugView(cameraPositionState = cameraPositionState)

    InfoWindowsDetailsBus(
        showView = viewModel.uiState.busArrivals.isNotEmpty(),
        viewModel = viewModel
    )
}

@Composable
fun BusLineLayer(
    viewModel: GoogleMapBusViewModel,
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
    viewModel: GoogleMapBusViewModel,
    cameraPositionState: CameraPositionState,
) {
    viewModel.showHideStops(cameraPositionState.position.zoom)

    if (viewModel.uiState.stopIcons.isNotEmpty()) {
        viewModel.uiState.busPatterns.forEachIndexed { index, busPattern ->
            busPattern.busStopsPatterns
                .filter { busStopPattern -> busStopPattern.type == "S" }
                .forEach { busStopPattern ->

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

    if (viewModel.uiState.busIcon != null) {
        viewModel.uiState.buses.forEach { bus ->
            Marker(
                title = "To ${bus.destination}",
                position = bus.position.toLatLng(),
                icon = viewModel.uiState.busIcon,
                rotation = bus.heading.toFloat(),
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                onClick = {
                    viewModel.loadBusEta(bus, false)
                    false
                },
                onInfoWindowClose = {
                    viewModel.resetDetails()
                }
            )
        }
    }
}

// FIXME: Should be re-usable with train
@Composable
fun InfoWindowsDetailsBus(
    showView: Boolean,
    viewModel: GoogleMapBusViewModel,
) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(50.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    enabled = true,
                    onClick = {
                        viewModel.loadBusEta(viewModel.uiState.detailsBus, true)
                    }
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
                            text = "To: ${viewModel.uiState.detailsBus.destination}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    val max = if (viewModel.uiState.detailsShowAll) {
                        viewModel.uiState.busArrivals.size - 1
                    } else {
                        6
                    }
                    for (i in 0..max) {
                        // FIXME: There is a bug here when selecting several buses in the UI
                        val busArrival = viewModel.uiState.busArrivals[i]
                        BusEtaView(stopName = busArrival.stopName, eta = busArrival.timeLeftDueDelay)
                    }
                    if (!viewModel.uiState.detailsShowAll && max >= 6) {
                        DisplayAllResultsRowView()
                    }
                }
            }
        }
    }
}

@Composable
fun BusEtaView(stopName: String, eta: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stopName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = eta,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DisplayAllResultsRowView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Display all results",
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


data class GoogleMapBusUiState(
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val colors: List<Color> = TrainLine.values().map { it.color }.dropLast(1),
    val stopIcons: List<BitmapDescriptor> = listOf(),

    val busRouteId: String = "",

    val buses: List<Bus> = listOf(),
    val busPatterns: List<BusPattern> = listOf(),
    val busArrivals: List<BusArrival> = listOf(),

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,

    val busIcon: BitmapDescriptor? = null,
    val busIconSmall: BitmapDescriptor? = null,
    val busIconMedium: BitmapDescriptor? = null,
    val busIconLarge: BitmapDescriptor? = null,
    val showStopIcon: Boolean = false,

    val detailsBus: Bus = Bus(),
    val detailsShowAll: Boolean = false,
)

@HiltViewModel
class GoogleMapBusViewModel @Inject constructor(
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapBusUiState())
        private set

    fun initModel(busRouteId: String): GoogleMapBusViewModel {
        uiState = uiState.copy(
            busRouteId = busRouteId,
        )
        loadPatterns()
        loadBuses()
        return this
    }

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun reloadData() {
        uiState = uiState.copy(isLoading = true)
        loadBuses()
        if (uiState.busPatterns.isEmpty()) {
            loadPatterns()
        }
    }

    fun loadBusEta(bus: Bus, showAll: Boolean) {
        uiState = uiState.copy(
            isLoading = true,
            detailsBus = bus,
            detailsShowAll = showAll,
        )
        busService.loadFollowBus(bus.id.toString())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { busArrivals ->
                    uiState = uiState.copy(
                        busArrivals = busArrivals,
                        isLoading = false,
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

    fun resetDetails() {
        uiState = uiState.copy(
            busArrivals = listOf(),
        )
    }

    // FIXME: this is duplicated code
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

    private fun loadPatterns() {
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

    private fun loadBuses() {
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

    // FIXME: to move to appropriate location
    private fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
