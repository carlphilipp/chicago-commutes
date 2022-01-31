package fr.cph.chicago.core.activity.station

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R

class ComposableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChicagoCommutesTheme {
                StationTitle(Header("Belmont", "2min"))
            }
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
                modifier = Modifier.padding(all = 8.dp)/*.background(Green)*/) {
                Image(
                    painter = painterResource(R.drawable.ic_train_white_24dp),
                    contentDescription = "Train",
                    colorFilter = ColorFilter.tint(Red),
                    modifier = Modifier
                        .size(30.dp),
                    /*.background(Black)*/
                )

                //Spacer(modifier = Modifier.width(8.dp))

                //Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = msg.name,
                    maxLines = 1,
                    modifier = Modifier
                        /*.background(Blue)*/
                        .weight(1f),
                )
                //}
                //Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = msg.time,
                    maxLines = 1,
                    modifier = Modifier,/*.background(Yellow)*/
                )
                //}
            }

            Divider(thickness = 1.dp)

            Row {
                Text(
                    text = "Arrivals to display here",
                    maxLines = 1,
                    modifier = Modifier
                        /*.background(Blue)*/
                        .weight(1f),
                )
            }

            Divider(thickness = 1.dp)

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()) {
                Button(
                    onClick = { /* ... */ }
                ) {
                    Text("Details")
                }
                Button(
                    onClick = { /* ... */ }
                ) {
                    Text("View Trains")
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
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
fun ChicagoCommutesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        //colors = if (darkTheme) DarkColors else LightColors,
        colors = LightColors,
        content = content
    )
}
private val DarkColors = darkColors(
    primary = Red,
    secondary = Green,
    // ...
)
private val LightColors = lightColors(
    primary = Yellow,
    primaryVariant = Color.DarkGray,
    secondary = Color.LightGray,
    // ...
)
