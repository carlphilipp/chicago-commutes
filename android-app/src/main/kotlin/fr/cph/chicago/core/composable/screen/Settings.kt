package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Settings(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            // Theme
            Row {
                Icon(
                    imageVector = Icons.Filled.Theaters,
                    contentDescription = "Theme"
                )
                Column {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Light",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Divider(thickness = 1.dp)
        }
        item {
            // Data cache
            Row {
                Icon(
                    imageVector = Icons.Filled.DataExploration,
                    contentDescription = "Data cache"
                )
                Column {
                    Text(
                        text = "Data cache",
                        style = MaterialTheme.typography.titleSmall,
                    )
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
            Row {
                Icon(
                    imageVector = Icons.Filled.DeveloperMode,
                    contentDescription = "Developer mode"
                )
                Column {
                    Text(
                        text = "Developer options",
                        style = MaterialTheme.typography.titleSmall,
                    )
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
            Row {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "About"
                )
                Column {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Version x.x.x",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
