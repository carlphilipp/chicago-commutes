package fr.cph.chicago.core.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.core.composable.isRefreshing
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import java.math.BigInteger
import timber.log.Timber

private val util = Util

@Composable
fun Favorites() {
    val time = Favorites.time.value

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
                            is TrainStation -> {
                                /*
                                HeaderCard(
                                    image = Icons.Filled.Train,
                                    title = model.name,
                                    lastUpdate = time.value,
                                )

                                Divider(thickness = 1.dp)

                                TrainArrivals(model)

                                Divider(thickness = 1.dp)

                                FooterCard()*/

                                NewDesign(model)

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

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun NewDesign(trainStation: TrainStation) {
    Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)) {
        Row {
            Image(
                imageVector = Icons.Filled.Train,
                contentDescription = "icon",
                //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                //modifier = Modifier
                //    .size(55.dp),
                //.padding(10.dp),
            )
            Text(
                text = trainStation.name,
                color = Color(0xFF4f76bf),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByLine(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                var expandedArrivals by remember { mutableStateOf<String?>(null) }
                val title = entry.key
                val value = entry.value
                val expendedId = trainStation.id.toString() + title + trainLine.toTextString()
                var nextTrainTime by remember { mutableStateOf(value[0]) }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    //color = Color.Transparent,
                    color = Color.Green,
                    shape = RoundedCornerShape(20.0.dp),
                ) {
                    TextButton(
                        onClick = { expandedArrivals = if (expandedArrivals == expendedId) null else expendedId },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                            //.clip(RoundedCornerShape(20.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(trainLine.color))
                                //.padding(start = 5.dp),
                            )
                            Text(
                                text = title,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                //.padding(horizontal = 10.dp),
                            )

                            //ArrivalTime2()
                            Button(onClick = { nextTrainTime = nextTrainTime + "1" }) {
                                Text("Add")
                            }
                            ArrivalTime(time = nextTrainTime, onTimeChange = { nextTrainTime = it })
                            Text(
                                text = value[0],
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF4f76bf),
                                maxLines = 1,
                            )
                        }
                    }
                }
/*                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        expandedArrivals = if (expandedArrivals == expendedId) null else expendedId
                    }
                        .padding(top = 10.dp, bottom = 10.dp)
                        //.clip(RoundedCornerShape(20.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(trainLine.color))
                            .padding(start = 5.dp),
                    )
                    Text(
                        text = title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        //.padding(horizontal = 10.dp),
                    )

                    Text(
                        text = value[0],
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4f76bf),
                        maxLines = 1,
                    )
                }*/
                val visible = expandedArrivals == expendedId
                AnimatedVisibility(visible = visible) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (visible) {
                    Row(modifier = Modifier.background(Color.Red)) {
                        for (index in 1 until value.size) {
                            Text(
                                text = value[index],
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = visible) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArrivalTime(time: String, onTimeChange: (String) -> Unit) {
    Row {
        var expanded by remember { mutableStateOf(false) }
        //var count by remember { mutableStateOf(time) }
        //var timeState by remember { mutableStateOf(time) }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            //onClick = { expanded = !expanded }
        ) {
            AnimatedContent(
                targetState = time,
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
                    color = Color(0xFF4f76bf),
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
                    color = Color(0xFF4f76bf),
                    maxLines = 1,
                )
            }
        }
        Button(onClick = { count++ }) {
            Text("Add")
        }
    }
}

class ArrivalTimePreviewParameterProvider() : PreviewParameterProvider<String> {
    override val values = sequenceOf("10 min")
}

@Composable
fun NewDesignPreview(@PreviewParameter(TrainStationProvider::class) trainStation: TrainStation) {
    ChicagoCommutesTheme {
        NewDesign(trainStation)
    }
}

class TrainStationProvider : PreviewParameterProvider<TrainStation> {
    override val values = sequenceOf(TrainStation(id = BigInteger("1"), name = "Belmont", stops = listOf()))
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
