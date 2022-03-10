package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.cph.chicago.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SnackbarHostInsets(state: SnackbarHostState) {
    SnackbarHost(hostState = state) { data ->
        Snackbar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
            snackbarData = data,
        )
    }
}

@Composable
fun ShowFavoriteSnackBar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    isFavorite: Boolean,
    onComplete: () -> Unit,
) {
    ShowSnackBar(
        scope = scope,
        snackbarHostState = snackbarHostState,
        element = isFavorite,
        message = if (isFavorite) stringResource(id = R.string.message_add_fav) else stringResource(id = R.string.message_remove_fav),
        onComplete = onComplete,
    )
}

@Composable
fun ShowLocationNotFoundSnackBar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    showErrorMessage: Boolean,
    onComplete: () -> Unit,
) {
    ShowErrorMessageSnackBar(
        scope = scope,
        snackbarHostState = snackbarHostState,
        showError = showErrorMessage,
        message = stringResource(id = R.string.error_location),
        onComplete = onComplete,
    )
}

@Composable
fun ShowErrorMessageSnackBar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    showError: Boolean,
    message: String = stringResource(id = R.string.error_message),
    onComplete: () -> Unit,
) {
    ShowSnackBar(
        scope = scope,
        snackbarHostState = snackbarHostState,
        element = showError,
        message = message,
        onComplete = onComplete,
    )
}

@Composable
fun ShowSnackBar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    element: Boolean,
    message: String,
    onComplete: () -> Unit,
) {
    LaunchedEffect(element) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message, withDismissAction = true)
            onComplete()
        }
    }
}
