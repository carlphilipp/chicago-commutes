package fr.cph.chicago.core.ui.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.theme.favorite_yellow
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val googleStreetClient = GoogleStreetClient

fun runWithDelay(delay: Long, timeUnit: TimeUnit, disableLoading: () -> Unit) {
    Single.timer(delay, timeUnit)
        .subscribeOn(Schedulers.computation())
        .subscribe({ disableLoading() }) {}
}

fun openExternalMapApplication(context: Context, scope: CoroutineScope, snackbarHostState: SnackbarHostState, latitude: Double, longitude: Double) {
    val uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        scope.launch {
            snackbarHostState.showSnackbar("Could not find any Map application on device")
        }
    }
}

fun loadGoogleStreet(position: Position, onSuccess: Consumer<Bitmap>, onError: Consumer<Throwable>) {
    googleStreetClient.getImage(position.latitude, position.longitude, 1000, 400)
        .observeOn(Schedulers.computation())
        .subscribe(onSuccess, onError)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BackHandler(content: @Composable () -> Unit) {
    val navController = LocalNavController.current
    val keyboardController = LocalSoftwareKeyboardController.current

    content()

    androidx.activity.compose.BackHandler {
        keyboardController?.hide()
        navController.navigateBack()
    }
}

/**
 * This only works if the insets library is in the classpath and if the component that render this composable is wrapped
 * by ProvideWindowInsets
 */
@Composable
fun NavigationBarsSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    )
}

@Composable
fun ColoredBox(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.tertiary) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    Row(modifier = modifier) {
        Surface(color = Color.Transparent) {
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    run {
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
                    color = color,
                )
            }
        }
    }
}

@Composable
fun AnimatedPlaceHolderList(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    size: Int = 10,
) {
    val brush = buildBrush()
    Column(modifier = modifier) {
        for (i in 1..size) {
            AnimatedVisibility(
                modifier = Modifier.height(80.dp),
                visible = isLoading,
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.0.dp))
                        .background(brush = brush)
                        .weight(3f)
                )
            }
        }
    }
}

@Composable
fun LargeImagePlaceHolderAnimated() {
    ShimmerLargeItem(brush = buildBrush())
}

@Composable
fun ShimmerAnimation(width: Dp = 150.dp, height: Dp = 150.dp) {
    ShimmerItem(brush = buildBrush(), width = width, height = height)
}

@Composable
private fun buildBrush(): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    val colorShades = listOf(
        MaterialTheme.colorScheme.outline.copy(0.5f),
        MaterialTheme.colorScheme.outline.copy(0.1f),
        MaterialTheme.colorScheme.outline.copy(0.5f),
    )

    return Brush.linearGradient(
        colors = colorShades,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )
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

@Composable
fun StationDetailsImageView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    showGoogleStreetImage: Boolean,
    googleStreetMapImage: Bitmap,
    scrollState: ScrollState? = null,
) {
    val navController = LocalNavController.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val height = (1f / 3f * screenHeight).dp

    Surface(modifier = modifier.zIndex(1f)) {
        AnimatedVisibility(
            modifier = Modifier
                .height(height)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                ),
            visible = isLoading,
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
        ) {
            LargeImagePlaceHolderAnimated()
        }
        AnimatedVisibility(
            modifier = Modifier.height(height),
            visible = !isLoading && showGoogleStreetImage,
            enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
        ) {
            val imageModifier = if (scrollState != null) {
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = min(1f, 1 - (scrollState.value / 600f))
                        translationY = -scrollState.value * 0.1f
                    }
            } else {
                Modifier.fillMaxWidth()
            }
            Image(
                bitmap = googleStreetMapImage.asImageBitmap(),
                contentDescription = "Google image street view",
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        }
        AnimatedVisibility(
            modifier = Modifier.height(height),
            visible = !isLoading && !showGoogleStreetImage,
            enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
        ) {
            val imageModifier = if (scrollState != null) {
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = min(1f, 1 - (scrollState.value / 600f))
                        translationY = -scrollState.value * 0.1f
                    }
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    )
            } else {
                Modifier.fillMaxWidth()
            }
            Image(
                painter = painterResource(R.drawable.placeholder_street_view),
                contentDescription = "Place holder",
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        }

        FilledTonalButton(
            modifier = Modifier
                .padding(start = 10.dp, top = 5.dp)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                ),
            onClick = { navController.navigateBack() },
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }
    }
}

