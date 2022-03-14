package fr.cph.chicago.core.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivvyScreen(
    modifier: Modifier = Modifier,
    title: String,
    search: String,
    mainViewModel: MainViewModel,
    navigationViewModel: NavigationViewModel
) {
    Timber.d("Compose DivvyScreen")
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    var searchBikeStations by remember { mutableStateOf(listOf<BikeStation>()) }
    var textSearch by remember { mutableStateOf(TextFieldValue(search)) }

    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            searchBikeStations = mainViewModel.uiState.bikeStations.filter { bikeStation ->
                bikeStation.name.contains(textSearch.text, true)
            }
        }
    })

    Scaffold(
        snackbarHost = { SnackbarHostInsets(state = mainViewModel.uiState.snackbarHostState) },
        content = {
            Column {
                DisplayTopBar(
                    title = title,
                    viewModel = navigationViewModel,
                )
                if (mainViewModel.uiState.bikeStations.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
                        TextFieldMaterial3(
                            modifier = Modifier.fillMaxWidth(),
                            text = textSearch,
                            onValueChange = { value ->
                                textSearch = value
                                searchBikeStations = mainViewModel.uiState.bikeStations.filter { bikeStation ->
                                    bikeStation.name.contains(value.text, true)
                                }
                            }
                        )
                        LazyColumn(modifier = modifier.fillMaxWidth()) {
                            items(searchBikeStations) { bikeStation ->
                                TextButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    onClick = {
                                        navController.navigate(
                                            screen = Screen.DivvyDetails,
                                            arguments = mapOf(
                                                "stationId" to bikeStation.id,
                                                "search" to textSearch.text
                                            )
                                        )
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            bikeStation.name,
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
                            mainViewModel.loadBikeStations()
                        }
                    )
                    if (mainViewModel.uiState.bikeStationsShowError) {
                        ShowErrorMessageSnackBar(
                            scope = scope,
                            snackbarHostState = mainViewModel.uiState.snackbarHostState,
                            showError = mainViewModel.uiState.bikeStationsShowError,
                            onComplete = {
                                mainViewModel.resetBikeStationsShowError()
                            }
                        )
                    }
                }
            }
        })
}
