package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.service.BusService

private val busService = BusService

@Composable
fun Bus(modifier: Modifier = Modifier, busRoutes: List<BusRoute>) {

    var showDialog by remember { mutableStateOf(false) }
    var selectedBusRoute by remember { mutableStateOf(BusRoute("", "")) }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            // Search
        }
        items(busRoutes) { busRoute ->
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onClick = {
                    showDialog = true
                    selectedBusRoute = busRoute
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.padding(end = 20.dp),
                        text = busRoute.id,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                    )
                    Text(
                        busRoute.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                }
            }
        }
    }
    BusRouteDialog(
        show = showDialog,
        busRoute = selectedBusRoute,
        hideDialog = { showDialog = false },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusRouteDialog(show: Boolean, busRoute: BusRoute, hideDialog: () -> Unit) {
    if (show) {
        var isLoading by remember { mutableStateOf(true) }
        var foundBusDirections by remember { mutableStateOf(BusDirections("")) }

        busService.loadBusDirectionsSingle(busRoute.id)
            .subscribe(
                { busDirections ->
                    foundBusDirections = busDirections
                    isLoading = false
                },
                { error ->
                    // TODO: handle error
                    isLoading = false
                }
            )

        AlertDialog(
            onDismissRequest = hideDialog,
            // FIXME workaround because the dialog do not resize after loading. This property makes it work but it also do a weird animation
            // Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Text(text = "Route: ${busRoute.id}")
            },
            text = {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CircularProgressIndicator()
                    }
                    /*Column {
                        for (i in 0..5) {
                            TextButton(onClick = { *//*TODO*//* }) {
                                Text(
                                    text = "$i"
                                )
                            }
                        }
                    }*/
                } else {
                    Column {
                        foundBusDirections.busDirections.forEach {
                            TextButton(onClick = { /*TODO*/ }) {
                                Text(
                                    text = it.text
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
            },
            dismissButton = {
                OutlinedButton(onClick = hideDialog) {
                    Text("Dismiss")
                }
            }
        )
    }
}
