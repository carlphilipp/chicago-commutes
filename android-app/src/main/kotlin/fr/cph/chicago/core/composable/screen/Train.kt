package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.core.composable.TrainListStationActivityComposable
import fr.cph.chicago.core.model.enumeration.TrainLine

@Composable
fun Train() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(TrainLine.size() - 1) { index ->
            val line = TrainLine.values()[index]
            val context = LocalContext.current
            TextButton(onClick = {
                val intent = Intent(context, TrainListStationActivityComposable::class.java)
                val extras = Bundle()
                extras.putString("line", line.toString())
                intent.putExtras(extras)
                startActivity(context, intent, null)
            }) {
                Text(
                    text = line.toStringWithLine()
                )
            }
        }
    }
}
