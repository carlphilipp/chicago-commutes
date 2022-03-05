package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusBoundActivity
import fr.cph.chicago.core.composable.common.AnimatedErrorView
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.composable.viewmodel.MainViewModel
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.startBusMapActivity
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Bus(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {

    var showDialog by remember { mutableStateOf(false) }
    var selectedBusRoute by remember { mutableStateOf(BusRoute.buildEmpty()) }
    var searchBusRoutes by remember { mutableStateOf<List<BusRoute>>(listOf()) }
    searchBusRoutes = mainViewModel.uiState.busRoutes
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            if (mainViewModel.uiState.busRoutes.isNotEmpty()) {
                Column {
                    TextFieldMaterial3(
                        modifier = Modifier.fillMaxWidth(),
                        text = textSearch,
                        onValueChange = { value ->
                            textSearch = value
                            searchBusRoutes = mainViewModel.uiState.busRoutes.filter { busRoute ->
                                busRoute.id.contains(value.text, true) || busRoute.name.contains(value.text, true)
                            }
                        }
                    )
                    LazyColumn(
                        modifier = modifier.fillMaxSize()
                    ) {
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
                                        text = busRoute.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                AnimatedErrorView(
                    onClick = {
                        mainViewModel.loadBusRoutes()
                    }
                )
                if (mainViewModel.uiState.busRoutesShowError) {
                    ShowErrorMessageSnackBar(
                        scope = scope,
                        snackbarHostState = mainViewModel.uiState.snackbarHostState,
                        showError = mainViewModel.uiState.busRoutesShowError,
                        onComplete = {
                            mainViewModel.resetBusRoutesShowError()
                        }
                    )
                }
            }

            BusRouteDialog(
                showDialog = showDialog,
                busRoute = selectedBusRoute,
                hideDialog = { showDialog = false },
            )
        })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusRouteDialog(
    showDialog: Boolean,
    busService: BusService = BusService,
    busRoute: BusRoute,
    hideDialog: () -> Unit
) {
    if (showDialog) {
        var isLoading by remember { mutableStateOf(true) }
        var foundBusDirections by remember { mutableStateOf(BusDirections("")) }
        var showDialogError by remember { mutableStateOf(false) }
        val context = LocalContext.current

        busService.loadBusDirectionsSingle(busRoute.id)
            .subscribe(
                { busDirections ->
                    foundBusDirections = busDirections
                    isLoading = false
                    showDialogError = false
                },
                { error ->
                    Timber.e(error, "Could not load bus directions")
                    isLoading = false
                    showDialogError = true
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
                    when {
                        isLoading -> {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 15.dp),
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        showDialogError -> {
                            Text(
                                modifier = Modifier.padding(bottom = 15.dp),
                                text = "Could not load directions!"
                            )
                        }
                        else -> {
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
                                            val intent = Intent(context, BusBoundActivity::class.java)
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
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            startBusMapActivity(context = context, busDirections = foundBusDirections)
                            hideDialog()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 5.dp)
                        )
                        Text(
                            text = stringResource(R.string.bus_see_all),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
        )
    }
}
