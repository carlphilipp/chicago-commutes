package fr.cph.chicago.core.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.permissions.NearbyLocationPermissionView
import fr.cph.chicago.core.permissions.onPermissionsResult
import fr.cph.chicago.core.ui.common.BottomSheetData
import fr.cph.chicago.core.ui.common.BottomSheetDataState
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.LoadingCircle
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import timber.log.Timber

// FIXME: handle zoom right after permissions has been approved or denied
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
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
    var isMapLoaded by remember { mutableStateOf(false) }
    // Show map after 5 seconds. This is needed because there is no callback from the sdk to know if the map can be loaded or not.
    // Meaning that we can have a situation where the onMapLoaded method is never triggered, while the map view has been populated
    // with some error messages from the google sdk like: "Play store needs to be updated"
    if (!isMapLoaded) {
        runWithDelay(5L, TimeUnit.SECONDS) { isMapLoaded = true }
    }
    val onPermissionsResult: (Map<String, Boolean>) -> Unit by remember {
        Timber.d("Calling onPermissionsResult ${Thread.currentThread().name}")
        mutableStateOf(onPermissionsResult(context = context, viewModel = viewModel))
    }

    NearbyLocationPermissionView(onPermissionsResult = onPermissionsResult)

    Column {
        DisplayTopBar(
            screen = Screen.Nearby,
            title = "Nearby", // FIXME: to update
            viewModel = navigationViewModel,
        )

        Scaffold(
            modifier = modifier.fillMaxWidth(),
            snackbarHost = { SnackbarHostInsets(state = viewModel.uiState.snackbarHostState) },
            content = {
                NearbyOsmdroidMapView(
                    onMapLoaded = { isMapLoaded = true },
                    nearbyViewModel = viewModel
                )

                LoadingCircle(show = !isMapLoaded)

/*                if (viewModel.uiState.showLocationError) {

                    ShowLocationNotFoundSnackBar(
                        scope = scope,
                        snackbarHostState = viewModel.uiState.snackbarHostState,
                        showErrorMessage = viewModel.uiState.showLocationError,
                        onComplete = { viewModel.setShowLocationError(false) }
                    )
                }
                if (viewModel.uiState.nearbyDetailsError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = viewModel.uiState.snackbarHostState,
                        showError = viewModel.uiState.nearbyDetailsError,
                        onComplete = { viewModel.setNearbyDetailsError(false) }
                    )
                } */
            }
        )
    }
}

@Composable
fun NearbyOsmdroidMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    nearbyViewModel: NearbyViewModel,
) {
    val uiState = nearbyViewModel.uiState
    val context = LocalContext.current

    //val cameraPositionState = rememberCameraPositionState {
    //    position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
    //}
    //cameraPositionState.position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)

    //Timber.d("Set location: ${uiState.nearbyMapCenterLocation}")

    Configuration.getInstance().load(
        context.applicationContext,
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    )

    AndroidView(factory = { c -> MapView(c) }, modifier = modifier) {
        it.setTileSource(TileSourceFactory.MAPNIK)
        it.isTilesScaledToDpi = true

        it.setMultiTouchControls(true)
        RotationGestureOverlay(it).apply {
            isEnabled = true
            it.overlays.add(this)
        }

        it.controller.setZoom(10.0)
        val center = chicagoPosition
        it.controller.setCenter(GeoPoint(center.latitude, center.longitude))

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                // TODO: remove this workaround after making sure the app view is not
                // recomposed at each station/stop marker click.
                // updateMapCenter(nearbyViewModel, it)
                //nearbyViewModel.setShowNearbyDetails(false)

                return false
            }

            override fun longPressHelper(point: GeoPoint): Boolean {
                return false
            }

        }
        it.overlays.add(0, MapEventsOverlay(mapEventsReceiver))

        it.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                val point = it.projection.fromPixels(event.x, event.y)
                /*mainViewModel.setCurrentUserLocation(
                    Position(point.latitude, point.longitude),
                    it.zoomLevelDouble.toFloat()
                )*/

                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                /*mainViewModel.setCurrentUserLocation(
                    Position(it.mapCenter.latitude, it.mapCenter.longitude),
                    event.zoomLevel.toFloat()
                )*/

                return false
            }
        })

        uiState.trainStations.forEach { trainStation ->
            val trainPosition = trainStation.stops[0].position
            Marker(it).apply {
                position = GeoPoint(trainPosition.latitude, trainPosition.longitude)
                title = trainStation.name
                icon = AppCompatResources.getDrawable(context, R.drawable.train_station_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(nearbyViewModel, it)
                    nearbyViewModel.loadNearbyTrainDetails(trainStation = trainStation)
                    false
                }
                it.overlays.add(this)
            }
        }

        uiState.busStops.forEach { busStop ->
            val busPosition = busStop.position
            Marker(it).apply {
                position = GeoPoint(busPosition.latitude, busPosition.longitude)
                title = busStop.name
                icon = AppCompatResources.getDrawable(context, R.drawable.bus_stop_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(nearbyViewModel, it)
                    nearbyViewModel.loadNearbyBusDetails(busStop = busStop)
                    false
                }
                it.overlays.add(this)
            }
        }

        uiState.bikeStations.forEach { bikeStation ->
            Marker(it).apply {
                position = GeoPoint(bikeStation.latitude, bikeStation.longitude)
                title = bikeStation.name
                icon = AppCompatResources.getDrawable(context, R.drawable.bike_station_icon)
                setOnMarkerClickListener { _, _ ->
                    // TODO: remove this workaround after making sure the app view is not
                    // recomposed at each station/stop marker click.
                    updateMapCenter(nearbyViewModel, it)
                    nearbyViewModel.loadNearbyBikeDetails(currentBikeStation = bikeStation)
                    false
                }
                it.overlays.add(this)
            }
        }
    }

