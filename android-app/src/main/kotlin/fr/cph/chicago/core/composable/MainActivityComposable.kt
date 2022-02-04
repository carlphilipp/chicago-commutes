package fr.cph.chicago.core.composable

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
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
import kotlinx.coroutines.launch
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
        // FIXME not a fan of that
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    ChicagoCommutesTheme {
        Surface {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val openDrawer = { scope.launch { drawerState.open() } }
            val navController = rememberNavController()
            val title = remember { mutableStateOf("Favorites") }
            val currentScreen = remember { mutableStateOf<DrawerScreens>(DrawerScreens.Favorites) }

            NavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    Drawer(
                        currentScreen = currentScreen.value,
                        onDestinationClicked = { screen ->
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            currentScreen.value = screen
                        }
                    )
                },
                content = {
                    Scaffold(
                        topBar = {
                            AppBar(title.value) { openDrawer() }
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = DrawerScreens.Favorites.route
                        ) {
                            composable(DrawerScreens.Favorites.route) {
                                title.value = DrawerScreens.Favorites.title
                                Favorites()
                            }
                            composable(DrawerScreens.Train.route) {
                                title.value = DrawerScreens.Train.title
                                Train()
                            }
                            composable(DrawerScreens.Bus.route) {
                                title.value = DrawerScreens.Bus.title
                                Bus()
                            }
                            composable(DrawerScreens.Divvy.route) {
                                title.value = DrawerScreens.Divvy.title
                                Divvy()
                            }
                            composable(DrawerScreens.Nearby.route) {
                                title.value = DrawerScreens.Nearby.title
                                Nearby()
                            }
                            composable(DrawerScreens.Map.route) {
                                title.value = DrawerScreens.Map.title
                                Map()
                            }
                            composable(DrawerScreens.Alerts.route) {
                                title.value = DrawerScreens.Alerts.title
                                Alerts()
                            }
                            composable(DrawerScreens.Rate.route) {
                                title.value = DrawerScreens.Rate.title
                                Rate()
                            }
                            composable(DrawerScreens.Settings.route) {
                                title.value = DrawerScreens.Settings.title
                                Settings()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AppBar(title: String, openDrawer: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            /*IconButton(onClick = {
                isRefreshing.value = true
                Timber.i("Start Refreshing")
                store.dispatch(FavoritesAction())
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }*/
        }
    )
}

@Composable
fun StationCard() {
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
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
                    elevation = 2.dp,
                ) {
                    Column {
                        when (val model = Favorites.getObject(index)) {
                            is TrainStation -> {
                                HeaderCard(
                                    image = Icons.Filled.Train,
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
                                    image = Icons.Filled.DirectionsBus,
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
                                    image = Icons.Filled.DirectionsBike,
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
fun HeaderCard(image: ImageVector, title: String, lastUpdate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(all = 0.dp)
    ) {
        Image(
            imageVector = image,
            contentDescription = "icon",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .size(55.dp)
                .padding(10.dp),
        )
        Text(
            text = title,
            maxLines = 1,
            //fontWeight = FontWeight.Bold,
            //style = MaterialTheme.typography.bodyLarge,
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
                // TODO: Handle different size for busdirection
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
    }
}

sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector) {
    object Favorites : DrawerScreens("Favorites", "fav", Icons.Filled.Favorite)
    object Train : DrawerScreens("Train", "train", Icons.Filled.Train)
    object Bus : DrawerScreens("Bus", "bus", Icons.Filled.DirectionsBus)
    object Divvy : DrawerScreens("Divvy", "divvy", Icons.Filled.DirectionsBike)
    object Nearby : DrawerScreens("Nearby", "nearby", Icons.Filled.NearMe)
    object Map : DrawerScreens("CTA map", "map", Icons.Filled.Map)
    object Alerts : DrawerScreens("CTA alerts", "alerts", Icons.Filled.Warning)
    object Rate : DrawerScreens("Rate this app", "rate", Icons.Filled.StarRate)
    object Settings : DrawerScreens("Settings", "settings", Icons.Filled.Settings)
}

private val screens = listOf(
    DrawerScreens.Favorites,
    DrawerScreens.Train,
    DrawerScreens.Bus,
    DrawerScreens.Divvy,
    DrawerScreens.Nearby,
    DrawerScreens.Map,
    DrawerScreens.Alerts,
    DrawerScreens.Rate,
    DrawerScreens.Settings
)

@Composable
fun Drawer(modifier: Modifier = Modifier, currentScreen: DrawerScreens, onDestinationClicked: (route: DrawerScreens) -> Unit) {
    Column(modifier = modifier) {
        Box {
            Image(
                painter = painterResource(R.drawable.header),
                contentDescription = "Chicago Skyline",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            )
            Text(
                text = "Chicago Commutes",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            )
        }

        screens.forEach { screen ->
            val colors = MaterialTheme.colorScheme
            val backgroundColor = if (screen.route == currentScreen.route) {
                colors.primary.copy(alpha = 0.12f)
            } else {
                Color.Transparent
            }
            val surfaceModifier = modifier
                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                .fillMaxWidth()
            Surface(
                modifier = surfaceModifier,
                color = backgroundColor,
                shape = RoundedCornerShape(20.0.dp),
            ) {
                TextButton(
                    onClick = { onDestinationClicked(screen) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            imageVector = screen.icon,
                            contentDescription = "Icon",
                            modifier = Modifier,
                            //colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = screen.title,
                            //style = MaterialTheme.typography.body2,
                            //color = textIconColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Favorites() {
    StationCard()
}

@Composable
fun Train() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Train Page content here.")
    }
}

@Composable
fun Bus() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bus Page content here.")
    }

}

@Composable
fun Divvy() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Divvy Page content here.")
    }

}

@Composable
fun Nearby() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nearby Page content here.")
    }

}

@Composable
fun Map() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Map Page content here.")
    }

}

@Composable
fun Alerts() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Alerts Page content here.")
    }

}

@Composable
fun Rate() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Rate Page content here.")
    }

}

@Composable
fun Settings() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings Page content here.")
    }

}
