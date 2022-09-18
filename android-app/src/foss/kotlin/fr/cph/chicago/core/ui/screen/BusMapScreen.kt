package fr.cph.chicago.core.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.ui.common.BottomSheetPagerData
import fr.cph.chicago.core.ui.common.BottomSheetStatus
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.service.BusService
import javax.inject.Inject

@Composable
fun BusMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    settingsViewModel: SettingsViewModel,
) {
}

data class FossMapBusUiState(
    val busRouteId: String = "",
    val bottomSheetStatus: BottomSheetStatus = BottomSheetStatus.EXPAND,

    val bikeStationBottomSheet: List<BottomSheetPagerData> = listOf(),
    val busData: List<BottomSheetPagerData> = listOf(),
)


@HiltViewModel
class MapBusViewModel @Inject constructor(
    busRouteId: String,
    private val busService: BusService = BusService,
) : ViewModel() {

    fun resetDetails() {
        TODO("Not yet implemented")
    }

    var uiState by mutableStateOf(FossMapBusUiState())
        private set
}
