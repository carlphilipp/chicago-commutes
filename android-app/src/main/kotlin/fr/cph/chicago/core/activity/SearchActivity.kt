package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import fr.cph.chicago.core.composable.SearchTopBar
import fr.cph.chicago.core.composable.common.ChipMaterial3
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.toComposeColor
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = SearchViewModel().initModel()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                SearchView(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(viewModel: SearchViewModel) {
    val uiState = viewModel.uiState
    Scaffold(
        topBar = {
            SearchTopBar(
                viewModel = viewModel,
                onValueChange = { textFieldValue ->
                    viewModel.updateText(textFieldValue)
                    viewModel.search(textFieldValue.text)
                },
                searchText = uiState.searchText
            )
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                colors = trainStation.lines.map { line -> line.toComposeColor() }
                            )
                        }
                    }
                    items(uiState.busRoutes) { busRoute ->
                        Row {
                            SearchRow(
                                imageVector = Icons.Filled.DirectionsBus,
                                title = busRoute.id + " " + busRoute.name
                            )
                        }
                    }
                    items(uiState.bikeStations) { bikeStation ->
                        Row {
                            SearchRow(
                                imageVector = Icons.Filled.DirectionsBike,
                                title = bikeStation.name
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SearchRow(
    imageVector: ImageVector,
    title: String,
    colors: List<Color> = listOf(),
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = {

        }) {
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
                    contentDescription = null,
                )
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
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

    val isBikeSelected: Boolean = true,
    val bikeStations: List<BikeStation> = listOf(),
)

class SearchViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
    private val busService: BusService = BusService,
    private val bikeService: BikeService = BikeService,
) {
    var uiState by mutableStateOf(SearchUiState())
        private set

    fun initModel(): SearchViewModel {
        return this
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
        if (uiState.searchText.text != "") {
            search(uiState.searchText.text)
        }

    }

    fun busSelect(value: Boolean) {
        uiState = uiState.copy(isBusSelected = value)
        if (uiState.searchText.text != "") {
            search(uiState.searchText.text)
        }
    }

    fun bikeSelect(value: Boolean) {
        uiState = uiState.copy(isBikeSelected = value)
        if (uiState.searchText.text != "") {
            search(uiState.searchText.text)
        }
    }
}
