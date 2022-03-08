package fr.cph.chicago.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val blue_line = Color(0xFF009eda)
val brown_line = Color(0xFF663300)
val green_line = Color(0xFF009900)
val orange_line = Color(0xFFff8000)
val pink_line = Color(0xFFcc0066)
val purple_line = Color(0xFF660066)
val red_line = Color(0xFFff0000)
val yellow_line = Color(0xFFfdd835)
val default_line = Color(0xFF000000)

val favorite_yellow = Color(0xFFE2B800)
val bike_orange = Color(0xFFff8000)

val md_theme_light_primary = Color(0xFF315da8)
val md_theme_light_onPrimary = Color(0xFFffffff)
val md_theme_light_primaryContainer = Color(0xFFd6e2ff)
val md_theme_light_onPrimaryContainer = Color(0xFF001a43)
val md_theme_light_secondary = Color(0xFF295ea9)
val md_theme_light_onSecondary = Color(0xFFffffff)
val md_theme_light_secondaryContainer = Color(0xFFd6e3ff)
val md_theme_light_onSecondaryContainer = Color(0xFF001b3f)
val md_theme_light_tertiary = Color(0xFF4758a9)
val md_theme_light_onTertiary = Color(0xFFffffff)
val md_theme_light_tertiaryContainer = Color(0xFFdde1ff)
val md_theme_light_onTertiaryContainer = Color(0xFF00115a)
val md_theme_light_error = Color(0xFFba1b1b)
val md_theme_light_errorContainer = Color(0xFFffdad4)
val md_theme_light_onError = Color(0xFFffffff)
val md_theme_light_onErrorContainer = Color(0xFF410001)
val md_theme_light_background = Color(0xFFfffbfd)
val md_theme_light_onBackground = Color(0xFF1d1b1f)
val md_theme_light_surface = Color(0xFFfffbfd)
val md_theme_light_onSurface = Color(0xFF1d1b1f)
val md_theme_light_surfaceVariant = Color(0xFFe6e0ec)
val md_theme_light_onSurfaceVariant = Color(0xFF48454f)
val md_theme_light_outline = Color(0xFF79767f)
val md_theme_light_inverseOnSurface = Color(0xFFf4eff4)
val md_theme_light_inverseSurface = Color(0xFF323033)
val md_theme_light_inversePrimary = Color(0xFFacc7ff)
val md_theme_light_shadow = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFacc7ff)
val md_theme_dark_onPrimary = Color(0xFF002e6c)
val md_theme_dark_primaryContainer = Color(0xFF0d448e)
val md_theme_dark_onPrimaryContainer = Color(0xFFd6e2ff)
val md_theme_dark_secondary = Color(0xFFa8c7ff)
val md_theme_dark_onSecondary = Color(0xFF002f68)
val md_theme_dark_secondaryContainer = Color(0xFF00458f)
val md_theme_dark_onSecondaryContainer = Color(0xFFd6e3ff)
val md_theme_dark_tertiary = Color(0xFFb8c4ff)
val md_theme_dark_onTertiary = Color(0xFF132879)
val md_theme_dark_tertiaryContainer = Color(0xFF2e4090)
val md_theme_dark_onTertiaryContainer = Color(0xFFdde1ff)
val md_theme_dark_error = Color(0xFFffb4a9)
val md_theme_dark_errorContainer = Color(0xFF930006)
val md_theme_dark_onError = Color(0xFF680003)
val md_theme_dark_onErrorContainer = Color(0xFFffdad4)
val md_theme_dark_background = Color(0xFF1d1b1f)
val md_theme_dark_onBackground = Color(0xFFe6e1e5)
val md_theme_dark_surface = Color(0xFF1d1b1f)
val md_theme_dark_onSurface = Color(0xFFe6e1e5)
val md_theme_dark_surfaceVariant = Color(0xFF48454f)
val md_theme_dark_onSurfaceVariant = Color(0xFFc9c4cf)
val md_theme_dark_outline = Color(0xFF938f99)
val md_theme_dark_inverseOnSurface = Color(0xFF1d1b1f)
val md_theme_dark_inverseSurface = Color(0xFFe6e1e5)
val md_theme_dark_inversePrimary = Color(0xFF315da8)
val md_theme_dark_shadow = Color(0xFF010000)

val seed = Color(0xFF6750A4)
val error = Color(0xFFB3261E)

internal val LightThemeColors = lightColorScheme(

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

internal val DarkThemeColors = darkColorScheme(

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