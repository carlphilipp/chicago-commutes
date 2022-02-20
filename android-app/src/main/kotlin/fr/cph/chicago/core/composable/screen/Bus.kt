package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.composable.BusBoundActivityComposable
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.service.BusService

private val busService = BusService

@Composable
fun Bus(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {

    var showDialog by remember { mutableStateOf(false) }
    var selectedBusRoute by remember { mutableStateOf(BusRoute.buildEmpty()) }
    var searchBusRoutes by remember { mutableStateOf(mainViewModel.uiState.busRoutes) }
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            TextFieldMaterial3(
                text = textSearch,
                onValueChange = { value ->
                    textSearch = value
                    searchBusRoutes = mainViewModel.uiState.busRoutes.filter { busRoute ->
                        busRoute.id.contains(value.text, true) || busRoute.name.contains(value.text, true)
                    }
                }
            )
        }
        items(searchBusRoutes) { busRoute ->
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
                        modifier = Modifier.requiredWidth(50.dp),
                        text = busRoute.id,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                    Text(
                        busRoute.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    if (showDialog) {
        BusRouteDialog(
            busRoute = selectedBusRoute,
            hideDialog = { showDialog = false },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusRouteDialog(busRoute: BusRoute, hideDialog: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var foundBusDirections by remember { mutableStateOf(BusDirections("")) }
    val context = LocalContext.current

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
        modifier = Modifier.padding(horizontal = 50.dp),
        onDismissRequest = hideDialog,
        // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = "${busRoute.id} - ${busRoute.name}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        foundBusDirections.busDirections.forEachIndexed { index, busDirection ->
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val lBusDirections = foundBusDirections.busDirections
                                    val extras = Bundle()
                                    val intent = Intent(context, BusBoundActivityComposable::class.java)
                                    extras.putString(context.getString(R.string.bundle_bus_route_id), busRoute.id)
                                    extras.putString(context.getString(R.string.bundle_bus_route_name), busRoute.name)
                                    extras.putString(context.getString(R.string.bundle_bus_bound), lBusDirections[index].text)
                                    extras.putString(context.getString(R.string.bundle_bus_bound_title), lBusDirections[index].text)
                                    intent.putExtras(extras)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(context, intent, null)
                                    hideDialog()
                                },
                            ) {
                                Text(
                                    text = busDirection.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val extras = Bundle()
                        val lBusDirections = foundBusDirections.busDirections
                        val busDirectionArray = arrayOfNulls<String>(lBusDirections.size)
                        var i = 0
                        for (busDir in lBusDirections) {
                            busDirectionArray[i++] = busDir.text
                        }
                        // FIXME: create new activity with correct theme
                        val intent = Intent(context, BusMapActivity::class.java)
                        extras.putString(context.getString(R.string.bundle_bus_route_id), foundBusDirections.id)
                        extras.putStringArray(context.getString(R.string.bundle_bus_bounds), busDirectionArray)
                        intent.putExtras(extras)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(context, intent, null)
                        hideDialog()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Map",
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(
                        text = "See all buses on line ${busRoute.id}", // FIXME: Add icon
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}
