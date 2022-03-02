package fr.cph.chicago.core.composable

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.cph.chicago.core.composable.screen.DrawerScreens
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
            var showSearch by remember { mutableStateOf(true)}
            showSearch = currentScreen.value == DrawerScreens.Favorites
            Scaffold(
                topBar = {
                    TopBar(
                        title = currentScreen.value.title,
                        openDrawer = { openDrawer() },
                        onSearch = { Toast.makeText(context, "Search", Toast.LENGTH_SHORT).show() },
                        showSearch = showSearch
                    )
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
