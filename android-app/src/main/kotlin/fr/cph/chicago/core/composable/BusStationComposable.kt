package fr.cph.chicago.core.composable

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
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
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.ShimmerAnimation
import fr.cph.chicago.core.composable.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.composable.common.StationDetailsImageView
import fr.cph.chicago.core.composable.common.StationDetailsTitleIconView
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import java.math.BigInteger
import java.util.Locale
import javax.inject.Inject

class BusStationComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val busStopId = BigInteger(intent.getStringExtra(getString(R.string.bundle_bus_stop_id)) ?: "0")
        val busStopName = intent.getStringExtra(getString(R.string.bundle_bus_stop_name)) ?: StringUtils.EMPTY

        val busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        val busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY

        val bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        val boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY

        val viewModel = BusStationViewModel().initModel(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
            busStopId = busStopId,
            busStopName = busStopName,
        )

        store.dispatch(
            BusStopArrivalsAction(
                busRouteId = busRouteId,
                busStopId = busStopId,
                bound = bound,
                boundTitle = boundTitle
            )
        )

        setContent {
            ChicagoCommutesTheme {
                BusStationView(viewModel = viewModel)
            }
        }
    }
}

data class BusStationUiState(
    val busRouteId: String = "",
    val busRouteName: String = "",
    val bound: String = "",
    val boundTitle: String = "",
    val busStopId: BigInteger = BigInteger.ZERO,
    val position: Position = Position(),
    val busStopName: String = "",

    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(underlying = ArrayMap()),
    val isFavorite: Boolean = false,
    val isRefreshing: Boolean = false,
    val applyFavorite: Boolean = false,
    val showBusArrivalData: Boolean = false,
    val googleStreetMapImage: Drawable = ShapeDrawable(),
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class BusStationViewModel @Inject constructor(
    private val googleStreetClient: GoogleStreetClient = GoogleStreetClient,
    private val preferenceService: PreferenceService = PreferenceService,
    private val busService: BusService = BusService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BusStationUiState())
        private set

    fun initModel(busRouteId: String, busRouteName: String, bound: String, boundTitle: String, busStopId: BigInteger, busStopName: String): BusStationViewModel {
        val defaultedArrivals = ArrayMap<String, MutableList<BusArrival>>()
        defaultedArrivals["Unknown"] = mutableListOf()
        uiState = uiState.copy(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
            busStopId = busStopId,
            busStopName = busStopName,
            isFavorite = isFavorite(busRouteId = busRouteId, busStopId = busStopId, boundTitle = boundTitle),
            busArrivalStopDTO = BusArrivalStopDTO(underlying = defaultedArrivals)
        )

        loadStopDetailsAndStreetImage()
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
                // TODO
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

    fun switchFavorite(busRouteId: String, busStopId: BigInteger, boundTitle: String, busRouteName: String, busStopName: String) {
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
                busRouteId = uiState.busRouteId,
                busStopId = uiState.busStopId,
                bound = uiState.bound,
                boundTitle = uiState.boundTitle
            )
        )
    }

    fun resetApplyFavorite() {
        uiState = uiState.copy(applyFavorite = false)
    }

    fun openMap(context: Context, scope: CoroutineScope) {
        // TODO: This is probably duplicated code that should be merged
        // TODO: show pin in google map or do not start other app, just do it within our app
        val uri = String.format(Locale.ENGLISH, "geo:%f,%f", uiState.position.latitude, uiState.position.longitude)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            scope.launch {
                uiState.snackbarHostState.showSnackbar("Could not find any Map application on device")
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun loadStopDetailsAndStreetImage() {
        // Load bus stop details and google street image
        busService.loadAllBusStopsForRouteBound(uiState.busRouteId, uiState.boundTitle)
            .observeOn(Schedulers.computation())
            .flatMap { stops ->
                val busStop: BusStop? = stops.firstOrNull { busStop -> busStop.id == uiState.busStopId }
                Single.just(busStop!!)
            }
            .map { busStop ->
                loadGoogleStreetImage(busStop.position)
                uiState = uiState.copy(
                    position = Position(
                        busStop.position.latitude,
                        busStop.position.longitude
                    )
                )
                busStop
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { throwable ->
                Timber.e(throwable, "Error while loading street image and stop details")
                //onError()
            }
            .subscribe(
                { busStop ->
                    //toolbar.title = "$busRouteId - ${busStop.name}"
/*                    streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
                    binding.header.favorites.mapContainer.setOnClickListener(OpenMapOnClickListener(position.latitude, position.longitude))
                    binding.header.favorites.walkContainer.setOnClickListener(OpenMapDirectionOnClickListener(position.latitude, position.longitude))*/
                },
                { throwable ->
                    Timber.e(throwable, "Error while loading street image and stop details")
                    // onError()
                })
    }


    @SuppressLint("CheckResult")
    private fun loadGoogleStreetImage(position: Position) {
        googleStreetClient.getImage(position.latitude, position.longitude, 1000, 400)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    uiState = uiState.copy(
                        googleStreetMapImage = drawable,
                        showGoogleStreetImage = true,
                    )
                },
                { error ->
                    // TODO: If that failed, we need to retry when the user refreshes data
                    Timber.e(error, "Error while loading street view image")
                }
            )
    }

    private fun isFavorite(busRouteId: String, busStopId: BigInteger, boundTitle: String): Boolean {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
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
                        )
                    }
                    item {
                        StationDetailsTitleIconView(
                            title = "${uiState.busRouteId} - ${uiState.busRouteName}",
                            subTitle = uiState.boundTitle,
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = {
                                viewModel.switchFavorite(
                                    boundTitle = uiState.boundTitle,
                                    busStopId = uiState.busStopId,
                                    busRouteId = uiState.busRouteId,
                                    busRouteName = uiState.busRouteName,
                                    busStopName = uiState.busStopName
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
                                text = uiState.busStopName,
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
                                                time = currentTime,
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

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}
