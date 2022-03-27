package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.dto.PreferenceDTO
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.screen.settings.DisplayElementSwitchView
import fr.cph.chicago.core.ui.screen.settings.DisplayElementView
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    viewModel: DeveloperOptionsViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
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
                    screen = Screen.DeveloperOptions,
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
                            },
                            imageVector = Icons.Outlined.Map,
                            isChecked = viewModel.uiState.showMapDebug,
                        )
                    }
                    item {
                        Column {
                            DisplayElementView(
                                title = "Data cache",
                                description = "Show cache",
                                onClick = {
                                    viewModel.showHideCacheData()
                                },
                                imageVector = Icons.Outlined.DeveloperMode,
                            )
                            if (viewModel.uiState.showCache) {
                                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    Spacer(modifier = Modifier.padding(10.dp))
                                    CacheDetail(viewModel)
                                }
                            }
                        }
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        })
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

data class DeveloperOptionsState(
    val showCache: Boolean = false,
    val showMapDebug: Boolean = false,
    val preferences: List<PreferenceDTO> = listOf(),
)

class DeveloperOptionsViewModel(private val preferenceService: PreferenceService = PreferenceService) : ViewModel() {
    var uiState by mutableStateOf(DeveloperOptionsState())
        private set

    /*init {
        getAllFavorites()
        setMapDebug()
    }*/

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
