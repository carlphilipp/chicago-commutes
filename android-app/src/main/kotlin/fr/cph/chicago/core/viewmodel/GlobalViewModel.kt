package fr.cph.chicago.core.viewmodel

import android.content.Context
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.activity.MainUiState
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.ui.common.LocationViewModel
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
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

val settingsViewModel = SettingsViewModel().initModel()
val locationViewModel = LocationViewModel()

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
abstract class MainViewModel @Inject constructor() : ViewModel(), StoreSubscriber<State> {
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

    fun loadBusRoutesAndBike() {
        store.dispatch(BusRoutesAndBikeStationAction())
    }

    fun updateBottomSheet(component: @Composable ColumnScope.() -> Unit) {
        uiState = uiState.copy(bottomSheetContent = component)
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
