package fr.cph.chicago.core.ui.screen

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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BikeStation.Companion.DEFAULT_AVAILABLE
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.LastUpdate
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.theme.bike_orange
import fr.cph.chicago.core.theme.defaultButtonShape
import fr.cph.chicago.core.ui.common.AnimatedText
import fr.cph.chicago.core.ui.common.BusDetailDialog
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SwipeRefreshThemed
import fr.cph.chicago.core.ui.common.TrainDetailDialog
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.util.TimeUtil
import java.util.Calendar
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    mainViewModel: MainViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
    favorites: Favorites = Favorites,
) {
    Timber.d("Compose FavoritesScreen")
    val lastUpdate: LastUpdate = favorites.time.value
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.favScrollBehavior) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.Favorites,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                SwipeRefreshThemed(
                    swipeRefreshState = rememberSwipeRefreshState(mainViewModel.uiState.isRefreshing),
                    onRefresh = {
                        mainViewModel.refresh()
                    },
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = mainViewModel.uiState.favLazyListState,
                    ) {
                        items(favorites.size()) { index ->
                            ElevatedCard(
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
                            ) {
                                Column {
                                    when (val model = favorites.getObject(index)) {
                                        is TrainStation -> TrainFavoriteCard(trainStation = model, lastUpdate = lastUpdate, favorites = favorites)
                                        is BusRoute -> BusFavoriteCard(busRoute = model, lastUpdate = lastUpdate, favorites = favorites)
                                        is BikeStation -> BikeFavoriteCard(bikeStation = model)
                                    }
                                }
                            }
                        }
                        item { NavigationBarsSpacer() }
                    }
                }
            }
        })
}

@Composable
fun TrainFavoriteCard(
    modifier: Modifier = Modifier,
    trainStation: TrainStation,
    lastUpdate: LastUpdate,
    favorites: Favorites,
) {
    val navController = LocalNavController.current
    var showDialog by remember { mutableStateOf(false) }

    FavoriteCardWrapper(modifier = modifier) {

        HeaderCard(name = trainStation.name, image = Icons.Filled.Train, lastUpdate = lastUpdate)

        trainStation.lines.forEach { trainLine ->
            val arrivals = favorites.getTrainArrivalByStopDirection(trainStation.id, trainLine)
            for (entry in arrivals.entries) {
                Arrivals(
                    color = trainLine.color,
                    destination = entry.key.destination,
                    direction = entry.key.trainDirection.toString(),
                    arrivals = entry.value,
                )
            }
        }

        FooterCard(
            detailsOnClick = {
                navController.navigate(Screen.TrainDetails, mapOf("stationId" to trainStation.id))
            },
            mapOnClick = {
                if (trainStation.lines.size == 1) {
                    val line = trainStation.lines.first()
                    navController.navigate(
                        Screen.TrainMap,
                        mapOf("line" to line.toTextString())
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
fun BusFavoriteCard(
    modifier: Modifier = Modifier,
    busRoute: BusRoute,
    lastUpdate: LastUpdate,
    favorites: Favorites,
) {
    val navController = LocalNavController.current
    var showDialog by remember { mutableStateOf(false) }

    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(
            name = if (busRoute.name != "?") "${busRoute.id} ${busRoute.name}" else busRoute.id,
            image = Icons.Filled.DirectionsBus,
            lastUpdate = lastUpdate
        )

        val busDetailsDTOs = mutableListOf<BusDetailsDTO>()
        val busArrivalDTO = favorites.getBusArrivalsMapped(busRoute.id)
        for ((stopName, boundMap) in busArrivalDTO.entries) {
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
                    destination = stopName,
                    direction = busDirection.shortLowerCase,
                    arrivals = value.map { busArrival -> busArrival.timeLeftDueDelay }
                )
            }
        }

        FooterCard(
            detailsOnClick = {
                if (busDetailsDTOs.size == 1) {
                    navController.navigate(
                        screen = Screen.BusDetails,
                        arguments = mapOf(
                            "busStopId" to busDetailsDTOs[0].stopId.toString(),
                            "busStopName" to busDetailsDTOs[0].stopName,
                            "busRouteId" to busDetailsDTOs[0].busRouteId,
                            "busRouteName" to busDetailsDTOs[0].routeName,
                            "bound" to busDetailsDTOs[0].bound,
                            "boundTitle" to busDetailsDTOs[0].boundTitle,
                        )
                    )
                } else {
                    showDialog = true
                }
            },
            mapOnClick = {
                navController.navigate(
                    screen = Screen.BusMap,
                    arguments = mapOf("busRouteId" to busRoute.id)
                )
            }
        )
        BusDetailDialog(
            show = showDialog,
            busDetailsDTOs = busDetailsDTOs,
            hideDialog = { showDialog = false },
        )
    }
}


@Composable
fun BikeFavoriteCard(modifier: Modifier = Modifier, bikeStation: BikeStation) {
    val navController = LocalNavController.current

    FavoriteCardWrapper(modifier = modifier) {
        HeaderCard(
            name = bikeStation.name,
            image = Icons.Filled.DirectionsBike,
            lastUpdate = LastUpdate(TimeUtil.formatTimeDifference(bikeStation.lastReported, Calendar.getInstance().time))
        )

        Arrivals(
            destination = stringResource(id = R.string.bike_available_bikes),
            arrivals = listOf(bikeStation.availableBikes.toString())
        )
        Arrivals(
            destination = stringResource(id = R.string.bike_available_docks),
            arrivals = listOf(bikeStation.availableDocks.toString())
        )

        FooterCard(
            detailsOnClick = {
                navController.navigate(
                    screen = Screen.DivvyDetails,
                    arguments = mapOf("stationId" to bikeStation.id)
                )
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
            contentDescription = null,
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
                    text = stringResource(id = R.string.last_updated),
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
        ElevatedButton(
            onClick = { detailsOnClick() },
            shape = defaultButtonShape,
        ) {
            Text(
                text = stringResource(id = R.string.fav_details),
            )
        }
        ElevatedButton(
            onClick = { mapOnClick() },
            modifier = Modifier.padding(start = 5.dp),
            shape = defaultButtonShape,
        ) {
            Text(
                text = stringResource(id = R.string.fav_show_map),
            )
        }
    }
}

@Composable
fun Arrivals(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.secondaryContainer, destination: String, direction: String? = null, arrivals: List<String>) {
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
            ColoredBox(color = color)
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
