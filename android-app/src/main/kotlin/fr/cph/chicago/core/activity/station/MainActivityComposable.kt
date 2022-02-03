package fr.cph.chicago.core.activity.station

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.lightColors
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.store
import fr.cph.chicago.task.refreshTask
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlin.random.Random
import org.rekotlin.StoreSubscriber
import timber.log.Timber

class MainActivityComposable : ComponentActivity(), StoreSubscriber<State> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                Home()
            }
        }
        startRefreshTask()
        store.subscribe(this)
        // not a fan of that
        if (store.state.busRoutes.isEmpty() || store.state.bikeStations.isEmpty()) {
            store.dispatch(BusRoutesAndBikeStationAction())
        }
    }

    override fun newState(state: State) {
        Timber.i("new state")
        Favorites.refreshFavorites()
        isRefreshing.value = false
    }
}

data class LastUpdate(val value: String, private val random: Int = Random.nextInt())

private var disposable: Disposable? = null
private val refreshTask: Observable<Long> = refreshTask()
private val util = Util

val isRefreshing = mutableStateOf(false)

private fun startRefreshTask() {
    disposable = refreshTask.subscribeWith(object : DisposableObserver<Long>() {
        override fun onNext(t: Long) {
            Timber.v("Update time. Thread id: %s", Thread.currentThread().id)
            Favorites.refreshTime()
        }

        override fun onError(e: Throwable) {
            Timber.v(e, "Error with refresh task: %s", e.message)
        }

        override fun onComplete() {
            Timber.v("Refresh task complete")
        }
    })
}


@Composable
fun Home() {
    ChicagoCommutesTheme {
        Scaffold(
            topBar = { AppBar() }
        ) { innerPadding ->
            StationCard(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun AppBar() {
    TopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        },
        title = {
            Text(text = stringResource(R.string.app_flavor))
        },
        backgroundColor = MaterialTheme.colors.primarySurface
    )
}

@Composable
fun StationCard(modifier: Modifier = Modifier) {
    val time = Favorites.time.value

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing.value),
        onRefresh = {
            isRefreshing.value = true
            Timber.i("Start Refreshing")
            store.dispatch(FavoritesAction())
        },
    ) {

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(Favorites.size()) { index ->
                Card(
                    modifier = modifier,
                    elevation = 2.dp,
                ) {
                    Column {
                        val model = Favorites.getObject(index)
                        when (model) {
                            is TrainStation -> {
                                HeaderCard(
                                    image = R.drawable.ic_train_white_24dp,
                                    title = model.name,
                                    lastUpdate = time.value,
                                )

                                Divider(thickness = 1.dp)

                                TrainArrivals(model)

                                Divider(thickness = 1.dp)

                                FooterCard()
                            }

                            is BusRoute -> {
                                HeaderCard(
                                    image = R.drawable.ic_directions_bus_white_24dp,
                                    title = model.id,
                                    lastUpdate = time.value,
                                )

                                Divider(thickness = 1.dp)

                                BusArrivals(model)

                                Divider(thickness = 1.dp)

                                FooterCard()
                            }
                            is BikeStation -> {
                                HeaderCard(
                                    image = R.drawable.ic_directions_bike_white_24dp,
                                    title = model.name,
                                    lastUpdate = time.value,
                                )

                                Divider(thickness = 1.dp)

                                BikeData(model)

                                Divider(thickness = 1.dp)

                                FooterCard()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCard(@DrawableRes image: Int, title: String, lastUpdate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(all = 0.dp)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = image.toString(),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
            modifier = Modifier
                .size(55.dp)
                .padding(10.dp),
        )
        Text(
            text = title,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = lastUpdate,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
fun FooterCard() {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .padding(all = 0.dp)
            .fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                store.dispatch(FavoritesAction())
            },
            modifier = Modifier.padding(3.dp),
        ) {
            Text(
                text = "Details".uppercase(),
            )
        }
        TextButton(
            onClick = {
            },
            modifier = Modifier.padding(3.dp),
        ) {
            Text(
                text = "View Trains".uppercase(),
            )
        }
    }
}

@Composable
fun TrainArrivals(trainStation: TrainStation) {
    ArrivalWrapper {
        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByLine(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                val text = entry.key
                val arrival = entry.value
                ArrivalLine(boxColor = Color(trainLine.color), text, arrival)
            }
        }
    }
}

@Composable
fun BusArrivals(busRoute: BusRoute) {
    ArrivalWrapper {
        val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)
            for ((key, value) in boundMap) {
                val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                val busDirection = BusDirection.fromString(key)
                val stopNameDisplay = if (busDirection == BusDirection.UNKNOWN) stopNameTrimmed else "$stopNameTrimmed ${busDirection.shortLowerCase}"
                ArrivalLine(
                    title = stopNameDisplay,
                    value = value.joinToString(separator = " ") { busArrival -> busArrival.timeLeftDueDelay }
                )
            }
        }
    }
}

@Composable
fun BikeData(bikeStation: BikeStation) {
    ArrivalWrapper {
        ArrivalLine(title = "Available bikes", value = bikeStation.availableBikes.toString())
        ArrivalLine(title = "Available docks", value = bikeStation.availableDocks.toString())
    }
}

@Composable
fun ArrivalWrapper(content: @Composable ColumnScope.() -> Unit) {
    Row {
        Column(Modifier.padding(10.dp), content = content)
    }
}

@Composable
fun ArrivalLine(boxColor: Color = Color.Black, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(boxColor)
        )
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        )
        Text(
            text = value,
            maxLines = 1,
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    ChicagoCommutesTheme {
        StationCard(
            /*stationsInfo = mutableListOf(
                StationInfo("Belmont", "2 min"),
            )*/
            /*fav = favorites.value*/
        )
    }
}

@Composable
fun ChicagoCommutesTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // https://developer.android.com/jetpack/compose/themes/material
    // https://google.github.io/accompanist/appcompat-theme/
    //AppCompatTheme(content = content)

    val context: Context = LocalContext.current

/*
    val themeParams = remember(context.theme) {
        context.createAppCompatTheme(
            readColors = true,
            readTypography = false
        )
    }
*/

    val colors = if (isDarkTheme) {
        DarkColors
    } else {
        LightColors
    }
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

@Preview("Home")
@Composable
private fun HomePreview() {
    Home()
}

private val LightColors = lightColors(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),

    primaryVariant = Color(0xFF455A64),

    secondaryVariant = Color(0xFF7D5260),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),


    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),

    )

private val DarkColors = darkColors(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),

    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),

    primaryVariant = Color(0xFFEFB8C8),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),

    //secondaryVariant = Color(0xFF4a5670),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)
