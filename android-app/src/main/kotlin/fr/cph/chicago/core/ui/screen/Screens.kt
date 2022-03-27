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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.screen.settings.DisplaySettingsScreen
import fr.cph.chicago.core.ui.screen.settings.SettingsScreen
import fr.cph.chicago.core.ui.screen.settings.ThemeChooserSettingsScreen
import fr.cph.chicago.core.viewmodel.locationViewModel
import fr.cph.chicago.core.viewmodel.mainViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.getActivity
import timber.log.Timber
import java.net.URLDecoder
import kotlin.random.Random

sealed class Screen(
    val id: Int = Random.nextInt(),
    val title: String = "",
    val route: String,
    val icon: ImageVector,
    val showOnDrawer: Boolean = true,
    val isGestureEnabled: Boolean = true,
    val topBar: ScreenTopBar,
    val component: @Composable (NavBackStackEntry, NavigationViewModel) -> Unit
) {
    object Favorites : Screen(
        title = App.instance.getString(R.string.menu_favorites),
        route = "fav",
        icon = Icons.Filled.Favorite,
        topBar = ScreenTopBar.MediumTopBarDrawerSearch,
        component = { _, navigationViewModel ->
            FavoritesScreen(
                title = stringResource(R.string.screen_favorites),
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel
            )
        }
    )

    object Train : Screen(
        title = App.instance.getString(R.string.menu_train),
        route = "train",
        icon = Icons.Filled.Train,
        topBar = ScreenTopBar.MediumTopBarDrawerSearch,
        component = { _, navigationViewModel ->
            TrainScreen(
                title = stringResource(R.string.screen_train),
                navigationViewModel = navigationViewModel
            )
        }
    )

    object TrainList : Screen(
        route = "train/line/{line}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val activity = navigationViewModel.uiState.context.getActivity()
            val line = URLDecoder.decode(backStackEntry.arguments?.getString("line", "0") ?: "", "UTF-8")
            val factory = TrainListStationViewModel.provideFactory(
                owner = activity,
                defaultArgs = backStackEntry.arguments
            )
            val viewModel = ViewModelProvider(activity, factory)[TrainListStationViewModel::class.java]
            viewModel.init(line = line)
            TrainLineStopsScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = "$line Line"
            )
        }
    )

    object TrainDetails : Screen(
        route = "train/details/{stationId}",
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
        route = "bus",
        icon = Icons.Filled.DirectionsBus,
        topBar = ScreenTopBar.MediumTopBarDrawerSearch,
        component = { _, navigationViewModel ->
            BusScreen(
                title = stringResource(R.string.screen_bus),
                navigationViewModel = navigationViewModel,
                mainViewModel = mainViewModel,
            )
        }
    )

    object BusDetails : Screen(
        route = "bus/details?busStopId={busStopId}&busStopName={busStopName}&busRouteId={busRouteId}&busRouteName={busRouteName}&bound={bound}&boundTitle={boundTitle}",
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
        route = "bus/bound?busRouteId={busRouteId}&busRouteName={busRouteName}&bound={bound}&boundTitle={boundTitle}&search={search}",
        icon = Icons.Filled.DirectionsBus,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val activity = navigationViewModel.uiState.context.getActivity()
            val busRouteId = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteId", "0") ?: "", "UTF-8")
            val busRouteName = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteName", "0") ?: "", "UTF-8")
            val bound = URLDecoder.decode(backStackEntry.arguments?.getString("bound", "0") ?: "", "UTF-8")
            val boundTitle = URLDecoder.decode(backStackEntry.arguments?.getString("boundTitle", "0") ?: "", "UTF-8")
            val search = URLDecoder.decode(backStackEntry.arguments?.getString("search", "") ?: "", "UTF-8")

            val factory = BusBoundUiViewModel.provideFactory(
                busRouteId = busRouteId,
                busRouteName = busRouteName,
                bound = bound,
                boundTitle = boundTitle,
                search = search,
                owner = activity,
                defaultArgs = backStackEntry.arguments
            )
            val viewModel = ViewModelProvider(activity, factory)[BusBoundUiViewModel::class.java]
            viewModel.initModel(
                busRouteId = busRouteId,
                busRouteName = busRouteName,
                bound = bound,
                boundTitle = boundTitle,
                search = search,
            )

            BusBoundScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = "$busRouteId - $boundTitle"
            )
        }
    )

    object Divvy : Screen(
        title = App.instance.getString(R.string.menu_divvy),
        route = "divvy",
        icon = Icons.Filled.DirectionsBike,
        topBar = ScreenTopBar.MediumTopBarDrawerSearch,
        component = { _, navigationViewModel ->
            DivvyScreen(
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel,
                title = stringResource(R.string.screen_divvy),
            )
        }
    )

    object DivvyDetails : Screen(
        route = "divvy/details/{stationId}",
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
        route = "developer",
        icon = Icons.Filled.DeveloperMode,
        showOnDrawer = false,
        topBar = ScreenTopBar.LargeTopBarBackSearch,
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
                title = stringResource(R.string.screen_settings_develop),
            )
        }
    )

    object Nearby : Screen(
        title = App.instance.getString(R.string.menu_nearby),
        route = "nearby",
        icon = Icons.Filled.NearMe,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        isGestureEnabled = false,
        component = { _, navigationViewModel ->
            settingsViewModel.loadShowMapDebug()
            NearbyScreen(
                mainViewModel = mainViewModel,
                locationViewModel = locationViewModel,
                navigationViewModel = navigationViewModel,
                settingsViewModel = settingsViewModel,
                title = stringResource(R.string.screen_nearby),
            )
        })

    object Map : Screen(
        title = App.instance.getString(R.string.menu_cta_map),
        route = "map",
        icon = Icons.Filled.Map,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        isGestureEnabled = false,
        component = { _, navigationViewModel ->
            Map(
                navigationViewModel = navigationViewModel,
                title = stringResource(R.string.screen_map),
            )
        })

    object Alerts : Screen(
        title = App.instance.getString(R.string.menu_cta_alert),
        route = "alerts",
        icon = Icons.Filled.Warning,
        topBar = ScreenTopBar.MediumTopBarDrawer,
        component = { _, navigationViewModel ->
            AlertsScreen(
                mainViewModel = mainViewModel,
                navigationViewModel = navigationViewModel,
                title = stringResource(R.string.screen_alerts),
            )
        })

    object AlertDetail : Screen(
        title = "Alert",
        route = "alerts/{stationId}?title={title}",
        icon = Icons.Filled.Train,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBackSearch,
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
        route = "rate",
        icon = Icons.Filled.StarRate,
        topBar = ScreenTopBar.MediumTopBarDrawerSearch,
        component = { _, _ -> RateScreen(mainViewModel = mainViewModel) })

    object Settings : Screen(
        title = App.instance.getString(R.string.menu_settings),
        route = "settings",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarDrawerSearch,
        component = { _, navigationViewModel ->
            SettingsScreen(
                title = stringResource(R.string.screen_settings),
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object SettingsDisplay : Screen(
        route = "settings/display",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBackSearch,
        showOnDrawer = false,
        component = { _, navigationViewModel ->
            DisplaySettingsScreen(
                title = stringResource(R.string.screen_settings_display),
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object SettingsThemeColorChooser : Screen(
        route = "settings/color",
        icon = Icons.Filled.Settings,
        topBar = ScreenTopBar.LargeTopBarBackSearch,
        showOnDrawer = false,
        component = { _, navigationViewModel ->
            ThemeChooserSettingsScreen(
                topBarTitle = stringResource(R.string.screen_settings_theme),
                viewModel = settingsViewModel,
                navigationViewModel = navigationViewModel,
            )
        })

    object Search : Screen(
        route = "search",
        icon = Icons.Filled.Search,
        showOnDrawer = false,
        topBar = ScreenTopBar.MediumTopBarBack,
        component = { backStackEntry, navigationViewModel ->
            val activity = navigationViewModel.uiState.context.getActivity()
            val factory = SearchViewModel.provideFactory(
                owner = activity,
                defaultArgs = backStackEntry.arguments
            )
            val viewModel = ViewModelProvider(activity, factory)[SearchViewModel::class.java]
            SearchViewScreen(
                title = stringResource(R.string.screen_search),
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
            )
        }
    )

    object TrainMap : Screen(
        route = "map/trains?line={line}",
        icon = Icons.Filled.Search,
        showOnDrawer = false,
        isGestureEnabled = false,
        topBar = ScreenTopBar.MediumTopBarBackReload,
        component = { backStackEntry, navigationViewModel ->
            val line = URLDecoder.decode(backStackEntry.arguments?.getString("line", "") ?: "", "UTF-8")
            val trainLine = TrainLine.fromXmlString(line)

            val viewModel = MapTrainViewModel(line = trainLine)
            TrainMapScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = trainLine.toStringWithLine(),
            )
        }
    )

    object BusMap : Screen(
        route = "map/buses?busRouteId={busRouteId}",
        icon = Icons.Filled.Search,
        showOnDrawer = false,
        isGestureEnabled = false,
        topBar = ScreenTopBar.MediumTopBarBackReload,
        component = { backStackEntry, navigationViewModel ->
            val busRouteId = URLDecoder.decode(backStackEntry.arguments?.getString("busRouteId", "") ?: "", "UTF-8")

            val viewModel = MapBusViewModel(busRouteId = busRouteId)
            BusMapScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                title = busRouteId,
            )
        }
    )
}

sealed class ScreenTopBar(
    val type: TopBarType,
    val leftIcon: ImageVector,
    val rightIcon: ImageVector? = null,
    val actionLeft: TopBarIconAction,
) {
    object MediumTopBarBack : ScreenTopBar(
        type = TopBarType.MEDIUM,
        leftIcon = Icons.Filled.ArrowBack,
        actionLeft = TopBarIconAction.BACK,
    )

    object MediumTopBarDrawerSearch : ScreenTopBar(
        type = TopBarType.MEDIUM,
        leftIcon = Icons.Filled.Menu,
        rightIcon = Icons.Filled.Search,
        actionLeft = TopBarIconAction.OPEN_DRAWER,
    )

    object MediumTopBarDrawer : ScreenTopBar(
        type = TopBarType.MEDIUM,
        leftIcon = Icons.Filled.Menu,
        actionLeft = TopBarIconAction.OPEN_DRAWER,
    )

    object MediumTopBarBackSearch : ScreenTopBar(
        type = TopBarType.MEDIUM,
        leftIcon = Icons.Filled.ArrowBack,
        rightIcon = Icons.Filled.Search,
        actionLeft = TopBarIconAction.BACK,
    )

    object MediumTopBarBackReload : ScreenTopBar(
        type = TopBarType.MEDIUM,
        leftIcon = Icons.Filled.ArrowBack,
        rightIcon = Icons.Filled.Refresh,
        actionLeft = TopBarIconAction.BACK,
    )

    object LargeTopBarDrawerSearch : ScreenTopBar(
        type = TopBarType.LARGE,
        leftIcon = Icons.Filled.Menu,
        rightIcon = Icons.Filled.Search,
        actionLeft = TopBarIconAction.OPEN_DRAWER,
    )

    object LargeTopBarBackSearch : ScreenTopBar(
        type = TopBarType.LARGE,
        leftIcon = Icons.Filled.ArrowBack,
        rightIcon = Icons.Filled.Search,
        actionLeft = TopBarIconAction.BACK,
    )

    object None : ScreenTopBar(
        type = TopBarType.NONE,
        leftIcon = Icons.Filled.ArrowBack,
        rightIcon = Icons.Filled.Search,
        actionLeft = TopBarIconAction.BACK,
    )
}

enum class TopBarIconAction {
    BACK, OPEN_DRAWER, NONE,
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
    Screen.TrainMap,
    Screen.BusMap,
)

val drawerScreens by lazy {
    screens.filter { screen -> screen.showOnDrawer }
}
