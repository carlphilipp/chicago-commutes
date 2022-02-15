package fr.cph.chicago.core.composable

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.LargeImagePlaceHolderAnimated
import fr.cph.chicago.core.composable.common.ShimmerAnimation
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.AddTrainFavoriteAction
import fr.cph.chicago.redux.RemoveTrainFavoriteAction
import fr.cph.chicago.redux.ResetTrainStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.math.BigInteger
import java.util.Locale
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber

private val preferenceService = PreferenceService

class TrainStationComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationId = BigInteger(intent.extras?.getString(getString(R.string.bundle_train_stationId), "0")!!)

        val viewModel = TrainStationViewModel()
        viewModel.initModel(stationId)

        store.dispatch(TrainStationAction(stationId))

        setContent {
            ChicagoCommutesTheme {
                TrainStationView(viewModel = viewModel)
            }
        }
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
    val showGoogleStreetImage: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

class TrainStationViewModel(
    private val trainService: TrainService = TrainService,
    private val preferenceService: PreferenceService = PreferenceService,
    private val googleStreetClient: GoogleStreetClient = GoogleStreetClient,
) : ViewModel(), StoreSubscriber<State> {

    var uiState by mutableStateOf(TrainStationUiState())
        private set

    fun initModel(stationId: BigInteger) {
        val trainStation = trainService.getStation(stationId)
        val isFavorite = isFavorite(stationId)

        uiState = uiState.copy(
            trainStation = trainStation,
            isFavorite = isFavorite,
        )
        store.subscribe(this)
        loadGoogleStreetImage(trainStation.stops[0].position)
    }

    override fun newState(state: State) {
        Timber.d("new state ${state.trainStationStatus}")
        when (state.trainStationStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(
                    trainEtasState = state.trainStationArrival.trainEtas,
                    showTrainArrivalData = true
                )
                store.dispatch(ResetTrainStationStatusAction())
            }
            Status.FAILURE -> {
                // TODO
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
    }

    fun resetApplyFavorite() {
        uiState = uiState.copy(applyFavorite = false)
    }

    fun openMap(context: Context, scope: CoroutineScope) {
        // TODO: show pin in google map or do not start other app, just do it within our app
        val uri = String.format(Locale.ENGLISH, "geo:%f,%f", uiState.trainStation.stops[0].position.latitude, uiState.trainStation.stops[0].position.longitude)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            scope.launch {
                uiState.snackbarHostState.showSnackbar("Could not find any Map application on device")
            }
        }
    }

    fun switchFavorite() {
        if (isFavorite(uiState.trainStation.id)) {
            store.dispatch(RemoveTrainFavoriteAction(uiState.trainStation.id))
        } else {
            store.dispatch(AddTrainFavoriteAction(uiState.trainStation.id))
        }
    }

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

    private fun isFavorite(trainStationId: BigInteger): Boolean {
        return preferenceService.isTrainStationFavorite(trainStationId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainStationView(
    modifier: Modifier = Modifier,
    viewModel: TrainStationViewModel,
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val activity = (LocalLifecycleOwner.current as ComponentActivity)
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
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    Surface(modifier = Modifier.zIndex(1f)) {
                        AnimatedVisibility(
                            modifier = Modifier.height(200.dp),
                            visible = uiState.showGoogleStreetImage,
                            enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                        ) {
                            Image(
                                bitmap = uiState.googleStreetMapImage.toBitmap().asImageBitmap(),
                                contentDescription = "Google image street view",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        alpha = min(1f, 1 - (scrollState.value / 600f))
                                        translationY = -scrollState.value * 0.1f
                                    },
                            )
                        }
                        AnimatedVisibility(
                            modifier = Modifier.height(200.dp),
                            visible = !uiState.showGoogleStreetImage,
                            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                        ) {
                            LargeImagePlaceHolderAnimated()
                        }
                        FilledTonalButton(
                            modifier = Modifier.padding(10.dp),
                            onClick = { activity.finish() },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = viewModel.uiState.trainStation.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { viewModel.switchFavorite() }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = if (viewModel.uiState.isFavorite) Color(fr.cph.chicago.util.Color.yellowLineDark) else LocalContentColor.current,
                                )
                            }
                            IconButton(onClick = { viewModel.openMap(context = context, scope = scope) }) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = "Map",
                                )
                            }
                        }
                    }
                    viewModel.uiState.trainStation.stopByLines.keys.forEach { line ->
                        val stops = viewModel.uiState.trainStation.stopByLines[line]!!
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
                                    color = Color(line.color),
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
                                Stop(
                                    stationId = viewModel.uiState.trainStation.id,
                                    line = line,
                                    stop = stop,
                                    trainEtas = uiState.trainEtasState
                                        .filter { trainEta -> trainEta.trainStation.id == viewModel.uiState.trainStation.id }
                                        .filter { trainEta -> trainEta.routeName == line },
                                    showStationName = uiState.showTrainArrivalData,
                                    showDivider = index != stops.size - 1
                                )
                            }
                        }
                    }
                }
            })
    }

    if (uiState.applyFavorite) {
        ShowSnackBar(viewModel = viewModel, scope = scope)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) }
    }
}

@Composable
fun ShowSnackBar(viewModel: TrainStationViewModel, scope: CoroutineScope) {
    viewModel.resetApplyFavorite()

    LaunchedEffect(viewModel.uiState.applyFavorite) {
        scope.launch {
            val message = if (viewModel.uiState.isFavorite) "Added to favorites" else "Removed from favorites"
            viewModel.uiState.snackbarHostState.showSnackbar(message = message, withDismissAction = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Stop(
    modifier: Modifier = Modifier,
    stationId: BigInteger,
    line: TrainLine,
    stop: Stop,
    trainEtas: List<TrainEta>,
    showStationName: Boolean,
    showDivider: Boolean
) {
    val checkboxChecked = remember { mutableStateOf(false) }
    checkboxChecked.value = preferenceService.getTrainFilter(stationId, line, stop.direction)
    // FIXME: This processing should probably not happen here
    val etas = trainEtas
        .filter { trainEta -> trainEta.stop.direction.toString() == stop.direction.toString() }
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

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checkboxChecked.value,
            onCheckedChange = { isChecked ->
                preferenceService.saveTrainFilter(stationId, line, stop.direction, isChecked)
                checkboxChecked.value = preferenceService.getTrainFilter(stationId, line, stop.direction)
                if (isChecked) {
                    store.dispatch(TrainStationAction(stationId))
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(line.color),
                uncheckedColor = Color(line.color),
            ),
        )
        Column {
            etaAugmented
                .forEach { eta ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val destination = eta.key
                        val direction = stop.direction.toString()
                        val actualEtas = eta.value
                        Column(modifier = Modifier.padding(end = 5.dp)) {
                            if (showStationName) {
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
                            if (showStationName) {
                                actualEtas.forEach {
                                    var currentTime by remember { mutableStateOf(it) }
                                    currentTime = "$it "
                                    AnimatedText(
                                        time = currentTime,
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
        }
        if (showDivider) {
            Row(Modifier.padding(top = 8.dp, bottom = 8.dp)) {
                Divider(thickness = 1.dp)
            }
        }
    }
}
