package fr.cph.chicago.core.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimationSpeedBottomView
import fr.cph.chicago.core.ui.common.FontSizeBottomView
import fr.cph.chicago.core.ui.common.FontTypefaceBottomView
import fr.cph.chicago.core.ui.common.ModalBottomSheetLayoutMaterial3
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SwitchMaterial3
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.launchWithDelay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DisplaySettingsScreen(
    modifier: Modifier = Modifier,
    title: String,
    viewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
) {
    Timber.d("Compose DisplaySettingsScreen")
    val uiState = viewModel.uiState
    val navController = LocalNavController.current

    val scope = rememberCoroutineScope()
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.settingsDisplayScrollBehavior) }

    ModalBottomSheetLayoutMaterial3(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        sheetState = viewModel.uiState.modalBottomSheetState,
        sheetContent = viewModel.uiState.bottomSheetContent,
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.SettingsDisplay,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 5.dp),
                            text = "Theme",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        DisplayElementView(
                            title = "Theme color",
                            description = "Choose a color",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {
                                    navController.navigate(screen = Screen.SettingsThemeColorChooser)
                                }
                            },
                            imageVector = Icons.Outlined.Palette
                        )
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
                            text = "Appearance",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        DisplayElementSwitchView(
                            title = "Follow System",
                            description = "Select dark/light mode based on system",
                            onClick = {
                                if (viewModel.uiState.theme != Theme.AUTO) {
                                    viewModel.setTheme(Theme.AUTO)
                                }
                            },
                            imageVector = Icons.Outlined.Brightness6,
                            isChecked = uiState.theme == Theme.AUTO,
                        )
                        DisplayElementSwitchView(
                            title = "Light Mode",
                            description = "Enable",
                            onClick = {
                                if (viewModel.uiState.theme != Theme.LIGHT) {
                                    viewModel.setTheme(Theme.LIGHT)
                                }
                            },
                            imageVector = Icons.Outlined.LightMode,
                            isChecked = uiState.theme == Theme.LIGHT,
                        )
                        DisplayElementSwitchView(
                            title = "Dark Mode",
                            description = "Enable",
                            onClick = {
                                if (viewModel.uiState.theme != Theme.DARK) {
                                    viewModel.setTheme(Theme.DARK)
                                }
                            },
                            imageVector = Icons.Outlined.DarkMode,
                            isChecked = uiState.theme == Theme.DARK,
                        )
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
                            text = "Fonts",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        DisplayElementView(
                            title = "Font type",
                            description = "Choose a font",
                            onClick = {
                                scope.launch {
                                    viewModel.updateBottomSheet {
                                        FontTypefaceBottomView(title = "Pick a font", viewModel = viewModel)
                                    }
                                    viewModel.uiState.modalBottomSheetState.show()
                                }
                            },
                            imageVector = Icons.Outlined.TextFormat,
                        )
                        DisplayElementView(
                            title = "Font size",
                            description = "Choose a font size",
                            onClick = {
                                scope.launch {
                                    viewModel.updateBottomSheet {
                                        FontSizeBottomView(title = "Pick a font size", viewModel = viewModel)
                                    }
                                    viewModel.uiState.modalBottomSheetState.show()
                                }
                            },
                            imageVector = Icons.Outlined.FormatSize,
                        )
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
                            text = "Animations",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        DisplayElementView(
                            title = "Animations speed",
                            description = "Choose how fast the animation are rendered",
                            onClick = {
                                scope.launch {
                                    viewModel.updateBottomSheet {
                                        AnimationSpeedBottomView(title = "Pick the animation speed", viewModel = viewModel)
                                    }
                                    viewModel.uiState.modalBottomSheetState.show()
                                }
                            },
                            imageVector = Icons.Outlined.Animation,
                        )
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        },
    )
}

@Composable
fun DisplayElementView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = 20.dp),
                imageVector = imageVector,
                contentDescription = null
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun DisplayElementSwitchView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val newModifier = if (enabled) modifier.clickable(onClick = onClick) else modifier.fillMaxWidth()
    val alpha = if (enabled) 1.0f else 0.2f
    Row(modifier = newModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .alpha(alpha),
                imageVector = imageVector,
                contentDescription = null
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    modifier = Modifier.alpha(alpha),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    modifier = Modifier.alpha(alpha),
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SwitchMaterial3(
                    modifier = Modifier,
                    onCheckedChange = {
                        onClick()
                    },
                    enabled = enabled,
                    checked = isChecked,
                )
            }
        }
    }
}
