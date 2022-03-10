package fr.cph.chicago.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.cph.chicago.core.App
import fr.cph.chicago.core.ui.Drawer
import fr.cph.chicago.core.ui.LargeTopBar
import fr.cph.chicago.core.ui.MediumTopBar
import fr.cph.chicago.core.ui.common.BackHandler
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.ui.screen.ScreenTopBar
import fr.cph.chicago.core.ui.screen.TopBarIconAction
import fr.cph.chicago.core.ui.screen.TopBarType
import java.util.Stack
import kotlinx.coroutines.launch
import timber.log.Timber

val LocalNavController = compositionLocalOf<NavHostControllerWrapper> {
    error("No NavHostControllerWrapper provided")
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun Navigation(viewModel: NavigationViewModel) {
    Timber.d("Compose Navigation")
    val uiState = viewModel.uiState
    val navController = remember { NavHostControllerWrapper(viewModel) }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = uiState.drawerState,
        gesturesEnabled = viewModel.isGestureEnabled(),
        drawerContent = {
            Drawer(
                currentScreen = uiState.currentScreen,
                onDestinationClicked = { screen ->
                    if (screen != Screen.Rate) {
                        scope.launch {
                            uiState.drawerState.close()
                        }
                    }
                    navController.navigate(screen)
                }
            )
        },
        content = {
            Timber.d("Compose Navigation content")
            // Add custom nav controller in local provider so it can be retrieve easily downstream
            CompositionLocalProvider(LocalNavController provides navController) {
                val scrollBehavior = topBarBehavior(viewModel.uiState.currentScreen)
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        DisplayTopBar(
                            viewModel = viewModel,
                            scrollBehavior = scrollBehavior,
                        )
                    }
                ) {
                    NavHost(
                        navController = navController.navController(),
                        startDestination = Screen.Favorites.route
                    ) {
                        uiState.screens.forEach { screen ->
                            Timber.d("Compose for each screen -> ${screen.title}")
                            composable(
                                route = screen.route,
                                arguments = emptyList(),
//                                enterTransition = {
//                                    //when (targetState.destination.route) {
//                                        //Screen.Favorites.route -> fadeIn(animationSpec = tween(200))
//                                        //Screen.SettingsDisplay.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(3000))
//                                        //else -> fadeIn(animationSpec = tween(1)/*, initialOffsetY = { it / 5 }*/,
//                                        //)
//                                    //}
//                                    EnterTransition.None
//                                },
//                                exitTransition = {
//                                    //when (targetState.destination.route) {
//                                        // TODO
//                                        //Screen.SettingsDisplay.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(3000))
//                                        //else -> fadeOut(animationSpec = tween(1))
//                                    //}
//                                    ExitTransition.None
//                                },
//                                popEnterTransition = {EnterTransition.None},
//                                popExitTransition = { ExitTransition.None},
                            ) { backStackEntry ->
                                // Add custom backhandler to all composable so we can handle when someone push the back button
                                BackHandler {
                                    Timber.d("Compose render -> ${screen.title}")
                                    uiState.currentScreen = screen
                                    screen.component(backStackEntry)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
data class NavigationUiState constructor(
    val gesturesEnabled: Boolean = true,
    val screens: List<Screen> = fr.cph.chicago.core.ui.screen.screens,
    var currentScreen: Screen = Screen.Favorites,
    var topBarTitle: String = Screen.Favorites.title,
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed),
    val navController: NavHostController = NavHostController(App.instance.applicationContext),
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun rememberNavigationState(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    navController: NavHostController = rememberNavController(),
    currentScreen: Screen = Screen.Favorites,
) = remember(drawerState, navController, currentScreen) {
    NavigationUiState(
        drawerState = drawerState,
        navController = navController,
        currentScreen = currentScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class NavigationViewModel : ViewModel() {
    var uiState by mutableStateOf(NavigationUiState())
        private set

    fun initModel(state: NavigationUiState): NavigationViewModel {
        uiState = state
        return this
    }

    fun updateScreen(screen: Screen, customTitle: String) {
        uiState = uiState.copy(
            currentScreen = screen,
            topBarTitle = customTitle,
        )
    }

    fun isGestureEnabled(): Boolean {
        return if (uiState.drawerState.isOpen) {
            true
        } else {
            uiState.currentScreen != Screen.Map && uiState.currentScreen != Screen.Nearby
        }
    }
}

class NavHostControllerWrapper(private val viewModel: NavigationViewModel) {

    private val previous: Stack<Triple<Screen, String, Map<String, String>>> = Stack()

    fun navController(): NavHostController {
        return viewModel.uiState.navController
    }

    fun navigate(screen: Screen, arguments: Map<String, String> = mapOf(), customTitle: String = "") {
        Timber.d("Navigate to ${screen.title}");
        val route = buildRoute(route = screen.route, arguments = arguments)
        viewModel.uiState.navController.navigate(route) {
            //popUpTo(viewModel.uiState.navController.graph.startDestinationId)
            launchSingleTop = true
        }
        val title = if (customTitle != "") customTitle else screen.title
        viewModel.updateScreen(screen = screen, customTitle = title)
        previous.push(Triple(screen, title, arguments))
        printStackState()
    }

    fun navigateBack() {
        Timber.d("Navigate back");
        previous.pop()
        if (previous.isEmpty()) {
            navigate(screen = Screen.Favorites)
        } else {
            val screen = previous.pop()
            navigate(
                screen = screen.first,
                arguments = screen.third,
                customTitle = screen.second
            )
        }
        printStackState()
    }

    fun printStackState() {
        Timber.d("Navigate Stack size: ${previous.size}");
        var str = "[ ";
        for (i in previous.size - 1 downTo 0) {
            str = str + previous[i].first.title + " <- "
        }
        str = str + " ]"
        Timber.d("Navigate Stack: $str")
    }

    private fun buildRoute(route: String, arguments: Map<String, String>): String {
        var routeWithArguments = route
        arguments.forEach {
            routeWithArguments = routeWithArguments.replace("{" + it.key + "}", it.value)
        }
        return routeWithArguments
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topBarBehavior(screen: Screen): TopAppBarScrollBehavior {
    return if (screen.topBar.type == TopBarType.LARGE) {
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayTopBar(
    viewModel: NavigationViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val screen = viewModel.uiState.currentScreen
    val navController = LocalNavController.current
    if (screen.topBar != ScreenTopBar.None) {
        val scope = rememberCoroutineScope()
        val openDrawer = { scope.launch { viewModel.uiState.drawerState.open() } }
        val onClick: () -> Unit = {
            if (screen.topBar.action == TopBarIconAction.BACK) {
                navController.navigateBack()
            } else {
                openDrawer()
            }
        }
        val topBar = screen.topBar

        if (topBar.type == TopBarType.LARGE) {
            LargeTopBar(
                title = viewModel.uiState.topBarTitle,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = topBar.icon,
                            contentDescription = null,
                        )
                    }
                },
            )
        } else {
            MediumTopBar(
                title = viewModel.uiState.topBarTitle,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = topBar.icon,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Search)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    }
}
