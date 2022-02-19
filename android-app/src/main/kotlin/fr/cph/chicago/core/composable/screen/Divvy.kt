package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.BikeStationComposable
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.model.BikeStation

@Composable
fun Divvy(modifier: Modifier = Modifier, bikeStations: List<BikeStation>) {

    val context = LocalContext.current
    var searchBikeStations by remember { mutableStateOf(bikeStations) }
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            TextFieldMaterial3(
                text = textSearch,
                onValueChange = { value ->
                    textSearch = value
                    searchBikeStations = bikeStations.filter { bikeStation ->
                        bikeStation.name.contains(value.text, true)
                    }
                }
            )
        }
        items(searchBikeStations) { bikeStation ->
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onClick = {
                    val intent = Intent(context, BikeStationComposable::class.java)
                    val extras = Bundle()
                    extras.putParcelable(context.getString(R.string.bundle_bike_station), bikeStation)
                    intent.putExtras(extras)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(context, intent, null)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        bikeStation.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
