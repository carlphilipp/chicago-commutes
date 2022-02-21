package fr.cph.chicago.core.composable.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.ZoomableImage


@Composable
fun Map() {
    // FIXME: Deactivate left panel
    // FIXME: Check double click zoom default
    ZoomableImage(
        painter = painterResource(R.drawable.ctamap),
        isRotation = false,
    )
}
