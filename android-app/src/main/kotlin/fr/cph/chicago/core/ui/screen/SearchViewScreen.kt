package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.BusRouteDialog
import fr.cph.chicago.core.ui.common.ChipMaterial3
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchViewScreen(
    viewModel: SearchViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Timber.d("Compose SearchViewScreen ${viewModel.uiState.searchText.text}")
    val uiState = viewModel.uiState
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            viewModel.search(viewModel.uiState.searchText.text)
        }
    })

    Column {
        DisplayTopBar(
            title = title,
            viewModel = navigationViewModel,
        )
        Scaffold(
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextFieldMaterial3(
                        text = uiState.searchText,
                        onValueChange = { textFieldValue ->
                            viewModel.updateText(textFieldValue)
                            viewModel.search(textFieldValue.text)
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChipMaterial3(
                            modifier = Modifier.padding(5.dp),
                            text = "Train",
                            isSelected = uiState.isTrainSelected,
                            onClick = {
                                viewModel.trainSelect(!uiState.isTrainSelected)
                            },
                        )
                        ChipMaterial3(
                            modifier = Modifier.padding(5.dp),
                            text = "Bus",
                            isSelected = uiState.isBusSelected,
                            onClick = {
                                viewModel.busSelect(!uiState.isBusSelected)
                            },
                        )
                        ChipMaterial3(
                            modifier = Modifier.padding(5.dp),
                            text = "Bike",
                            isSelected = uiState.isBikeSelected,
                            onClick = {
                                viewModel.bikeSelect(!uiState.isBikeSelected)
                            }
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.trains) { trainStation ->
                            Row {
                                SearchRow(
                                    imageVector = Icons.Filled.Train,
                                    title = trainStation.name,
                                    colors = trainStation.lines.map { line -> line.color },
                                    onClick = {
                                        navController.navigate(Screen.TrainDetails,
                                            mapOf(
                                                "stationId" to trainStation.id,
                                                "search" to uiState.searchText.text,
                                            )
                                        )
                                    }
                                )
                            }
                        }
                        items(uiState.busRoutes) { busRoute ->
                            Row {
                                SearchRow(
                                    imageVector = Icons.Filled.DirectionsBus,
                                    title = busRoute.id + " " + busRoute.name,
                                    onClick = {
                                        viewModel.setBusRoute(busRoute)
                                        viewModel.showBusDialog(true)
                                    }
                                )
                            }
                        }
                        items(uiState.bikeStations) { bikeStation ->
                            Row {
                                SearchRow(
                                    imageVector = Icons.Filled.DirectionsBike,
                                    title = bikeStation.name,
                                    onClick = {
                                        navController.navigate(
                                            screen = Screen.DivvyDetails,
                                            arguments = mapOf(
                                                "stationId" to bikeStation.id,
                                                "search" to uiState.searchText.text,
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
        BusRouteDialog(
            search = uiState.searchText.text,
            showDialog = uiState.showBusDialog,
            busRoute = uiState.busRoute,
            hideDialog = {
                viewModel.showBusDialog(false)
            }
        )
    }
}

@Composable
private fun SearchRow(
    imageVector: ImageVector,
    title: String,
    colors: List<Color> = listOf(),
    onClick: () -> Unit,
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = onClick
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
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
                Icon(
                    imageVector = imageVector,
                    contentDescription = "icon",
                )
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
                colors.forEach { color ->
                    ColoredBox(modifier = Modifier.padding(start = 5.dp), color = color)
                }
            }
        }
    }
}

data class SearchUiState(
    val searchText: TextFieldValue = TextFieldValue(""),

    val isTrainSelected: Boolean = true,
    val trains: List<TrainStation> = listOf(),

    val isBusSelected: Boolean = true,
    val busRoutes: List<BusRoute> = listOf(),
    val showBusDialog: Boolean = false,
    val busRoute: BusRoute = BusRoute("", ""),

    val isBikeSelected: Boolean = true,
    val bikeStations: List<BikeStation> = listOf(),
)

class SearchViewModel @Inject constructor(
    searchText: TextFieldValue,
    private val trainService: TrainService = TrainService,
    private val busService: BusService = BusService,
    private val bikeService: BikeService = BikeService,
) : ViewModel() {
    var uiState by mutableStateOf(SearchUiState())
        private set

    init {
        uiState = SearchUiState(
            searchText = searchText
        )
    }

    fun updateText(searchText: TextFieldValue) {
        uiState = uiState.copy(searchText = searchText)
    }

    fun search(query: String) {
        Timber.d("Search text: $query")
        val foundStations = trainService.searchStations(query)
        val foundBusRoutes = busService.searchBusRoutes(query)
        val foundBikeStations = bikeService.searchBikeStations(query)
        Single.zip(foundStations, foundBusRoutes, foundBikeStations) { trains, buses, bikes -> Triple(trains, buses, bikes) }
            .observeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    uiState = uiState.copy(
                        trains = if (uiState.isTrainSelected) result.first else listOf(),
                        busRoutes = if (uiState.isBusSelected) result.second else listOf(),
                        bikeStations = if (uiState.isBikeSelected) result.third else listOf(),
                    )
                },
                { error -> Timber.e(error) }
            )
    }

    fun trainSelect(value: Boolean) {
        uiState = uiState.copy(isTrainSelected = value)
        search(uiState.searchText.text)
    }

    fun busSelect(value: Boolean) {
        uiState = uiState.copy(isBusSelected = value)
        search(uiState.searchText.text)
    }

    fun bikeSelect(value: Boolean) {
        uiState = uiState.copy(isBikeSelected = value)
        search(uiState.searchText.text)
    }

    fun showBusDialog(value: Boolean) {
        uiState = uiState.copy(showBusDialog = value)
    }

    fun setBusRoute(busRoute: BusRoute) {
        uiState = uiState.copy(busRoute = busRoute)
    }

    companion object {
        fun provideFactory(
            searchText: TextFieldValue,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return SearchViewModel(searchText) as T
                }
            }
    }
}
