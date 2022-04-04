package fr.cph.chicago.core.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BikeStation.Companion.DEFAULT_AVAILABLE
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedText
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.StationDetailsImageView
import fr.cph.chicago.core.ui.common.StationDetailsTitleIconView
import fr.cph.chicago.core.ui.common.SwipeRefreshThemed
import fr.cph.chicago.core.ui.common.loadGoogleStreet
import fr.cph.chicago.core.ui.common.openExternalMapApplication
import fr.cph.chicago.redux.AddBikeFavoriteAction
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.RemoveBikeFavoriteAction
import fr.cph.chicago.redux.ResetBikeStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.TimeUtil
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeStationScreen(
    modifier: Modifier = Modifier,
    viewModel: BikeStationViewModel,
    navigationViewModel: NavigationViewModel,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            viewModel.load()
        }
    })

    Column {
        DisplayTopBar(
            screen = Screen.DivvyDetails,
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
                                title = uiState.bikeStation.name,
                                subTitle = "Last updated: ${TimeUtil.formatTimeDifference(uiState.bikeStation.lastReported, Calendar.getInstance().time)}",
                                isFavorite = uiState.isFavorite,
                                onFavoriteClick = { viewModel.switchFavorite() },
                                onMapClick = { viewModel.openMap(context = context, scope = scope) }
                            )
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.padding(bottom = 3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.bike_available_bikes),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                        var availableBikes by remember { mutableStateOf(uiState.bikeStation.availableBikes.toString()) }
                                        availableBikes = if (uiState.bikeStation.availableBikes == -1) "?" else uiState.bikeStation.availableBikes.toString()
                                        AnimatedText(
                                            text = availableBikes,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.bike_available_docks),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                        var availableDocks by remember { mutableStateOf(uiState.bikeStation.availableDocks.toString()) }
                                        availableDocks = if (uiState.bikeStation.availableDocks == -1) "?" else uiState.bikeStation.availableDocks.toString()

                                        AnimatedText(
                                            text = availableDocks,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
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


data class BikeStationUiState(
    val stationId: String,
    val bikeStation: BikeStation = BikeStation.buildUnknownStation(),
    val position: Position = Position(),
    val isFavorite: Boolean = false,
    val isRefreshing: Boolean = false,
    val applyFavorite: Boolean = false,
    val googleStreetMapImage: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
    val isGoogleStreetImageLoading: Boolean = true,
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val showErrorMessage: Boolean = false,
)

@HiltViewModel
class BikeStationViewModel @Inject constructor(
    stationId: String,
    private val bikeService: BikeService = BikeService,
    private val preferenceService: PreferenceService = PreferenceService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BikeStationUiState(stationId = stationId))
        private set

    fun load() {
        Single.fromCallable {
            store.state.bikeStations
                .find { bikeStation -> bikeStation.id == uiState.stationId }
                ?: bikeService.createEmptyBikeStation(uiState.stationId)
        }
            .map { bikeStation ->
                uiState = uiState.copy(
                    bikeStation = bikeStation,
                    isFavorite = isFavorite(bikeStation.id),
                )
                bikeStation
            }
            .map { bikeStation ->
                if (canLoadGoogleMapImage()) {
                    loadGoogleStreetImage(bikeStation.latitude, bikeStation.longitude)
                } else {
                    uiState = uiState.copy(
                        isGoogleStreetImageLoading = false,
                        showGoogleStreetImage = false,
                    )
                }
            }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                {}, { Timber.e(it, "Could not init bike station screen") }
            )
    }

    override fun newState(state: State) {
        Timber.d("BikeStationViewModel new state ${state.busStopStatus} thread: ${Thread.currentThread().name}")
        when (state.bikeStationsStatus) {
            Status.SUCCESS -> {
                val bikeStation = store.state.bikeStations.firstOrNull { bikeStation ->
                    bikeStation.id == uiState.bikeStation.id
                }
                uiState = if (bikeStation == null) {
                    uiState.copy(showErrorMessage = true)
                } else {
                    uiState.copy(bikeStation = bikeStation)
                }
                if (shouldLoadGoogleMapImage()) {
                    loadGoogleStreetImage(uiState.bikeStation.latitude, uiState.bikeStation.longitude)
                }
                store.dispatch(ResetBikeStationStatusAction())
            }
            Status.FULL_FAILURE,
            Status.FAILURE -> {
                uiState = uiState.copy(
                    showErrorMessage = true,
                )
                store.dispatch(ResetBikeStationStatusAction())
            }
            Status.ADD_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = true,
                    applyFavorite = true,
                )
                store.dispatch(ResetBikeStationStatusAction())
            }
            Status.REMOVE_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = false,
                    applyFavorite = true,
                )
                store.dispatch(ResetBikeStationStatusAction())
            }
            else -> Timber.d("Status not handled")
        }
        uiState = uiState.copy(isRefreshing = false)
    }

    fun switchFavorite() {
        if (isFavorite(uiState.bikeStation.id)) {
            store.dispatch(RemoveBikeFavoriteAction(uiState.bikeStation.id))
        } else {
            store.dispatch(AddBikeFavoriteAction(uiState.bikeStation.id, uiState.bikeStation.name))
        }
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        store.dispatch(BikeStationAction())
        if (!isGoogleMapImageLoaded() && canLoadGoogleMapImage()) {
            Timber.d("Trying to reload google street image")
            loadGoogleStreetImage(uiState.bikeStation.latitude, uiState.bikeStation.longitude)
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

    private fun canLoadGoogleMapImage(): Boolean {
        return uiState.bikeStation.availableBikes != DEFAULT_AVAILABLE
    }

    private fun shouldLoadGoogleMapImage(): Boolean {
        return canLoadGoogleMapImage() && !uiState.isGoogleStreetImageLoading && !uiState.showGoogleStreetImage
    }

    private fun isFavorite(id: String): Boolean {
        return preferenceService.isBikeStationFavorite(id)
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }

    companion object {
        fun provideFactory(
            stationId: String,
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
                    return BikeStationViewModel(stationId) as T
                }
            }
    }
}
