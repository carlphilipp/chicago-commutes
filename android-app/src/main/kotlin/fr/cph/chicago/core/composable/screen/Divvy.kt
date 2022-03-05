package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.composable.common.AnimatedErrorView
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.composable.viewmodel.MainViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.util.startBikeStationActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Divvy(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    var searchBikeStations by remember { mutableStateOf(listOf<BikeStation>()) }
    searchBikeStations = mainViewModel.uiState.bikeStations
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            if (mainViewModel.uiState.bikeStations.isNotEmpty()) {
                LazyColumn(modifier = modifier.fillMaxWidth()) {
                    item {
                        TextFieldMaterial3(
                            text = textSearch,
                            onValueChange = { value ->
                                textSearch = value
                                searchBikeStations = mainViewModel.uiState.bikeStations.filter { bikeStation ->
                                    bikeStation.name.contains(value.text, true)
                                }
                            }
                        )
                    }
                    items(searchBikeStations) { bikeStation ->
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            onClick = { startBikeStationActivity(context = context, bikeStation = bikeStation) }
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
        })
}
