package fr.cph.chicago.core.composable

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.AddTrainFavoriteAction
import fr.cph.chicago.redux.RemoveTrainFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.math.BigInteger
import java.util.Locale
import kotlinx.coroutines.launch
import org.rekotlin.StoreSubscriber
import timber.log.Timber

private val googleStreetClient = GoogleStreetClient
private val preferenceService = PreferenceService
private val util = Util

private var isFavorite = mutableStateOf(false)

class TrainStationComposable : ComponentActivity(), StoreSubscriber<State> {
    private var googleStreetMapImage = mutableStateOf<Drawable>(ShapeDrawable())
    private var showGoogleStreetImage = mutableStateOf(false)
    private var trainEtasState = mutableStateOf(listOf<TrainEta>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleStreetMapImage.value = AppCompatResources.getDrawable(App.instance.applicationContext, R.drawable.placeholder_street_view)!!
        val stationId = BigInteger(intent.extras?.getString(getString(R.string.bundle_train_stationId), "0")!!)
        isFavorite.value = isFavorite(stationId)
        val trainStation = TrainService.getStation(stationId)

        val position = trainStation.stops[0].position
        loadGoogleStreetImage(position)
        store.subscribe(this)
        store.dispatch(TrainStationAction(trainStation.id))
        setContent {
            ChicagoCommutesTheme {
                TrainStationView(
                    trainStation = trainStation,
                    trainEtas = trainEtasState.value,
                    googleStreetMapImage = googleStreetMapImage.value,
                    showGoogleStreetImage = showGoogleStreetImage.value,
                    isFavorite = isFavorite.value,
                )
            }
        }
    }

    override fun newState(state: State) {
        Timber.i("new state")
        when (state.trainStationStatus) {
            Status.SUCCESS -> {
                trainEtasState.value = state.trainStationArrival.trainEtas
            }
            Status.FAILURE -> {
                TODO()
            }
            Status.ADD_FAVORITES -> {
                TODO()
            }
            Status.REMOVE_FAVORITES -> {
                TODO()
            }
            else -> Timber.d("Status not handled")
        }
    }

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position) {
        googleStreetClient.getImage(position.latitude, position.longitude, 1000, 400)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    googleStreetMapImage.value = drawable
                    showGoogleStreetImage.value = true
                },
                { error ->
                    Timber.e(error, "Error while loading street view image")
                }
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainStationView(
    modifier: Modifier = Modifier,
    trainStation: TrainStation,
    trainEtas: List<TrainEta>,
    googleStreetMapImage: Drawable,
    showGoogleStreetImage: Boolean,
    isFavorite: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        //scaffoldState = scaffoldState,
        snackbarHost = {
            // reuse default SnackbarHost to have default animation and timing handling
            SnackbarHost(
                hostState = snackbarHostState,
            ) { data ->
                // custom snackbar with the custom border
                Snackbar(
                    //modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.secondary),
                    snackbarData = data
                )
            }
        },
        content = {
            val activity = (LocalLifecycleOwner.current as ComponentActivity)
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Surface(modifier = Modifier.zIndex(1f)) {
                    AnimatedVisibility(
                        visible = showGoogleStreetImage,
                        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                    ) {
                        Image(
                            bitmap = googleStreetMapImage.toBitmap().asImageBitmap(),
                            contentDescription = "Google image street view",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    AnimatedVisibility(
                        visible = !showGoogleStreetImage,
                        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                    ) {
                        Image(
                            bitmap = AppCompatResources.getDrawable(App.instance.applicationContext, R.drawable.placeholder_street_view)!!.toBitmap().asImageBitmap(),
                            contentDescription = "Placeholder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(245.dp),
                        )
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
                Surface(
                    modifier = Modifier
                        .absoluteOffset(y = (-14).dp)
                        .zIndex(5f)
                        .fillMaxWidth()
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${trainStation.name} (${trainStation.id})",
                                style = MaterialTheme.typography.titleLarge
                            )
                            IconButton(onClick = { switchFavorite(trainStation.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color(fr.cph.chicago.util.Color.yellowLineDark) else LocalContentColor.current
                                )
                            }
                            val context = LocalContext.current
                            val (snackbarVisibleState, setSnackBarState) = remember { mutableStateOf(false) }
                            if (snackbarVisibleState) {
                                Snackbar(
                                    action = {
                                        Button(onClick = {}) {
                                            Text("MyAction")
                                        }
                                    },
                                    modifier = Modifier.padding(8.dp)
                                ) { Text(text = "This is a snackbar!") }
                            }

                            IconButton(onClick = {
                                val uri = String.format(Locale.ENGLISH, "geo:%f,%f", trainStation.stops[0].position.latitude, trainStation.stops[0].position.longitude)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Could not find any Map application on device")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = "Map",
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            trainStation.stopByLines.forEach { entry ->
                                val line = entry.key
                                val stops = entry.value
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Surface(
                                        color = Color(line.color),
                                        shadowElevation = 3.dp,
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
                                stops.sorted().forEach { stop ->
                                    Stop(
                                        stationId = trainStation.id,
                                        line = line,
                                        stop = stop,
                                        trainEtas = trainEtas
                                            .filter { trainEta -> trainEta.trainStation.id == trainStation.id }
                                            .filter { trainEta -> trainEta.routeName == line }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Stop(modifier: Modifier = Modifier, stationId: BigInteger, line: TrainLine, stop: Stop, trainEtas: List<TrainEta>) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray),
    ) {
        Checkbox(
            checked = preferenceService.getTrainFilter(stationId, line, stop.direction),
            onCheckedChange = {},
            colors = CheckboxDefaults.colors(), // TODO to customize
        )
        Text(text = stop.direction.toString())
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
        Column {
            etas
                .forEach { trainEta ->
                    Row {
                        Text(text = trainEta.key + ": ")
                        trainEta.value.forEach {
                            Text(text = "$it ")
                        }
                    }
                }
        }
    }
}

fun isFavorite(trainStationId: BigInteger): Boolean {
    return preferenceService.isTrainStationFavorite(trainStationId)
}

fun switchFavorite(trainStationId: BigInteger) {
    if (isFavorite(trainStationId)) {
        store.dispatch(RemoveTrainFavoriteAction(trainStationId))
    } else {
        store.dispatch(AddTrainFavoriteAction(trainStationId))
    }
    isFavorite.value = !isFavorite.value
}
