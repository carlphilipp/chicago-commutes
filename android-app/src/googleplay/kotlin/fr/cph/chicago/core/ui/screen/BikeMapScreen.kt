package fr.cph.chicago.core.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberMarkerState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedText
import fr.cph.chicago.core.ui.common.ChipMaterial3
import fr.cph.chicago.core.ui.common.LoadingBar
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.SwipeRefreshThemed
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.util.DebugView
import fr.cph.chicago.util.GoogleMapUtil.defaultZoom
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
    title: String,
) {
    Timber.d("Compose BikeMapScreen $title")
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
                viewModel.searchBikeStationInState()
                viewModel.loadIcon()
            }
        })
    }
    Column {
        DisplayTopBar(
            screen = Screen.BusMap,
            title = title,
            viewModel = navigationViewModel,
            onClickRightIcon = listOf {
                viewModel.reloadData()
            }
        )
        SwipeRefreshThemed(
            modifier = modifier,
            swipeRefreshState = rememberSwipeRefreshState(viewModel.uiState.isRefreshing),
            onRefresh = { },
        ) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHostInsets(state = snackbarHostState) },
                content = {
                    GoogleBikeBusMapView(
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        onMapLoaded = { isMapLoaded = true },
                    )

                    LoadingBar(show = viewModel.uiState.isMapLoading)

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

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

@Composable
private fun GoogleBikeBusMapView(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    settingsViewModel: SettingsViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    if (uiState.moveCamera != null && uiState.moveCameraZoom != null) {
        Timber.d("Move camera to ${uiState.moveCamera} with zoom ${uiState.zoom}")

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
        BikeStationMarker(
            show = true,
            viewModel = viewModel,
            bikeStation = viewModel.uiState.bikeStation,
        )
        viewModel.uiState.bikeStations.forEach { bikeStation ->
            BikeStationMarker(
                show = viewModel.uiState.showAllStations,
                viewModel = viewModel,
                bikeStation = bikeStation,
            )
        }
    }

    Column {
        ChipMaterial3(
            modifier = Modifier.padding(10.dp),
            text = "Show all stations",
            isSelected = viewModel.uiState.showAllStations,
            onClick = { viewModel.showAllStations(!viewModel.uiState.showAllStations) }
        )

        if (settingsViewModel.uiState.showMapDebug) {
            DebugView(uiState.cameraPositionState)
        }
    }
}

@Composable
fun BikeStationMarker(
    show: Boolean,
    viewModel: MapBikesViewModel,
    bikeStation: BikeStation
) {
    val markerState = rememberMarkerState()
    markerState.position = Position(latitude = bikeStation.latitude, longitude = bikeStation.longitude).toLatLng()
    MarkerInfoWindowContent(
        state = markerState,
        icon = viewModel.uiState.bikeStationIcon,
        title = bikeStation.name,
        visible = show,
        content = { _ ->
            val color = Color.Black
            Column() {
                Text(
                    text = bikeStation.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                )

                Row(modifier = Modifier.padding(top = 10.dp)) {
                    Column {
                        Text(
                            text = stringResource(R.string.bike_available_bikes),
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                        )
                        Text(
                            text = stringResource(R.string.bike_available_docks),
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                        )
                    }
                    Column(modifier = Modifier.padding(start = 20.dp)) {
                        var availableBikes by remember { mutableStateOf(bikeStation.availableBikes.toString()) }
                        availableBikes = if (bikeStation.availableBikes == -1) "?" else bikeStation.availableBikes.toString()
                        AnimatedText(
                            text = availableBikes,
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                        )
                        var availableDocks by remember { mutableStateOf(viewModel.uiState.bikeStation.availableDocks.toString()) }
                        availableDocks = if (bikeStation.availableDocks == -1) "?" else bikeStation.availableDocks.toString()

                        AnimatedText(
                            text = availableDocks,
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                        )
                    }
                }
            }
        }
    )
}

data class GoogleMapBikeUiState(
    val isMapLoading: Boolean = false,
    val showError: Boolean = false,
    val isRefreshing: Boolean = false,

    val id: String,
    val showAllStations: Boolean = false,
    val bikeStation: BikeStation = BikeStation.buildUnknownStation(),
    val bikeStations: List<BikeStation> = listOf(),
    val bikeStationIcon: BitmapDescriptor? = null,

    val zoom: Float = defaultZoom,
    val shouldMoveCamera: Boolean = true,
    val moveCamera: LatLng? = null,
    val moveCameraZoom: Float? = null,
    val cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(MapUtil.chicagoPosition.toLatLng(), defaultZoom)
    ),
)

@HiltViewModel
class MapBikesViewModel @Inject constructor(
    id: String,
    private val bikeService: BikeService = BikeService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(GoogleMapBikeUiState(id = id))
        private set

    fun showError(showError: Boolean) {
        uiState = uiState.copy(showError = showError)
    }

    fun showAllStations(showAllStations: Boolean) {
        uiState = uiState.copy(showAllStations = showAllStations)
    }

    fun searchBikeStationInState() {
        Single.fromCallable { bikeService.getAllBikeStationsFromState() }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { bikeStations ->
                    val bikeStation = bikeStations.find { bikeStation -> bikeStation.id == uiState.id }
                        ?: bikeService.createEmptyBikeStation(uiState.id)
                    uiState = uiState.copy(
                        bikeStation = bikeStation,
                        bikeStations = bikeStations,
                        moveCamera = LatLng(bikeStation.latitude, bikeStation.longitude),
                        moveCameraZoom = 14f,
                    )
                },
                {
                    Timber.e(it, "Could not load bike station")
                }
            )
    }

    fun reloadData() {
        Single.fromCallable {
            uiState = uiState.copy(isRefreshing = true)
            store.dispatch(BikeStationAction())
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
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

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
