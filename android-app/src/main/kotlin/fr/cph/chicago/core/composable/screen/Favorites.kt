package fr.cph.chicago.core.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
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
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.core.composable.isRefreshing
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import timber.log.Timber

private val util = Util

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
                Card(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    //backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    //contentColor = contentColorFor(backgroundColor),
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TrainFavoriteCard(modifier: Modifier = Modifier, trainStation: TrainStation, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {

        HeaderCard2(name = trainStation.name, lines = trainStation.lines, image = Icons.Filled.Train, lastUpdate = lastUpdate)

        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByLine(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                TrainDirectionArrivals(
                    trainLine = trainLine,
                    destination = entry.key,
                    arrivals = entry.value,
                )
            }
        }

        FooterCard2()
    }
}

@Composable
fun BusFavoriteCard(modifier: Modifier = Modifier, busRoute: BusRoute, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard2(name = "${busRoute.id} ${busRoute.name}", image = Icons.Filled.DirectionsBus, lastUpdate = lastUpdate)

        val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)
            for ((key, value) in boundMap) {
                val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                val busDirection = BusDirection.fromString(key)
                // TODO: Handle different size for busdirection
                val stopNameDisplay = if (busDirection == BusDirection.UNKNOWN) stopNameTrimmed else "$stopNameTrimmed ${busDirection.shortLowerCase}"
                TrainDirectionArrivals(
                    destination = stopNameDisplay,
                    arrivals = value.map { busArrival -> busArrival.timeLeftDueDelay }
                )
            }
        }

        FooterCard2()
    }
}

@Composable
fun BikeFavoriteCard(modifier: Modifier = Modifier, bikeStation: BikeStation, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard2(name = bikeStation.name, image = Icons.Filled.DirectionsBike, lastUpdate = lastUpdate)

        TrainDirectionArrivals(destination = "Available bikes", arrivals = listOf(bikeStation.availableBikes.toString()))
        TrainDirectionArrivals(destination = "Available docks", arrivals = listOf(bikeStation.availableDocks.toString()))

        FooterCard2()
    }
}

@Composable
fun FavoriteCardWrapper(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)) {
        content()
    }
}

@Composable
fun HeaderCard2(modifier: Modifier = Modifier, name: String, lines: Set<TrainLine> = mutableSetOf(), image: ImageVector, lastUpdate: LastUpdate) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Image(
            imageVector = image,
            contentDescription = "train icon",
            //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .padding(/*start = 10.dp, */end = 10.dp)
                .size(50.dp),
        )
        Column {
            Text(
                text = name,
                //color = Color(0xFF4f76bf),
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
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp)) {
            lines.forEach { trainLine ->
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(trainLine.color)),
                )
            }
        }
    }
}

@Composable
fun FooterCard2(modifier: Modifier = Modifier, detailsOnClick: () -> Unit = {}, mapOnClick: () -> Unit = {}) {
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
fun TrainDirectionArrivals(modifier: Modifier = Modifier, trainLine: TrainLine = TrainLine.NA, destination: String, arrivals: List<String>) {
    //var nextTrainTime by remember { mutableStateOf(arrivals[0]) }
    TextButton(
        onClick = { },
        modifier = modifier
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(trainLine.color)),
                )
                Text(
                    text = destination,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                )
                //nextTrainTime = arrivals[0]
                //AnimatedText(time = nextTrainTime, style = MaterialTheme.typography.bodyMedium)
                for (index in arrivals.indices) {
                    var nextTime by remember { mutableStateOf(arrivals[index]) }
                    nextTime = arrivals[index]
                    AnimatedText(
                        time = nextTime,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedText(modifier: Modifier = Modifier, time: String, style: TextStyle = LocalTextStyle.current) {
    Row(modifier = modifier) {
        Surface(color = Color.Transparent) {
            AnimatedContent(
                targetState = time,
                transitionSpec = {
                    run {
                        // The target slides up and fades in while the initial string slides up and fades out.
                        slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }
            ) { target ->
                Text(
                    text = target,
                    style = style,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArrivalTime2(/*time: String*/) {
    Row {
        var expanded by remember { mutableStateOf(false) }
        var count by remember { mutableStateOf(0) }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            onClick = { expanded = !expanded }
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    // Compare the incoming number with the previous number.
                    if (targetState > initialState) {
                        // If the target number is larger, it slides up and fades in
                        // while the initial (smaller) number slides up and fades out.
                        slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        // If the target number is smaller, it slides down and fades in
                        // while the initial number slides down and fades out.
                        slideInVertically { height -> -height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()
                    }.using(
                        // Disable clipping since the faded slide-in/out should
                        // be displayed out of bounds.
                        SizeTransform(clip = false)
                    )
                }
            ) { target ->
                Text(
                    text = "$target",
                    style = MaterialTheme.typography.bodyLarge,
                    //color = Color(0xFF4f76bf),
                    maxLines = 1,
                )
            }
        }
        Button(onClick = { count++ }) {
            Text("Add")
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
        ElevatedButton(
            onClick = { store.dispatch(FavoritesAction()) },
            modifier = Modifier.padding(3.dp),
        ) {
            Image(
                imageVector = Icons.Filled.Details,
                contentDescription = "Icon",
                modifier = Modifier.padding(end = 5.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
            Text(
                text = "Details".uppercase(),
            )
        }
        ElevatedButton(
            onClick = {
            },
            modifier = Modifier.padding(3.dp),
        ) {
            Image(
                imageVector = Icons.Filled.Map,
                contentDescription = "Icon",
                modifier = Modifier.padding(end = 5.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
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
                val arrival = entry.value.joinToString(" ")
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
