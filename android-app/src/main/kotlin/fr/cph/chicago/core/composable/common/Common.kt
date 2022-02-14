package fr.cph.chicago.core.composable.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.composable.theme.ShimmerColorShades

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
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// FIXME: Refactor duplicated code
@Composable
fun LargeImagePlaceHolderAnimated() {
    /*
 Create InfiniteTransition
 which holds child animation like [Transition]
 animations start running as soon as they enter
 the composition and do not stop unless they are removed
*/
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        /*
         Specify animation positions,
         initial Values 0F means it
         starts from 0 position
        */
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            // Tween Animates between values over specified [durationMillis]
            tween(durationMillis = 500/*1200*/, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    /*
      Create a gradient using the list of colors
      Use Linear Gradient for animating in any direction according to requirement
      start=specifies the position to start with in cartesian like system Offset(10f,10f) means x(10,0) , y(0,10)
      end = Animate the end position to give the shimmer effect using the transition created above
    */
    val colorShades = listOf(
        MaterialTheme.colorScheme.outline.copy(0.5f),
        MaterialTheme.colorScheme.outline.copy(0.1f),
        MaterialTheme.colorScheme.outline.copy(0.5f),
    )
    val brush = Brush.linearGradient(
        colors = colorShades,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )
    ShimmerLargeItem(brush = brush)
}

@Composable
fun ShimmerAnimation(width: Dp = 150.dp, height: Dp = 150.dp) {
    /*
     Create InfiniteTransition
     which holds child animation like [Transition]
     animations start running as soon as they enter
     the composition and do not stop unless they are removed
    */
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        /*
         Specify animation positions,
         initial Values 0F means it
         starts from 0 position
        */
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            // Tween Animates between values over specified [durationMillis]
            tween(durationMillis = 500/*1200*/, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    /*
      Create a gradient using the list of colors
      Use Linear Gradient for animating in any direction according to requirement
      start=specifies the position to start with in cartesian like system Offset(10f,10f) means x(10,0) , y(0,10)
      end = Animate the end position to give the shimmer effect using the transition created above
    */
    val colorShades = listOf(
        MaterialTheme.colorScheme.outline.copy(0.5f),
        MaterialTheme.colorScheme.outline.copy(0.1f),
        MaterialTheme.colorScheme.outline.copy(0.5f),
    )
    val brush = Brush.linearGradient(
        colors = colorShades,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )
    ShimmerItem(brush = brush, width = width, height = height)
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    brush: Brush,
    width: Dp,
    height: Dp,
) {
    Surface(
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(20.0.dp),
    ) {
        Spacer(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(brush = brush)
        )
    }
}

@Composable
fun ShimmerLargeItem(
    modifier: Modifier = Modifier,
    brush: Brush,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.0.dp))
                .background(brush = brush)
                .weight(3f)
        )
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.0.dp))
                .background(brush = brush)
                .weight(1f)
        )
    }
}

// FIXME: TextField is not implemented yet in material3. This should be replaced by the official implementation when available
@Composable
fun TextFieldMaterial3(modifier: Modifier = Modifier, text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    Surface(
        modifier = modifier
            .padding(horizontal = 25.dp)
            .fillMaxWidth()
            .height(50.dp)
            .fillMaxWidth(),/*.background(Color.Red)*/
        //color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.0.dp),

        ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier.padding(start = 15.dp),
                imageVector = Icons.Filled.Search,
                contentDescription = "Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            )
            //Spacer(Modifier.width(16.dp))
            TextField(
                value = text,
                onValueChange = onValueChange,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,//LocalContentColor.current.copy(LocalContentAlpha.current),
                    //disabledTextColor = textColor.copy(ContentAlpha.disabled),
                    backgroundColor = Color.Transparent,//MaterialTheme.colorScheme.onSurface.copy(alpha = BackgroundOpacity),
                    cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,//MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    focusedIndicatorColor = Color.Transparent,//MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedIndicatorColor = Color.Transparent,//MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity),
                    //disabledIndicatorColor = unfocusedIndicatorColor.copy(alpha = ContentAlpha.disabled),
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                    //disabledLeadingIconColor = leadingIconColor.copy(alpha = ContentAlpha.disabled),
                    //errorLeadingIconColor = leadingIconColor,
                    trailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                    //disabledTrailingIconColor = trailingIconColor.copy(alpha = ContentAlpha.disabled),
                    errorTrailingIconColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                    //disabledLabelColor = unfocusedLabelColor.copy(ContentAlpha.disabled),
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                    //disabledPlaceholderColor = placeholderColor.copy(ContentAlpha.disabled)
                )
            )
        }
    }
}
