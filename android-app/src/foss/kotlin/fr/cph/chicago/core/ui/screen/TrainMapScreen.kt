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
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.ui.common.BottomSheetStatus
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.service.TrainService
import kotlinx.coroutines.CoroutineScope

@Composable
fun TrainMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    settingsViewModel: SettingsViewModel,
) {

}

data class FossMapTrainUiState(
    val line: TrainLine = TrainLine.NA,
    val bottomSheetStatus: BottomSheetStatus = BottomSheetStatus.COLLAPSE,
    val train: Train = Train(),
    val trainEtas: List<Pair<String, String>> = listOf(),
)

class MapTrainViewModel(
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(FossMapTrainUiState())
        private set

    fun reloadData() {
        TODO("Not yet implemented")
    }

    fun loadTrainLine(scope: CoroutineScope, trainLine: TrainLine) {
        TODO("Not yet implemented")
    }

    fun collapseBottomSheet(scope: CoroutineScope, runAfter: () -> Unit) {
        TODO("Not yet implemented")
    }

    fun resetDetails() {
        TODO("Not yet implemented")
    }

    fun setTrainLine(trainLine: TrainLine) {
        uiState = uiState.copy(line = trainLine)
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
                    return MapTrainViewModel() as T
                }
            }
    }
}
