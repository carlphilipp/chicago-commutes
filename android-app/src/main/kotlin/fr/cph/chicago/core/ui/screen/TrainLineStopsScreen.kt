package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainLineStopsScreen(
    viewModel: TrainListStationViewModel,
    title: String,
    navigationViewModel: NavigationViewModel
) {
    Timber.d("Compose TrainLineStopsScreen")
    val uiState = viewModel.uiState
    val navController = LocalNavController.current
    val scrollBehavior by remember { mutableStateOf(viewModel.uiState.scrollBehavior) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = uiState.trainLine, block = {
        scope.launch {
            viewModel.loadData(uiState.trainLine)
        }
    })

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                DisplayTopBar(
                    screen = Screen.TrainList,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .fillMaxSize(),
                    state = viewModel.uiState.listState,
                ) {
                    items(
                        items = uiState.trainStations,
                        key = { it.id }
                    ) { station ->
                        TextButton(onClick = { navController.navigate(Screen.TrainDetails, mapOf("stationId" to station.id)) }) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            station.lines.forEach { line ->
                                ColoredBox(modifier = Modifier.padding(start = 5.dp), color = line.color)
                            }
                        }
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
data class TrainListStationUiState constructor(
    val title: String = "",
    val trainLine: TrainLine = TrainLine.NA,
    val trainStations: List<TrainStation> = listOf(),
    val listState: LazyListState = LazyListState(),
    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(TopAppBarState(-Float.MAX_VALUE, 0f, 0f)),
)

class TrainListStationViewModel(
    private val trainService: TrainService = TrainService,
) : ViewModel() {

    var uiState by mutableStateOf(TrainListStationUiState())
        private set

    @OptIn(ExperimentalMaterial3Api::class)
    fun init(line: String) {
        if (line != uiState.trainLine.toString()) {
            val trainLine = TrainLine.fromString(line)
            val title = trainLine.toStringWithLine()

            uiState = uiState.copy(
                title = title,
                trainLine = trainLine,
                trainStations = listOf(),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(TopAppBarState(-Float.MAX_VALUE, 0f, 0f)),
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun loadData(trainLine: TrainLine) {
        Single.fromCallable { trainService.getStationsForLine(trainLine) }
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    uiState = uiState.copy(
                        trainStations = result,
                    )
                },
                {
                    Timber.e(it, "Could not load stations for line ${trainLine.toTextString()}")
                }
            )
    }

    companion object {
        fun provideFactory(
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
                    return TrainListStationViewModel() as T
                }
            }
    }
}
