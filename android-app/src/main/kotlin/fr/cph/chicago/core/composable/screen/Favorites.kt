package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.core.composable.isRefreshing
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import timber.log.Timber

private val util = Util

@Composable
fun Favorites() {
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
