package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.composable.screen.SettingsViewModel
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
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
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

val mainViewModel = MainViewModel()
val settingsViewModel = SettingsViewModel().initModel()

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

    val routesAlerts: List<RoutesAlertsDTO> = listOf(),

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun initModel(): MainViewModel {
        loadBusRoutesAndBike()
        return this
    }

    override fun newState(state: State) {
        Timber.d("new state")
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
            uiState = uiState.copy(routesAlerts = state.alertsDTO)
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

    fun loadAlertsIfNeeded() {
        if (uiState.routesAlerts.isEmpty()) {
            loadAlerts()
        }
    }

    fun loadAlerts() {
        uiState = uiState.copy(isRefreshing = true)
        store.dispatch(AlertAction())
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
