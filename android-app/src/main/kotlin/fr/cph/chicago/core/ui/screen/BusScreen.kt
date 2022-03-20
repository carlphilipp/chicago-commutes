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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
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
import kotlinx.coroutines.launch
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
    var searchBusRoutes by remember { mutableStateOf(mainViewModel.uiState.busRoutes) }
    val scope = rememberCoroutineScope()

    var textSearch by remember { mutableStateOf(TextFieldValue(mainViewModel.uiState.busRouteSearch)) }
    textSearch = TextFieldValue(
        text = mainViewModel.uiState.busRouteSearch,
        selection = TextRange(mainViewModel.uiState.busRouteSearch.length)
    )

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            searchBusRoutes = search(mainViewModel = mainViewModel, searchText = textSearch.text)
        }
    })

    Scaffold(
        snackbarHost = { SnackbarHostInsets(state = mainViewModel.uiState.snackbarHostState) },
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.Bus,
                    title = title,
                    viewModel = navigationViewModel,
                )
                if (mainViewModel.uiState.busRoutes.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
                        TextFieldMaterial3(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
                            text = textSearch,
                            onValueChange = { value ->
                                mainViewModel.updateBusRouteSearch(value.text)
                                searchBusRoutes = search(mainViewModel = mainViewModel, searchText = value.text)
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

private fun search(mainViewModel: MainViewModel, searchText: String): List<BusRoute> {
    return mainViewModel.uiState.busRoutes.filter { busRoute ->
        busRoute.id.contains(searchText, true) || busRoute.name.contains(searchText, true)
    }
}
