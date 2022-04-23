package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.BottomSheetData
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    // TODO with OSM
}

data class NearbyScreenUiState(
    val bottomSheetData: BottomSheetData = BottomSheetData(),
)

class NearbyViewModel() :  ViewModel() {
    var uiState by mutableStateOf(NearbyScreenUiState())
        private set

    fun collapseBottomSheet(scope: CoroutineScope, runAfter: () -> Unit) {
        TODO("Not yet implemented")
    }

    fun resetDetails() {
        TODO("Not yet implemented")
    }

    fun setMapCenterLocationAndLoadNearby(position: Position, zoom: Float) {
        TODO("Not yet implemented")
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
                    return NearbyViewModel() as T
                }
            }
    }
}
