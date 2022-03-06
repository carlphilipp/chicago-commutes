package fr.cph.chicago.core.ui.screen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.TrainListStationActivity
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.model.enumeration.TrainLine

@Composable
fun TrainScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(TrainLine.size() - 1) { index ->
            val line = TrainLine.values()[index]
            val context = LocalContext.current
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onClick = {
                    val extras = Bundle()
                    val intent = Intent(context, TrainListStationActivity::class.java)
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
                    ColoredBox(modifier = Modifier.padding(end = 20.dp), color = line.color)
                    Text(
                        text = line.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
