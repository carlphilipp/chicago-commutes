package fr.cph.chicago.core.composable.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.BikeStationComposable
import fr.cph.chicago.core.composable.BusStationComposable
import fr.cph.chicago.core.composable.TrainStationComposable
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.isRefreshing
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.model.enumeration.toComposeColor
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import timber.log.Timber

private val util = Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Favorites() {
    val lastUpdate: LastUpdate = Favorites.time.value

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing.value),
        onRefresh = {
            isRefreshing.value = true
            Timber.d("Start Refreshing")
            store.dispatch(FavoritesAction())
        },
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(Favorites.size()) { index ->
                ElevatedCard(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
                ) {
                    Column {
                        when (val model = Favorites.getObject(index)) {
                            is TrainStation -> TrainFavoriteCard(trainStation = model, lastUpdate = lastUpdate)
                            is BusRoute -> BusFavoriteCard(busRoute = model, lastUpdate = lastUpdate)
                            is BikeStation -> BikeFavoriteCard(bikeStation = model, lastUpdate = lastUpdate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrainFavoriteCard(modifier: Modifier = Modifier, trainStation: TrainStation, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {

        HeaderCard(name = trainStation.name, image = Icons.Filled.Train, lastUpdate = lastUpdate)

        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByStopDirection(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                Arrivals(
                    trainLine = trainLine,
                    destination  = entry.key.destination,
                    direction  = entry.key.trainDirection.toString(),
                    arrivals = entry.value,
                )
            }
        }

        val context = LocalContext.current
        FooterCard(detailsOnClick = {
            // Start train station activity
            val extras = Bundle()
            val intent = Intent(context, TrainStationComposable::class.java)
            extras.putString(context.getString(R.string.bundle_train_stationId), trainStation.id.toString())
            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, intent, null)
        })
    }
}

@Composable
fun BusFavoriteCard(modifier: Modifier = Modifier, busRoute: BusRoute, lastUpdate: LastUpdate) {

    var showDialog by remember { mutableStateOf(false) }

    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(name = "${busRoute.id} ${busRoute.name}", image = Icons.Filled.DirectionsBus, lastUpdate = lastUpdate)

        val busDetailsDTOs = mutableListOf<BusDetailsDTO>()
        val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)
            for ((key, value) in boundMap) {
                val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                val busDetailsDTO = BusDetailsDTO(
                    busRouteId = busRoute.id,
                    bound = key,
                    boundTitle = boundTitle,
                    stopId = stopId,
                    routeName = busRoute.name,
                    stopName = stopName,
                )
                busDetailsDTOs.add(busDetailsDTO)
                val busDirection = BusDirection.fromString(key)
                Arrivals(
                    destination = stopNameTrimmed,
                    direction = busDirection.shortLowerCase,
                    arrivals = value.map { busArrival -> busArrival.timeLeftDueDelay }
                )
            }
        }
        val context = LocalContext.current
        FooterCard(detailsOnClick = {
            if (busDetailsDTOs.size == 1) {
                startBusDetailActivity(context, busDetailsDTOs[0])
            } else {
                showDialog = true
            }
        })
        BusDetailDialog(
            show = showDialog,
            busDetailsDTOs = busDetailsDTOs,
            hideDialog = { showDialog = false },
        )
    }
}

// FIXME: Consider merging in common with Bus.BusRouteDialog
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusDetailDialog(show: Boolean, busDetailsDTOs: List<BusDetailsDTO>, hideDialog: () -> Unit) {
    if (show) {
        val context = LocalContext.current
        AlertDialog(
            modifier = Modifier.padding(horizontal = 50.dp),
            onDismissRequest = hideDialog,
            // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "Choose a bus stop",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    busDetailsDTOs.forEachIndexed() { index, busDetailsDTO ->
                        val modifier = if (index == busDetailsDTOs.size - 1) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                        }
                        OutlinedButton(
                            modifier = modifier,
                            onClick = {
                                startBusDetailActivity(context, busDetailsDTO)
                                hideDialog()
                            },
                        ) {
                            Text(
                                text = "${busDetailsDTO.stopName} (${busDetailsDTO.bound})",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                }
            },
            confirmButton = {},
            dismissButton = {
                FilledTonalButton(onClick = { hideDialog() }) {
                    Text(
                        text = "Dismiss",
                    )
                }
            },
        )
    }
}

fun startBusDetailActivity(context: Context, busDetailsDTO: BusDetailsDTO) {
    val intent = Intent(context, BusStationComposable::class.java)
    val extras = Bundle()
    extras.putString(context.getString(R.string.bundle_bus_stop_id), busDetailsDTO.stopId.toString())
    extras.putString(context.getString(R.string.bundle_bus_route_id), busDetailsDTO.busRouteId)
    extras.putString(context.getString(R.string.bundle_bus_route_name), busDetailsDTO.routeName)
    extras.putString(context.getString(R.string.bundle_bus_bound), busDetailsDTO.bound)
    extras.putString(context.getString(R.string.bundle_bus_bound_title), busDetailsDTO.boundTitle)
    extras.putString(context.getString(R.string.bundle_bus_stop_name), busDetailsDTO.stopName)

    intent.putExtras(extras)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}

@Composable
fun BikeFavoriteCard(modifier: Modifier = Modifier, bikeStation: BikeStation, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(name = bikeStation.name, image = Icons.Filled.DirectionsBike, lastUpdate = lastUpdate)

        Arrivals(destination = "Available bikes", arrivals = listOf(bikeStation.availableBikes.toString()))
        Arrivals(destination = "Available docks", arrivals = listOf(bikeStation.availableDocks.toString()))

        val context = LocalContext.current
        FooterCard(
            detailsOnClick = {
                val intent = Intent(context, BikeStationComposable::class.java)
                val extras = Bundle()
                extras.putParcelable(context.getString(R.string.bundle_bike_station), bikeStation)
                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intent, null)
            }
        )
    }
}

@Composable
fun FavoriteCardWrapper(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.padding(10.dp)) {
        content()
    }
}

@Composable
fun HeaderCard(modifier: Modifier = Modifier, name: String, image: ImageVector, lastUpdate: LastUpdate) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Image(
            imageVector = image,
            contentDescription = "train icon",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier
                .padding(/*start = 10.dp, */end = 10.dp)
                .size(50.dp),
        )
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "last updated: ",
                    style = MaterialTheme.typography.labelSmall,
                )
                AnimatedText(time = lastUpdate.value, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun FooterCard(modifier: Modifier = Modifier, detailsOnClick: () -> Unit = {}, mapOnClick: () -> Unit = {}) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .padding(top = 2.dp)
            .fillMaxWidth()
    ) {
        FilledTonalButton(
            onClick = { detailsOnClick() },
            modifier = Modifier.padding(0.dp),
        ) {
            Text(
                text = "Details",
            )
        }
        OutlinedButton(
            onClick = { mapOnClick() },
            modifier = Modifier.padding(start = 5.dp),
        ) {
            Text(
                text = "Show map",
            )
        }
    }
}

@Composable
fun Arrivals(modifier: Modifier = Modifier, trainLine: TrainLine = TrainLine.NA, destination: String, direction: String? = null, arrivals: List<String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
    ) {
        ColoredBox(color = trainLine.toComposeColor())
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(
                text = destination,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
            if (direction != null) {
                Text(
                    text = direction,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            arrivals.forEach {
                var currentTime by remember { mutableStateOf(it) }
                currentTime = it
                AnimatedText(
                    time = currentTime,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }
}
