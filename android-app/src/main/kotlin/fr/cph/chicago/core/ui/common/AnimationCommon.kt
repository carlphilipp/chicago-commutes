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
import timber.log.Timber

private const val openDrawerSlideDuration: Int = 400
private const val closeDrawerSlideDuration: Int = 200
private const val slideDuration: Int = 800
private const val fadeDuration: Int = 600
private const val scaleDuration: Int = 600

sealed class AnimationSpeed(
    val name: String,
    val openDrawerSlideDuration: Int,
    val closeDrawerSlideDuration: Int,
    val slideDuration: Int,
    val fadeDuration: Int,
    val scaleDuration: Int,
) {
    object Normal : AnimationSpeed(
        name = "Normal",
        openDrawerSlideDuration = openDrawerSlideDuration,
        closeDrawerSlideDuration = closeDrawerSlideDuration,
        slideDuration = slideDuration,
        fadeDuration = fadeDuration,
        scaleDuration = scaleDuration,
    )

    object Slow : AnimationSpeed(
        name = "Slow",
        openDrawerSlideDuration = openDrawerSlideDuration * 2,
        closeDrawerSlideDuration = closeDrawerSlideDuration * 2,
        slideDuration = slideDuration * 2,
        fadeDuration = fadeDuration * 2,
        scaleDuration = scaleDuration * 2,
    )

    object Fast : AnimationSpeed(
        name = "Fast",
        openDrawerSlideDuration = openDrawerSlideDuration / 2,
        closeDrawerSlideDuration = closeDrawerSlideDuration / 2,
        slideDuration = slideDuration / 2,
        fadeDuration = fadeDuration / 2,
        scaleDuration = scaleDuration / 2,
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
fun fallBackEnterTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition) {
    return {
        fadeIn(animationSpec = tween(durationMillis = animationSpeed.fadeDuration))
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun fallBackExitTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition) {
    return {
        fadeOut(animationSpec = tween(durationMillis = animationSpeed.fadeDuration), targetAlpha = 0f)
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun enterTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) {
    return {
        val slideInFromRight = slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(durationMillis = animationSpeed.slideDuration))
        val scaleIn = scaleIn(animationSpec = tween(durationMillis = animationSpeed.scaleDuration), initialScale = 0.9f)

        val origin = initialState.destination.route
        val destination = targetState.destination.route
        Timber.v("EnterTransition Origin: $origin - Destination: $destination")
        when {
            // Fav -> Details
            origin == Screen.Favorites.route && (destination == Screen.TrainDetails.route || destination == Screen.BusDetails.route || destination == Screen.DivvyDetails.route) -> {
                slideInFromRight
            }
            // Train -> TrainList
            origin == Screen.Train.route && destination == Screen.TrainList.route -> {
                slideInFromRight
            }
            // TrainList -> TrainDetails
            origin == Screen.TrainList.route && destination == Screen.TrainDetails.route -> {
                slideInFromRight
            }
            // Bus -> BusBound
            origin == Screen.Bus.route && destination == Screen.BusBound.route -> {
                slideInFromRight
            }
            // BusBound -> BusDetails
            origin == Screen.BusBound.route && destination == Screen.BusDetails.route -> {
                slideInFromRight
            }
            // Divvy -> DivvyDetails
            origin == Screen.Divvy.route && destination == Screen.DivvyDetails.route -> {
                slideInFromRight
            }
            // Search
            destination == Screen.Search.route -> {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(durationMillis = animationSpeed.slideDuration))
            }
            // Settings -> Display/Developer
            origin == Screen.Settings.route && (destination == Screen.SettingsDisplay.route || destination == Screen.DeveloperOptions.route) -> {
                scaleIn
            }
            // Display -> ThemeChooser
            origin == Screen.SettingsDisplay.route && destination == Screen.SettingsThemeColorChooser.route -> {
                scaleIn
            }
            else -> null
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun exitTransition(animationSpeed: AnimationSpeed): (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) {
    return {
        val slideOutToRight = slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(durationMillis = animationSpeed.slideDuration))
        val scaleOut = scaleOut(
            animationSpec = tween(
                durationMillis = 100,
                delayMillis = 0,
                easing = FastOutSlowInEasing
            ),
            targetScale = 0.0f,
            transformOrigin = TransformOrigin.Center,
        )

        val origin = initialState.destination.route
        val destination = targetState.destination.route
        Timber.v("ExitTransition Origin: $origin - Destination: $destination")
        when {
            // Details -> Fav
            (origin == Screen.TrainDetails.route || origin == Screen.BusDetails.route || origin == Screen.DivvyDetails.route) && destination == Screen.Favorites.route -> {
                slideOutToRight
            }
            // TrainList -> Train
            origin == Screen.TrainList.route && destination == Screen.Train.route -> {
                slideOutToRight
            }
            // TrainDetails -> TrainList
            origin == Screen.TrainDetails.route && destination == Screen.TrainList.route -> {
                slideOutToRight
            }
            // BusBound -> Bus
            origin == Screen.BusBound.route && destination == Screen.Bus.route -> {
                slideOutToRight
            }
            // BusDetails -> BusBound
            origin == Screen.BusDetails.route && destination == Screen.BusBound.route -> {
                slideOutToRight
            }
            // DivvyDetails -> Divvy
            origin == Screen.DivvyDetails.route && destination == Screen.Divvy.route -> {
                slideOutToRight
            }
            // Search
            origin == Screen.Search.route -> {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(durationMillis = animationSpeed.slideDuration))
            }
            // Display -> Settings
            origin == Screen.SettingsDisplay.route && destination == Screen.Settings.route -> {
                scaleOut
            }
            // ThemeChooser -> Display
            origin == Screen.SettingsThemeColorChooser.route && destination == Screen.SettingsDisplay.route -> {
                scaleOut
            }
            // Developer -> Settings
            origin == Screen.DeveloperOptions.route && destination == Screen.Settings.route -> {
                scaleOut
            }
            else -> null
        }
    }
}
