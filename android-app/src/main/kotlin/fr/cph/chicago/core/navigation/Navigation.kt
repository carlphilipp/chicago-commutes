package fr.cph.chicago.core.navigation

import android.content.Intent
import androidx.compose.animation.rememberSplineBasedDecay
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.ui.Drawer
import fr.cph.chicago.core.ui.LargeTopBar
import fr.cph.chicago.core.ui.MediumTopBar
import fr.cph.chicago.core.ui.common.BackHandler
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.ui.screen.TopBarIconAction
import fr.cph.chicago.core.ui.screen.TopBarType
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Stack

val LocalNavController = compositionLocalOf<NavHostTopBarController> {
    error("No NavHostTopBarController provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(viewModel: NavigationViewModel, settingsViewModel: SettingsViewModel) {
    val uiState = viewModel.uiState
    val navController = remember { NavHostTopBarController(viewModel) }
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
            // FIXME: this whole thing should probably be refactored
/*            val decayAnimationSpec = rememberSplineBasedDecay<Float>()

            if (uiState.currentScreen == Screen.Settings || uiState.currentScreen == Screen.SettingsDisplay) {
                settingsViewModel.setScrollBehavior(
                    scrollBehavior = remember(decayAnimationSpec) { TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec) }
                )
            } else {
                settingsViewModel.setScrollBehavior(
                    scrollBehavior = remember { TopAppBarDefaults.enterAlwaysScrollBehavior() }
                )
            }*/

            // Add custom nav controller in local provider so it can be retrieve easily downstream
            CompositionLocalProvider(LocalNavController provides navController) {
                Scaffold(
                    modifier = Modifier.nestedScroll(settingsViewModel.uiState.scrollBehavior.nestedScrollConnection),
                    topBar = {
                        DisplayTopBar(
                            settingsViewModel = settingsViewModel,
                            viewModel = viewModel,
                        )
                    }
                ) {
                    NavHost(
                        navController = navController.navController(),
                        startDestination = Screen.Favorites.route
                    ) {
                        uiState.screens.forEach { screen ->
                            composable(screen.route) {
                                // Add custom backhandler to all composable so we can handle when someone push the back button
                                BackHandler {
                                    uiState.currentScreen = screen
                                    screen.component()
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
)

@OptIn(ExperimentalMaterial3Api::class)
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

    fun updateScreen(screen: Screen) {
        uiState = uiState.copy(
            currentScreen = screen
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

class NavHostTopBarController(private val viewModel: NavigationViewModel) {

    private val previous: Stack<Screen> = Stack()

    fun navController(): NavHostController {
        return viewModel.uiState.navController
    }

    fun navigate(screen: Screen) {
        Timber.d("Navigate to ${screen.title}");
        viewModel.uiState.navController.navigate(screen.route) {
            popUpTo(viewModel.uiState.navController.graph.startDestinationId)
            launchSingleTop = true
        }
        viewModel.updateScreen(screen = screen)
        previous.push(screen)
        printStackState()
    }

    fun navigateBack() {
        Timber.d("Navigate back");
        previous.pop()
        if (previous.isEmpty()) {
            navigate(screen = Screen.Favorites)
        } else {
            val screen = previous.pop()
            navigate(screen = screen)
        }
        printStackState()
    }

    fun printStackState() {
        Timber.d("Stack size: ${previous.size}");
        var str = "[ ";
        for (i in previous.size - 1 downTo 0) {
            str = str + previous[i].title + " <- "
        }
        str = str + " ]"
        Timber.d("Stack: $str")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayTopBar(
    settingsViewModel: SettingsViewModel,
    viewModel: NavigationViewModel,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val screen = viewModel.uiState.currentScreen
    val scope = rememberCoroutineScope()
    val openDrawer = { scope.launch { viewModel.uiState.drawerState.open() } }
    val onClick: () -> Unit = {
        if (screen.topBar.action == TopBarIconAction.BACK) {
            navController.navigateBack()
        } else {
            openDrawer()
        }
    }

    if (screen.topBar.type == TopBarType.LARGE) {
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()
        settingsViewModel.setScrollBehavior(scrollBehavior = remember(decayAnimationSpec) {
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
        })
        LargeTopBar(
            title = screen.title,
            scrollBehavior = settingsViewModel.uiState.scrollBehavior,
            navigationIcon = {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = screen.topBar.icon,
                        contentDescription = null,
                    )
                }
            },
        )
    } else {
        settingsViewModel.setScrollBehavior(scrollBehavior = remember {
            TopAppBarDefaults.pinnedScrollBehavior()
        })
        MediumTopBar(
            title = screen.title,
            scrollBehavior = settingsViewModel.uiState.scrollBehavior,
            navigationIcon = {
                IconButton(onClick = { openDrawer() }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = null
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(context, intent, null)
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
