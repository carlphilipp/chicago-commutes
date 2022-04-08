package fr.cph.chicago.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import fr.cph.chicago.R

const val defaultFontName = "Product Sans"

enum class FontSize(val description: String, val offset: Int) {
    REGULAR("Regular", 0),
    MEDIUM("Medium", 2),
    LARGE("Large", 4);

    companion object {
        fun fromString(str: String): FontSize {
            return values()
                .find { str == it.description }
                ?: REGULAR
        }
    }
}

val availableFonts = mapOf(
    "Actor" to Font(R.font.actor),
    "Cambo" to Font(R.font.cambo),
    "Droid Sans" to Font(R.font.droid_sans),
    "Monda" to Font(R.font.monda),
    "nevis" to Font(R.font.nevis),
    "Roboto" to Font(R.font.roboto),
    defaultFontName to Font(R.font.product_sans_regular),
    "Source Sans Pro" to Font(R.font.source_sans_pro),
)

private val fontSizes = mapOf(
    "displayLarge" to 57,
    "displayMedium" to 45,
    "displaySmall" to 36,
    "headlineLarge" to 32,
    "headlineMedium" to 28,
    "headlineSmall" to 24,
    "titleLarge" to 25,
    "titleMedium" to 20,
    "titleSmall" to 14,
    "labelLarge" to 14,
    "bodyLarge" to 16,
    "bodyMedium" to 14,
    "bodySmall" to 12,
    "labelMedium" to 12,
    "labelSmall" to 11,
)

private var defaultFont = availableFonts[defaultFontName]!!

fun getTypographyWithFont(
    font: String,
    fontSize: FontSize,
): Typography {
    val fontFamily = (availableFonts[font] ?: defaultFont).toFontFamily()
    return generateTypography(fontFamily, fontSize)
}

private fun generateTypography(
    fontFamily: FontFamily,
    fontSize: FontSize = FontSize.REGULAR,
): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["displayLarge"]!! + fontSize.offset).sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["displayMedium"]!! + fontSize.offset).sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["displaySmall"]!! + fontSize.offset).sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["headlineLarge"]!! + fontSize.offset).sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["headlineMedium"]!! + fontSize.offset).sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["headlineSmall"]!! + fontSize.offset).sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["titleLarge"]!! + fontSize.offset).sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (fontSizes["titleMedium"]!! + fontSize.offset).sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (fontSizes["titleSmall"]!! + fontSize.offset).sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (fontSizes["labelLarge"]!! + fontSize.offset).sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["bodyLarge"]!! + fontSize.offset).sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["bodyMedium"]!! + fontSize.offset).sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = (fontSizes["bodySmall"]!! + fontSize.offset).sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (fontSizes["labelMedium"]!! + fontSize.offset).sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (fontSizes["labelSmall"]!! + fontSize.offset).sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
    )
}
