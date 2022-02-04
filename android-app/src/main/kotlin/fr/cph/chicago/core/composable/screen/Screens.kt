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
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector) {
    object Favorites : DrawerScreens("Favorites", "fav", Icons.Filled.Favorite)
    object Train : DrawerScreens("Train", "train", Icons.Filled.Train)
    object Bus : DrawerScreens("Bus", "bus", Icons.Filled.DirectionsBus)
    object Divvy : DrawerScreens("Divvy", "divvy", Icons.Filled.DirectionsBike)
    object Nearby : DrawerScreens("Nearby", "nearby", Icons.Filled.NearMe)
    object Map : DrawerScreens("CTA map", "map", Icons.Filled.Map)
    object Alerts : DrawerScreens("CTA alerts", "alerts", Icons.Filled.Warning)
    object Rate : DrawerScreens("Rate this app", "rate", Icons.Filled.StarRate)
    object Settings : DrawerScreens("Settings", "settings", Icons.Filled.Settings)
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
