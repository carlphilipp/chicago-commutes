/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import fr.cph.chicago.R
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.util.MapUtil.chicagoPosition

object GoogleMapUtil {

    const val defaultZoom = 10f
    val chicago: LatLng by lazy { LatLng(chicagoPosition.latitude, chicagoPosition.longitude) }

    fun getBitmapDescriptor(context: Context?, @DrawableRes icon: Int): BitmapDescriptor {
        return if (context != null) {
            val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
            val bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitMapBusStation)
            val shape = ContextCompat.getDrawable(context, icon)!!
            shape.setBounds(0, 0, px, bitMapBusStation.height)
            shape.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitMapBusStation)
        } else {
            BitmapDescriptorFactory.defaultMarker()
        }
    }

    fun isIn(num: Float, sup: Float, inf: Float): Boolean {
        return num in inf..sup
    }

    fun createBitMapDescriptor(icon: Bitmap, size: Int): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(icon, icon.width / size, icon.height / size, true)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

@Composable
fun CameraDebugView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center
    ) {
        val moving = if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
    }
}

@Composable
fun InfoWindowsDetails(
    showView: Boolean,
    destination: String,
    showAll: Boolean,
    results: List<Pair<String, String>>,
    onClick: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 50.dp, end = 50.dp, bottom = 50.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            AnimatedVisibility(
                visible = showView,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "To: $destination",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (results.isNotEmpty()) {
                        val max = if (showAll) {
                            results.size
                        } else {
                            6
                        }
                        for (i in 0 until max - 1) {
                            val pair = results[i]
                            EtaView(stopName = pair.first, eta = pair.second)
                        }
                        if (!showAll && max >= 6) {
                            DisplayAllResultsRowView(onClick = onClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EtaView(stopName: String, eta: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stopName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = eta,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DisplayAllResultsRowView(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(
            onClick = onClick,
        ) {
            Text(text = "More")
        }
    }
}

@NonNull
fun Position.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

@NonNull
fun LatLng.toPosition(): Position {
    return Position(this.latitude, this.longitude)
}

@Composable
fun mapStyle(settingsViewModel: SettingsViewModel): Int {
    val isDarkTheme = when (settingsViewModel.uiState.theme) {
        Theme.AUTO -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }
    return if (isDarkTheme) R.raw.style_json_dark else R.raw.style_json_light
}
