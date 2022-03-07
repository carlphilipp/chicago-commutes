package fr.cph.chicago.core.activity.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.ui.screen.SettingsViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel

class DisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                DisplaySettingsView(viewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun DisplaySettingsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current as ComponentActivity
    Column {
        LargeTopAppBar(
            navigationIcon = {
                IconButton(onClick = { context.finish() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            title = {
                Text(
                    text = "Display"
                )
            },
        )
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            item {
                DisplayElementView(
                    title = "Theme",
                    description = "Choose theme",
                    onClick = {

                    }
                )
            }
            item {
                DisplayElementView(
                    title = "Dark Mod",
                    description = "Enable",
                    onClick = {

                    }
                )
            }
            item {
                DisplayElementView(
                    title = "Fonts",
                    description = "Choose fonts",
                    onClick = {

                    }
                )
            }
        }
    }
}

@Composable
fun DisplayElementView(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            modifier = Modifier,
            onCheckedChange ={
               true
            },
            checked = true
        )
    }
}
