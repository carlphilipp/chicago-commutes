package fr.cph.chicago.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.color.DynamicColors
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel

@Composable
fun ChicagoCommutesTheme(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {

    val isDarkTheme = when (settingsViewModel.uiState.theme) {
        Theme.AUTO -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    val dynamicColor = settingsViewModel.uiState.dynamicColorEnabled && DynamicColors.isDynamicColorAvailable()
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }

        isDarkTheme -> {
            settingsViewModel.uiState.themeColor.darkTheme
        }
        else -> {
            settingsViewModel.uiState.themeColor.lightTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChicagoCommutesTypography,
        content = content
    )

    SystemUiSetup(isDarkTheme)
}

@Composable
private fun SystemUiSetup(isDarkTheme: Boolean) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
    }
}
