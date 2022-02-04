package fr.cph.chicago.core.composable

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.cph.chicago.core.composable.screen.Alerts
import fr.cph.chicago.core.composable.screen.Bus
import fr.cph.chicago.core.composable.screen.Divvy
import fr.cph.chicago.core.composable.screen.DrawerScreens
import fr.cph.chicago.core.composable.screen.Favorites
import fr.cph.chicago.core.composable.screen.Map
import fr.cph.chicago.core.composable.screen.Nearby
import fr.cph.chicago.core.composable.screen.Rate
import fr.cph.chicago.core.composable.screen.Settings
import fr.cph.chicago.core.composable.screen.Train
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(screens: List<DrawerScreens>) {
    Surface {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = { scope.launch { drawerState.open() } }
        val navController = rememberNavController()
        val currentScreen = remember { mutableStateOf<DrawerScreens>(DrawerScreens.Favorites) }

        NavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Drawer(
                    currentScreen = currentScreen.value,
                    onDestinationClicked = { screen ->
                        scope.launch {
                            drawerState.close()
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
                Scaffold(
                    topBar = {
                        TopBar(currentScreen.value.title) { openDrawer() }
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
}
