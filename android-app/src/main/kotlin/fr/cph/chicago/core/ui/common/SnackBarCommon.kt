package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SnackbarHostInsets(state: SnackbarHostState) {
    SnackbarHost(hostState = state) { data ->
        val actionLabel = data.visuals.actionLabel
        val actionComposable: (@Composable () -> Unit)? = if (actionLabel != null) {
            @Composable {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    onClick = { data.performAction() },
                    content = { Text(actionLabel) }
                )
            }
        } else {
            null
        }
        val dismissActionComposable: (@Composable () -> Unit)? =
            if (data.visuals.withDismissAction) {
                @Composable {
                    IconButton(
                        onClick = { data.dismiss() },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    )
                }
            } else {
                null
            }
        val modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
        Snackbar(
            modifier = modifier.padding(12.dp),
            action = actionComposable,
            dismissAction = dismissActionComposable,
            actionOnNewLine = false,
            shape = RoundedCornerShape(6.0.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            content = { Text(data.visuals.message) }
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
    withDismissAction: Boolean = true,
) {
    LaunchedEffect(element) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = withDismissAction
            )
            onComplete()
        }
    }
}
