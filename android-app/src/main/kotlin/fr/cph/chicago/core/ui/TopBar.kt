package fr.cph.chicago.core.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    openDrawer: () -> Unit,
    onSearch: () -> Unit,
    showSearch: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

// TODO refactor and rename cause it does not make sense
@Composable
fun RefreshTopBar(title: String, showRefresh: Boolean = false, onRefresh: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = { /*activity.finish()*/ }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (showRefresh) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    )
}

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

@Composable
fun getTopBarBackgroundColor(scrollBehavior: TopAppBarScrollBehavior): Color {
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    return backgroundColors.containerColor(scrollFraction = scrollBehavior.scrollFraction).value
}

@Composable
fun getTopBarForegroundColors(): TopAppBarColors {
    return TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
}
