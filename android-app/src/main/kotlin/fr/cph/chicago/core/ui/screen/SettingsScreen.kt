package fr.cph.chicago.core.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.redux.ResetStateAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import fr.cph.chicago.util.startSettingsDisplayActivity

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel, util: Util = Util) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
    ) {
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Brightness6,
                title = "Display",
                description = "Theme, dark mode and fonts",
                onClick = {
                    startSettingsDisplayActivity(context = context)
                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.DeveloperMode,
                title = "Developer options",
                description = "Beep boop",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
        item {
            SettingsElementView(
                imageVector = Icons.Outlined.Info,
                title = "About",
                description = "Chicago commutes",
                onClick = {

                }
            )
        }
/*        item {
            // Theme
            Column(
                modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showThemeChangerDialog(true) }) {
                Row(modifier = cellModifier) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.preferences_data_theme_title),
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
                            text = stringResource(id = R.string.preferences_data_cache_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = stringResource(id = R.string.preferences_clear_cache_title),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(id = R.string.preferences_clear_cache_desc),
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
                        val intent = Intent(context, DeveloperOptionsActivity::class.java)
                        startActivity(context, intent, null)
                    }) {
                // Developer options
                Row(modifier = cellModifier) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.preferences_developer_options),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = stringResource(id = R.string.preferences_developer_options_show),
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
                        text = stringResource(id = R.string.preferences_about_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = stringResource(id = R.string.preferences_version) + " ${util.getCurrentVersion()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }*/
    }

    if (uiState.showThemeChangerDialog) {
        ThemeChangerDialog(viewModel = viewModel)
    }

    if (uiState.showClearCacheDialog) {
        ClearCacheDialog(viewModel = viewModel)
    }
}

@Composable
fun SettingsElementView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = 20.dp),
                imageVector = imageVector,
                contentDescription = null
            )
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
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ThemeChangerDialog(viewModel: SettingsViewModel) {
    var currentThemeState by remember { mutableStateOf(viewModel.uiState.theme) }
    currentThemeState = viewModel.uiState.theme
    var currentDynamicColor by remember { mutableStateOf(viewModel.uiState.dynamicColorEnabled) }
    currentDynamicColor = viewModel.uiState.dynamicColorEnabled

    AlertDialog(
        modifier = Modifier.padding(horizontal = 50.dp),
        onDismissRequest = { viewModel.showThemeChangerDialog(false) },
        // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(text = "Theme change") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Theme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentThemeState = theme },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            // modifier = Modifier.padding(10.dp),
                            selected = theme == currentThemeState,
                            onClick = { currentThemeState = theme }
                        )
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Divider(thickness = 2.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = currentDynamicColor,
                            onCheckedChange = { currentDynamicColor = !currentDynamicColor }
                        )
                        Text(
                            text = "Enable dynamic colors"
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    viewModel.setTheme(currentThemeState)
                    viewModel.setDynamicColor(currentDynamicColor)
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
        onDismissRequest = { viewModel.showClearCache(false) },
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
    val dynamicColorEnabled: Boolean = false,
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

    fun setDynamicColor(value: Boolean) {
        preferenceService.saveDynamicColor(value)
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
            theme = preferenceService.getTheme(),
            dynamicColorEnabled = preferenceService.getDynamicColor(),
        )
    }
}