/*        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SearchThisAreaButton(mainViewModel = nearbyViewModel)
            //DebugView(cameraPositionState)
        }

        MapStationDetailsView(
            showView = nearbyViewModel.uiState.nearbyDetailsShow,
            title = nearbyViewModel.uiState.nearbyDetailsTitle,
            image = nearbyViewModel.uiState.nearbyDetailsIcon,
            arrivals = nearbyViewModel.uiState.nearbyDetailsArrivals,
        )*/
}

@Composable
private fun SearchThisAreaButton(mainViewModel: MainViewModel) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
    ) {
        ElevatedButton(onClick = {
            //mainViewModel.loadNearby()
        }) {
            Text(text = stringResource(id = R.string.search_area))
        }
    }
}

@Composable
fun MapStationDetailsView(showView: Boolean, title: String, image: ImageVector/*, arrivals: NearbyResult*/) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 50.dp, end = 50.dp, bottom = 50.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            AnimatedVisibility(
                visible = showView,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)) {
                    /*HeaderCard(name = title, image = image, lastUpdate = arrivals.lastUpdate)
                    arrivals.arrivals.forEach { entry ->
                        Arrivals(
                            destination = entry.key.destination,
                            arrivals = entry.value,
                            direction = entry.key.direction
                        )
                    }*/
                }
            }
        }
    }
}

fun updateMapCenter(viewModel: NearbyViewModel, mapView: MapView) {
    /*mainViewModel.setCurrentUserLocation(
        Position(mapView.mapCenter.latitude, mapView.mapCenter.longitude),
        mapView.zoomLevelDouble.toFloat()
    )*/
}

@OptIn(ExperimentalMaterialApi::class)
data class NearbyScreenUiState(
    // data
    val trainStations: List<TrainStation> = listOf(),
    val busStops: List<BusStop> = listOf(),
    val bikeStations: List<BikeStation> = listOf(),

    // map
    val isMapLoaded: Boolean = false,
    val moveCamera: Position? = null,
    val moveCameraZoom: Float? = null,
    val isMyLocationEnabled: Boolean = false,
    val showLocationError: Boolean = false,
    //val cameraPositionState: CameraPositionState = CameraPositionState(position = CameraPosition.fromLatLngZoom(chicagoPosition.toLatLng(), defaultZoom)),

    // error
    val nearbyDetailsError: Boolean = false,

    // bottom sheet
    val bottomSheetData: BottomSheetData = BottomSheetData(),
    val scaffoldState: BottomSheetScaffoldState = BottomSheetScaffoldState(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded),
        snackbarHostState = androidx.compose.material.SnackbarHostState(),
    ),

    // bitmap descriptor
    val bitmapDescriptorLoaded: Boolean = false,

    // snack bar
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
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

    fun setMapLoaded() {
        uiState = uiState.copy(isMapLoaded = true)
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
            //setCurrentUserLocation(chicagoPosition)
            loadNearbyStations(chicagoPosition)
            //setShowLocationError(true)
        }
    }

    fun loadBitmapDescriptor(context: Context) {
        viewModelScope.launch {
/*            Single.fromCallable {
                val bitmapDescriptorTrain = getBitmapDescriptor(context, R.drawable.train_station_icon)
                val bitmapDescriptorBus = getBitmapDescriptor(context, R.drawable.bus_stop_icon)
                val bitmapDescriptorBike = getBitmapDescriptor(context, R.drawable.bike_station_icon)
                listOf(bitmapDescriptorTrain, bitmapDescriptorBus, bitmapDescriptorBike)
            }
                .subscribe(
                    {
                        uiState = uiState.copy(
                            bitmapDescriptorLoaded = true,
                            bitmapDescriptorTrain = it[0],
                            bitmapDescriptorBus = it[1],
                            bitmapDescriptorBike = it[2],
                        )
                    }, {
                        Timber.e(it, "Could not load bitmap descriptor")
                    })
*/
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
                    bikeStations = bikeStations.values.toList(),
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
