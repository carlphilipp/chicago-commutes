package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainLineStopsScreen(viewModel: TrainListStationViewModel) {

    val navController = LocalNavController.current

    LazyColumn(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxSize()
    ) {
        items(viewModel.uiState.trainStations.size) { index ->
            val station = viewModel.uiState.trainStations[index]
            TextButton(onClick = {
                navController.navigate(Screen.TrainDetails, mapOf("stationId" to station.id))
            }) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                station.lines.forEach { line ->
                    ColoredBox(modifier = Modifier.padding(start = 5.dp), color = line.color)
                }
            }
        }
    }
}

data class TrainListStationUiState(
    val title: String = "",
    val trainLine: TrainLine = TrainLine.NA,
    val trainStations: List<TrainStation> = listOf(),
)

class TrainListStationViewModel(
    val line: String,
    private val trainService: TrainService = TrainService,
) : ViewModel() {

    var uiState by mutableStateOf(TrainListStationUiState())
        private set

    init {
        viewModelScope.launch {
            val trainLine = TrainLine.fromString(line)
            val title = trainLine.toStringWithLine()

            uiState = TrainListStationUiState(
                title = title,
                trainLine = trainLine,
            )
            loadData(trainLine)
        }
    }

    private fun loadData(trainLine: TrainLine) {
        Single.fromCallable { trainService.getStationsForLine(trainLine) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    uiState = uiState.copy(trainStations = result)
                },
                {
                    Timber.e(it, "Could not load stations for line ${trainLine.toTextString()}")
                }
            )

    }

    companion object {
        fun provideFactory(
            line: String,
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
                    return TrainListStationViewModel(line) as T
                }
            }
    }
}
