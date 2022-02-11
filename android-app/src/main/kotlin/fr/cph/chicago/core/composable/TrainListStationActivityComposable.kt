package fr.cph.chicago.core.composable

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
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
            ChicagoCommutesTheme {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(trainStations.size) { index ->
                        val station = trainStations[index]
                        TextButton(onClick = {
                            val extras = Bundle()
                            val intent = Intent(App.instance.applicationContext, TrainStationComposable::class.java)
                            extras.putString(App.instance.getString(R.string.bundle_train_stationId), station.id.toString())
                            intent.putExtras(extras)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            App.instance.startActivity(intent)
                        }) {
                            Text(
                                text = station.name
                            )
                            station.lines.forEach {  line ->
                                ColoredBox(color = Color(line.color))
                            }
                        }
                    }
                }
            }
        }
    }
}