@Composable
fun StationDetailsTitleIconView(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String = "",
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .zIndex(5f)
            .fillMaxWidth()
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                if (subTitle != "") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                // FIXME:
                // See to implement that or find something with some ui animation
/*                var chec = remember { mutableStateOf(true) }
                IconToggleButton(checked = chec.value, onCheckedChange = {
                    chec.value = !chec.value
                }) {
                    val tint by animateColorAsState(if (chec.value) Color(0xFFEC407A) else Color(0xFFB0BEC5))
                    Icon(Icons.Filled.Favorite, contentDescription = "Localized description", tint = tint)
                }*/
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) favorite_yellow else LocalContentColor.current,
                    )
                }
                IconButton(onClick = onMapClick) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Map",
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedErrorView(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: () -> Unit,
    text: String = "Something went wrong, try again",
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        ErrorView(
            modifier = modifier,
            onClick = onClick,
            text = text,
        )
    }
}

@Composable
private fun ErrorView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.padding(top = 40.dp, bottom = 25.dp),
            painter = painterResource(R.drawable.error),
            contentDescription = "Error image"
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.padding(10.dp))
        FilledTonalButton(onClick = onClick) {
            Text(
                text = stringResource(id = R.string.error_button)
            )
        }
    }
}

// Taken from: https://github.com/umutsoysl/ComposeZoomableImage
// Jitpack build was not working
@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    maxScale: Float = .30f,
    minScale: Float = 3f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false,
    isZoomable: Boolean = true
) {
    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(1f) }
    val offsetY = remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                if (isZoomable) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                scale.value *= event.calculateZoom()
                                if (scale.value > 1) {
                                    val offset = event.calculatePan()
                                    offsetX.value += offset.x
                                    offsetY.value += offset.y
                                    rotationState.value += event.calculateRotation()
                                } else {
                                    scale.value = 1f
                                    offsetX.value = 1f
                                    offsetY.value = 1f
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                }
            }

    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    if (isZoomable) {
                        scaleX = maxOf(maxScale, minOf(minScale, scale.value))
                        scaleY = maxOf(maxScale, minOf(minScale, scale.value))
                        if (isRotation) {
                            rotationZ = rotationState.value
                        }
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
                }
        )
    }
}

@Composable
fun LoadingBar(
    modifier: Modifier = Modifier,
    show: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    if (show) {
        LinearProgressIndicator(
            modifier = modifier.fillMaxWidth(),
            color = color,
        )
    }
}

@Composable
fun LoadingCircle(
    modifier: Modifier = Modifier,
    show: Boolean
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxSize(),
        visible = show,
        enter = EnterTransition.None,
        exit = fadeOut()
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .wrapContentSize()
        )
    }
}

@Composable
fun ThemeColorButton(
    color: Color,
    enabled: Boolean,
    alpha: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (isSelected) {
        ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    } else {
        ButtonDefaults.elevatedButtonColors()
    }
    ElevatedButton(
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
            .alpha(alpha),
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(12.0.dp),
        contentPadding = PaddingValues(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color)
                .alpha(alpha),
        ) {
            if (isSelected) {
                Icon(
                    modifier = Modifier.align(alignment = Alignment.Center),
                    imageVector = Icons.Filled.Check,
                    contentDescription = "ticked",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SwipeRefreshThemed(
    modifier: Modifier = Modifier,
    swipeRefreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit,
) {
    SwipeRefresh(
        modifier = modifier,
        state = swipeRefreshState,
        onRefresh = onRefresh,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = content
    )
}

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
        value = text,
        singleLine = true,
        leadingIcon = {
            Image(
                modifier = Modifier.padding(start = 15.dp),
                imageVector = Icons.Filled.Search,
                contentDescription = "Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            )
        },
        onValueChange = onValueChange,
        shape = RoundedCornerShape(28.0.dp),
        colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}
