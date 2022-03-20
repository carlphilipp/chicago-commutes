package fr.cph.chicago.core.navigation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import fr.cph.chicago.core.ui.common.ShowSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.enterTransition
import fr.cph.chicago.core.ui.common.exitTransition
import fr.cph.chicago.core.ui.common.fallBackEnterTransition
import fr.cph.chicago.core.ui.common.fallBackExitTransition
import fr.cph.chicago.core.ui.common.runWithDelay
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.ui.screen.ScreenTopBar
import fr.cph.chicago.core.ui.screen.TopBarIconAction
import fr.cph.chicago.core.ui.screen.TopBarType
import fr.cph.chicago.core.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URLEncoder
import java.util.Stack
import java.util.concurrent.TimeUnit

val LocalNavController = compositionLocalOf<NavHostControllerWrapper> {
    error("No NavHostControllerWrapper provided")
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun Navigation(
    show: Boolean,
    mainViewModel: MainViewModel,
    viewModel: NavigationViewModel,
) {
    if (show) {
        Timber.d("Compose Navigation")
        val uiState = viewModel.uiState
        val navController = remember { NavHostControllerWrapper(viewModel) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                mainViewModel.loadBusRoutesAndBike()
            }
        })

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
                        snackbarHost = { SnackbarHostInsets(state = mainViewModel.uiState.snackbarHostState) },
                        modifier = Modifier.nestedScroll(viewModel.uiState.scrollBehavior.nestedScrollConnection),
                    ) {
                        if (viewModel.uiState.shouldExit) {
                            ShowSnackBar(
                                scope = scope,
                                snackbarHostState = mainViewModel.uiState.snackbarHostState,
                                element = viewModel.uiState.shouldExit,
                                message = "Press BACK again to exit",
                                onComplete = {},
                                withDismissAction = false,
                            )
                        }
                        AnimatedNavHost(
                            navController = navController.navController(),
                            startDestination = Screen.Favorites.route,
                            enterTransition = fallBackEnterTransition(),
                            exitTransition = fallBackExitTransition(),
                        ) {
                            uiState.screens.forEach { screen: Screen ->
                                Timber.v("Compose for each screen -> ${screen.title}")
                                composable(
                                    route = screen.route,
                                    arguments = emptyList(),
                                    enterTransition = enterTransition(),
                                    exitTransition = exitTransition(),
                                ) { backStackEntry ->
                                    // Add custom backhandler to all composable so we can handle when someone push the back button
                                    BackHandler {
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
}

@OptIn(ExperimentalMaterial3Api::class)
data class NavigationUiState constructor(
    val gesturesEnabled: Boolean = true,
    val screens: List<Screen> = fr.cph.chicago.core.ui.screen.screens,
    var currentScreen: Screen = Screen.Favorites,
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed),
    val navController: NavHostController = NavHostController(App.instance.applicationContext),

    // Exiting the app
    val context: Context = App.instance.applicationContext,
    val shouldExit: Boolean = false,

    // Scroll behavior
    val decayAnimationSpec: DecayAnimationSpec<Float>? = null,
    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    val scrollMediumBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    val scrollLargeBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun rememberNavigationState(
    context: Context = LocalContext.current,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    navController: NavHostController = rememberAnimatedNavController(),
    currentScreen: Screen = remember { Screen.Favorites },
    scrollBehavior: TopAppBarScrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() },
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    scrollLargeBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
) = remember(drawerState, navController, currentScreen, scrollBehavior, decayAnimationSpec, scrollLargeBehavior) {
    NavigationUiState(
        context = context,
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
            uiState.currentScreen.isGestureEnabled
        }
    }

    fun exit() {
        (uiState.context as ComponentActivity).finish()
    }

    fun shouldBackSpaceAgainToExit() {
        uiState = uiState.copy(shouldExit = true)
        runWithDelay(2L, TimeUnit.SECONDS) {
            uiState = uiState.copy(shouldExit = false)
        }
    }
}

/**
 * Handle manually navigation as the default behavior did not seem to work the way I was expecting it to work
 */
class NavHostControllerWrapper(private val viewModel: NavigationViewModel) {

    private val previous: Stack<Pair<Screen, Map<String, String>>> = Stack()

    init {
        previous.add(Pair(Screen.Favorites, mapOf()))
    }

    fun navController(): NavHostController {
        return viewModel.uiState.navController
    }

    fun navigate(screen: Screen, arguments: Map<String, String> = mapOf()) {
        Timber.d("Navigate to ${screen.title} with args $arguments")
        when {
            previous.isEmpty() -> navigateTo(screen = screen, arguments = arguments)
            previous.isNotEmpty() -> {
                val previousScreen = previous.peek().first
                when {
                    previousScreen == Screen.Favorites && screen == Screen.Favorites -> viewModel.shouldBackSpaceAgainToExit()
                    previousScreen != screen -> navigateTo(screen = screen, arguments = arguments)
                    else -> Timber.e("Current screen is the same, do not do anything")
                }
            }
        }
        printStackState()
    }

    fun navigateBack() {
        Timber.d("Navigate back")
        when {
            previous.isEmpty() -> Timber.d("Empty, no where to go, this should not happen")
            previous.isNotEmpty() -> {
                val currentScreenData = previous.pop()
                when (currentScreenData.first) {
                    Screen.Favorites -> {
                        previous.push(currentScreenData)
                        if (viewModel.uiState.shouldExit) {
                            viewModel.exit()
                        } else {
                            viewModel.shouldBackSpaceAgainToExit()
                        }
                    }
                    else -> {
                        when {
                            previous.isEmpty() -> Timber.d("Empty, no where to go, this should not happen")
                            previous.isNotEmpty() -> {
                                val previousData = previous.pop()
                                val newArgs = mutableMapOf<String, String>()
                                newArgs.putAll(previousData.second)
                                if (currentScreenData.second.containsKey("search")) {
                                    newArgs["search"] = currentScreenData.second["search"]!!
                                }
                                navigate(screen = previousData.first, arguments = newArgs)
                            }
                        }
                    }
                }
            }
        }
        printStackState()
    }

    private fun navigateTo(screen: Screen, arguments: Map<String, String>) {
        val route = buildRoute(route = screen.route, arguments = arguments)
        viewModel.uiState.navController.navigate(route) {
            // TODO: this should be used when base activity is no more
            //popUpTo(viewModel.uiState.navController.graph.startDestinationId)
            launchSingleTop = true
        }
        viewModel.updateScreen(screen = screen)
        previous.push(Pair(screen, arguments))
    }

    private fun printStackState() {
        Timber.d("Navigate Stack size: ${previous.size}");
        var str = "[ "
        for (i in previous.size - 1 downTo 0) {
            str = str + previous[i].first.title + " <- "
        }
        str = "$str ]"
        Timber.d("Navigate Stack: $str")
    }

    private fun buildRoute(route: String, arguments: Map<String, String>): String {
        var routeWithArguments = route
        arguments.forEach {
            routeWithArguments = routeWithArguments.replace("{" + it.key + "}", URLEncoder.encode(it.value, "UTF-8"))
        }
        return routeWithArguments
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTopBar(
    title: String = "",
    viewModel: NavigationViewModel,
    onClickRightIcon: (() -> Unit)? = null,
) {
    if (viewModel.uiState.currentScreen.topBar != ScreenTopBar.None) {
        val screen = viewModel.uiState.currentScreen
        val navController = LocalNavController.current
        val scope = rememberCoroutineScope()
        val openDrawer = { scope.launch { viewModel.uiState.drawerState.open() } }
        val onClickLeftIcon: () -> Unit = {
            if (screen.topBar.actionLeft == TopBarIconAction.BACK) {
                navController.navigateBack()
            } else {
                openDrawer()
            }
        }
        val rightClick = if (onClickRightIcon == null) {
            { navController.navigate(Screen.Search) }
        } else {
            onClickRightIcon
        }
        val topBar = screen.topBar

        if (topBar.type == TopBarType.LARGE) {
            LargeTopBar(
                title = title,
                scrollBehavior = viewModel.uiState.scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onClickLeftIcon) {
                        Icon(
                            imageVector = topBar.leftIcon,
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
                    IconButton(onClick = onClickLeftIcon) {
                        Icon(
                            imageVector = topBar.leftIcon,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = rightClick) {  //navController.navigate(Screen.Search)
                        Icon(
                            imageVector = topBar.rightIcon,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    }
}
