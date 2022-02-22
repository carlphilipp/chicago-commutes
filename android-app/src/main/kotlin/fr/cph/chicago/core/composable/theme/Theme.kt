package fr.cph.chicago.core.composable.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import fr.cph.chicago.core.composable.screen.SettingsViewModel
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.service.PreferenceService

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

    val colors = if (isDarkTheme) {
        DarkThemeColors
    } else {
        LightThemeColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = ChicagoCommutesTypography,
        content = content
    )
}
