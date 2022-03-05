package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.AnimatedErrorView
import fr.cph.chicago.core.composable.common.AnimatedPlaceHolderList
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.startBusDetailActivity
import timber.log.Timber

private val busService = BusService

class BusBoundActivityComposable : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: ""
        val busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: ""
        val bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: ""
        val boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: ""

        val viewModel = BusBoundUiViewModel(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
        )
        viewModel.loadData()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                BusBoundView(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusBoundView(
    modifier: Modifier = Modifier,
    viewModel: BusBoundUiViewModel,
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = { RefreshTopBar("${uiState.busRouteId} - ${uiState.boundTitle}") },
        snackbarHost = { SnackbarHost(hostState = uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            when {
                uiState.isRefreshing && uiState.busStops.isEmpty() -> {
                    AnimatedPlaceHolderList(isLoading = uiState.isRefreshing)
                }
                uiState.isErrorState -> {
                    AnimatedErrorView(onClick = { viewModel.refresh() })
                    if (uiState.isErrorState) {
                        ShowErrorMessageSnackBar(
                            scope = scope,
                            snackbarHostState = uiState.snackbarHostState,
                            showError = uiState.isErrorState,
                            onComplete = {
                                viewModel.resetShowError()
                            }
                        )
                    }
                }
                else -> {
                    Column {
                        TextFieldMaterial3(
                            modifier = Modifier.fillMaxWidth(),
                            text = uiState.searchText,
                            onValueChange = { textFieldValue ->
                                viewModel.updateSearch(textFieldValue = textFieldValue)
                            }
                        )
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.searchBusStops) { busStop ->
                                TextButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    onClick = {
                                        startBusDetailActivity(
                                            context = context,
                                            busDetailsDTO = BusDetailsDTO(
                                                stopId = busStop.id.toInt(),
                                                stopName = busStop.name,
                                                bound = uiState.bound,
                                                boundTitle = uiState.boundTitle,
                                                busRouteId = uiState.busRouteId,
                                                routeName = uiState.busRouteName,
                                            )
                                        )
                                    },
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = busStop.description,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

data class BusBoundUiState(
    val busRouteId: String,
    val busRouteName: String,
    val bound: String,
    val boundTitle: String,

    val busStops: List<BusStop> = listOf(),

    val searchBusStops: List<BusStop> = listOf(),
    val searchText: TextFieldValue = TextFieldValue(),

    val isRefreshing: Boolean = true,
    val isErrorState: Boolean = false,
    val showError: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

class BusBoundUiViewModel(
    busRouteId: String,
    busRouteName: String,
    bound: String,
    boundTitle: String
) : ViewModel() {
    var uiState by mutableStateOf(
        BusBoundUiState(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
        )
    )
        private set

    fun refresh() {
        Timber.d("Start Refreshing")
        uiState = uiState.copy(isRefreshing = true)
        loadData()
    }

    fun updateSearch(textFieldValue: TextFieldValue) {
        uiState = uiState.copy(
            searchText = textFieldValue,
            searchBusStops = uiState.busStops.filter { busStop -> busStop.description.contains(textFieldValue.text, true) }
        )
    }

    fun loadData() {
        busService.loadAllBusStopsForRouteBound(uiState.busRouteId, uiState.bound)
            .subscribe(
                { result ->
                    val searchBusStops = if (uiState.searchText.text != "") {
                        result.filter { busStop -> busStop.description.contains(uiState.searchText.text, true) }
                    } else {
                        result
                    }
                    uiState = uiState.copy(
                        busStops = result,
                        searchBusStops = searchBusStops,
                        isRefreshing = false,
                        isErrorState = false,
                        showError = false,
                    )
                },
                { throwable ->
                    Timber.e(throwable, "Error while getting bus stops for route bound")
                    uiState = uiState.copy(
                        isRefreshing = false,
                        isErrorState = true,
                        showError = true,
                    )
                })
    }

    fun resetShowError() {
        uiState = uiState.copy(showError = false)
    }
}
