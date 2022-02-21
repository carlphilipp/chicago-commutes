package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.ResetBikeStationFavoritesAction
import fr.cph.chicago.redux.ResetBusRoutesFavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

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
    var bikeStations: List<BikeStation> = listOf(),
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun initModel(): MainViewModel {
        store.dispatch(BusRoutesAndBikeStationAction())
        return this
    }

    override fun newState(state: State) {
        Timber.d("new state")
        Favorites.refreshFavorites()
        if (state.busRoutesStatus == Status.SUCCESS) {
            uiState = uiState.copy(busRoutes = state.busRoutes)
            store.dispatch(ResetBusRoutesFavoritesAction())
        }
        if (state.bikeStationsStatus == Status.SUCCESS) {
            uiState = uiState.copy(bikeStations = state.bikeStations)
            store.dispatch(ResetBikeStationFavoritesAction())
        }
        uiState = uiState.copy(isRefreshing = false)
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        store.dispatch(FavoritesAction())
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
