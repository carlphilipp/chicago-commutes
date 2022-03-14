package fr.cph.chicago.core.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.screen.settings.DisplaySettingsScreen
import fr.cph.chicago.core.ui.screen.settings.SettingsScreen
import fr.cph.chicago.core.ui.screen.settings.ThemeChooserSettingsScreen
import fr.cph.chicago.core.viewmodel.locationViewModel
import fr.cph.chicago.core.viewmodel.mainViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel
import java.net.URLDecoder
import kotlin.random.Random

sealed class Screen(
    val id: Int = Random.nextInt(),
    val title: String,
    val route: String,
    val icon: ImageVector,
    val showOnDrawer: Boolean = true,
    val topBar: ScreenTopBar,
    val component: @Composable (NavBackStackEntry, NavigationViewModel) -> Unit
) {
    object Favorites : Screen(
        title = App.instance.getString(R.string.menu_favorites),
        route = "/fav",
        icon = Icons.Filled.Favorite,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            FavoritesScreen(
                title = "Favorites",
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel
            )
        }
    )

    object Train : Screen(
        title = App.instance.getString(R.string.menu_train),
        route = "/train",
        icon = Icons.Filled.Train,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            TrainScreen(title = "Train", navigationViewModel = navigationViewModel)
        }
    )

    object TrainList : Screen(
        title = "Train station list",
        route = "/train/line/{line}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val line = URLDecoder.decode(backStackEntry.arguments?.getString("line", "0") ?: "", "UTF-8")
            val viewModel: TrainListStationViewModel = viewModel(
                factory = TrainListStationViewModel.provideFactory(
                    line = line,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            TrainLineStopsScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = "$line Line"
            )
        }
    )

    object TrainDetails : Screen(
        title = "Train station details",
        route = "/train/details/{stationId}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.None,
        component = { backStackEntry, navigationViewModel ->
            val stationId = backStackEntry.arguments?.getString("stationId", "0") ?: ""
            val viewModel: TrainStationViewModel = viewModel(
                factory = TrainStationViewModel.provideFactory(
                    stationId = stationId,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            TrainStationScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel
            )
        }
    )

    object Bus : Screen(
        title = App.instance.getString(R.string.menu_bus),
        route = "/bus?search={search}",
        icon = Icons.Filled.DirectionsBus,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { backStackEntry, navigationViewModel ->
            val search = URLDecoder.decode(backStackEntry.arguments?.getString("search", "") ?: "", "UTF-8")
            BusScreen(
                title = "Bus",
                search = search,
                navigationViewModel = navigationViewModel,
                mainViewModel = mainViewModel,
            )
        }
    )

    object BusDetails : Screen(
        title = "Bus details",
        route = "/bus/details?busStopId={busStopId}&busStopName={busStopName}&busRouteId={busRouteId}&busRouteName={busRouteName}&bound={bound}&boundTitle={boundTitle}",
        icon = Icons.Filled.DirectionsBus,
        showOnDrawer = false,
        topBar = ScreenTopBar.None,
        component = { backStackEntry, navigationViewModel ->
            val busStopId = URLDecoder.decode(backStackEntry.arguments?.getString("busStopId", "0") ?: "", "UTF-8")
            val busStopName = URLDecoder.decode(backStackEntry.arguments?.getString("busStopName", "0") ?: "", "UTF-8")
            val busRouteId = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteId", "0") ?: "", "UTF-8")
            val busRouteName = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteName", "0") ?: "", "UTF-8")
            val bound = URLDecoder.decode(backStackEntry.arguments?.getString("bound", "0") ?: "", "UTF-8")
            val boundTitle = URLDecoder.decode(backStackEntry.arguments?.getString("boundTitle", "0") ?: "", "UTF-8")
            val viewModel: BusStationViewModel = viewModel(
                factory = BusStationViewModel.provideFactory(
                    busStopId = busStopId,
                    busStopName = busStopName,
                    busRouteId = busRouteId,
                    busRouteName = busRouteName,
                    bound = bound,
                    boundTitle = boundTitle,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            BusStationScreen(
                navigationViewModel = navigationViewModel,
                viewModel = viewModel,
            )
        }
    )

    object BusBound : Screen(
        title = "Bus bound",
        route = "/bus/bound?busRouteId={busRouteId}&busRouteName={busRouteName}&bound={bound}&boundTitle={boundTitle}&searchBusBound={searchBusBound}",
        icon = Icons.Filled.DirectionsBus,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val busRouteId = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteId", "0") ?: "", "UTF-8")
            val busRouteName = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteName", "0") ?: "", "UTF-8")
            val bound = URLDecoder.decode(backStackEntry.arguments?.getString("bound", "0") ?: "", "UTF-8")
            val boundTitle = URLDecoder.decode(backStackEntry.arguments?.getString("boundTitle", "0") ?: "", "UTF-8")
            val searchBusBound = URLDecoder.decode(backStackEntry.arguments?.getString("searchBusBound", "") ?: "", "UTF-8")
            val viewModel: BusBoundUiViewModel = viewModel(
                factory = BusBoundUiViewModel.provideFactory(
                    busRouteId = busRouteId,
                    busRouteName = busRouteName,
                    bound = bound,
                    boundTitle = boundTitle,
                    searchText = TextFieldValue(searchBusBound),
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            BusBoundScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = "$busRouteId - $boundTitle",
            )
        }
    )

    object Divvy : Screen(
        title = App.instance.getString(R.string.menu_divvy),
        route = "/divvy?search={search}",
        icon = Icons.Filled.DirectionsBike,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { backStackEntry, navigationViewModel ->
            val search = URLDecoder.decode(backStackEntry.arguments?.getString("search", "") ?: "", "UTF-8")
            DivvyScreen(
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel,
                title = "Divvy",
                search = search,
            )
        }
    )

    object DivvyDetails : Screen(
        title = "Divvy station details",
        route = "/divvy/details/{stationId}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.None,
        component = { backStackEntry, navigationViewModel ->
            val stationId = URLDecoder.decode(backStackEntry.arguments?.getString("stationId", "0") ?: "", "UTF-8")
            val viewModel: BikeStationViewModel = viewModel(
                factory = BikeStationViewModel.provideFactory(
                    stationId = stationId,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            BikeStationScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
            )
        }
    )

    object DeveloperOptions : Screen(
        title = "Developer options",
        route = "/developer",
        icon = Icons.Filled.DeveloperMode,
        showOnDrawer = false,
        topBar = ScreenTopBar.LargeTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val viewModel: DeveloperOptionsViewModel = viewModel(
                factory = DeveloperOptionsViewModel.provideFactory(
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            DeveloperOptionsScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = "Developer",
            )
        }
    )

    object Nearby : Screen(
        title = App.instance.getString(R.string.menu_nearby),
        route = "/nearby",
        icon = Icons.Filled.NearMe,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            NearbyScreen(
                mainViewModel = mainViewModel,
                locationViewModel = locationViewModel,
                navigationViewModel = navigationViewModel,
                title = "Nearby",
            )
        })

    object Map : Screen(
        title = App.instance.getString(R.string.menu_cta_map),
        route = "/map",
        icon = Icons.Filled.Map,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            Map(
                navigationViewModel = navigationViewModel,
                title = "CTA Map",
            )
        })

    object Alerts : Screen(
        title = App.instance.getString(R.string.menu_cta_alert),
        route = "/alerts",
        icon = Icons.Filled.Warning,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            AlertsScreen(
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel,
                title = "CTA Alerts",
            )
        })

    object AlertDetail : Screen(
        title = "Alert",
        route = "/alerts/{stationId}?title={title}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val routeId = URLDecoder.decode(backStackEntry.arguments?.getString("routeId", "") ?: "", "UTF-8")
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title", "") ?: "", "UTF-8")
            val viewModel: AlertDetailsViewModel = viewModel(
                factory = AlertDetailsViewModel.provideFactory(
                    routeId = routeId,
                    title = title,
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            AlertDetailsScreen(
                navigationViewModel = navigationViewModel,
                viewModel = viewModel,
                title = title,
            )
        }
    )

    object Rate : Screen(
        title = App.instance.getString(R.string.menu_rate),
        route = "/rate",
        icon = Icons.Filled.StarRate,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, _ -> RateScreen(mainViewModel = mainViewModel) })

    object Settings : Screen(
        title = App.instance.getString(R.string.menu_settings),
        route = "/settings",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarDrawer,
        component = { _, navigationViewModel ->
            SettingsScreen(
                title = "Settings",
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object SettingsDisplay : Screen(
        title = "Display",
        route = "/settings/display",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBack,
        showOnDrawer = false,
        component = { _, navigationViewModel ->
            DisplaySettingsScreen(
                title = "Display",
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object SettingsThemeColorChooser : Screen(
        title = "Theme color",
        route = "/settings/color",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBack,
        showOnDrawer = false,
        component = { _, navigationViewModel ->
            ThemeChooserSettingsScreen(
                topBarTitle = "Theme color",
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object Search : Screen(
        title = "Search",
        route = "/search?search={search}",
        icon = Icons.Filled.Search,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val search = URLDecoder.decode(backStackEntry.arguments?.getString("search", "") ?: "", "UTF-8")
            val viewModel: SearchViewModel = viewModel(
                factory = SearchViewModel.provideFactory(
                    searchText = TextFieldValue(search),
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )

            SearchViewScreen(
                title = "Search",
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
            )
        }
    )
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

    object MediumTopBarBack : ScreenTopBar(
        type = TopBarType.MEDIUM,
        icon = Icons.Filled.ArrowBack,
        action = TopBarIconAction.BACK,
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
    Screen.TrainList,
    Screen.TrainDetails,
    Screen.Bus,
    Screen.BusBound,
    Screen.BusDetails,
    Screen.DeveloperOptions,
    Screen.Divvy,
    Screen.DivvyDetails,
    Screen.Nearby,
    Screen.Map,
    Screen.Alerts,
    Screen.AlertDetail,
    Screen.Rate,
    Screen.Settings,
    Screen.SettingsDisplay,
    Screen.SettingsThemeColorChooser,
    Screen.Search,
)

val drawerScreens by lazy {
    screens.filter { screen -> screen.showOnDrawer }
}
