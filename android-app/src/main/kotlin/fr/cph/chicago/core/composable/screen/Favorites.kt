package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.TrainStationActivity
import fr.cph.chicago.core.composable.TrainStationComposable
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

        HeaderCard(name = trainStation.name, lines = trainStation.lines, image = Icons.Filled.Train, lastUpdate = lastUpdate)

        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByLine(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                Arrivals(
                    trainLine = trainLine,
                    destination = entry.key,
                    arrivals = entry.value,
                )
            }
        }

        FooterCard(detailsOnClick = {
            // Start train station activity
            val extras = Bundle()
            val intent = Intent(App.instance.applicationContext, TrainStationComposable::class.java)
            extras.putString(App.instance.getString(R.string.bundle_train_stationId), trainStation.id.toString())
            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.startActivity(intent)
        })
    }
}

@Composable
fun BusFavoriteCard(modifier: Modifier = Modifier, busRoute: BusRoute, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(name = "${busRoute.id} ${busRoute.name}", image = Icons.Filled.DirectionsBus, lastUpdate = lastUpdate)

        val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)
            for ((key, value) in boundMap) {
                val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                val busDirection = BusDirection.fromString(key)
                // TODO: Handle different size for busdirection
                val stopNameDisplay = if (busDirection == BusDirection.UNKNOWN) stopNameTrimmed else "$stopNameTrimmed ${busDirection.shortLowerCase}"
                Arrivals(
                    destination = stopNameDisplay,
                    arrivals = value.map { busArrival -> busArrival.timeLeftDueDelay }
                )
            }
        }

        FooterCard()
    }
}

@Composable
fun BikeFavoriteCard(modifier: Modifier = Modifier, bikeStation: BikeStation, lastUpdate: LastUpdate) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(name = bikeStation.name, image = Icons.Filled.DirectionsBike, lastUpdate = lastUpdate)

        Arrivals(destination = "Available bikes", arrivals = listOf(bikeStation.availableBikes.toString()))
        Arrivals(destination = "Available docks", arrivals = listOf(bikeStation.availableDocks.toString()))

        FooterCard()
    }
}

@Composable
fun FavoriteCardWrapper(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)) {
        content()
    }
}

@Composable
fun HeaderCard(modifier: Modifier = Modifier, name: String, lines: Set<TrainLine> = mutableSetOf(), image: ImageVector, lastUpdate: LastUpdate) {
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
        Row(
            horizontalArrangement = Arrangement.End, modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp)
        ) {
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
fun Arrivals(modifier: Modifier = Modifier, trainLine: TrainLine = TrainLine.NA, destination: String, arrivals: List<String>) {
    Column(modifier = modifier.padding(start = 12.dp, end = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp),
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
