package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory.provideFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.ui.screen.settings.DisplaySettingsScreen
import fr.cph.chicago.core.ui.screen.settings.SettingsScreen
import fr.cph.chicago.core.ui.screen.settings.ThemeChooserSettingsScreen
import fr.cph.chicago.core.viewmodel.locationViewModel
import fr.cph.chicago.core.viewmodel.mainViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel
import timber.log.Timber

sealed class Screen(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val showOnDrawer: Boolean = true,
    val topBar: ScreenTopBar,
    val component: @Composable (NavBackStackEntry) -> Unit
) {
    object Favorites : Screen(
        title = App.instance.getString(R.string.menu_favorites),
        route = "fav",
        icon = Icons.Filled.Favorite,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { FavoritesScreen(mainViewModel = mainViewModel) }
    )

    object Train : Screen(
        title = App.instance.getString(R.string.menu_train),
        route = "train",
        icon = Icons.Filled.Train,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = {
            Timber.i("Displaying train screen")
            TrainScreen()
        }
    )

    object TrainDetails : Screen(
        title = "Train station details",
        route = "train/{stationId}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.None,
        component = { backStackEntry ->
            val stationId = backStackEntry.arguments?.getString("stationId", "0") ?: ""
            val viewModel: TrainStationViewModel = viewModel(
                factory = TrainStationViewModel.provideFactory(
                    stationId = stationId,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            TrainStationScreen(viewModel = viewModel)
        }
    )

    object Bus : Screen(
        title = App.instance.getString(R.string.menu_bus),
        route = "bus",
        icon = Icons.Filled.DirectionsBus,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { BusScreen(mainViewModel = mainViewModel) }
    )

    object Divvy : Screen(
        title = App.instance.getString(R.string.menu_divvy),
        route = "divvy",
        icon = Icons.Filled.DirectionsBike,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { DivvyScreen(mainViewModel = mainViewModel) }
    )

    object Nearby : Screen(
        title = App.instance.getString(R.string.menu_nearby),
        route = "nearby",
        icon = Icons.Filled.NearMe,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { NearbyScreen(mainViewModel = mainViewModel, locationViewModel = locationViewModel) })

    object Map : Screen(
        title = App.instance.getString(R.string.menu_cta_map),
        route = "map",
        icon = Icons.Filled.Map,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { Map() })

    object Alerts : Screen(
        title = App.instance.getString(R.string.menu_cta_alert),
        route = "alerts",
        icon = Icons.Filled.Warning,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { AlertsScreen(mainViewModel = mainViewModel) })

    object Rate : Screen(
        title = App.instance.getString(R.string.menu_rate),
        route = "rate",
        icon = Icons.Filled.StarRate,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { RateScreen(mainViewModel = mainViewModel) })

    object Settings : Screen(
        title = App.instance.getString(R.string.menu_settings),
        route = "settings",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarDrawer,
        component = { SettingsScreen(viewModel = settingsViewModel) })

    object SettingsDisplay : Screen(
        title = "Display",
        route = "settings/display",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBack,
        showOnDrawer = false,
        component = { DisplaySettingsScreen(viewModel = settingsViewModel) })

    object SettingsThemeColorChooser : Screen(
        title = "Theme color",
        route = "settings/color",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBack,
        showOnDrawer = false,
        component = { ThemeChooserSettingsScreen(viewModel = settingsViewModel) })
}

sealed class ScreenTopBar(
    val type: TopBarType,
    val icon: ImageVector,
    val action: TopBarIconAction,
) {
    object MediumTopBarDrawer : ScreenTopBar(
        type = TopBarType.MEDIUM,
        icon = Icons.Filled.Menu,
        action = TopBarIconAction.OPEN_DRAWER,
    )

    object LargeTopBarDrawer : ScreenTopBar(
        type = TopBarType.LARGE,
        icon = Icons.Filled.Menu,
        action = TopBarIconAction.OPEN_DRAWER,
    )

    object LargeTopBarBack : ScreenTopBar(
        type = TopBarType.LARGE,
        icon = Icons.Filled.ArrowBack,
        action = TopBarIconAction.BACK,
    )

    object None : ScreenTopBar(
        type = TopBarType.NONE,
        icon = Icons.Filled.ArrowBack,
        action = TopBarIconAction.BACK,
    )
}

enum class TopBarIconAction {
    BACK, OPEN_DRAWER,
}

enum class TopBarType {
    LARGE, MEDIUM, NONE,
}

val screens = listOf(
    Screen.Favorites,
    Screen.Train,
    Screen.TrainDetails,
    Screen.Bus,
    Screen.Divvy,
    Screen.Nearby,
    Screen.Map,
    Screen.Alerts,
    Screen.Rate,
    Screen.Settings,
    Screen.SettingsDisplay,
    Screen.SettingsThemeColorChooser,
)
