package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import fr.cph.chicago.core.ui.RefreshTopBar
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.dto.PreferenceDTO
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class DeveloperOptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                val viewModel = DeveloperOptionsViewModel().initModel()

                DeveloperOptions(viewModel = viewModel)
            }
        }
    }
}

data class DeveloperOptionsState(
    val showCache: Boolean = false,
    val preferences: List<PreferenceDTO> = listOf(),
)

class DeveloperOptionsViewModel(private val preferenceService: PreferenceService = PreferenceService) {
    var uiState by mutableStateOf(DeveloperOptionsState())
        private set

    fun initModel(): DeveloperOptionsViewModel {
        getAllFavorites()
        return this
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptions(viewModel: DeveloperOptionsViewModel) {
    Scaffold(
        topBar = { RefreshTopBar(title = stringResource(id = R.string.preferences_developer_options)) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showHideCacheData() }
            ) {
                Row(modifier = Modifier.padding(15.dp)) {
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
        }
    )
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
