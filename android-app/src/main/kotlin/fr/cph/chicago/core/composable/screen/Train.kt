package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.TrainListStationActivityComposable
import fr.cph.chicago.core.composable.TrainStationComposable
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.TrainService

private val trainService = TrainService

@Composable
fun Train(modifier: Modifier = Modifier) {
/*    val context = LocalContext.current
    val trainStations: List<TrainStation> = trainService.getStationsForLine(TrainLine.BROWN)
    Column {
        //TopBar(title)
        //Text("test")
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
                        ColoredBox(modifier = Modifier.padding(start = 5.dp), color = Color(line.color))
                    }
                }
            }
        }
    }*/
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(TrainLine.size() - 1) { index ->
            val line = TrainLine.values()[index]
            val context = LocalContext.current
            TextButton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                onClick = {
                    val extras = Bundle()
                    val intent = Intent(context, TrainListStationActivityComposable::class.java)
                    extras.putString(context.getString(R.string.bundle_train_line), line.toString())
                    intent.putExtras(extras)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(context, intent, null)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    ColoredBox(modifier = Modifier.padding(end = 20.dp), color = Color(line.color))
                    Text(
                        text = line.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
