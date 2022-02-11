package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.core.composable.TrainListStationActivityComposable
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.model.enumeration.TrainLine

@Composable
fun Train(modifier: Modifier = Modifier) {
    Text(text = "deee")
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Text(text = "deee2")
        }
        items(TrainLine.size() - 1) { index ->
            val line = TrainLine.values()[index]
            val context = LocalContext.current
/*            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {*/
/*            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(context, TrainListStationActivityComposable::class.java)
                    val extras = Bundle()
                    extras.putString("line", line.toString())
                    intent.putExtras(extras)
                    startActivity(context, intent, null)
                }
            ) {*/
                ColoredBox(modifier = Modifier.padding(end = 20.dp), color = Color(line.color))
                Text(
                    text = line.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            //}
            //}
        }
    }
}
