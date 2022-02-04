package fr.cph.chicago.core.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun Navigation() {
    Surface {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = { scope.launch { drawerState.open() } }
        val navController = rememberNavController()
        //val title = remember { mutableStateOf("Favorites") }
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
                        AppBar(currentScreen.value.title) { openDrawer() }
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DrawerScreens.Favorites.route
                    ) {
                        composable(DrawerScreens.Favorites.route) {
                            currentScreen.value = DrawerScreens.Favorites
                            Favorites()
                        }
                        composable(DrawerScreens.Train.route) {
                            currentScreen.value = DrawerScreens.Train
                            Train()
                        }
                        composable(DrawerScreens.Bus.route) {
                            currentScreen.value = DrawerScreens.Bus
                            Bus()
                        }
                        composable(DrawerScreens.Divvy.route) {
                            currentScreen.value = DrawerScreens.Divvy
                            Divvy()
                        }
                        composable(DrawerScreens.Nearby.route) {
                            currentScreen.value = DrawerScreens.Nearby
                            Nearby()
                        }
                        composable(DrawerScreens.Map.route) {
                            currentScreen.value = DrawerScreens.Map
                            Map()
                        }
                        composable(DrawerScreens.Alerts.route) {
                            currentScreen.value = DrawerScreens.Alerts
                            Alerts()
                        }
                        composable(DrawerScreens.Rate.route) {
                            currentScreen.value = DrawerScreens.Rate
                            Rate()
                        }
                        composable(DrawerScreens.Settings.route) {
                            currentScreen.value = DrawerScreens.Settings
                            Settings()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AppBar(title: String, openDrawer: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            /*IconButton(onClick = {
                isRefreshing.value = true
                Timber.i("Start Refreshing")
                store.dispatch(FavoritesAction())
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }*/
        }
    )
}
