package fr.cph.chicago.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigationIcon: @Composable () -> Unit,
) {
    val backgroundColor = getTopBarBackgroundColor(scrollBehavior)
    val foregroundColors = getTopBarForegroundColors()

    Surface(color = backgroundColor) {
        LargeTopAppBar(
            navigationIcon = navigationIcon,
            title = { Text(text = title) },
            colors = foregroundColors,
            scrollBehavior = scrollBehavior,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val backgroundColor = getTopBarBackgroundColor(scrollBehavior)
    val foregroundColors = getTopBarForegroundColors()
    Surface(color = backgroundColor) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            colors = foregroundColors,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getTopBarBackgroundColor(scrollBehavior: TopAppBarScrollBehavior): Color {
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    return backgroundColors.containerColor(colorTransitionFraction = scrollBehavior.state.contentOffset).value
}

@Composable
fun getTopBarForegroundColors(): TopAppBarColors {
    return TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
}
