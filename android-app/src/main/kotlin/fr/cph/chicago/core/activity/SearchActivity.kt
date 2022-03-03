package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Train
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fr.cph.chicago.core.composable.SearchTopBar
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
import timber.log.Timber
import javax.inject.Inject

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
        topBar = { SearchTopBar(viewModel = viewModel) },
        //snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.trains) { trainStation ->
                    Row {
                        SearchRow(
                            imageVector = Icons.Filled.Train ,
                            title = trainStation.name,
                        )
                    }
                }
/*                items(uiState.busRoutes) { busRoute ->
                    Row {
                        SearchRow(
                            imageVector = Icons.Filled.DirectionsBus ,
                            title = busRoute.id + " " + busRoute.name
                        )
                    }
                }*/
                /*items(uiState.bikeStations) { bikeStation ->
                    Row {
                        SearchRow(
                            imageVector = Icons.Filled.DirectionsBike ,
                            title = bikeStation.name
                        )
                    }
                }*/
            }
        }
    )
}

@Composable
private fun SearchRow(imageVector: ImageVector, title: String, colors: List<Color> = listOf()) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth().background(Color.Red)) {
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
                ColoredBox(color = color)
            }
        }
    }
}

data class SearchUiState(
    val trains: List<TrainStation> = listOf(),
    val busRoutes: List<BusRoute> = listOf(),
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
        search("")
        return this
    }

    fun search(query: String) {
        val foundStations = trainService.searchStations(query)
        val foundBusRoutes = busService.searchBusRoutes(query)
        val foundBikeStations = bikeService.searchBikeStations(query)
        Single.zip(foundStations, foundBusRoutes, foundBikeStations) { trains, buses, bikes -> Triple(trains, buses, bikes) }
            .observeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    Timber.i("Station found: " + result.first.size)
                    uiState = uiState.copy(
                        trains = result.first,
                        busRoutes = result.second,
                        bikeStations = result.third,
                    )
                },
                { error -> Timber.e(error) }
            )
    }
}
