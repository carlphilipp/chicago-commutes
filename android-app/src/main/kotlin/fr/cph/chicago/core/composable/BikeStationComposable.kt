package fr.cph.chicago.core.composable

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.composable.common.StationDetailsImageView
import fr.cph.chicago.core.composable.common.StationDetailsTitleIconView
import fr.cph.chicago.core.composable.common.loadGoogleStreet
import fr.cph.chicago.core.composable.common.openMapApplication
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.ResetBusStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import org.rekotlin.StoreSubscriber
import timber.log.Timber

class BikeStationComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bikeStation = intent.extras?.getParcelable(getString(R.string.bundle_bike_station)) ?: BikeStation.buildUnknownStation()

        val viewModel = BikeStationViewModel().initModel(bikeStation = bikeStation)

        setContent {
            ChicagoCommutesTheme {
                BikeStationView(viewModel = viewModel)
            }
        }
    }
}

data class BikeStationUiState(
    val bikeStation: BikeStation = BikeStation.buildUnknownStation(),
    val position: Position = Position(),
    val isFavorite: Boolean = false,
    val isRefreshing: Boolean = false,
    val applyFavorite: Boolean = false,
    val showBusArrivalData: Boolean = false,
    val googleStreetMapImage: Drawable = ShapeDrawable(),
    val isGoogleStreetImageLoading: Boolean = true,
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val showErrorMessage: Boolean = false,
)

@HiltViewModel
class BikeStationViewModel @Inject constructor(
    private val preferenceService: PreferenceService = PreferenceService,
    private val busService: BusService = BusService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BikeStationUiState())
        private set

    fun initModel(bikeStation: BikeStation): BikeStationViewModel {
        uiState = uiState.copy(bikeStation = bikeStation)

        loadGoogleStreetImage(bikeStation.latitude, bikeStation.longitude)
        return this
    }

    override fun newState(state: State) {
        Timber.d("new state ${state.busStopStatus}")
        when (state.busStopStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(
                    //busArrivalStopDTO = state.busArrivalStopDTO,
                    showBusArrivalData = true
                )
                store.dispatch(ResetBusStationStatusAction())
            }
            Status.FAILURE -> {
                uiState = uiState.copy(
                    showBusArrivalData = true,
                    showErrorMessage = true,
                )
                store.dispatch(ResetBusStationStatusAction())
            }
            Status.ADD_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = true,
                    applyFavorite = true,
                )

                store.dispatch(ResetBusStationStatusAction())
            }
            Status.REMOVE_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = false,
                    applyFavorite = true,
                )
                store.dispatch(ResetBusStationStatusAction())
            }
            else -> Timber.d("Status not handled")
        }
        uiState = uiState.copy(isRefreshing = false)
    }

    fun switchFavorite(busRouteId: String, busStopId: Int, boundTitle: String, busRouteName: String, busStopName: String) {
        if (isFavorite(busRouteId = busRouteId, busStopId = busStopId, boundTitle = boundTitle)) {
            store.dispatch(RemoveBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle))
        } else {
            store.dispatch(AddBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle, busRouteName, busStopName))
        }
    }

    fun refresh() {
        /*uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        store.dispatch(
            BusStopArrivalsAction(
                busRouteId = uiState.busDetails.busRouteId,
                busStopId = BigInteger(uiState.busDetails.stopId.toString()),
                bound = uiState.busDetails.bound,
                boundTitle = uiState.busDetails.boundTitle
            )
        )
        if (!isPositionSetup()) {
            Timber.d("Trying to reload stop position and google street image")
            loadStopPositionAndGoogleStreetImage()
        } else if (!isGoogleMapImageLoaded()) {
            Timber.d("Trying to reload google street image")
            loadGoogleStreetImage(uiState.position)
        }*/
    }

    fun resetApplyFavorite() {
        uiState = uiState.copy(applyFavorite = false)
    }

    fun resetShowErrorMessage() {
        uiState = uiState.copy(showErrorMessage = false)
    }

    fun openMap(context: Context, scope: CoroutineScope) {
        openMapApplication(
            context = context,
            scope = scope,
            snackbarHostState = uiState.snackbarHostState,
            latitude = uiState.position.latitude,
            longitude = uiState.position.longitude,
        )
    }

    private fun loadGoogleStreetImage(latitude: Double, longitude: Double) {
        loadGoogleStreet(
            position = Position(latitude = latitude, longitude = longitude),
            onSuccess = { drawable ->
                uiState = uiState.copy(
                    googleStreetMapImage = drawable,
                    isGoogleStreetImageLoading = false,
                    showGoogleStreetImage = true,
                )
            },
            onError = { throwable ->
                Timber.e(throwable, "Error while loading street view image")
                uiState = uiState.copy(
                    isGoogleStreetImageLoading = false,
                    showGoogleStreetImage = false,
                )
            }
        )
    }

    private fun isGoogleMapImageLoaded(): Boolean {
        return !uiState.isGoogleStreetImageLoading && uiState.showGoogleStreetImage
    }

    private fun isPositionSetup(): Boolean {
        return uiState.position != Position()
    }

    private fun isFavorite(busRouteId: String, busStopId: Int, boundTitle: String): Boolean {
        //return preferenceService.isStopFavorite(busRouteId, BigInteger(busStopId.toString()), boundTitle)
        return false
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeStationView(
    modifier: Modifier = Modifier,
    viewModel: BikeStationViewModel,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val activity = (LocalLifecycleOwner.current as ComponentActivity)
    val context = LocalContext.current
    //val busArrivalsKeys = uiState.busArrivalStopDTO.keys.toList()

    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(uiState.isRefreshing),
        onRefresh = { viewModel.refresh() },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
            content = {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        StationDetailsImageView(
                            activity = activity,
                            showGoogleStreetImage = uiState.showGoogleStreetImage,
                            googleStreetMapImage = uiState.googleStreetMapImage,
                            isLoading = uiState.isGoogleStreetImageLoading,
                        )
                    }
                    item {
                        StationDetailsTitleIconView(
                            title = "${uiState.bikeStation.id} - ${uiState.bikeStation.name}",
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = {
                                /*viewModel.switchFavorite(
                                    boundTitle = uiState.busDetails.boundTitle,
                                    busStopId = uiState.busDetails.stopId,
                                    busRouteId = uiState.busDetails.busRouteId,
                                    busRouteName = uiState.busDetails.routeName,
                                    busStopName = uiState.busDetails.stopName
                                )*/
                            },
                            onMapClick = {
                                viewModel.openMap(context = context, scope = scope)
                            }
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.padding(bottom = 3.dp))
                            val destination = uiState.bikeStation.name
                            Text(
                                text = uiState.bikeStation.address,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            })
    }

    if (uiState.applyFavorite) {
        viewModel.resetApplyFavorite()
        ShowFavoriteSnackBar(
            scope = scope,
            snackbarHostState = viewModel.uiState.snackbarHostState,
            isFavorite = viewModel.uiState.isFavorite,
        )
    }

    if (uiState.showErrorMessage) {
        viewModel.resetShowErrorMessage()
        ShowErrorMessageSnackBar(
            scope = scope,
            snackbarHostState = viewModel.uiState.snackbarHostState,
            showErrorMessage = uiState.showErrorMessage
        )
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}
