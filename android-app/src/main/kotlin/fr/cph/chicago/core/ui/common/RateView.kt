package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.core.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RateView(
    startMarket: Boolean,
    mainViewModel: MainViewModel,
    onComplete: () -> Unit,
) {
    if (startMarket) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = startMarket, block = {
            scope.launch {
                if (startMarket) {
                    mainViewModel.startMarket(context)
                }
                onComplete()
            }
        })
    }

    if (mainViewModel.uiState.startMarketFailed) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 50.dp),
            onDismissRequest = {
                mainViewModel.resetRateMeFailed()
            },
            // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            text = {
                Text(
                    text = "Play Store not found on your device"
                )
            },
            confirmButton = {
                FilledTonalButton(onClick = {
                    // start = false
                    mainViewModel.resetRateMeFailed()
                }) {
                    Text(
                        text = "Ok",
                    )
                }
            },
            dismissButton = {},
        )
    }
}
