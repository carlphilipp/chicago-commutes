package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.ui.RefreshTopBar
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.startTrainStationActivity
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class TrainListStationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lineParam = if (savedInstanceState != null) savedInstanceState.getString(getString(R.string.bundle_train_line))
            ?: "" else intent.getStringExtra(getString(R.string.bundle_train_line)) ?: ""
        val trainLine = TrainLine.fromString(lineParam)
        val title = trainLine.toStringWithLine()
        val viewModel = TrainListStationViewModel(
            title = title,
            trainLine = trainLine,
        )
        viewModel.loadData()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                TrainLineStops(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainLineStops(viewModel: TrainListStationViewModel) {
    val context = LocalContext.current
    Column {
        RefreshTopBar(viewModel.uiState.title)
        Scaffold(
            content = {
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .fillMaxSize()
                ) {
                    items(viewModel.uiState.trainStations.size) { index ->
                        val station = viewModel.uiState.trainStations[index]
                        TextButton(onClick = { startTrainStationActivity(context, station) }) {
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
        )
    }
}

data class TrainListStationUiState(
    val title: String,
    val trainLine: TrainLine,
    val trainStations: List<TrainStation> = listOf(),
)

class TrainListStationViewModel(
    private val title: String,
    private val trainLine: TrainLine,
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(
        TrainListStationUiState(
            title = title,
            trainLine = trainLine
        )
    )
        private set

    fun loadData() {
        Single.fromCallable { trainService.getStationsForLine(trainLine) }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    uiState = uiState.copy(trainStations = result)
                },
                {
                    Timber.e(it, "Could not load stations for line ${trainLine.toTextString()}")
                }
            )
    }
}
