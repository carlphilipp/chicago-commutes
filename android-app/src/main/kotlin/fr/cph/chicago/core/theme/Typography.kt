package fr.cph.chicago.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import fr.cph.chicago.R
import timber.log.Timber

//val font = FontFamily.Default
//val font = FontFamily(Font(R.font.open_sans_condensed_light))
//val font = FontFamily(Font(R.font.nevis))
//val font = FontFamily(Font(R.font.product_sans_regular))

const val defaultFontName = "Product Sans"

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

private var defaultFont = availableFonts[defaultFontName]!!.toFontFamily()

fun getTypography(font: String): Typography {
    Timber.i("Font to use: $font")
    if (availableFonts.containsKey(font)) {
        defaultFont = availableFonts[font]!!.toFontFamily()
        Timber.i("Font loaded as default font")
    }
    return generateTypography()
}

private fun generateTypography(): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 25.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.W400,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = defaultFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
    )

}
