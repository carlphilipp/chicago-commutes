package fr.cph.chicago.core.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.ColoredBox
import timber.log.Timber

@Composable
fun TrainScreen(modifier: Modifier = Modifier) {
    Timber.d("Compose TrainScreen")
    val navController = LocalNavController.current
    val lines by remember { mutableStateOf(TrainLine.values().filter { line -> line != TrainLine.NA }) }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(lines) { line ->
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onClick = {
                    navController.navigate(
                        screen = Screen.TrainList,
                        arguments = mapOf("line" to line.toString()),
                        customTitle = line.toStringWithLine()
                    )
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
