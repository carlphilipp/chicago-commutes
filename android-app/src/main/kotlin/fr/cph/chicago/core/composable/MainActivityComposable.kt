package fr.cph.chicago.core.composable

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.composable.screen.LocationViewModel
import fr.cph.chicago.core.composable.screen.NearbyResult
import fr.cph.chicago.core.composable.screen.SettingsViewModel
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.redux.AlertAction
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.BusRoutesAction
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.ResetAlertsStatusAction
import fr.cph.chicago.redux.ResetBikeStationFavoritesAction
import fr.cph.chicago.redux.ResetBusRoutesFavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.TimeUtil
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import java.util.Calendar
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

val mainViewModel = MainViewModel()
val settingsViewModel = SettingsViewModel().initModel()
val locationViewModel = LocationViewModel()

class MainActivityComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = mainViewModel.initModel()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                Navigation(screens = screens)

                DisposableEffect(key1 = viewModel) {
                    viewModel.onStart()
                    onDispose { viewModel.onStop() }
                }
            }
        }
        lifecycle.addObserver(RefreshTaskLifecycleEventObserver())
    }
}

data class MainUiState(
    val isRefreshing: Boolean = false,

    val busRoutes: List<BusRoute> = listOf(),
    val busRoutesShowError: Boolean = false,

    val bikeStations: List<BikeStation> = listOf(),
    val bikeStationsShowError: Boolean = false,

    val startMarket: Boolean = true,
    val startMarketFailed: Boolean = false,
    val justClosed: Boolean = false,

    val routesAlerts: List<RoutesAlertsDTO> = listOf(),
    val routeAlertErrorState: Boolean = false,
    val routeAlertShowError: Boolean = false,

    val nearbyMapCenterLocation: Position = chicagoPosition,
    val nearbyTrainStations: List<TrainStation> = listOf(),
    val nearbyBusStops: List<BusStop> = listOf(),
    val nearbyBikeStations: List<BikeStation> = listOf(),
    val nearbyZoomIn: Float = 8f,
    val nearbyIsMyLocationEnabled: Boolean = false,
    val nearbyShowLocationError: Boolean = false,
    val nearbyDetailsShow: Boolean = false,
    val nearbyDetailsTitle: String = StringUtils.EMPTY,
    val nearbyDetailsIcon: ImageVector = Icons.Filled.Train,
    val nearbyDetailsArrivals: NearbyResult = NearbyResult(),

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
    private val busService: BusService = BusService,
    private val bikeService: BikeService = BikeService,
    private val util: Util = Util,
    private val mapUtil: MapUtil = MapUtil,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun initModel(): MainViewModel {
        loadBusRoutesAndBike()
        return this
    }

    override fun newState(state: State) {
        Timber.i("new state ${state.alertStatus}")
        Favorites.refreshFavorites()
        if (state.busRoutesStatus == Status.SUCCESS) {
            uiState = uiState.copy(
                busRoutes = state.busRoutes,
                busRoutesShowError = false,
            )
            store.dispatch(ResetBusRoutesFavoritesAction())
        }
        if (state.busRoutesStatus == Status.FULL_FAILURE) {
            uiState = uiState.copy(busRoutesShowError = true)
            store.dispatch(ResetBusRoutesFavoritesAction())
        }

        if (state.bikeStationsStatus == Status.SUCCESS) {
            uiState = uiState.copy(
                bikeStations = state.bikeStations,
                bikeStationsShowError = false
            )
            store.dispatch(ResetBikeStationFavoritesAction())
        }
        if (state.bikeStationsStatus == Status.FULL_FAILURE) {
            uiState = uiState.copy(bikeStationsShowError = true)
            store.dispatch(ResetBikeStationFavoritesAction())
        }

        if (state.alertStatus == Status.SUCCESS) {
            uiState = uiState.copy(
                routesAlerts = state.alertsDTO,
                routeAlertErrorState = false,
                routeAlertShowError = false,
            )
            store.dispatch(ResetAlertsStatusAction())
        }

        if (state.alertStatus == Status.FAILURE || state.alertStatus == Status.FULL_FAILURE) {
            uiState = uiState.copy(
                routeAlertErrorState = true,
                routeAlertShowError = true,
            )
            store.dispatch(ResetAlertsStatusAction())
        }

        uiState = uiState.copy(isRefreshing = false)
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        store.dispatch(FavoritesAction())
    }

    fun loadBusRoutes() {
        store.dispatch(BusRoutesAction())
    }

    fun loadBikeStations() {
        store.dispatch(BikeStationAction())
    }

    fun resetBusRoutesShowError() {
        uiState = uiState.copy(busRoutesShowError = false)
    }

    fun resetBikeStationsShowError() {
        uiState = uiState.copy(bikeStationsShowError = false)
    }

    fun resetAlertsShowError() {
        uiState = uiState.copy(routeAlertShowError = false)
    }

    fun shouldLoadAlerts() {
        if (uiState.routesAlerts.isEmpty() && !uiState.routeAlertErrorState) {
            loadAlerts()
        }
    }

    fun loadAlerts() {
        uiState = uiState.copy(isRefreshing = true)
        store.dispatch(AlertAction())
    }

    fun startMarket(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse("market://details?id=fr.cph.chicago")
            startActivity(context, intent, null)
        } catch (ex: Exception) {
            Timber.e(ex, "Could not start market")
            uiState = uiState.copy(
                startMarket = false,
                startMarketFailed = true
            )
        }
    }

    fun resetRateMeFailed() {
        uiState = uiState.copy(startMarketFailed = false)
    }

    @SuppressLint("MissingPermission")
    fun refreshUserLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val position = Position(location.latitude, location.longitude)
                uiState = uiState.copy(nearbyIsMyLocationEnabled = true)
                setCurrentUserLocation(position)
                loadNearbyStations(position)
            } else {
                setDefaultUserLocation()
            }
        }
    }

    fun setDefaultUserLocation() {
        mainViewModel.setCurrentUserLocation(chicagoPosition)
        mainViewModel.loadNearbyStations(chicagoPosition)
        mainViewModel.setShowLocationError(true)
    }

    fun setMapCenterLocationAndLoadNearby(position: Position, zoom: Float) {
        mainViewModel.setCurrentUserLocation(position, zoom)
        mainViewModel.loadNearbyStations(position)
        mainViewModel.setShowLocationError(false)
    }

    fun setShowLocationError(value: Boolean) {
        uiState = uiState.copy(nearbyShowLocationError = value)
    }

    fun setShowNearbyDetails(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsShow = value)
    }

    fun loadNearbyTrainDetails(trainStation: TrainStation) {
        trainService.loadStationTrainArrival(trainStation.id)
            .map { trainArrival ->
                NearbyResult(arrivals = NearbyResult.toArrivals(trainArrival.trainEtas.filter { trainEta -> trainEta.trainStation.id == trainStation.id }))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = trainStation.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.Train,
                    )
                },
                // FIXME: Handle error
                { onError -> Timber.e(onError, "Error while loading train arrivals") })
    }

    fun loadNearbyBusDetails(busStop: BusStop) {
        busService.loadBusArrivals(busStop)
            .map { busArrivals -> NearbyResult(arrivals = NearbyResult.toArrivals(busArrivals)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = busStop.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBus,
                    )
                },
                // FIXME: Handle error
                { onError -> Timber.e(onError, "Error while loading bus arrivals") })
    }

    fun loadNearbyBikeDetails(currentBikeStation: BikeStation) {
        bikeService.findBikeStation(currentBikeStation.id)
            .map { bikeStation ->
                NearbyResult(
                    arrivals = NearbyResult.toArrivals(bikeStation),
                    lastUpdate = LastUpdate(TimeUtil.formatTimeDifference(bikeStation.lastReported, Calendar.getInstance().time))
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = currentBikeStation.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBike,
                    )
                },
                // FIXME: Handle error
                { onError -> Timber.e(onError, "Error while loading bus arrivals") })
    }


    private fun setCurrentUserLocation(position: Position, zoom: Float = 16f) {
        uiState = uiState.copy(
            nearbyMapCenterLocation = position,
            nearbyZoomIn = zoom,
        )
    }

    private fun loadNearbyStations(position: Position) {
        val trainStationAround = trainService.readNearbyStation(position = position)
        val busStopsAround = busService.busStopsAround(position = position)
        val bikeStationsAround = mapUtil.readNearbyStation(position = position, store.state.bikeStations)
        Single.zip(trainStationAround, busStopsAround, bikeStationsAround) { trains, buses, bikeStations ->
            uiState = uiState.copy(
                nearbyTrainStations = trains,
                nearbyBusStops = buses,
                nearbyBikeStations = bikeStations,
            )
            Any()
        }.subscribe({}, { error -> Timber.e(error) })
    }

    private fun loadBusRoutesAndBike() {
        store.dispatch(BusRoutesAndBikeStationAction())
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
