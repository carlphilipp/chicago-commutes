package fr.cph.chicago.core.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import fr.cph.chicago.R
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.ZoomableImage

@Composable
fun Map(
    navigationViewModel: NavigationViewModel,
    title: String
) {
    // FIXME: Check double click zoom default
    Column {
        DisplayTopBar(
            screen = Screen.Map,
            title = title,
            viewModel = navigationViewModel,
        )
        ZoomableImage(
            painter = painterResource(R.drawable.ctamap),
            isRotation = false,
        )
    }
}
