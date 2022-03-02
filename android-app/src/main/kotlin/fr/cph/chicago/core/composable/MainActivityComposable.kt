package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.vector.ImageVector
import fr.cph.chicago.core.composable.common.NearbyResult
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.mainViewModel
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.util.MapUtil.chicagoPosition

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
    val nearbyDetailsTitle: String = "",
    val nearbyDetailsIcon: ImageVector = Icons.Filled.Train,
    val nearbyDetailsArrivals: NearbyResult = NearbyResult(),
    val nearbyDetailsError: Boolean = false,

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)
