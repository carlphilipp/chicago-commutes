package fr.cph.chicago.core.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class GreenColors {
    companion object {
        val md_theme_light_primary = Color(0xFF006c46)
        val md_theme_light_onPrimary = Color(0xFFffffff)
        val md_theme_light_primaryContainer = Color(0xFF8df8c2)
        val md_theme_light_onPrimaryContainer = Color(0xFF002112)
        val md_theme_light_secondary = Color(0xFF4d6356)
        val md_theme_light_onSecondary = Color(0xFFffffff)
        val md_theme_light_secondaryContainer = Color(0xFFcfe8d7)
        val md_theme_light_onSecondaryContainer = Color(0xFF0b1f15)
        val md_theme_light_tertiary = Color(0xFF3d6472)
        val md_theme_light_onTertiary = Color(0xFFffffff)
        val md_theme_light_tertiaryContainer = Color(0xFFc0e9fa)
        val md_theme_light_onTertiaryContainer = Color(0xFF001f29)
        val md_theme_light_error = Color(0xFFba1b1b)
        val md_theme_light_errorContainer = Color(0xFFffdad4)
        val md_theme_light_onError = Color(0xFFffffff)
        val md_theme_light_onErrorContainer = Color(0xFF410001)
        val md_theme_light_background = Color(0xFFfbfdf8)
        val md_theme_light_onBackground = Color(0xFF191c1a)
        val md_theme_light_surface = Color(0xFFfbfdf8)
        val md_theme_light_onSurface = Color(0xFF191c1a)
        val md_theme_light_surfaceVariant = Color(0xFFdce5dd)
        val md_theme_light_onSurfaceVariant = Color(0xFF404943)
        val md_theme_light_outline = Color(0xFF707972)
        val md_theme_light_inverseOnSurface = Color(0xFFf0f1ed)
        val md_theme_light_inverseSurface = Color(0xFF2d312e)
        val md_theme_light_inversePrimary = Color(0xFF71dba7)
        val md_theme_light_shadow = Color(0xFF000000)

        val md_theme_dark_primary = Color(0xFF71dba7)
        val md_theme_dark_onPrimary = Color(0xFF003822)
        val md_theme_dark_primaryContainer = Color(0xFF005234)
        val md_theme_dark_onPrimaryContainer = Color(0xFF8df8c2)
        val md_theme_dark_secondary = Color(0xFFb4ccbc)
        val md_theme_dark_onSecondary = Color(0xFF20352a)
        val md_theme_dark_secondaryContainer = Color(0xFF364b3f)
        val md_theme_dark_onSecondaryContainer = Color(0xFFcfe8d7)
        val md_theme_dark_tertiary = Color(0xFFa4cddd)
        val md_theme_dark_onTertiary = Color(0xFF063542)
        val md_theme_dark_tertiaryContainer = Color(0xFF234c5a)
        val md_theme_dark_onTertiaryContainer = Color(0xFFc0e9fa)
        val md_theme_dark_error = Color(0xFFffb4a9)
        val md_theme_dark_errorContainer = Color(0xFF930006)
        val md_theme_dark_onError = Color(0xFF680003)
        val md_theme_dark_onErrorContainer = Color(0xFFffdad4)
        val md_theme_dark_background = Color(0xFF191c1a)
        val md_theme_dark_onBackground = Color(0xFFe1e3df)
        val md_theme_dark_surface = Color(0xFF191c1a)
        val md_theme_dark_onSurface = Color(0xFFe1e3df)
        val md_theme_dark_surfaceVariant = Color(0xFF404943)
        val md_theme_dark_onSurfaceVariant = Color(0xFFc0c9c1)
        val md_theme_dark_outline = Color(0xFF8a938c)
        val md_theme_dark_inverseOnSurface = Color(0xFF191c1a)
        val md_theme_dark_inverseSurface = Color(0xFFe1e3df)
        val md_theme_dark_inversePrimary = Color(0xFF006c46)
        val md_theme_dark_shadow = Color(0xFF000000)

        val seed = Color(0xFF195239)
        val error = Color(0xFFba1b1b)

        val lightThemeColor = lightColorScheme(
            primary = md_theme_light_primary,
            onPrimary = md_theme_light_onPrimary,
            primaryContainer = md_theme_light_primaryContainer,
            onPrimaryContainer = md_theme_light_onPrimaryContainer,
            secondary = md_theme_light_secondary,
            onSecondary = md_theme_light_onSecondary,
            secondaryContainer = md_theme_light_secondaryContainer,
            onSecondaryContainer = md_theme_light_onSecondaryContainer,
            tertiary = md_theme_light_tertiary,
            onTertiary = md_theme_light_onTertiary,
            tertiaryContainer = md_theme_light_tertiaryContainer,
            onTertiaryContainer = md_theme_light_onTertiaryContainer,
            error = md_theme_light_error,
            errorContainer = md_theme_light_errorContainer,
            onError = md_theme_light_onError,
            onErrorContainer = md_theme_light_onErrorContainer,
            background = md_theme_light_background,
            onBackground = md_theme_light_onBackground,
            surface = md_theme_light_surface,
            onSurface = md_theme_light_onSurface,
            surfaceVariant = md_theme_light_surfaceVariant,
            onSurfaceVariant = md_theme_light_onSurfaceVariant,
            outline = md_theme_light_outline,
            inverseOnSurface = md_theme_light_inverseOnSurface,
            inverseSurface = md_theme_light_inverseSurface,
            inversePrimary = md_theme_light_inversePrimary,
        )

        val darkThemeColors = darkColorScheme(
            primary = md_theme_dark_primary,
            onPrimary = md_theme_dark_onPrimary,
            primaryContainer = md_theme_dark_primaryContainer,
            onPrimaryContainer = md_theme_dark_onPrimaryContainer,
            secondary = md_theme_dark_secondary,
            onSecondary = md_theme_dark_onSecondary,
            secondaryContainer = md_theme_dark_secondaryContainer,
            onSecondaryContainer = md_theme_dark_onSecondaryContainer,
            tertiary = md_theme_dark_tertiary,
            onTertiary = md_theme_dark_onTertiary,
            tertiaryContainer = md_theme_dark_tertiaryContainer,
            onTertiaryContainer = md_theme_dark_onTertiaryContainer,
            error = md_theme_dark_error,
            errorContainer = md_theme_dark_errorContainer,
            onError = md_theme_dark_onError,
            onErrorContainer = md_theme_dark_onErrorContainer,
            background = md_theme_dark_background,
            onBackground = md_theme_dark_onBackground,
            surface = md_theme_dark_surface,
            onSurface = md_theme_dark_onSurface,
            surfaceVariant = md_theme_dark_surfaceVariant,
            onSurfaceVariant = md_theme_dark_onSurfaceVariant,
            outline = md_theme_dark_outline,
            inverseOnSurface = md_theme_dark_inverseOnSurface,
            inverseSurface = md_theme_dark_inverseSurface,
            inversePrimary = md_theme_dark_inversePrimary,
        )
    }
}
