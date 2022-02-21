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
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.BusRoutesAction
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.ResetBikeStationFavoritesAction
import fr.cph.chicago.redux.ResetBusRoutesFavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import javax.inject.Inject
import org.rekotlin.StoreSubscriber
import timber.log.Timber

val mainViewModel = MainViewModel()

class MainActivityComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = mainViewModel.initModel()

        setContent {
            ChicagoCommutesTheme {
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
    var busRoutes: List<BusRoute> = listOf(),
    var busRoutesShowError: Boolean = false,
    var bikeStations: List<BikeStation> = listOf(),
    var bikeStationsShowError: Boolean = false,

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
