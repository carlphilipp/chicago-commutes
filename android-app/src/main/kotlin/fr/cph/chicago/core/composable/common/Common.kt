package fr.cph.chicago.core.composable.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ColoredBox(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedText(modifier: Modifier = Modifier, time: String, style: TextStyle = LocalTextStyle.current) {
    Row(modifier = modifier) {
        Surface(color = Color.Transparent) {
            AnimatedContent(
                targetState = time,
                transitionSpec = {
                    run {
                        // The target slides up and fades in while the initial string slides up and fades out.
                        slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }
            ) { target ->
                Text(
                    text = target,
                    style = style,
                    maxLines = 1,
                )
            }
        }
    }
}
