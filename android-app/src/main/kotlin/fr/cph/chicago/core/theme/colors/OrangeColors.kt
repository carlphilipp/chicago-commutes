package fr.cph.chicago.core.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class OrangeColors {
    companion object {
        val md_theme_light_primary = Color(0xFFa43d00)
        val md_theme_light_onPrimary = Color(0xFFffffff)
        val md_theme_light_primaryContainer = Color(0xFFffdbcb)
        val md_theme_light_onPrimaryContainer = Color(0xFF360f00)
        val md_theme_light_secondary = Color(0xFF77574a)
        val md_theme_light_onSecondary = Color(0xFFffffff)
        val md_theme_light_secondaryContainer = Color(0xFFffdbcb)
        val md_theme_light_onSecondaryContainer = Color(0xFF2c160c)
        val md_theme_light_tertiary = Color(0xFF675f30)
        val md_theme_light_onTertiary = Color(0xFFffffff)
        val md_theme_light_tertiaryContainer = Color(0xFFeee3a8)
        val md_theme_light_onTertiaryContainer = Color(0xFF201c00)
        val md_theme_light_error = Color(0xFFba1b1b)
        val md_theme_light_errorContainer = Color(0xFFffdad4)
        val md_theme_light_onError = Color(0xFFffffff)
        val md_theme_light_onErrorContainer = Color(0xFF410001)
        val md_theme_light_background = Color(0xFFfcfcfc)
        val md_theme_light_onBackground = Color(0xFF201a18)
        val md_theme_light_surface = Color(0xFFfcfcfc)
        val md_theme_light_onSurface = Color(0xFF201a18)
        val md_theme_light_surfaceVariant = Color(0xFFf5ded5)
        val md_theme_light_onSurfaceVariant = Color(0xFF53443d)
        val md_theme_light_outline = Color(0xFF85736c)
        val md_theme_light_inverseOnSurface = Color(0xFFfbeeea)
        val md_theme_light_inverseSurface = Color(0xFF362f2c)
        val md_theme_light_inversePrimary = Color(0xFFffb593)
        val md_theme_light_shadow = Color(0xFF000000)

        val md_theme_dark_primary = Color(0xFFffb593)
        val md_theme_dark_onPrimary = Color(0xFF581d00)
        val md_theme_dark_primaryContainer = Color(0xFF7d2d00)
        val md_theme_dark_onPrimaryContainer = Color(0xFFffdbcb)
        val md_theme_dark_secondary = Color(0xFFe7bead)
        val md_theme_dark_onSecondary = Color(0xFF432a1f)
        val md_theme_dark_secondaryContainer = Color(0xFF5c4034)
        val md_theme_dark_onSecondaryContainer = Color(0xFFffdbcb)
        val md_theme_dark_tertiary = Color(0xFFd2c78f)
        val md_theme_dark_onTertiary = Color(0xFF373107)
        val md_theme_dark_tertiaryContainer = Color(0xFF4e471b)
        val md_theme_dark_onTertiaryContainer = Color(0xFFeee3a8)
        val md_theme_dark_error = Color(0xFFffb4a9)
        val md_theme_dark_errorContainer = Color(0xFF930006)
        val md_theme_dark_onError = Color(0xFF680003)
        val md_theme_dark_onErrorContainer = Color(0xFFffdad4)
        val md_theme_dark_background = Color(0xFF201a18)
        val md_theme_dark_onBackground = Color(0xFFede0dc)
        val md_theme_dark_surface = Color(0xFF201a18)
        val md_theme_dark_onSurface = Color(0xFFede0dc)
        val md_theme_dark_surfaceVariant = Color(0xFF53443d)
        val md_theme_dark_onSurfaceVariant = Color(0xFFd7c2ba)
        val md_theme_dark_outline = Color(0xFFa08d86)
        val md_theme_dark_inverseOnSurface = Color(0xFF201a18)
        val md_theme_dark_inverseSurface = Color(0xFFede0dc)
        val md_theme_dark_inversePrimary = Color(0xFFa43d00)
        val md_theme_dark_shadow = Color(0xFF000000)

        val seed = Color(0xFFff6600)
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
