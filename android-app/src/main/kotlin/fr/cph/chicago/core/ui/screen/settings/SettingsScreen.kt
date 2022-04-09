@file:OptIn(ExperimentalMaterialApi::class)

package fr.cph.chicago.core.ui.screen.settings

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.theme.FontSize
import fr.cph.chicago.core.theme.ThemeColor
import fr.cph.chicago.core.ui.common.AnimationSpeed
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.launchWithDelay
import fr.cph.chicago.service.PreferenceService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.settingsScrollBehavior) }

    // Wrapping with Scaffold as the animation is overridden if it's not the case
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.Settings,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth()
                ) {
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Brightness6,
                            title = "Display",
                            description = "Theme, dark mode and fonts",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {
                                    navController.navigate(screen = Screen.SettingsDisplay)
                                }
                            }
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.DeveloperMode,
                            title = "Developer options",
                            description = "Beep boop",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {
                                    navController.navigate(screen = Screen.SettingsDeveloperOptions)
                                }
                            }
                        )
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.Info,
                            title = "About",
                            description = "Chicago commutes",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {
                                    navController.navigate(screen = Screen.SettingsAbout)
                                }
                            }
                        )
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        })
}

@Composable
fun SettingsElementView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    SettingsElementView(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier.padding(end = 20.dp),
                imageVector = imageVector,
                contentDescription = null
            )
        },
        title = title,
        description = description,
        onClick = onClick,
    )
}

@Composable
fun SettingsElementView(
    modifier: Modifier = Modifier,
    painter: Painter,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    SettingsElementView(
        modifier = modifier,
        icon = {
            Image(
                modifier = Modifier.padding(end = 20.dp),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        },
        title = title,
        description = description,
        onClick = onClick,
    )
}

@Composable
private fun SettingsElementView(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
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
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Column(
                modifier = Modifier.fillMaxWidth(),
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

@OptIn(ExperimentalMaterial3Api::class)
data class SettingsState(
    val theme: Theme = Theme.AUTO,
    val themeColor: ThemeColor = ThemeColor.Blue,
    val showThemeChangerDialog: Boolean = false,
    val dynamicColorEnabled: Boolean = false,
    val showMapDebug: Boolean = false,
    val fontTypeFace: String = "",
    val fontSize: FontSize = FontSize.REGULAR,
    val animationSpeed: AnimationSpeed = AnimationSpeed.Normal,

    // FIXME: To delete
    val bottomSheetContent: @Composable ColumnScope.() -> Unit = { Text("") },
    val modalBottomSheetState: ModalBottomSheetState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = tween(durationMillis = animationSpeed.slideDuration),
        isSkipHalfExpanded = true,
    ),
)

class SettingsViewModel(private val preferenceService: PreferenceService = PreferenceService) {
    var uiState by mutableStateOf(SettingsState())
        private set

    fun initModel(): SettingsViewModel {
        refreshCurrentTheme()
        return this
    }

    fun setAnimationSpeed(animationSpeed: AnimationSpeed) {
        preferenceService.saveAnimationSpeed(animationSpeed)
        refreshCurrentTheme()
    }

    fun setTheme(theme: Theme) {
        preferenceService.saveTheme(theme)
        refreshCurrentTheme()
    }

    fun setThemeColor(themeColor: ThemeColor) {
        preferenceService.saveThemeColor(themeColor)
        refreshCurrentTheme()
    }

    fun setDynamicColor(value: Boolean) {
        preferenceService.saveDynamicColor(value)
        refreshCurrentTheme()
    }

    fun setFontTypeFace(font: String) {
        preferenceService.saveFont(font)
        refreshCurrentTheme()
    }

    fun setFontSize(value: FontSize) {
        preferenceService.saveFontSize(value)
        refreshCurrentTheme()
    }

    fun refreshCurrentTheme() {
        uiState = uiState.copy(
            showMapDebug = preferenceService.getShowDebug(),
            theme = preferenceService.getTheme(),
            themeColor = preferenceService.getThemeColor(),
            dynamicColorEnabled = preferenceService.getDynamicColor(),
            fontTypeFace = preferenceService.getFont(),
            fontSize = preferenceService.getFontSize(),
            animationSpeed = preferenceService.getAnimationSpeed(),
        )
    }
}

enum class BottomSheetState {
    FONT_TYPE, FONT_SIZE, ANIMATION_SPEED
}
