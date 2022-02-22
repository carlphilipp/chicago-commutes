package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import timber.log.Timber

@Composable
fun Settings(modifier: Modifier = Modifier, viewModel: SettingsViewModel, util: Util = Util) {

    val uiState = viewModel.uiState

    Timber.i("Current theme found: ${uiState.theme}")

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        val cellModifier = Modifier.padding(15.dp)
        item {
            // Theme
            Row(modifier = cellModifier.clickable {
                viewModel.showThemeChangerDialog()
            }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = uiState.theme.description,
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
                        text = "Version ${util.getCurrentVersion()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
    if (uiState.showThemeChangerDialog) {
        Timber.i("show dialog")
        ThemeChangerDialog(viewModel = viewModel)
    }
}

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ThemeChangerDialog(viewModel: SettingsViewModel) {
    var currentThemeState by remember { mutableStateOf(viewModel.uiState.theme) }
    currentThemeState = viewModel.uiState.theme

    AlertDialog(
        modifier = Modifier.padding(horizontal = 50.dp),
        onDismissRequest = { viewModel.hideThemeChangerDialog() },
        // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = "Theme change",
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Theme.values().forEach { theme ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(10.dp),
                            selected = theme == currentThemeState,
                            onClick = {
                                currentThemeState = theme
                            }
                        )
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                   viewModel.setTheme(currentThemeState)
                    viewModel.hideThemeChangerDialog()
                },
            ) {
                Text(
                    text = "Save",
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.hideThemeChangerDialog() },
            ) {
                Text(
                    text = "Cancel",
                )
            }
        },
    )
}

data class SettingsState(
    val theme: Theme = Theme.AUTO,
    val showThemeChangerDialog: Boolean = false,
)

class SettingsViewModel(private val preferenceService: PreferenceService = PreferenceService) {
    var uiState by mutableStateOf(SettingsState())
        private set

    fun initModel(): SettingsViewModel {
        refreshCurrentTheme()
        return this
    }

    fun refreshCurrentTheme() {
        uiState = uiState.copy(
            theme = preferenceService.getTheme()
        )
    }

    fun setTheme(theme: Theme) {
        preferenceService.saveTheme(theme)
        refreshCurrentTheme()
    }

    fun showThemeChangerDialog() {
        uiState = uiState.copy(
            showThemeChangerDialog = true
        )
    }

    fun hideThemeChangerDialog() {
        uiState = uiState.copy(
            showThemeChangerDialog = false
        )
    }
}
