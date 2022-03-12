package fr.cph.chicago.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.ui.screen.drawerScreens
import timber.log.Timber

@Composable
fun Drawer(modifier: Modifier = Modifier, currentScreen: Screen, onDestinationClicked: (route: Screen) -> Unit) {
    Timber.d("Compose Drawer")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box {
                Image(
                    painter = painterResource(R.drawable.header),
                    contentDescription = "Chicago Skyline",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                )
                Text(
                    text = stringResource(id = R.string.current_app_name),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                )
            }
        }

        items(
            items = drawerScreens,
            key = { it.id }
        ) { screen ->
            Timber.v("Compose drawing drawer button")
            val backgroundColor = if (screen.route == currentScreen.route) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            }
            DrawerButton(
                screen = screen,
                backgroundColor = backgroundColor,
                modifier = modifier
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                onDestinationClicked = onDestinationClicked
            )
        }
        item { NavigationBarsSpacer(modifier = Modifier.padding(bottom = 5.dp)) }
    }
}

@Composable
fun DrawerButton(
    modifier: Modifier,
    screen: Screen,
    backgroundColor: Color,
    onDestinationClicked: (route: Screen) -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(20.0.dp),
    ) {
        TextButton(
            onClick = { onDestinationClicked(screen) },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = "Icon",
                    modifier = Modifier,
                    //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = screen.title,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
