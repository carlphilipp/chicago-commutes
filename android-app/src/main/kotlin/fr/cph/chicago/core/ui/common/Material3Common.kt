package fr.cph.chicago.core.ui.common

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val BoundPadding = 4.dp
private val ThumbRippleRadius = 24.dp
private val TrackStrokeWidth = 30.dp
private val TrackWidth = 55.dp
private val ThumbDiameter = 21.dp
private val ThumbPathLength = TrackWidth - ThumbDiameter - BoundPadding
private val DefaultSwitchPadding = 2.dp
private val SwitchWidth = TrackWidth
private val SwitchHeight = ThumbDiameter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchMaterial3(
    modifier: Modifier = Modifier,
    checked: Boolean = true,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.primary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedTrackAlpha = 0.54f,
        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface,
        uncheckedTrackAlpha = 0.38f,
        disabledCheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = ContentAlpha.disabled)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledCheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = ContentAlpha.disabled)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = ContentAlpha.disabled)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = ContentAlpha.disabled)
            .compositeOver(MaterialTheme.colorScheme.surface)
    )
) {
    val minBound = with(LocalDensity.current) { BoundPadding.toPx() }
    val maxBound = with(LocalDensity.current) { ThumbPathLength.toPx() }
    val swipeableState = rememberSwipeableStateFor(checked, onCheckedChange ?: {}, TweenSpec(durationMillis = 100))
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val toggleableModifier =
        if (onCheckedChange != null) {
            Modifier.toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
        } else {
            Modifier
        }

    Box(
        modifier
            .then(Modifier)
            .then(toggleableModifier)
            .swipeable(
                state = swipeableState,
                anchors = mapOf(minBound to false, maxBound to true),
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Horizontal,
                enabled = enabled && onCheckedChange != null,
                reverseDirection = isRtl,
                interactionSource = interactionSource,
                resistance = null
            )
            .wrapContentSize(Alignment.Center)
            .padding(DefaultSwitchPadding)
            .requiredSize(SwitchWidth, SwitchHeight)
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val hasInteraction = interactions.isNotEmpty()
        val elevation = if (hasInteraction) {
            6.dp
        } else {
            1.dp
        }
        val trackColor by colors.trackColor(enabled, checked)
        Canvas(
            Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        ) {
            drawTrack(trackColor, TrackWidth.toPx(), TrackStrokeWidth.toPx())
        }
        val thumbColor by colors.thumbColor(enabled, checked)
        val elevationOverlay = LocalElevationOverlay.current
        val absoluteElevation = LocalAbsoluteElevation.current + elevation
        val resolvedThumbColor =
            if (thumbColor == androidx.compose.material.MaterialTheme.colors.surface && elevationOverlay != null) {
                elevationOverlay.apply(thumbColor, absoluteElevation)
            } else {
                thumbColor
            }
        Spacer(
            Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = false, radius = ThumbRippleRadius)
                )
                .requiredSize(ThumbDiameter)
                .shadow(elevation, CircleShape, clip = false)
                .background(resolvedThumbColor, CircleShape)
        )
    }
}


private fun DrawScope.drawTrack(trackColor: Color, trackWidth: Float, strokeWidth: Float) {
    val strokeRadius = strokeWidth / 2
    drawLine(
        trackColor,
        Offset(strokeRadius, center.y),
        Offset(trackWidth - strokeRadius, center.y),
        strokeWidth,
        StrokeCap.Round
    )
}

@Composable
@ExperimentalMaterialApi
private fun <T : Any> rememberSwipeableStateFor(
    value: T,
    onValueChange: (T) -> Unit,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec
): SwipeableState<T> {
    val swipeableState = remember {
        SwipeableState(
            initialValue = value,
            animationSpec = animationSpec,
            confirmStateChange = { true }
        )
    }
    val forceAnimationCheck = remember { mutableStateOf(false) }
    LaunchedEffect(value, forceAnimationCheck.value) {
        if (value != swipeableState.currentValue) {
            swipeableState.animateTo(value)
        }
    }
    DisposableEffect(swipeableState.currentValue) {
        if (value != swipeableState.currentValue) {
            onValueChange(swipeableState.currentValue)
            forceAnimationCheck.value = !forceAnimationCheck.value
        }
        onDispose { }
    }
    return swipeableState
}

// FIXME: this does not exist yet in material3
@Composable
fun ChipMaterial3(
    modifier: Modifier = Modifier,
    text: String = "Filter",
    isSelected: Boolean = true,
    onClick: () -> Unit = {},
) {
    val backgroundColor: Color
    val border: BorderStroke
    if (isSelected) {
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer)
    } else {
        backgroundColor = Color.Transparent
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
    }
    Surface(
        modifier = modifier,
        border = border,
        shape = RoundedCornerShape(8.0.dp),
    ) {
        Row(modifier = Modifier
            .clickable { onClick() }
            .background(backgroundColor)) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(end = 5.dp),
                    imageVector = Icons.Filled.Check,
                    contentDescription = "ticked",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
