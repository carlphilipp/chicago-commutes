package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.startBusMapActivity
import fr.cph.chicago.util.startTrainMapActivity
import timber.log.Timber

@Composable
fun TitleDetailDialog(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrainDetailDialog(
    show: Boolean,
    trainLines: Set<TrainLine>,
    hideDialog: () -> Unit
) {
    if (show) {
        val context = LocalContext.current
        AlertDialog(
            modifier = Modifier.padding(horizontal = 50.dp),
            onDismissRequest = hideDialog,
            // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                TitleDetailDialog(title = stringResource(id = R.string.train_choose_line))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    trainLines.forEachIndexed() { index, trainLine ->
                        val modifier = if (index == trainLines.size - 1) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                        }
                        OutlinedButton(
                            modifier = modifier,
                            onClick = {
                                startTrainMapActivity(context, trainLine)
                                hideDialog()
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 10.dp),
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = trainLine.color,
                            )
                            Text(
                                text = trainLine.toStringWithLine(),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                }
            },
            confirmButton = {},
            dismissButton = {
                FilledTonalButton(onClick = { hideDialog() }) {
                    Text(
                        text = stringResource(id = R.string.dismiss),
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusDetailDialog(show: Boolean, busDetailsDTOs: List<BusDetailsDTO>, hideDialog: () -> Unit) {
    if (show) {
        val navController = LocalNavController.current
        AlertDialog(
            modifier = Modifier.padding(horizontal = 50.dp),
            onDismissRequest = hideDialog,
            // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                TitleDetailDialog(title = stringResource(id = R.string.bus_choose_stop))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    busDetailsDTOs.forEachIndexed() { index, busDetailsDTO ->
                        val modifier = if (index == busDetailsDTOs.size - 1) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                        }
                        OutlinedButton(
                            modifier = modifier,
                            onClick = {
                                navController.navigate(
                                    screen = Screen.BusDetails,
                                    arguments = mapOf(
                                        "busStopId" to busDetailsDTO.stopId.toString(),
                                        "busStopName" to busDetailsDTO.stopName,
                                        "busRouteId" to busDetailsDTO.busRouteId,
                                        "busRouteName" to busDetailsDTO.routeName,
                                        "bound" to busDetailsDTO.bound,
                                        "boundTitle" to busDetailsDTO.boundTitle,
                                    )
                                )
                                hideDialog()
                            },
                        ) {
                            Text(
                                text = "${busDetailsDTO.stopName} (${busDetailsDTO.bound})",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                }
            },
            confirmButton = {},
            dismissButton = {
                FilledTonalButton(onClick = { hideDialog() }) {
                    Text(
                        text = stringResource(id = R.string.dismiss),
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BusRouteDialog(
    search: String,
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
        val navController = LocalNavController.current

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
                TitleDetailDialog(title = "${busRoute.id} - ${busRoute.name}")
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

                                            navController.navigate(
                                                screen = Screen.BusBound,
                                                arguments = mapOf(
                                                    "busRouteId" to busRoute.id,
                                                    "busRouteName" to busRoute.name,
                                                    "bound" to lBusDirections[index].text,
                                                    "boundTitle" to lBusDirections[index].text,
                                                    "search" to search,
                                                ),
                                            )
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
