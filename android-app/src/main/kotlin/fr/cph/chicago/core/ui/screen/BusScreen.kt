package fr.cph.chicago.core.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.BusRouteDialog
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.viewmodel.MainViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScreen(
    modifier: Modifier = Modifier,
    title: String,
    mainViewModel: MainViewModel,
    navigationViewModel: NavigationViewModel
) {
    Timber.d("Compose BusScreen")
    var showDialog by remember { mutableStateOf(false) }
    var selectedBusRoute by remember { mutableStateOf(BusRoute.buildEmpty()) }
    var searchBusRoutes by remember { mutableStateOf<List<BusRoute>>(listOf()) }
    searchBusRoutes = mainViewModel.uiState.busRoutes
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHostInsets(state = mainViewModel.uiState.snackbarHostState) },
        content = {
            Column {
                DisplayTopBar(
                    title = title,
                    viewModel = navigationViewModel,
                )
                if (mainViewModel.uiState.busRoutes.isNotEmpty()) {
                    Column {
                        TextFieldMaterial3(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
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
                            items(
                                items = searchBusRoutes,
                                key = { it.id }
                            ) { busRoute ->
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
                            item { NavigationBarsSpacer() }
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
            }

            BusRouteDialog(
                showDialog = showDialog,
                busRoute = selectedBusRoute,
                hideDialog = { showDialog = false },
            )
        })

}
