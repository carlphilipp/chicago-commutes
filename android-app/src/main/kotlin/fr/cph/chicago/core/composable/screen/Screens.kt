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
import fr.cph.chicago.core.composable.bikeStations
import fr.cph.chicago.core.composable.busRoutes

sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector, val component: @Composable () -> Unit) {
    object Favorites : DrawerScreens("Favorites", "fav", Icons.Filled.Favorite, { Favorites() })
    object Train : DrawerScreens("Train", "train", Icons.Filled.Train, { Train() })
    object Bus : DrawerScreens("Bus", "bus", Icons.Filled.DirectionsBus, { Bus(busRoutes = busRoutes) })
    object Divvy : DrawerScreens("Divvy", "divvy", Icons.Filled.DirectionsBike, { Divvy(bikeStations = bikeStations) })
    object Nearby : DrawerScreens("Nearby", "nearby", Icons.Filled.NearMe, { Nearby() })
    object Map : DrawerScreens("CTA map", "map", Icons.Filled.Map, { Map() })
    object Alerts : DrawerScreens("CTA alerts", "alerts", Icons.Filled.Warning, { Alerts() })
    object Rate : DrawerScreens("Rate this app", "rate", Icons.Filled.StarRate, { Rate() })
    object Settings : DrawerScreens("Settings", "settings", Icons.Filled.Settings, { Settings() })
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
