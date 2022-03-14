package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.navigation.Navigation
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.navigation.rememberNavigationState
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.ui.common.NearbyResult
import fr.cph.chicago.core.viewmodel.mainViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.task.RefreshTaskLifecycleEventObserver
import fr.cph.chicago.util.MapUtil.chicagoPosition
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : CustomComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("MainActivity started")

        val viewModel = mainViewModel.initModel()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                val navigationUiState = rememberNavigationState()
                Navigation(
                    viewModel = NavigationViewModel().initModel(navigationUiState),
                )

                DisposableEffect(key1 = viewModel) {
                    viewModel.onStart()
                    onDispose { viewModel.onStop() }
                }
            }
        }
        lifecycle.addObserver(RefreshTaskLifecycleEventObserver())
    }
}

// FIXME: This should probably be multiple states/viewmodels
data class MainUiState(
    val isRefreshing: Boolean = false,

    val busRoutes: List<BusRoute> = listOf(),
    val busRoutesShowError: Boolean = false,
    val busSearch: TextFieldValue = TextFieldValue(""),

    val bikeStations: List<BikeStation> = listOf(),
    val bikeStationsShowError: Boolean = false,
    val bikeSearch: TextFieldValue = TextFieldValue(""),

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
    val nearbyDetailsTitle: String = "",
    val nearbyDetailsIcon: ImageVector = Icons.Filled.Train,
    val nearbyDetailsArrivals: NearbyResult = NearbyResult(),
    val nearbyDetailsError: Boolean = false,

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val favoriteListState: LazyListState = LazyListState(),
    val divvyListState: LazyListState = LazyListState(),
    val busListState: LazyListState = LazyListState(),
)
