package fr.cph.chicago.core.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import fr.cph.chicago.core.ui.screen.Screen
import timber.log.Timber

@OptIn(ExperimentalAnimationApi::class)
fun fallBackEnterTransition(): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition) {
    return {
        fadeIn()
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun fallBackExitTransition(): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition) {
    return {
        fadeOut(
            animationSpec = tween(durationMillis = 100),
            targetAlpha = 0f,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun enterTransition(): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) {
    return {

        val slideInFromRight = slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(durationMillis = 200))

        val origin = initialState.destination.route
        val destination = targetState.destination.route

        when (destination) {
            Screen.TrainDetails.route, Screen.BusDetails.route, Screen.DivvyDetails.route -> {
                Timber.i("Animation enterTransition targetState.destination $destination SLIDE IN LEFT")
                slideInFromRight
            }
            Screen.TrainList.route -> {
                when (origin) {
                    Screen.TrainDetails.route -> {
                        fadeIn(animationSpec = tween(delayMillis = 100))
                    }
                    else -> {
                        Timber.i("Animation enterTransition targetState.destination $destination SLIDE IN LEFT")
                        slideInFromRight
                    }

                }
            }
            Screen.BusBound.route -> {
                when (destination) {
                    Screen.BusDetails.route -> fadeIn(animationSpec = tween(delayMillis = 100))
                    else -> {
                        slideInFromRight
                    }
                }
            }
            Screen.SettingsDisplay.route, Screen.SettingsThemeColorChooser.route, Screen.DeveloperOptions.route -> {
                scaleIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        delayMillis = 0,
                        easing = FastOutSlowInEasing
                    ),
                    initialScale = 0.8f,
                    transformOrigin = TransformOrigin.Center,
                )
            }
            else -> {
                Timber.i("Animation enterTransition targetState.destination ${destination} NO ANIMATION")
                null
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun exitTransition(): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) {
    return {
        val slideOutToRight = slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(200))

        val origin = initialState.destination.route
        val destination = targetState.destination.route
        when (origin) {
            Screen.TrainDetails.route, Screen.BusDetails.route, Screen.DivvyDetails.route -> {
                Timber.i("Animation exitTransition initialState.destination $origin SLIDE IN RIGHT")
                slideOutToRight
            }
            Screen.TrainList.route, Screen.BusBound.route, Screen.AlertDetail.route -> {
                slideOutToRight
            }
            Screen.Settings.route -> {
                Timber.i("Animation exitTransition initialState.destination $origin ExitTransition.None")
                ExitTransition.None
            }
            else -> {
                Timber.i("Animation exitTransition initialState.destination $origin NO ANIMATION")
                null//ExitTransition.None
            }
        }
    }
}
