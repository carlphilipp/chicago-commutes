package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.PreferenceDTO
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

data class DeveloperOptionsState(
    val showCache: Boolean = false,
    val preferences: List<PreferenceDTO> = listOf(),
)

class DeveloperOptionsViewModel(private val preferenceService: PreferenceService = PreferenceService) : ViewModel() {
    var uiState by mutableStateOf(DeveloperOptionsState())
        private set

    init {
        getAllFavorites()
    }

    fun showHideCacheData() {
        uiState = uiState.copy(showCache = !uiState.showCache)
    }

    private fun getAllFavorites() {
        preferenceService.getAllFavorites()
            .observeOn(Schedulers.computation())
            .map { favorites -> favorites.preferences }
            .flatMapObservable { preferences -> Observable.fromIterable(preferences) }
            .filter { preference -> preference.favorites.isNotEmpty() }
            .sorted { preference1, preference2 -> preference1.name.value.compareTo(preference2.name.value) }
            .observeOn(AndroidSchedulers.mainThread())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    viewModel: DeveloperOptionsViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Column {
        DisplayTopBar(
            title = title,
            viewModel = navigationViewModel,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            item {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showHideCacheData() }
                    .padding(15.dp)) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.preferences_data_cache_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(id = R.string.developer_show_cache),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (viewModel.uiState.showCache) {
                            Spacer(modifier = Modifier.padding(10.dp))
                            CacheDetail(viewModel)
                        }
                    }
                }
            }
            item { NavigationBarsSpacer() }
        }
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
