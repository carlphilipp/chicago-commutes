package fr.cph.chicago.core.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.LocationViewModel
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.core.viewmodel.MainViewModel

@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    title: String,
    mainViewModel: MainViewModel,
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    // TODO with OSM
}
