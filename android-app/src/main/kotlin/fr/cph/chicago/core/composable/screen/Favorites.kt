package fr.cph.chicago.core.composable.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.AnimatedText
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.map.BusMapActivity
import fr.cph.chicago.core.composable.map.TrainMapActivity
import fr.cph.chicago.core.composable.theme.bike_orange
import fr.cph.chicago.core.composable.viewmodel.MainViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BikeStation.Companion.DEFAULT_AVAILABLE
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.TimeUtil
import fr.cph.chicago.util.Util
import fr.cph.chicago.util.startBikeStationActivity
import fr.cph.chicago.util.startBusDetailActivity
import fr.cph.chicago.util.startTrainStationActivity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Favorites(mainViewModel: MainViewModel) {
    val lastUpdate: LastUpdate = Favorites.time.value

    SwipeRefresh(
        state = rememberSwipeRefreshState(mainViewModel.uiState.isRefreshing),
        onRefresh = {
            mainViewModel.refresh()
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
                            is BikeStation -> BikeFavoriteCard(bikeStation = model)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrainFavoriteCard(modifier: Modifier = Modifier, trainStation: TrainStation, lastUpdate: LastUpdate) {
    var showDialog by remember { mutableStateOf(false) }

    FavoriteCardWrapper(modifier = modifier) {

        HeaderCard(name = trainStation.name, image = Icons.Filled.Train, lastUpdate = lastUpdate)

        trainStation.lines.forEach { trainLine ->
            val arrivals = Favorites.getTrainArrivalByStopDirection(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                Arrivals(
                    trainLine = trainLine,
                    destination = entry.key.destination,
                    direction = entry.key.trainDirection.toString(),
                    arrivals = entry.value,
                )
            }
        }

        val context = LocalContext.current
        FooterCard(
            detailsOnClick = { startTrainStationActivity(context, trainStation) },
            mapOnClick = {
                if (trainStation.lines.size == 1) {
                    val line = trainStation.lines.first()
                    startTrainMapActivity(
                        context = context,
                        trainLine = line
                    )
                } else {
                    showDialog = true
                }
            }
        )
        TrainDetailDialog(
            show = showDialog,
            trainLines = trainStation.lines,
            hideDialog = { showDialog = false },
        )
    }
}

@Composable
fun BusFavoriteCard(modifier: Modifier = Modifier, busRoute: BusRoute, lastUpdate: LastUpdate) {
    var showDialog by remember { mutableStateOf(false) }

    FavoriteCardWrapper(modifier = modifier) {
        // FIXME: This is some UI logic, should be moved in a view model or something
        val title = if (busRoute.name != "?") {
            "${busRoute.id} ${busRoute.name}"
        } else {
            busRoute.id
        }
        HeaderCard(name = title, image = Icons.Filled.DirectionsBus, lastUpdate = lastUpdate)

        // FIXME: This is some UI logic, should be moved in a view model or something
        val busDetailsDTOs = mutableListOf<BusDetailsDTO>()
        val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
            val stopNameTrimmed = stopName//FIXME: not sure if that's needed? util.trimBusStopNameIfNeeded(stopName)
            for ((key, value) in boundMap) {
                val (_, _, _, stopId, _, _, boundTitle) = value.iterator().next()
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
        FooterCard(
            detailsOnClick = {
                if (busDetailsDTOs.size == 1) {
                    startBusDetailActivity(context, busDetailsDTOs[0])
                } else {
                    showDialog = true
                }
            },
            mapOnClick = {
                val busArrivalDTO = Favorites.getBusArrivalsMapped(busRoute.id) //FIXME
                for ((stopName, boundMap) in busArrivalDTO.entries) {
                    val stopNameTrimmed = Util.trimBusStopNameIfNeeded(stopName) // FIXME
                    for ((key, value) in boundMap) {
                        val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                        val busDirectionEnum: BusDirection = BusDirection.fromString(boundTitle)
                        val busDetails = BusDetailsDTO(routeId, busDirectionEnum.shortUpperCase, boundTitle, stopId, busRoute.name, stopName)
                        busDetailsDTOs.add(busDetails)

                    }
                }
                val extras = Bundle()
                val intent = Intent(context, BusMapActivity::class.java)
                extras.putString(context.getString(R.string.bundle_bus_route_id), busRoute.id)
                extras.putStringArray(context.getString(R.string.bundle_bus_bounds), busDetailsDTOs.map { it.bound }.toSet().toTypedArray())
                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, intent, null)
            }
        )
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
fun TrainDetailDialog(show: Boolean, trainLines: Set<TrainLine>, hideDialog: () -> Unit) {
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
                        text = stringResource(id = R.string.train_choose_line),
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
                    trainLines.forEachIndexed() { index, trainLine ->
                        val modifier = if (index == trainLines.size - 1) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                        }
                        OutlinedButton(
                            modifier = modifier,
                            onClick = {
                                startTrainMapActivity(context, trainLine)
                                hideDialog()
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 10.dp),
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = trainLine.color,
                            )
                            Text(
                                text = trainLine.toStringWithLine(),
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
                        text = stringResource(id = R.string.bus_choose_stop),
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

private fun startTrainMapActivity(context: Context, trainLine: TrainLine) {
    val extras = Bundle()
    val intent = Intent(context, TrainMapActivity::class.java)
    extras.putString(context.getString(R.string.bundle_train_line), trainLine.toTextString())
    intent.putExtras(extras)
    startActivity(context, intent, null)
}

@Composable
fun BikeFavoriteCard(modifier: Modifier = Modifier, bikeStation: BikeStation) {
    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(name = bikeStation.name, image = Icons.Filled.DirectionsBike, lastUpdate = LastUpdate(TimeUtil.formatTimeDifference(bikeStation.lastReported, Calendar.getInstance().time)))

        Arrivals(destination = "Available bikes", arrivals = listOf(bikeStation.availableBikes.toString()))
        Arrivals(destination = "Available docks", arrivals = listOf(bikeStation.availableDocks.toString()))

        val context = LocalContext.current
        FooterCard(
            detailsOnClick = {
                startBikeStationActivity(context = context, bikeStation = bikeStation)
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
        Icon(
            imageVector = image,
            contentDescription = "icon",
            modifier = Modifier
                .padding(end = 10.dp)
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
                AnimatedText(text = lastUpdate.value, style = MaterialTheme.typography.labelSmall)
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
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val (left, right) = createRefs()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.constrainAs(left) {
                start.linkTo(anchor = parent.start, margin = 12.dp)
                end.linkTo(anchor = right.start)
                width = Dimension.fillToConstraints
            }
        ) {
            ColoredBox(color = trainLine.color)
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (direction != null) {
                    Text(
                        text = direction,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.constrainAs(right) {
                start.linkTo(anchor = left.end)
                end.linkTo(anchor = parent.end, margin = 12.dp)
                width = Dimension.wrapContent
                centerVerticallyTo(left)
            }
        ) {
            arrivals.forEach {
                var currentTime by remember { mutableStateOf(it) }
                var color = Color.Unspecified
                if (it == DEFAULT_AVAILABLE.toString()) {
                    currentTime = "?"
                    color = bike_orange
                } else {
                    currentTime = it
                }
                AnimatedText(
                    text = currentTime,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 3.dp),
                    color = color
                )
            }
        }
    }
}
