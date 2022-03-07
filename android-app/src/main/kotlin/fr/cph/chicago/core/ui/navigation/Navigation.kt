package fr.cph.chicago.core.ui.navigation

import android.content.Intent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.ui.Drawer
import fr.cph.chicago.core.ui.TopBar
import fr.cph.chicago.core.ui.screen.DrawerScreens
import fr.cph.chicago.core.viewmodel.settingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(screens: List<DrawerScreens>) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer = { scope.launch { drawerState.open() } }
    val navController = rememberNavController()
    val currentScreen = remember { mutableStateOf<DrawerScreens>(DrawerScreens.Favorites) }
    var gesturesEnabled by remember { mutableStateOf(true) }
    gesturesEnabled = if (drawerState.isOpen) {
        true
    } else {
        currentScreen.value != DrawerScreens.Map && currentScreen.value != DrawerScreens.Nearby
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            Drawer(
                currentScreen = currentScreen.value,
                onDestinationClicked = { screen ->
                    if (screen != DrawerScreens.Rate) {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    currentScreen.value = screen
                }
            )
        },
        content = {
            // FIXME: this whole thing should be refactored
            val decayAnimationSpec = rememberSplineBasedDecay<Float>()

            val settingsViewModel = settingsViewModel
            if (currentScreen.value == DrawerScreens.Settings) {
                settingsViewModel.setScrollBehavior(
                    scrollBehavior = remember(decayAnimationSpec) { TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec) }
                )
            } else {
                settingsViewModel.setScrollBehavior(
                    scrollBehavior = remember { TopAppBarDefaults.enterAlwaysScrollBehavior() }
                )
            }

            var showSearch by remember { mutableStateOf(true) }
            showSearch = currentScreen.value == DrawerScreens.Favorites

            Scaffold(
                modifier = Modifier.nestedScroll(settingsViewModel.uiState.scrollBehavior.nestedScrollConnection),
                topBar = {
                    // FIXME: Duplicated code
                    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    val backgroundColor = backgroundColors.containerColor(scrollFraction = settingsViewModel.uiState.scrollBehavior.scrollFraction).value
                    val foregroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)

                    Surface(color = backgroundColor) {
                        if (currentScreen.value == DrawerScreens.Settings) {
                            LargeTopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { openDrawer() }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "Menu"
                                        )
                                    }
                                },
                                title = {
                                    Text(
                                        text = currentScreen.value.title
                                    )
                                },
                                colors = foregroundColors,
                                scrollBehavior = settingsViewModel.uiState.scrollBehavior,
                                modifier = Modifier.windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                )
                            )
                        } else {
                            TopBar(
                                title = currentScreen.value.title,
                                openDrawer = { openDrawer() },
                                onSearch = {
                                    val intent = Intent(context, SearchActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ContextCompat.startActivity(context, intent, null)
                                },
                                showSearch = showSearch,
                                scrollBehavior = settingsViewModel.uiState.scrollBehavior,
                                modifier = Modifier.windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                )
                            )
                        }
                    }
                }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = DrawerScreens.Favorites.route
                ) {
                    screens.forEach { drawerScreens ->
                        composable(drawerScreens.route) {
                            currentScreen.value = drawerScreens
                            drawerScreens.component()
                        }
                    }
                }
            }
        }
    )
}
