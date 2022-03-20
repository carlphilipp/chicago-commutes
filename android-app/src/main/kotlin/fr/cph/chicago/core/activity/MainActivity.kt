package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
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
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.NearbyResult
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.viewmodel.mainViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.redux.BaseAction
import fr.cph.chicago.redux.DefaultSettingsAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.task.RefreshTaskLifecycleEventObserver
import fr.cph.chicago.util.MapUtil.chicagoPosition
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : CustomComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel = mainViewModel
            val baseViewModel = SplashViewModel(
                ctaTrainKey = stringResource(R.string.cta_train_key),
                ctaBusKey = stringResource(R.string.cta_bus_key),
                googleStreetKey = stringResource(R.string.google_maps_api_key),
            )

            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {

                SplashScreen(
                    show = !baseViewModel.uiState.isLoaded,
                    viewModel = baseViewModel,
                )

                Navigation(
                    show = baseViewModel.uiState.isLoaded,
                    mainViewModel = mainViewModel,
                    viewModel = NavigationViewModel().initModel(rememberNavigationState()),
                )

                DisposableEffect(key1 = mainViewModel) {
                    mainViewModel.onStart()
                    onDispose { mainViewModel.onStop() }
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
    val busRouteSearch: String = "",

    val bikeStations: List<BikeStation> = listOf(),
    val bikeStationsShowError: Boolean = false,
    val bikeSearch: String = "",

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
    val favLazyListState: LazyListState = LazyListState(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    show: Boolean,
    viewModel: SplashViewModel
) {
    if (show) {
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                viewModel.setUpDefaultSettings()
            }
        })

        Scaffold(
            snackbarHost = { SnackbarHostInsets(state = viewModel.uiState.snackbarHostState) },
            content = {
                LoadingView(show = !viewModel.uiState.isError)
                AnimatedErrorView(
                    visible = viewModel.uiState.isError,
                    onClick = { viewModel.setUpDefaultSettings() }
                )
                if (viewModel.uiState.showErrorSnackBar) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = viewModel.uiState.snackbarHostState,
                        showError = viewModel.uiState.showErrorSnackBar,
                        onComplete = {
                            viewModel.showHideSnackBar(false)
                        }
                    )
                }
            }
        )
    }
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier, show: Boolean) {
    AnimatedVisibility(
        visible = show,
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        // Re-implemented Icon because the scale is not an option for some reason
        val vec = ImageVector.vectorResource(id = R.drawable.skyline_vector)
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .toolingGraphicsLayer()
                .paint(
                    painter = rememberVectorPainter(vec),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                    contentScale = ContentScale.FillBounds
                )
        )
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.padding(20.dp))
                Text(
                    text = stringResource(id = R.string.progress_message),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {}
        }
    }
}

data class SplashUiState(
    val ctaTrainKey: String,
    val ctaBusKey: String,
    val googleStreetKey: String,
    val isError: Boolean = false,
    val showErrorSnackBar: Boolean = false,
    val isLoaded: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    ctaTrainKey: String,
    ctaBusKey: String,
    googleStreetKey: String,
    private val realmConfig: RealmConfig = RealmConfig
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(
        SplashUiState(
            ctaTrainKey = ctaTrainKey,
            ctaBusKey = ctaBusKey,
            googleStreetKey = googleStreetKey,
        )
    )
        private set

    fun showHideSnackBar(value: Boolean) {
        uiState = uiState.copy(showErrorSnackBar = value)
    }

    fun setUpDefaultSettings() {
        Single.fromCallable { realmConfig.setUpRealm() }
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .doOnSuccess {
                Timber.d("Realm setup, dispatching default settings action and base action")
                val defaultSettingsAction = DefaultSettingsAction(
                    ctaTrainKey = uiState.ctaTrainKey,
                    ctaBusKey = uiState.ctaBusKey,
                    googleStreetKey = uiState.googleStreetKey
                )
                store.dispatch(defaultSettingsAction)
                store.dispatch(BaseAction())
            }
            .subscribe(
                {
                    Timber.d("Realm setup properly")
                },
                { throwable ->
                    Timber.e(throwable, "Could not setup realm")
                    uiState = uiState.copy(
                        isError = true,
                        showErrorSnackBar = true,
                    )
                }
            )
    }

    override fun newState(state: State) {
        when (state.status) {
            Status.SUCCESS -> {
                store.unsubscribe(this)
                isLoaded()
            }
            Status.FAILURE -> {
                store.unsubscribe(this)
                isLoaded()
            }
            Status.FULL_FAILURE -> {
                uiState = uiState.copy(
                    isError = true,
                    showErrorSnackBar = true,
                )
            }
            else -> Timber.d("Unknown status on new state")
        }
    }

    private fun isLoaded() {
        uiState = uiState.copy(isLoaded = true)
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
