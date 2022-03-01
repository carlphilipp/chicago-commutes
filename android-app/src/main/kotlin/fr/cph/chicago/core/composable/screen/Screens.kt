package fr.cph.chicago.core.composable.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import fr.cph.chicago.core.composable.viewmodel.locationViewModel
import fr.cph.chicago.core.composable.viewmodel.mainViewModel
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel

sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector, val component: @Composable () -> Unit) {
    object Favorites : DrawerScreens("Favorites", "fav", Icons.Filled.Favorite, { Favorites(mainViewModel = mainViewModel) })
    object Train : DrawerScreens("Train", "train", Icons.Filled.Train, { Train(mainViewModel = mainViewModel) })
    object Bus : DrawerScreens("Bus", "bus", Icons.Filled.DirectionsBus, { Bus(mainViewModel = mainViewModel) })
    object Divvy : DrawerScreens("Divvy", "divvy", Icons.Filled.DirectionsBike, { Divvy(mainViewModel = mainViewModel) })
    object Nearby : DrawerScreens("Nearby", "nearby", Icons.Filled.NearMe, { Nearby(mainViewModel = mainViewModel, locationViewModel = locationViewModel) })
    object Map : DrawerScreens("CTA map", "map", Icons.Filled.Map, { Map() })
    object Alerts : DrawerScreens("CTA alerts", "alerts", Icons.Filled.Warning, { Alerts(mainViewModel = mainViewModel) })
    object Rate : DrawerScreens("Rate this app", "rate", Icons.Filled.StarRate, { Rate(mainViewModel = mainViewModel) })
    object Settings : DrawerScreens("Settings", "settings", Icons.Filled.Settings, { Settings(viewModel = settingsViewModel) })
}

val screens = listOf(
    DrawerScreens.Favorites,
    DrawerScreens.Train,
    DrawerScreens.Bus,
    DrawerScreens.Divvy,
    DrawerScreens.Nearby,
    DrawerScreens.Map,
    DrawerScreens.Alerts,
    DrawerScreens.Rate,
    DrawerScreens.Settings
)
