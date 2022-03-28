package fr.cph.chicago.core.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.ArrayMap
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedText
import fr.cph.chicago.core.ui.common.ShimmerAnimation
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.StationDetailsImageView
import fr.cph.chicago.core.ui.common.StationDetailsTitleIconView
import fr.cph.chicago.core.ui.common.SwipeRefreshThemed
import fr.cph.chicago.core.ui.common.loadGoogleStreet
import fr.cph.chicago.core.ui.common.openExternalMapApplication
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.ResetBusStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusStationScreen(
    modifier: Modifier = Modifier,
    viewModel: BusStationViewModel,
    navigationViewModel: NavigationViewModel,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val busArrivalsKeys = uiState.busArrivalStopDTO.keys.toList()

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            viewModel.loadData()
            viewModel.loadStopPositionAndGoogleStreetImage()
            viewModel.setFavorite()
        }
    })

    Column {
        DisplayTopBar(
            screen = Screen.BusDetails,
            viewModel = navigationViewModel
        )

        SwipeRefreshThemed(
            modifier = modifier,
            swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = { viewModel.refresh() },
        ) {
            Scaffold(
                snackbarHost = { SnackbarHostInsets(state = uiState.snackbarHostState) },
                content = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            StationDetailsImageView(
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
                                        busStopId = uiState.busDetails.stopId.toString(),
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
            ShowFavoriteSnackBar(
                scope = scope,
                snackbarHostState = viewModel.uiState.snackbarHostState,
                isFavorite = viewModel.uiState.isFavorite,
                onComplete = {
                    viewModel.resetApplyFavorite()
                }
            )
        }

        if (uiState.showErrorMessage) {
            ShowErrorMessageSnackBar(
                scope = scope,
                snackbarHostState = viewModel.uiState.snackbarHostState,
                showError = uiState.showErrorMessage,
                onComplete = {
                    viewModel.resetShowErrorMessage()
                }
            )
        }

        DisposableEffect(key1 = viewModel) {
            viewModel.onStart()
            onDispose { viewModel.onStop() }
        }
    }
}

data class BusStationUiState(
    private val defaultedArrivals: ArrayMap<String, MutableList<BusArrival>> = ArrayMap<String, MutableList<BusArrival>>(),
    val busDetails: BusDetailsDTO = BusDetailsDTO(),
    val position: Position = Position(),
    val busArrivalStopDTO: BusArrivalStopDTO = BusArrivalStopDTO(underlying = defaultedArrivals),
    val isFavorite: Boolean = false,
    val isRefreshing: Boolean = false,
    val applyFavorite: Boolean = false,
    val showBusArrivalData: Boolean = false,
    val googleStreetMapImage: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
    val isGoogleStreetImageLoading: Boolean = true,
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val showErrorMessage: Boolean = false,
) {
    init {
        defaultedArrivals["Unknown"] = mutableListOf()
    }
}

@HiltViewModel
class BusStationViewModel @Inject constructor(
    busDetails: BusDetailsDTO,
    private val preferenceService: PreferenceService = PreferenceService,
    private val busService: BusService = BusService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BusStationUiState(busDetails = busDetails))
        private set

    override fun newState(state: State) {
        Timber.d("BusStationViewModel new state ${state.busStopStatus} thread: ${Thread.currentThread().name}")
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

    fun switchFavorite(busRouteId: String, busStopId: String, boundTitle: String, busRouteName: String, busStopName: String) {
        Single.fromCallable {
            isFavorite(uiState.busDetails.busRouteId, uiState.busDetails.stopId.toString(), uiState.busDetails.boundTitle)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { isFavorite ->
                    if (isFavorite) {
                        store.dispatch(RemoveBusFavoriteAction(busRouteId, busStopId, boundTitle))
                    } else {
                        store.dispatch(AddBusFavoriteAction(busRouteId, busStopId, boundTitle, busRouteName, busStopName))
                    }
                },
                {
                    Timber.e(it, "Could not obtain favorite data")
                }
            )
    }

    fun loadData() {
        store.dispatch(
            BusStopArrivalsAction(
                busRouteId = uiState.busDetails.busRouteId,
                busStopId = uiState.busDetails.stopId.toString(),
                bound = uiState.busDetails.bound,
                boundTitle = uiState.busDetails.boundTitle
            )
        )
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        loadData()
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
        openExternalMapApplication(
            context = context,
            scope = scope,
            snackbarHostState = uiState.snackbarHostState,
            latitude = uiState.position.latitude,
            longitude = uiState.position.longitude,
        )
    }

    fun loadStopPositionAndGoogleStreetImage() {
        // Load bus position and google street image
        busService.getStopPosition(uiState.busDetails.busRouteId, uiState.busDetails.boundTitle, uiState.busDetails.stopId.toString())
            .observeOn(Schedulers.computation())
            .doOnSuccess { position -> loadGoogleStreetImage(position) }
            .observeOn(Schedulers.computation())
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

    fun setFavorite() {
        Single.fromCallable {
            isFavorite(uiState.busDetails.busRouteId, uiState.busDetails.stopId.toString(), uiState.busDetails.boundTitle)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    uiState = uiState.copy(isFavorite = result)
                },
                {
                    Timber.e(it, "Could not obtain favorite data")
                }
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

    private fun isFavorite(busRouteId: String, busStopId: String, boundTitle: String): Boolean {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }

    companion object {
        fun provideFactory(
            busStopId: String,
            busStopName: String,
            busRouteId: String,
            busRouteName: String,
            bound: String,
            boundTitle: String,
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
                    val busDetails = BusDetailsDTO(
                        busRouteId = busRouteId,
                        routeName = busRouteName,
                        bound = bound,
                        boundTitle = boundTitle,
                        stopId = busStopId.toInt(),
                        stopName = busStopName,
                    )
                    return BusStationViewModel(busDetails) as T
                }
            }
    }
}
