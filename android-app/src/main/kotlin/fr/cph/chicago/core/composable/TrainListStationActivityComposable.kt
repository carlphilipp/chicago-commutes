package fr.cph.chicago.core.composable

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.model.enumeration.toComposeColor
import fr.cph.chicago.service.TrainService
import org.apache.commons.lang3.StringUtils

private val trainService = TrainService

class TrainListStationActivityComposable : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lineParam = if (savedInstanceState != null) savedInstanceState.getString(getString(R.string.bundle_train_line))
            ?: StringUtils.EMPTY else intent.getStringExtra(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        val trainLine = TrainLine.fromString(lineParam)
        val title = trainLine.toStringWithLine()
        val trainStations: List<TrainStation> = trainService.getStationsForLine(trainLine)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                TrainLineStops(
                    title = title,
                    trainStations = trainStations,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainLineStops(title: String, trainStations: List<TrainStation>) {
    val context = LocalContext.current
    Column {
        TopBar(title)
        Scaffold(
            //snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
            content = {
                LazyColumn(modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxSize()) {
                    items(trainStations.size) { index ->
                        val station = trainStations[index]
                        TextButton(onClick = {
                            val extras = Bundle()
                            val intent = Intent(context, TrainStationComposable::class.java)
                            extras.putString(context.getString(R.string.bundle_train_stationId), station.id.toString())
                            intent.putExtras(extras)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(context, intent, null)
                        }) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            station.lines.forEach { line ->
                                ColoredBox(modifier = Modifier.padding(start = 5.dp), color = line.toComposeColor())
                            }
                        }
                    }
                }
            }
        )
    }
}
