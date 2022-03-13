package fr.cph.chicago.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import fr.cph.chicago.core.theme.colors.BlueColors
import fr.cph.chicago.core.theme.colors.GreenColors
import fr.cph.chicago.core.theme.colors.OrangeColors
import fr.cph.chicago.core.theme.colors.PurpleColors

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

sealed class ThemeColor(
    val name: String,
    val lightTheme: ColorScheme,
    val darkTheme: ColorScheme,
) {
    object Blue : ThemeColor(name = "Blue", lightTheme = BlueColors.lightThemeColor, darkTheme = BlueColors.darkThemeColors)
    object Green : ThemeColor(name = "Green", lightTheme = GreenColors.lightThemeColor, darkTheme = GreenColors.darkThemeColors)
    object Purple : ThemeColor(name = "Purple", lightTheme = PurpleColors.lightThemeColor, darkTheme = PurpleColors.darkThemeColors)
    object Orange : ThemeColor(name = "Orange", lightTheme = OrangeColors.lightThemeColor, darkTheme = OrangeColors.darkThemeColors)

    companion object {
        fun getThemeColor(str: String): ThemeColor {
            return listOf(Blue, Green, Purple, Orange)
                .find {
                    it.name == str
                } ?: Blue
        }
    }
}

