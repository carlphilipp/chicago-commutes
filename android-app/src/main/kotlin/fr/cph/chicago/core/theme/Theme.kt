package fr.cph.chicago.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

    val dynamicColor = settingsViewModel.uiState.dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isDarkTheme -> DarkThemeColors
        else -> LightThemeColors
    }

    ProvideWindowInsets {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChicagoCommutesTypography,
            content = content
        )
    }

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
