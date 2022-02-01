package fr.cph.chicago.core.activity.station

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.appcompattheme.createAppCompatTheme
import fr.cph.chicago.R

class ComposableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StationTitle(Header("Belmont", "2min"))
        }
    }
}

data class Header(val name: String, val time: String)

@Composable
fun StationTitle(msg: Header) {
    ChicagoCommutesTheme {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(all = 0.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_train_white_24dp),
                    contentDescription = "Train",
                    colorFilter = ColorFilter.tint(Red),
                    modifier = Modifier
                        .size(30.dp),
                    /*.background(Black)*/
                )
                Text(
                    text = msg.name,
                    maxLines = 1,
                    modifier = Modifier
                        /*.background(Blue)*/
                        .weight(1f),
                    //color = MaterialTheme.colors.primary,
                )
                Text(
                    text = msg.time,
                    maxLines = 1,
                    modifier = Modifier,/*.background(Yellow)*/
                    //color = MaterialTheme.colors.primary,
                )
            }

            Divider(thickness = 1.dp)

            Row {
                Text(
                    text = "Arrivals to display here",
                    maxLines = 1,
                    modifier = Modifier
                        /*.background(Blue)*/
                        .weight(1f),
                    //color = MaterialTheme.colors.primary,
                )
            }

            Divider(thickness = 1.dp)

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(all = 0.dp)
                    .fillMaxWidth()) {
                TextButton(
                    onClick = { /* ... */ }
                ) {
                    Text(
                        text = "Details".uppercase(),
                        //color = MaterialTheme.colors.primary
                    )
                }
                TextButton(
                    onClick = { /* ... */ }
                ) {
                    Text("View Trains".uppercase())
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    StationTitle(
        msg = Header("Belmont", "2 min")
    )
}

@Composable
fun ChicagoCommutesTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // https://developer.android.com/jetpack/compose/themes/material
    // https://google.github.io/accompanist/appcompat-theme/
    //AppCompatTheme(content = content)
    val colors = if (isDarkTheme) {
        DarkColors
    } else {
        LightColors
    }
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

private val DarkColors = darkColors(
    primary = Color.Green,
    primaryVariant = Color.Green,
    secondary = Color.Green,
    secondaryVariant = Color.Green,
    background = Color.Green,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFB00020),
    surface = Color.Green,
    onSurface = Color(0xFFB00020),
    error = Color.Green,
    onError = Color(0xFFB00020),
)
private val LightColors = lightColors(
    primary = Color.Yellow,
    primaryVariant = Color.Yellow,
    //secondary = Color.Black,
    //secondaryVariant = Color.Black,
)
