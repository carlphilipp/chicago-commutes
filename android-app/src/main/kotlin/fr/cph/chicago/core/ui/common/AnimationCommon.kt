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
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import fr.cph.chicago.core.ui.screen.Screen

private const val drawerSlideDuration: Int = 400
private const val slideDuration: Int = 800
private const val fadeDuration: Int = 100
private const val scaleDuration: Int = 100
private const val settingsScaleDuration: Int = 50

sealed class AnimationSpeed(
    val name: String,
    val drawerSlideDuration: Int,
    val slideDuration: Int,
    val fadeDuration: Int,
    val scaleDuration: Int,
    val settingsScaleDuration: Int,
) {
    object Normal : AnimationSpeed(
        name = "Normal",
        drawerSlideDuration = drawerSlideDuration,
        slideDuration = slideDuration,
        fadeDuration = fadeDuration,
        scaleDuration = scaleDuration,
        settingsScaleDuration = settingsScaleDuration,
    )

    object Slow : AnimationSpeed(
        name = "Slow",
        drawerSlideDuration = drawerSlideDuration * 2,
        slideDuration = slideDuration * 2,
        fadeDuration = fadeDuration * 2,
        scaleDuration = scaleDuration * 2,
        settingsScaleDuration = settingsScaleDuration * 2,
    )

    object Fast : AnimationSpeed(
        name = "Fast",
        drawerSlideDuration = drawerSlideDuration / 2,
        slideDuration = slideDuration / 2,
        fadeDuration = fadeDuration / 2,
        scaleDuration = scaleDuration / 2,
        settingsScaleDuration = settingsScaleDuration / 2,
    )

    companion object {
        fun allAnimationsSpeed(): List<AnimationSpeed> {
            return listOf(Slow, Normal, Fast)
        }

        fun fromString(str: String): AnimationSpeed {
            return when (str) {
                "Slow" -> Slow
                "Fast" -> Fast
                else -> Normal
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun fallBackEnterTransition(): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition) {
    return {
        fadeIn()
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun fallBackExitTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition) {
    return {
        fadeOut(
            animationSpec = tween(durationMillis = animationSpeed.fadeDuration),
            targetAlpha = 0f,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun enterTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) {
    return {
        val slideInFromRight = slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(durationMillis = animationSpeed.slideDuration))
        val scaleInSettings = scaleIn(
            animationSpec = tween(
                durationMillis = animationSpeed.scaleDuration,
                delayMillis = 0,
                easing = FastOutSlowInEasing
            ),
            initialScale = 0.9f,
            transformOrigin = TransformOrigin.Center,
        )

        val origin = initialState.destination.route
        val destination = targetState.destination.route

        when (destination) {
            Screen.TrainDetails.route, Screen.BusDetails.route, Screen.DivvyDetails.route -> {
                slideInFromRight
            }
            Screen.TrainList.route -> {
                when (origin) {
                    Screen.TrainDetails.route -> {
                        fadeIn(animationSpec = tween(delayMillis = animationSpeed.fadeDuration))
                    }
                    else -> {
                        slideInFromRight
                    }

                }
            }
            Screen.BusBound.route -> {
                when (origin) {
                    Screen.BusDetails.route -> fadeIn(animationSpec = tween(delayMillis = animationSpeed.fadeDuration))
                    else -> {
                        slideInFromRight
                    }
                }
            }
            Screen.SettingsThemeColorChooser.route, Screen.DeveloperOptions.route -> {
                scaleInSettings
            }
            Screen.Settings.route -> {
                when (origin) {
                    Screen.SettingsDisplay.route, Screen.DeveloperOptions.route -> {// FIXME: Add about here when it exists
                        EnterTransition.None
                    }
                    else -> null
                }
            }
            Screen.SettingsDisplay.route -> {
                when (origin) {
                    Screen.SettingsThemeColorChooser.route -> {
                        EnterTransition.None
                    }
                    else -> scaleInSettings
                }
            }
            else -> {
                null
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun exitTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) {
    return {
        val slideOutToRight = slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(animationSpeed.slideDuration))

        val origin = initialState.destination.route
        val destination = targetState.destination.route
        when (origin) {
            Screen.TrainDetails.route, Screen.BusDetails.route, Screen.DivvyDetails.route -> {
                slideOutToRight
            }
            Screen.TrainList.route, Screen.BusBound.route, Screen.AlertDetail.route -> {
                slideOutToRight
            }
            Screen.SettingsDisplay.route, Screen.SettingsThemeColorChooser.route, Screen.DeveloperOptions.route -> {
                scaleOut(
                    animationSpec = tween(
                        durationMillis = animationSpeed.settingsScaleDuration,
                        delayMillis = 0,
                        easing = FastOutSlowInEasing
                    ),
                    targetScale = 0.0f,
                    transformOrigin = TransformOrigin.Center,
                )
            }
            else -> null
        }
    }
}
