package fr.cph.chicago.core.ui.screen.settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.activity.settings.ThemeColorChooserActivity
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.SwitchMaterial3
import fr.cph.chicago.core.ui.screen.SettingsViewModel
import fr.cph.chicago.core.ui.screen.ThemeChangerDialog
import fr.cph.chicago.util.startSettingsActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current as ComponentActivity
    val navController = LocalNavController.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "Theme",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            DisplayElementView(
                title = "Theme color",
                description = "Automatic or choose a color",
                onClick = {
                    startSettingsActivity(context = context, clazz = ThemeColorChooserActivity::class.java)
                },
                imageVector = Icons.Outlined.Palette
            )
        }
        item {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            DisplayElementSwitchView(
                title = "Follow System",
                description = "Select dark/light mode based on system",
                onClick = {
                    if (viewModel.uiState.theme != Theme.AUTO) {
                        viewModel.setTheme(Theme.AUTO)
                    }
                },
                imageVector = Icons.Outlined.Brightness6,
                isChecked = uiState.theme == Theme.AUTO,
            )
            DisplayElementSwitchView(
                title = "Light Mode",
                description = "Enable",
                onClick = {
                    if (viewModel.uiState.theme != Theme.LIGHT) {
                        viewModel.setTheme(Theme.LIGHT)
                    }
                },
                imageVector = Icons.Outlined.LightMode,
                isChecked = uiState.theme == Theme.LIGHT,
            )
            DisplayElementSwitchView(

                title = "Dark Mode",
                description = "Enable",
                onClick = {
                    if (viewModel.uiState.theme != Theme.DARK) {
                        viewModel.setTheme(Theme.DARK)
                    }
                },
                imageVector = Icons.Outlined.DarkMode,
                isChecked = uiState.theme == Theme.DARK,
            )
        }
        item {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "Fonts",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            DisplayElementSwitchView(
                title = "Fonts",
                description = "Choose fonts",
                onClick = {

                },
                imageVector = Icons.Default.FontDownload,
                isChecked = false,
            )
        }
    }
    if (uiState.showThemeChangerDialog) {
        ThemeChangerDialog(viewModel = viewModel)
    }
    BackHandler {
        navController.navigateBack()
    }
}

@Composable
private fun DisplayElementView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = 20.dp),
                imageVector = imageVector,
                contentDescription = null
            )
            Column(
                modifier = Modifier,
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
        }
    }
}

@Composable
fun DisplayElementSwitchView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val newModifier = if (enabled) modifier.clickable(onClick = onClick) else modifier.fillMaxWidth()
    val alpha = if (enabled) 1.0f else 0.2f
    Row(modifier = newModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .alpha(alpha),
                imageVector = imageVector,
                contentDescription = null
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    modifier = Modifier.alpha(alpha),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    modifier = Modifier.alpha(alpha),
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SwitchMaterial3(
                    modifier = Modifier,
                    onCheckedChange = {
                        onClick()
                    },
                    enabled = enabled,
                    checked = isChecked,
                )
            }
        }
    }
}