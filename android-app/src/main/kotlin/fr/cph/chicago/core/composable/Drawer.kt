package fr.cph.chicago.core.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.screen.DrawerScreens
import fr.cph.chicago.core.composable.screen.screens

@Composable
fun Drawer(modifier: Modifier = Modifier, currentScreen: DrawerScreens, onDestinationClicked: (route: DrawerScreens) -> Unit) {
    Column(modifier = modifier) {
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
                text = "Chicago Commutes",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            )
        }

        screens.forEach { screen ->
            val colors = MaterialTheme.colorScheme
            val backgroundColor = if (screen.route == currentScreen.route) {
                colors.primary.copy(alpha = 0.12f)
            } else {
                Color.Transparent
            }
            val surfaceModifier = modifier
                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .height(50.dp)
            DrawerButton(
                screen = screen,
                backgroundColor = backgroundColor,
                modifier = surfaceModifier,
                onDestinationClicked = onDestinationClicked
            )
        }
    }
}

@Composable
fun DrawerButton(
    screen: DrawerScreens,
    backgroundColor: Color,
    modifier: Modifier,
    onDestinationClicked: (route: DrawerScreens) -> Unit) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(20.0.dp),
    ) {
        TextButton(
            onClick = { onDestinationClicked(screen) },
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    imageVector = screen.icon,
                    contentDescription = "Icon",
                    modifier = Modifier,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
