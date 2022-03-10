package fr.cph.chicago.core.ui.screen

import android.os.Bundle
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.AnimatedPlaceHolderList
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.service.BusService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusBoundScreen(
    modifier: Modifier = Modifier,
    viewModel: BusBoundUiViewModel,
) {
    val uiState = viewModel.uiState
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHostInsets(state = uiState.snackbarHostState) },
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
                                        // FIXME: that's not going to work if this is still an activity
                                        navController.navigate(
                                            screen = Screen.BusDetails,
                                            arguments = mapOf(
                                                "busStopId" to busStop.id,
                                                "busStopName" to busStop.name,
                                                "busRouteId" to uiState.busRouteId,
                                                "busRouteName" to uiState.busRouteName,
                                                "bound" to uiState.bound,
                                                "boundTitle" to uiState.boundTitle,
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
    val busRouteId: String = "",
    val busRouteName: String = "",
    val bound: String = "",
    val boundTitle: String = "",

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
    boundTitle: String,
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(BusBoundUiState())
        private set

    init {
        uiState = BusBoundUiState(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
        )
        loadData()
    }

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

    private fun loadData() {
        busService.loadAllBusStopsForRouteBound(uiState.busRouteId, uiState.bound)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
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

    companion object {
        fun provideFactory(
            busRouteId: String,
            busRouteName: String,
            bound: String,
            boundTitle: String,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return BusBoundUiViewModel(
                        busRouteId = busRouteId,
                        busRouteName = busRouteName,
                        bound = bound,
                        boundTitle = boundTitle,
                    ) as T
                }
            }
    }
}
