package fr.cph.chicago.core.composable

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.composable.common.LocationViewModel
import fr.cph.chicago.core.composable.common.NearbyResult
import fr.cph.chicago.core.composable.screen.SettingsViewModel
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.mainViewModel
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
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
    val nearbyDetailsError: Boolean = false,

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)
