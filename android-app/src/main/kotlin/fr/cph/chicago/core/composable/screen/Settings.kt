package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.cph.chicago.service.PreferenceService
import timber.log.Timber

@Composable
fun Settings(modifier: Modifier = Modifier) {
    val theme = PreferenceService.getTheme()
    Timber.i("Current theme found: $theme")
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        val cellModifier = Modifier.padding(15.dp)
        item {
            // Theme
            Row(modifier = cellModifier) {
                Column {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Divider(thickness = 1.dp)
        }
        item {
            // Data cache
            Row(modifier = cellModifier) {
                Column {
                    Text(
                        text = "Data cache",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Clear cache",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Remove all cache data",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Divider(thickness = 1.dp)
        }
        item {
            // Developer options
            Row(modifier = cellModifier) {
                Column {
                    Text(
                        text = "Developer options",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Show developer options",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Divider(thickness = 1.dp)
        }
        item {
            // About
            Row(modifier = cellModifier) {
                Column {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Version x.x.x",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
