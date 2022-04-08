package fr.cph.chicago.core.ui.screen.settings

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.jakewharton.processphoenix.ProcessPhoenix
import fr.cph.chicago.core.model.dto.PreferenceDTO
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    title: String,
    viewModel: DeveloperOptionsViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Timber.d("Compose DeveloperOptionsScreen")
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.settingsDeveloperScrollBehavior) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            viewModel.getAllFavorites()
            viewModel.setMapDebug()
        }
    })

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.SettingsDeveloperOptions,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    item {
                        DisplayElementSwitchView(
                            title = "Map",
                            description = "Show debug info on map",
                            onClick = {
                                viewModel.showMapDebug(!viewModel.uiState.showMapDebug)
                                settingsViewModel.refreshCurrentTheme()
                            },
                            imageVector = Icons.Outlined.Map,
                            isChecked = viewModel.uiState.showMapDebug,
                        )
                    }
                    item {
                        Column {
                            Text(
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 5.dp),
                                text = "Data Local",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            DisplayElementView(
                                title = "Show cache",
                                description = "Show local data",
                                onClick = {
                                    viewModel.showHideCacheData()
                                },
                                imageVector = Icons.Outlined.Insights,
                            )
                            if (viewModel.uiState.showCache) {
                                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    Spacer(modifier = Modifier.padding(10.dp))
                                    CacheDetail(viewModel)
                                }
                            }
                        }
                    }
                    item {
                        DisplayElementView(
                            title = "Clear cache",
                            description = "Delete local data",
                            onClick = {
                                viewModel.showClearCache(true)
                            },
                            imageVector = Icons.Outlined.Clear,
                        )
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        })

    if (viewModel.uiState.showClearCacheDialog) {
        ClearCacheDialog(viewModel = viewModel)
    }
}

@Composable
fun CacheDetail(viewModel: DeveloperOptionsViewModel) {
    viewModel.uiState.preferences.forEach { preference ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = preference.name.value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            preference.favorites.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClearCacheDialog(viewModel: DeveloperOptionsViewModel) {
    val context = LocalContext.current
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
                    viewModel.restartApp(context = context)
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

data class DeveloperOptionsState(
    val showCache: Boolean = false,
    val showMapDebug: Boolean = false,
    val showClearCacheDialog: Boolean = false,
    val preferences: List<PreferenceDTO> = listOf(),
)

class DeveloperOptionsViewModel(
    private val preferenceService: PreferenceService = PreferenceService,
    private val realmConfig: RealmConfig = RealmConfig,
) : ViewModel() {
    var uiState by mutableStateOf(DeveloperOptionsState())
        private set

    fun showMapDebug(value: Boolean) {
        preferenceService.saveShowDebug(value)
        setMapDebug()
        getAllFavorites()
    }

    fun showHideCacheData() {
        uiState = uiState.copy(showCache = !uiState.showCache)
    }

    fun setMapDebug() {
        uiState = uiState.copy(
            showMapDebug = preferenceService.getShowDebug()
        )
    }

    fun getAllFavorites() {
        uiState = uiState.copy(preferences = listOf())
        preferenceService.getAllFavorites()
            .observeOn(Schedulers.computation())
            .map { favorites -> favorites.preferences }
            .flatMapObservable { preferences -> Observable.fromIterable(preferences) }
            .filter { preference -> preference.favorites.isNotEmpty() }
            .sorted { preference1, preference2 -> preference1.name.value.compareTo(preference2.name.value) }
            .observeOn(Schedulers.computation())
            .subscribe(
                { preference ->
                    val newPreferences = uiState.preferences + preference
                    uiState = uiState.copy(
                        preferences = newPreferences
                    )
                },
                { throwable ->
                    Timber.e(throwable)
                })
    }

    fun restartApp(context: Context) {
        ProcessPhoenix.triggerRebirth(context)
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
            Timber.e(ignored, "Could not delete cache")
        }
    }

    companion object {
        fun provideFactory(
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return DeveloperOptionsViewModel() as T
                }
            }
    }
}
