package fr.cph.chicago.core.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.core.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RateScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    var firstRun by remember { mutableStateOf(true) }

    if (firstRun) {
        mainViewModel.startMarket(context)
        firstRun = false
    }

    if (mainViewModel.uiState.startMarketFailed) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 50.dp),
            onDismissRequest = {
                firstRun = false
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
                    firstRun = false
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
