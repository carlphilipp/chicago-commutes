package fr.cph.chicago.core.activity

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
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
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.composable.common.StationDetailsImageView
import fr.cph.chicago.core.composable.common.StationDetailsTitleIconView
import fr.cph.chicago.core.composable.common.loadGoogleStreet
import fr.cph.chicago.core.composable.common.openMapApplication
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BikeStation.Companion.DEFAULT_AVAILABLE
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.redux.AddBikeFavoriteAction
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.RemoveBikeFavoriteAction
import fr.cph.chicago.redux.ResetBikeStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.TimeUtil
import java.util.Calendar
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
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
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
    val googleStreetMapImage: Drawable = ShapeDrawable(),
    val isGoogleStreetImageLoading: Boolean = true,
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val showErrorMessage: Boolean = false,
)

@HiltViewModel
class BikeStationViewModel @Inject constructor(
    private val preferenceService: PreferenceService = PreferenceService,
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BikeStationUiState())
        private set

    fun initModel(bikeStation: BikeStation): BikeStationViewModel {
        uiState = uiState.copy(
            bikeStation = bikeStation,
            isFavorite = isFavorite(bikeStation.id),
        )

        if (canLoadGoogleMapImage()) {
            loadGoogleStreetImage(bikeStation.latitude, bikeStation.longitude)
        } else {
            uiState = uiState.copy(
                isGoogleStreetImageLoading = false,
                showGoogleStreetImage = false,
            )
        }
        return this
    }

    override fun newState(state: State) {
        Timber.d("new state ${state.busStopStatus}")
        when (state.bikeStationsStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(
                    bikeStation = store.state.bikeStations.first { bikeStation -> bikeStation.id == uiState.bikeStation.id }
                )
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
                                    text = "Available bikes",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    var availableBikes by remember { mutableStateOf(uiState.bikeStation.availableBikes.toString()) }
                                    availableBikes = uiState.bikeStation.availableBikes.toString()
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
                                    text = "Available docks",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    var availableDocks by remember { mutableStateOf(uiState.bikeStation.availableDocks.toString()) }
                                    availableDocks = uiState.bikeStation.availableDocks.toString()

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
