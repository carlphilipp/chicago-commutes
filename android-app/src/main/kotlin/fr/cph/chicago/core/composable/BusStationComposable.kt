package fr.cph.chicago.core.composable

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.util.ArrayMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.ShimmerAnimation
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.composable.common.StationDetailsImageView
import fr.cph.chicago.core.composable.common.StationDetailsTitleIconView
import fr.cph.chicago.core.composable.common.loadGoogleStreet
import fr.cph.chicago.core.composable.common.openMapApplication
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.ResetBusStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigInteger
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

class BusStationComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val busStopId = BigInteger(intent.getStringExtra(getString(R.string.bundle_bus_stop_id)) ?: "0")
        val busStopName = intent.getStringExtra(getString(R.string.bundle_bus_stop_name)) ?: StringUtils.EMPTY

        val busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        val busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY

        val bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        val boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY

        val busDetails = BusDetailsDTO(
            busRouteId = busRouteId,
            routeName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
            stopId = busStopId.toInt(),
            stopName = busStopName,
        )

        val viewModel = BusStationViewModel().initModel(busDetails = busDetails)

        store.dispatch(
            BusStopArrivalsAction(
                busRouteId = busRouteId,
                busStopId = busStopId,
                bound = bound,
                boundTitle = boundTitle
            )
        )

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                BusStationView(viewModel = viewModel)
            }
        }
    }
}

data class BusStationUiState(
    val busDetails: BusDetailsDTO = BusDetailsDTO(),
    val position: Position = Position(),
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(underlying = ArrayMap()),
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
class BusStationViewModel @Inject constructor(
    private val preferenceService: PreferenceService = PreferenceService,
    private val busService: BusService = BusService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BusStationUiState())
        private set

    fun initModel(busDetails: BusDetailsDTO): BusStationViewModel {
        val defaultedArrivals = ArrayMap<String, MutableList<BusArrival>>()
        defaultedArrivals["Unknown"] = mutableListOf()
        uiState = uiState.copy(
            busDetails = busDetails,
            isFavorite = isFavorite(busRouteId = busDetails.busRouteId, busStopId = busDetails.stopId, boundTitle = busDetails.boundTitle),
            busArrivalStopDTO = BusArrivalStopDTO(underlying = defaultedArrivals)
        )

        loadStopPositionAndGoogleStreetImage()
        return this
    }

    override fun newState(state: State) {
        Timber.d("new state ${state.busStopStatus}")
        when (state.busStopStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(
                    busArrivalStopDTO = state.busArrivalStopDTO,
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
        uiState = uiState.copy(isRefreshing = true)
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
        }
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

    private fun loadGoogleStreetImage(position: Position) {
        loadGoogleStreet(
            position = position,
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

    private fun loadStopPositionAndGoogleStreetImage() {
        // Load bus position and google street image
        busService.getStopPosition(uiState.busDetails.busRouteId, uiState.busDetails.boundTitle, uiState.busDetails.stopId.toBigInteger())
            .observeOn(Schedulers.computation())
            .doOnSuccess { position -> loadGoogleStreetImage(position) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { position ->
                    uiState = uiState.copy(position = Position(position.latitude, position.longitude))
                },
                { throwable ->
                    Timber.e(throwable, "Error while loading bus position and google street image")
                    uiState = uiState.copy(
                        isGoogleStreetImageLoading = false,
                        showGoogleStreetImage = false,
                    )
                })
    }

    private fun isFavorite(busRouteId: String, busStopId: Int, boundTitle: String): Boolean {
        return preferenceService.isStopFavorite(busRouteId, BigInteger(busStopId.toString()), boundTitle)
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
fun BusStationView(
    modifier: Modifier = Modifier,
    viewModel: BusStationViewModel,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val activity = (LocalLifecycleOwner.current as ComponentActivity)
    val context = LocalContext.current
    val busArrivalsKeys = uiState.busArrivalStopDTO.keys.toList()

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
                            title = "${uiState.busDetails.busRouteId} - ${uiState.busDetails.routeName}",
                            subTitle = uiState.busDetails.boundTitle,
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = {
                                viewModel.switchFavorite(
                                    boundTitle = uiState.busDetails.boundTitle,
                                    busStopId = uiState.busDetails.stopId,
                                    busRouteId = uiState.busDetails.busRouteId,
                                    busRouteName = uiState.busDetails.routeName,
                                    busStopName = uiState.busDetails.stopName
                                )
                            },
                            onMapClick = {
                                viewModel.openMap(context = context, scope = scope)
                            }
                        )
                    }
                    items(busArrivalsKeys.size) { index ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.padding(bottom = 3.dp))
                            val destination = busArrivalsKeys[index]
                            val arrivals = uiState.busArrivalStopDTO[busArrivalsKeys[index]]!!
                            Text(
                                text = uiState.busDetails.stopName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.showBusArrivalData) {
                                    Text(
                                        text = if (arrivals.size == 0) destination else "To $destination",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                } else {
                                    ShimmerAnimation(width = 100.dp, height = 25.dp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.showBusArrivalData) {
                                        arrivals.forEach { busArrival ->
                                            var currentTime by remember { mutableStateOf(busArrival.formatArrivalTime()) }
                                            currentTime = busArrival.formatArrivalTime()
                                            AnimatedText(
                                                text = currentTime,
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                    } else {
                                        ShimmerAnimation(width = 100.dp, height = 25.dp)
                                    }
                                }
                            }
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
