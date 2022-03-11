package fr.cph.chicago.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.material.icons.Icons
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
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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
                            navController.navigate(screen)
                        }
                    }
                }
            )
        },
        content = {
            Timber.d("Compose Navigation content")
            // Add custom nav controller in local provider so it can be retrieve easily downstream
            CompositionLocalProvider(LocalNavController provides navController) {
                Scaffold(
                    modifier = Modifier.nestedScroll(viewModel.uiState.scrollBehavior.nestedScrollConnection),
                ) {
                    AnimatedNavHost(
                        navController = navController.navController(),
                        startDestination = Screen.Favorites.route
                    ) {
                        uiState.screens.forEach { screen: Screen ->
                            Timber.d("Compose for each screen -> ${screen.title}")
                            composable(
                                route = screen.route,
                                arguments = emptyList(),
                                enterTransition = {

                                    when (targetState.destination.route) {
                                        Screen.TrainDetails.route -> {
                                            Timber.i("Animation enterTransition targetState.destination ${initialState.destination.route} SLIDE IN LEFT")
                                            slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(500))
                                        }
                                        Screen.TrainList.route -> {
                                            when(initialState.destination.route) {
                                                Screen.TrainDetails.route -> {
                                                    null
                                                }
                                                else -> {
                                                    slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(500))
                                                }
                                            }
                                        }
                                        else -> {
                                            Timber.i("Animation enterTransition targetState.destination ${initialState.destination.route} NO ANIMATION")
                                            null//EnterTransition.None
                                        }
                                    }
                                    //EnterTransition.None
                                },
                                exitTransition = {
                                    when (initialState.destination.route) {
                                        Screen.TrainDetails.route -> {
                                            Timber.i("Animation exitTransition initialState.destination ${initialState.destination.route} SLIDE IN RIGHT")
                                            slideOutOfContainer(
                                                AnimatedContentScope.SlideDirection.Right, animationSpec = tween(500)

                                            )
                                        }
                                        else -> {
                                            Timber.i("Animation exitTransition initialState.destination ${initialState.destination.route} NO ANIMATION")
                                            ExitTransition.None
                                        }

                                    }
                                    //ExitTransition.None
                                },
                                //popEnterTransition = { EnterTransition.None },
                                //popExitTransition = { ExitTransition.None },
                            ) { backStackEntry ->
                                // Add custom backhandler to all composable so we can handle when someone push the back button
                                BackHandler {
                                    Timber.d("Compose render -> ${screen.title}")
                                    uiState.currentScreen = screen // FIXME: I don't think it's needed?
                                    screen.component(backStackEntry, viewModel)
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
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed),
    val navController: NavHostController = NavHostController(App.instance.applicationContext),

    val decayAnimationSpec: DecayAnimationSpec<Float>? = null,

    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    val scrollMediumBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    val scrollLargeBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun rememberNavigationState(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    navController: NavHostController = rememberAnimatedNavController(),
    currentScreen: Screen = remember { Screen.Favorites },
    scrollBehavior: TopAppBarScrollBehavior = remember {
        TopAppBarDefaults.pinnedScrollBehavior()
    },
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    scrollLargeBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
) = remember(drawerState, navController, currentScreen, scrollBehavior, decayAnimationSpec, scrollLargeBehavior) {
    NavigationUiState(
        drawerState = drawerState,
        navController = navController,
        currentScreen = currentScreen,
        scrollBehavior = scrollBehavior,
        decayAnimationSpec = decayAnimationSpec,
        scrollLargeBehavior = scrollLargeBehavior
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

    fun updateScreen(screen: Screen) {
        val scrollBehavior = if (screen.topBar.type == TopBarType.LARGE) {
            uiState.scrollLargeBehavior
        } else {
            uiState.scrollMediumBehavior
        }
        uiState = uiState.copy(
            currentScreen = screen,
            scrollBehavior = scrollBehavior,
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

class NavHostControllerWrapper(
    private
    val viewModel: NavigationViewModel
) {

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
        // FIXME, to delete?
        val title = if (customTitle != "") customTitle else screen.title
        viewModel.updateScreen(screen = screen)
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
fun DisplayTopBar(
    title: String = "",
    viewModel: NavigationViewModel,
) {
    if (viewModel.uiState.currentScreen.topBar != ScreenTopBar.None) {
        val screen = viewModel.uiState.currentScreen
        val navController = LocalNavController.current
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
                title = title,
                scrollBehavior = viewModel.uiState.scrollBehavior,
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
                title = title,
                scrollBehavior = viewModel.uiState.scrollBehavior,
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
