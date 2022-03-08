package fr.cph.chicago.core.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import fr.cph.chicago.R
import fr.cph.chicago.core.ui.common.ZoomableImage

@Composable
fun Map() {
    // FIXME: Check double click zoom default
    ZoomableImage(
        painter = painterResource(R.drawable.ctamap),
        isRotation = false,
    )
}
