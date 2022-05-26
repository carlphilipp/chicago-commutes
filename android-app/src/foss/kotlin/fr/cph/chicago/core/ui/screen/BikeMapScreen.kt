package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetStatus
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.service.BikeService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    settingsViewModel: SettingsViewModel,
    title: String,
) {

}

data class FossMapBikeUiState(
    val id: String = "",
    val bottomSheetStatus: BottomSheetStatus = BottomSheetStatus.EXPAND,
    val bottomSheetTitle: String = "Bikes",

    val bikeStationBottomSheet: List<BottomSheetPagerData> = listOf(),
)

class MapBikesViewModel constructor(
    private val bikeService: BikeService = BikeService,
) : ViewModel() {
    fun setId(id: String) {
        uiState = uiState.copy(id = id)
    }

    var uiState by mutableStateOf(FossMapBikeUiState())
        private set

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
                    return MapBikesViewModel() as T
                }
            }
    }
}
