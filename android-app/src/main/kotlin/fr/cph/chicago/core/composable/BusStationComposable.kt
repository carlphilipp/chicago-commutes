package fr.cph.chicago.core.composable

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.LargeImagePlaceHolderAnimated
import fr.cph.chicago.core.composable.common.ShimmerAnimation
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.ResetBusStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigInteger
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

private val googleStreetClient = GoogleStreetClient
private val preferenceService = PreferenceService
private val busService = BusService
private val util = Util

class BusStationComposable : ComponentActivity(), StoreSubscriber<State> {
    private lateinit var busRouteId: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private var busStopId: BigInteger = BigInteger.ZERO

    private lateinit var busRouteName: String
    private var position: Position = Position()
    private var busStop = mutableStateOf(BusStop(BigInteger.ZERO, "", "", Position()))
    private var busArrivalStopDTO = mutableStateOf(BusArrivalStopDTO()) //FIXME: understand the alert
    private lateinit var action: BusStopArrivalsAction

    private var googleStreetMapImage = mutableStateOf<Drawable>(ShapeDrawable())
    private var showGoogleStreetImage = mutableStateOf(false)

    private var showStationName = mutableStateOf(false)
    private var busStopName = mutableStateOf("")

    private var isFavorite = mutableStateOf(false)
    private val applyFavorite = mutableStateOf(false)
    private val isRefreshing = mutableStateOf(false)
    private val snackbarHostState = mutableStateOf(SnackbarHostState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        busStopId = BigInteger(intent.getStringExtra(getString(R.string.bundle_bus_stop_id)) ?: "0")
        busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY
        busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY

        Timber.i("busStopId -> $busStopId")
        Timber.i("busRouteId -> $busRouteId")
        Timber.i("busRouteName -> $busRouteName")

        Timber.i("bound -> $bound")
        Timber.i("boundTitle -> $boundTitle")

        action = BusStopArrivalsAction(
            busRouteId = busRouteId,
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)

        store.subscribe(this)
        store.dispatch(action)

        isFavorite.value = isFavorite2(
            busRouteId = busRouteId,
            busStopId = busStopId,
            boundTitle = boundTitle
        )

        loadStopDetailsAndStreetImage()

        setContent {
            ChicagoCommutesTheme {
                val scope = rememberCoroutineScope()
                if (applyFavorite.value) {
                    applyFavorite.value = false
                    LaunchedEffect(applyFavorite.value) {
                        scope.launch {
                            val message = if (isFavorite.value) "Added to favorites" else "Removed from favorites"
                            snackbarHostState.value.showSnackbar(message)
                        }
                    }
                }
                BusStationView(
                    busArrivals = busArrivalStopDTO.value,
                    busRouteId = busRouteId,
                    busStopId = busStopId,
                    boundTitle = boundTitle,
                    busRouteName = busRouteName,
                    busStopName = busStopName.value,
                    googleStreetMapImage = googleStreetMapImage.value,
                    showGoogleStreetImage = showGoogleStreetImage.value,
                    snackbarHostState = snackbarHostState.value,
                    isFavorite = isFavorite.value,
                    isTrainStationRefreshing = isRefreshing.value,
                    showStationName = showStationName.value,
                    onRefresh = {
                        isRefreshing.value = true
                        Timber.d("Start Refreshing")
                        //store.dispatch(TrainStationAction(trainStation.id))
                    }
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun loadStopDetailsAndStreetImage() {
        // Load bus stop details and google street image
        busService.loadAllBusStopsForRouteBound(busRouteId, boundTitle)
            .observeOn(Schedulers.computation())
            .flatMap { stops ->
                val busStop: BusStop? = stops.firstOrNull { busStop -> busStop.id == busStopId }
                Single.just(busStop!!)
            }
            .map { busStop ->
                loadGoogleStreetImage(busStop.position)
                position = Position(busStop.position.latitude, busStop.position.longitude)
                busStopName.value = busStop.name
                busStop
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { throwable ->
                Timber.e(throwable, "Error while loading street image and stop details")
                //onError()
            }
            .subscribe(
                { busStop ->
                    /*toolbar.title = "$busRouteId - ${busStop.name}"
                    streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
                    binding.header.favorites.mapContainer.setOnClickListener(OpenMapOnClickListener(position.latitude, position.longitude))
                    binding.header.favorites.walkContainer.setOnClickListener(OpenMapDirectionOnClickListener(position.latitude, position.longitude))*/
                },
                { throwable ->
                    Timber.e(throwable, "Error while loading street image and stop details")
                    // onError()
                })
    }

    override fun newState(state: State) {
        Timber.i("new state ${state.busStopStatus}")
        when (state.busStopStatus) {
            Status.SUCCESS -> {
                Timber.i("Set new bus arrival stop DTO ${state.busArrivalStopDTO.keys.size}")
                busArrivalStopDTO.value = state.busArrivalStopDTO
                showStationName.value = true
                store.dispatch(ResetBusStationStatusAction())
            }
            Status.FAILURE -> {
                // TODO
                store.dispatch(ResetBusStationStatusAction())
            }
            Status.ADD_FAVORITES -> {
                isFavorite.value = true
                applyFavorite.value = true
                store.dispatch(ResetBusStationStatusAction())
            }
            Status.REMOVE_FAVORITES -> {
                isFavorite.value = false
                applyFavorite.value = true
                store.dispatch(ResetBusStationStatusAction())
            }
            else -> Timber.d("Status not handled")
        }
        isRefreshing.value = false
    }

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position) {
        googleStreetClient.getImage(position.latitude, position.longitude, 1000, 400)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    googleStreetMapImage.value = drawable
                    //val bit = (bitmap as BitmapDrawable).bitmap
                    //Timber.i("Dim: ${bit.width}x${bit.height}")
                    showGoogleStreetImage.value = true
                },
                { error ->
                    // TODO: If that failed, we need to retry when the user refreshes data
                    Timber.e(error, "Error while loading street view image")
                }
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusStationView(
    modifier: Modifier = Modifier,
    busArrivals: BusArrivalStopDTO,
    busRouteId: String,
    busStopId: BigInteger,
    boundTitle: String,
    busRouteName: String,
    busStopName: String,
    googleStreetMapImage: Drawable,
    showGoogleStreetImage: Boolean,
    snackbarHostState: SnackbarHostState,
    isFavorite: Boolean,
    isTrainStationRefreshing: Boolean,
    showStationName: Boolean,
    onRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = (LocalLifecycleOwner.current as ComponentActivity)
    val busArrivalsKeys = busArrivals.keys.toList()
    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isTrainStationRefreshing),
        onRefresh = onRefresh,
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
            content = {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Surface(modifier = Modifier.zIndex(1f)) {
                            AnimatedVisibility(
                                modifier = Modifier.height(200.dp),
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
                                modifier = Modifier.height(200.dp),
                                visible = !showGoogleStreetImage,
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
                    }
                    item {
                        Surface(
                            modifier = Modifier
                                //.absoluteOffset(y = (-14).dp)
                                .zIndex(5f)
                                .fillMaxWidth()
                        ) {
                            Column {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 7.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        val text = if(busStopName != "") {
                                            "$busRouteId - $busRouteName"
                                        } else {
                                            busRouteId
                                        }
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.titleLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = boundTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    }

                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    IconButton(onClick = {
                                        switchFavorite2(
                                            boundTitle = boundTitle,
                                            busStopId = busStopId,
                                            busRouteId = busRouteId,
                                            busRouteName = busRouteName,
                                            busStopName = busStopName
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Favorite,
                                            contentDescription = "Favorite",
                                            tint = if (isFavorite) Color(fr.cph.chicago.util.Color.yellowLineDark) else LocalContentColor.current,
                                        )
                                    }
                                    IconButton(onClick = {
                                        // TODO: show pin or do not start other app, just do it within our app
/*                                        val uri = String.format(Locale.ENGLISH, "geo:%f,%f", trainStation.stops[0].position.latitude, trainStation.stops[0].position.longitude)
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {*/
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Could not find any Map application on device")
                                        }
                                        // }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Map,
                                            contentDescription = "Map",
                                        )
                                    }
                                }
                            }
                        }
                    }
                    items(busArrivalsKeys.size) { index ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.padding(bottom = 3.dp))
                            val destination = busArrivalsKeys[index]
                            val arrivals = busArrivals[busArrivalsKeys[index]]
                            Text(
                                text = busStopName
                            )
                            Text(
                                text = "$destination -> " + arrivals?.joinToString(separator = " ") { util.formatArrivalTime(it) }
                            )
                        }
                    }
                }
            })
    }
}


fun isFavorite2(busRouteId: String, busStopId: BigInteger, boundTitle: String): Boolean {
    return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
}

fun switchFavorite2(busRouteId: String, busStopId: BigInteger, boundTitle: String, busRouteName: String, busStopName: String) {
    if (isFavorite2(busRouteId = busRouteId, busStopId = busStopId, boundTitle = boundTitle)) {
        store.dispatch(RemoveBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle))
    } else {
        store.dispatch(AddBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle, busRouteName, busStopName))
    }
}
