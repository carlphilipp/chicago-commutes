package fr.cph.chicago.core.ui.screen

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.ui.common.AnimatedText
import fr.cph.chicago.core.ui.common.ShimmerAnimation
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.ShowFavoriteSnackBar
import fr.cph.chicago.core.ui.common.StationDetailsImageView
import fr.cph.chicago.core.ui.common.StationDetailsTitleIconView
import fr.cph.chicago.core.ui.common.loadGoogleStreet
import fr.cph.chicago.core.ui.common.openExternalMapApplication
import fr.cph.chicago.redux.AddTrainFavoriteAction
import fr.cph.chicago.redux.RemoveTrainFavoriteAction
import fr.cph.chicago.redux.ResetTrainStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import kotlinx.coroutines.CoroutineScope
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainStationScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainStationViewModel,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing),
        onRefresh = { viewModel.refresh() },
    ) {
        Scaffold(
            content = {
                Column(
                    modifier = Modifier
                        .verticalScroll(uiState.scrollState)
                        .fillMaxWidth()
                ) {
                    StationDetailsImageView(
                        showGoogleStreetImage = uiState.showGoogleStreetImage,
                        googleStreetMapImage = uiState.googleStreetMapImage,
                        isLoading = uiState.isGoogleStreetImageLoading,
                        scrollState = uiState.scrollState
                    )
                    StationDetailsTitleIconView(
                        title = uiState.trainStation.name,
                        isFavorite = uiState.isFavorite,
                        onFavoriteClick = { viewModel.switchFavorite() },
                        onMapClick = { viewModel.openMap(context = context, scope = scope) }
                    )
                    uiState.trainStation.stopByLines.keys.forEach { line ->
                        val stops = uiState.trainStation.stopByLines[line]!!
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Surface(
                                    color = line.color,
                                    shadowElevation = 1.dp,
                                    shape = RoundedCornerShape(15.0.dp),
                                ) {
                                    Text(
                                        text = line.toStringWithLine(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.padding(bottom = 3.dp))
                            stops.sorted().forEachIndexed { index, stop ->
                                TrainStop(
                                    viewModel = TrainStopViewModel(
                                        stationId = uiState.trainStation.id,
                                        line = line,
                                        stop = stop,
                                        trainEtas = uiState.trainEtasState
                                            .filter { trainEta -> trainEta.trainStation.id == uiState.trainStation.id }
                                            .filter { trainEta -> trainEta.routeName == line },
                                        showStationName = uiState.showTrainArrivalData,
                                        showDivider = index != stops.size - 1
                                    ).initModel()
                                )
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) }
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

data class TrainStationUiState(
    val trainStation: TrainStation = TrainStation.buildEmptyStation(),
    val isFavorite: Boolean = false,
    val trainEtasState: List<TrainEta> = listOf(),
    val isRefreshing: Boolean = false,
    val applyFavorite: Boolean = false,
    val showTrainArrivalData: Boolean = false,
    val googleStreetMapImage: Drawable = ShapeDrawable(),
    val isGoogleStreetImageLoading: Boolean = true,
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val scrollState: ScrollState = ScrollState(0),
    val showErrorMessage: Boolean = false,
)

@HiltViewModel
class TrainStationViewModel @Inject constructor(
    stationId: String,
    private val trainService: TrainService = TrainService,
    private val preferenceService: PreferenceService = PreferenceService,
) : ViewModel(), StoreSubscriber<State> {

    var uiState by mutableStateOf(TrainStationUiState())
        private set

    init {
        val trainStation = trainService.getStation(stationId)
        val isFavorite = isFavorite(stationId)

        uiState = TrainStationUiState(
            trainStation = trainStation,
            isFavorite = isFavorite,
        )

        store.dispatch(TrainStationAction(stationId))
        loadGoogleStreetImage(trainStation.stops[0].position)
    }

    override fun newState(state: State) {
        Timber.d("new state ${state.trainStationStatus}")
        when (state.trainStationStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(
                    trainEtasState = state.trainStationArrival.trainEtas,
                    showTrainArrivalData = true,
                )
                store.dispatch(ResetTrainStationStatusAction())
            }
            Status.FAILURE -> {
                uiState = uiState.copy(
                    showTrainArrivalData = true,
                    showErrorMessage = true,
                )
                store.dispatch(ResetTrainStationStatusAction())
            }
            Status.ADD_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = true,
                    applyFavorite = true
                )
                store.dispatch(ResetTrainStationStatusAction())
            }
            Status.REMOVE_FAVORITES -> {
                uiState = uiState.copy(
                    isFavorite = false,
                    applyFavorite = true
                )
                store.dispatch(ResetTrainStationStatusAction())
            }
            else -> Timber.d("Status not handled")
        }
        uiState = uiState.copy(isRefreshing = false)
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        Timber.d("Start Refreshing")
        store.dispatch(TrainStationAction(uiState.trainStation.id))
        if (!isGoogleMapImageLoaded()) {
            Timber.d("Trying to reload google street image")
            loadGoogleStreetImage(uiState.trainStation.stops[0].position)
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
            latitude = uiState.trainStation.stops[0].position.latitude,
            longitude = uiState.trainStation.stops[0].position.longitude,
        )
    }

    fun switchFavorite() {
        if (isFavorite(uiState.trainStation.id)) {
            store.dispatch(RemoveTrainFavoriteAction(uiState.trainStation.id))
        } else {
            store.dispatch(AddTrainFavoriteAction(uiState.trainStation.id))
        }
    }

    private fun isGoogleMapImageLoaded(): Boolean {
        return !uiState.isGoogleStreetImageLoading && uiState.showGoogleStreetImage
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

    private fun isFavorite(trainStationId: String): Boolean {
        return preferenceService.isTrainStationFavorite(trainStationId)
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
                    return TrainStationViewModel(stationId) as T
                }
            }
    }
}

data class StopStationUiState(
    val stationId: String,
    val line: TrainLine,
    val stop: Stop,
    val trainEtas: Map<String, List<String>> = mapOf(),
    val showStationName: Boolean,
    val showDivider: Boolean,
    val isFiltered: Boolean = false,
)

class TrainStopViewModel(
    stationId: String,
    line: TrainLine,
    stop: Stop,
    showStationName: Boolean,
    showDivider: Boolean,
    val trainEtas: List<TrainEta>,
    val preferenceService: PreferenceService = PreferenceService,
) : ViewModel() {
    var stopUiState by mutableStateOf(
        StopStationUiState(
            stationId = stationId,
            line = line,
            stop = stop,
            showStationName = showStationName,
            showDivider = showDivider,
        )
    )
        private set

    fun initModel(): TrainStopViewModel {
        stopUiState = stopUiState.copy(
            isFiltered = isFiltered(),
            trainEtas = computeTrainEtas(trainEtas)
        )
        return this
    }

    fun switchFiltering(isChecked: Boolean) {
        preferenceService.saveTrainFilter(stopUiState.stationId, stopUiState.line, stopUiState.stop.direction, isChecked)
        stopUiState = stopUiState.copy(isFiltered = isFiltered())
        if (isChecked) {
            store.dispatch(TrainStationAction(stopUiState.stationId))
        }
    }

    private fun computeTrainEtas(trainEtas: List<TrainEta>): Map<String, List<String>> {
        val etas = trainEtas
            .filter { trainEta -> trainEta.stop.direction.toString() == stopUiState.stop.direction.toString() }
            .fold(mutableMapOf<String, MutableList<String>>()) { acc, cur ->
                if (acc.containsKey(cur.destName)) {
                    acc[cur.destName]!!.add(cur.timeLeftDueDelay)
                } else {
                    acc[cur.destName] = mutableListOf(cur.timeLeftDueDelay)
                }
                acc
            }
        val etaAugmented = if (etas.isEmpty()) {
            mutableMapOf("Unknown" to mutableListOf())
        } else {
            etas
        }
        return etaAugmented
    }

    private fun isFiltered(): Boolean {
        return preferenceService.getTrainFilter(stopUiState.stationId, stopUiState.line, stopUiState.stop.direction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainStop(
    modifier: Modifier = Modifier,
    viewModel: TrainStopViewModel,
) {
    val stopUiState = viewModel.stopUiState
    val isFiltered = stopUiState.isFiltered

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isFiltered,
            onCheckedChange = { isChecked -> viewModel.switchFiltering(isChecked = isChecked) },
            colors = CheckboxDefaults.colors(
                checkedColor = stopUiState.line.color,
                uncheckedColor = stopUiState.line.color,
                checkmarkColor = Color.White,
            ),
        )
        Column {
            stopUiState.trainEtas
                .forEach { eta ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val destination = eta.key
                        val direction = stopUiState.stop.direction.toString()
                        val actualEtas = eta.value
                        Column(modifier = Modifier.padding(end = 5.dp)) {
                            if (stopUiState.showStationName) {
                                Text(
                                    text = destination,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                )
                            } else {
                                ShimmerAnimation(width = 100.dp, height = 25.dp)
                            }
                            Text(
                                text = direction,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (stopUiState.showStationName) {
                                actualEtas.forEach {
                                    var currentTime by remember { mutableStateOf(it) }
                                    currentTime = "$it "
                                    AnimatedText(
                                        text = currentTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 3.dp)
                                    )
                                }
                            } else {
                                ShimmerAnimation(width = 100.dp, height = 25.dp)
                            }
                        }
                    }
                }
            if (stopUiState.showDivider) {
                Row(Modifier.padding(top = 8.dp, bottom = 8.dp)) {
                    Divider(thickness = 1.dp)
                }
            }
        }
    }
}
