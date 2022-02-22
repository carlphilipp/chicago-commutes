package fr.cph.chicago.core.composable.screen

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.core.activity.DeveloperOptionsActivity
import fr.cph.chicago.core.composable.DeveloperOptionsComposable
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.redux.ResetStateAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import timber.log.Timber

@Composable
fun Settings(modifier: Modifier = Modifier, viewModel: SettingsViewModel, util: Util = Util) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    Timber.d("Current theme: ${uiState.theme}")

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        val cellModifier = Modifier.padding(15.dp)
        item {
            // Theme
            Column(
                modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showThemeChangerDialog(true) }) {
                Row(modifier = cellModifier) {
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
        }
        item {
            // Data cache
            Column(
                modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showClearCache(true) }) {
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
        }
        item {
            Column(
                modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, DeveloperOptionsComposable::class.java)
                        startActivity(context, intent, null)
                    }) {
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
        ThemeChangerDialog(viewModel = viewModel)
    }

    if (uiState.showClearCacheDialog) {
        ClearCacheDialog(viewModel = viewModel)
    }
}

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ThemeChangerDialog(viewModel: SettingsViewModel) {
    var currentThemeState by remember { mutableStateOf(viewModel.uiState.theme) }
    currentThemeState = viewModel.uiState.theme

    AlertDialog(
        modifier = Modifier.padding(horizontal = 50.dp),
        onDismissRequest = { viewModel.showThemeChangerDialog(false) },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentThemeState = theme
                            },
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
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    viewModel.setTheme(currentThemeState)
                    viewModel.showThemeChangerDialog(false)
                },
            ) {
                Text(
                    text = "Save",
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.showThemeChangerDialog(false) },
            ) {
                Text(
                    text = "Cancel",
                )
            }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClearCacheDialog(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val activity = (context as ComponentActivity)
    AlertDialog(
        modifier = Modifier.padding(horizontal = 50.dp),
        onDismissRequest = {},
        // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = "Clear cache",
            )
        },
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 15.dp),
                    text = "This is going to:",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "- Delete all your favorites",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "- Clear application cache",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "- Reload the application",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    viewModel.showClearCache(false)
                    viewModel.clearLocalData(context = context)
                    viewModel.restartApp(context = context, activity = activity)
                },
            ) {
                Text(
                    text = "Clear cache",
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    viewModel.showClearCache(false)
                },
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
    val showClearCacheDialog: Boolean = false,
)

class SettingsViewModel(private val preferenceService: PreferenceService = PreferenceService, private val realmConfig: RealmConfig = RealmConfig) {
    var uiState by mutableStateOf(SettingsState())
        private set

    fun initModel(): SettingsViewModel {
        refreshCurrentTheme()
        return this
    }

    fun setTheme(theme: Theme) {
        preferenceService.saveTheme(theme)
        refreshCurrentTheme()
    }

    fun showThemeChangerDialog(show: Boolean) {
        uiState = uiState.copy(
            showThemeChangerDialog = show
        )
    }

    fun showClearCache(show: Boolean) {
        uiState = uiState.copy(
            showClearCacheDialog = show
        )
    }

    fun clearLocalData(context: Context) {
        deleteCache(context)
        preferenceService.clearPreferences()
        realmConfig.cleanRealm()
    }

    private fun deleteCache(context: Context?) {
        try {
            context?.cacheDir?.deleteRecursively()
        } catch (ignored: Exception) {
        }
    }

    fun restartApp(context: Context, activity: ComponentActivity) {
        store.dispatch(ResetStateAction())
        val intent = Intent(context, BaseActivity::class.java)
        activity.finish()
        startActivity(context, intent, null)
    }

    private fun refreshCurrentTheme() {
        uiState = uiState.copy(
            theme = preferenceService.getTheme()
        )
    }
}
