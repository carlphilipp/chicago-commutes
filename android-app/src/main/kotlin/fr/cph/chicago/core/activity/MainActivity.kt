package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App.Companion.exceptionHandler
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
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Turn off the decor fitting system windows (top and bottom)
        // It means that now we need to handle manually the top padding
        // This allows to have a somewhat fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val mainViewModel = mainViewModel
            val factory = SplashViewModel.provideFactory(
                ctaTrainKey = stringResource(R.string.cta_train_key),
                ctaBusKey = stringResource(R.string.cta_bus_key),
                googleStreetKey = stringResource(R.string.google_maps_api_key),
                owner = this,
                defaultArgs = savedInstanceState,
            )
            val splashViewModel = ViewModelProvider(this, factory)[SplashViewModel::class.java]

            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                SplashScreen(
                    show = !splashViewModel.uiState.isLoaded,
                    viewModel = splashViewModel,
                )
                Navigation(
                    show = splashViewModel.uiState.isLoaded,
                    mainViewModel = mainViewModel,
                    navigationViewModel = NavigationViewModel().initModel(rememberNavigationState()),
                    settingsViewModel = settingsViewModel,
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
    val busLazyListState: LazyListState = LazyListState(),
    val divvyLazyListState: LazyListState = LazyListState(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    show: Boolean,
    viewModel: SplashViewModel
) {
    Timber.d("Compose SplashScreen")
    if (show) {
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = Unit, block = {
            scope.launch(exceptionHandler) {
                viewModel.setUpDefaultSettings()
            }
        })

        Scaffold(
            snackbarHost = { SnackbarHostInsets(state = viewModel.uiState.snackbarHostState) },
            content = {
                LoadingView(show = !viewModel.uiState.isError)
                AnimatedErrorView(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
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
        if (uiState.isError) {
            uiState = uiState.copy(isError = false)
        }
        Single.fromCallable { realmConfig.setUpRealm() }
            .subscribeOn(Schedulers.computation())
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
        Timber.d("SplashViewModel new state thread: ${Thread.currentThread().name}")
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

    companion object {
        fun provideFactory(
            ctaTrainKey: String,
            ctaBusKey: String,
            googleStreetKey: String,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return SplashViewModel(ctaTrainKey, ctaBusKey, googleStreetKey) as T
                }
            }
    }
}
