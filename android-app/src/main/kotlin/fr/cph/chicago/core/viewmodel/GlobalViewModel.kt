package fr.cph.chicago.core.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.activity.MainUiState
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.core.ui.common.NearbyResult
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
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
import fr.cph.chicago.util.TimeUtil
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val settingsViewModel = SettingsViewModel().initModel()
val locationViewModel = LocationViewModel()

@HiltViewModel
abstract class MainViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
    private val busService: BusService = BusService,
    private val bikeService: BikeService = BikeService,
    private val mapUtil: MapUtil = MapUtil,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(MainUiState())
        internal set

    override fun newState(state: State) {
        Timber.d("MainViewModel new state thread: ${Thread.currentThread().name}")
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

    fun updateBusRouteSearch(search: String) {
        uiState = uiState.copy(
            busRouteSearch = search
        )
    }

    fun updateBikeSearch(search: String) {
        uiState = uiState.copy(
            bikeSearch = search
        )
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

    abstract fun startMarket(context: Context)

    fun resetRateMeFailed() {
        uiState = uiState.copy(startMarketFailed = false)
    }

    fun setNearbyIsMyLocationEnabled(value: Boolean) {
        uiState = uiState.copy(nearbyIsMyLocationEnabled = value)
    }

    fun setDefaultUserLocation() {
        mainViewModel.setCurrentUserLocation(MapUtil.chicagoPosition)
        mainViewModel.loadNearbyStations(MapUtil.chicagoPosition)
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

    fun setNearbyDetailsError(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsError = value)
    }

    fun setShowNearbyDetails(value: Boolean) {
        uiState = uiState.copy(nearbyDetailsShow = value)
    }

    fun loadNearbyTrainDetails(trainStation: TrainStation) {
        trainService.loadStationTrainArrival(trainStation.id)
            .map { trainArrival ->
                NearbyResult(arrivals = NearbyResult.toArrivals(trainArrival.trainEtas.filter { trainEta -> trainEta.trainStation.id == trainStation.id }))
            }
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = trainStation.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.Train,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading train arrivals")
                    setNearbyDetailsError(true)
                })
    }

    fun loadNearbyBusDetails(busStop: BusStop) {
        busService.loadBusArrivals(busStop)
            .map { busArrivals -> NearbyResult(arrivals = NearbyResult.toArrivals(busArrivals)) }
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = busStop.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBus,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading bus arrivals")
                    setNearbyDetailsError(true)
                })
    }

    fun loadNearbyBikeDetails(currentBikeStation: BikeStation) {
        bikeService.findBikeStation(currentBikeStation.id)
            .map { bikeStation ->
                NearbyResult(
                    arrivals = NearbyResult.toArrivals(bikeStation),
                    lastUpdate = LastUpdate(TimeUtil.formatTimeDifference(bikeStation.lastReported, Calendar.getInstance().time))
                )
            }
            .observeOn(Schedulers.computation())
            .subscribe(
                {
                    uiState = uiState.copy(
                        nearbyDetailsTitle = currentBikeStation.name,
                        nearbyDetailsArrivals = it,
                        nearbyDetailsShow = true,
                        nearbyDetailsIcon = Icons.Filled.DirectionsBike,
                    )
                },
                { onError ->
                    Timber.e(onError, "Error while loading bus arrivals")
                    setNearbyDetailsError(true)
                })
    }


    fun setCurrentUserLocation(position: Position, zoom: Float = 16f) {
        uiState = uiState.copy(
            nearbyMapCenterLocation = position,
            nearbyZoomIn = zoom,
        )
    }

    fun loadNearbyStations(position: Position) {
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

    fun loadBusRoutesAndBike() {
        store.dispatch(BusRoutesAndBikeStationAction())
    }

    fun onStart() {
        val current = this
        viewModelScope.launch(Dispatchers.Default) {
            store.subscribe(current)
        }
    }

    fun onStop() {
        val current = this
        viewModelScope.launch(Dispatchers.Default) {
            store.unsubscribe(current)
        }
    }
}
