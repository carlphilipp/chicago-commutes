package fr.cph.chicago.core.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.service.BikeService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBikesViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
    title: String,
) {

}

data class GoogleMapBikeUiState(
    val id: String,
)

class MapBikesViewModel constructor(
    id: String,
    private val bikeService: BikeService = BikeService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapBikeUiState(id = id))
        private set
}
