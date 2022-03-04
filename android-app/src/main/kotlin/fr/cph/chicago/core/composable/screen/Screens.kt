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
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.viewmodel.locationViewModel
import fr.cph.chicago.core.composable.viewmodel.mainViewModel
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel

sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector, val component: @Composable () -> Unit) {
    object Favorites : DrawerScreens(
        title = App.instance.getString(R.string.menu_favorites),
        route = "fav",
        icon = Icons.Filled.Favorite,
        component = { Favorites(mainViewModel = mainViewModel) }
    )

    object Train : DrawerScreens(
        title = App.instance.getString(R.string.menu_train),
        route = "train",
        icon = Icons.Filled.Train,
        component = { Train() }
    )

    object Bus : DrawerScreens(
        title = App.instance.getString(R.string.menu_bus),
        route = "bus",
        icon = Icons.Filled.DirectionsBus,
        { Bus(mainViewModel = mainViewModel) }
    )

    object Divvy : DrawerScreens(
        title = App.instance.getString(R.string.menu_divvy),
        route = "divvy",
        icon = Icons.Filled.DirectionsBike,
        component = { Divvy(mainViewModel = mainViewModel) }
    )

    object Nearby : DrawerScreens(
        title = App.instance.getString(R.string.menu_nearby),
        route = "nearby",
        icon = Icons.Filled.NearMe,
        component = { Nearby(mainViewModel = mainViewModel, locationViewModel = locationViewModel) })

    object Map : DrawerScreens(
        title = App.instance.getString(R.string.menu_cta_map),
        route = "map",
        icon = Icons.Filled.Map,
        component = { Map() })

    object Alerts : DrawerScreens(
        title = App.instance.getString(R.string.menu_cta_alert),
        route = "alerts",
        icon = Icons.Filled.Warning,
        component = { Alerts(mainViewModel = mainViewModel) })

    object Rate : DrawerScreens(
        title = App.instance.getString(R.string.menu_rate),
        route = "rate",
        icon = Icons.Filled.StarRate,
        component = { Rate(mainViewModel = mainViewModel) })

    object Settings : DrawerScreens(
        title = App.instance.getString(R.string.menu_settings),
        route = "settings",
        icon = Icons.Filled.Settings,
        component = { Settings(viewModel = settingsViewModel) })
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
