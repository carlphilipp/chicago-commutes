package fr.cph.chicago.core.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.launchWithDelay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
    topBarTitle: String,
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.settingsAboutScrollBehavior) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.SettingsAbout,
                    title = topBarTitle,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 5.dp),
                            text = "App info",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Android,
                            title = "Chicago commutes Flavor",
                            description = "Version",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Android,
                            title = "Rate this app",
                            description = "blablbla",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Android,
                            title = "GitHub",
                            description = "Open source project",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Android,
                            title = "License",
                            description = "blablbla",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )
                    }
                }
            }
        }
    )
}
