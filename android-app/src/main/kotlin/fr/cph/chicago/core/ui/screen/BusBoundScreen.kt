package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.AnimatedPlaceHolderList
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SearchTextField
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.service.BusService
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun BusBoundScreen(
    modifier: Modifier = Modifier,
    viewModel: BusBoundUiViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    Timber.d("Compose BusBoundScreen")
    val uiState = viewModel.uiState
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var textSearch by remember { mutableStateOf(TextFieldValue(viewModel.uiState.search)) }
    textSearch = TextFieldValue(
        text = viewModel.uiState.search,
        selection = TextRange(viewModel.uiState.search.length)
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHostInsets(state = uiState.snackbarHostState) },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                DisplayTopBar(
                    screen = Screen.BusBound,
                    title = title,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                when {
                    uiState.isRefreshing -> {
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
                        Column(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
                            SearchTextField(
                                modifier = Modifier,
                                text = textSearch.text,
                                onValueChange = { value ->
                                    viewModel.updateSearch(search = value)
                                }
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = viewModel.uiState.lazyListState
                            ) {
                                items(
                                    items = uiState.searchBusStops,
                                    key = { it.id }
                                ) { busStop ->
                                    val keyboardController = LocalSoftwareKeyboardController.current
                                    TextButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        onClick = {
                                            navController.navigate(
                                                screen = Screen.BusDetails,
                                                arguments = mapOf(
                                                    "busStopId" to busStop.id,
                                                    "busStopName" to busStop.name,
                                                    "busRouteId" to uiState.busRouteId,
                                                    "busRouteName" to uiState.busRouteName,
                                                    "bound" to uiState.bound,
                                                    "boundTitle" to uiState.boundTitle,
                                                    "search" to uiState.search,
                                                ),
                                                closeKeyboard = {
                                                    keyboardController?.hide()
                                                }
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
                                item { NavigationBarsSpacer() }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
data class BusBoundUiState constructor(
    val busRouteId: String,
    val busRouteName: String,
    val bound: String,
    val boundTitle: String,

    val busStops: List<BusStop> = listOf(),

    val searchBusStops: List<BusStop> = listOf(),
    val search: String,

    val isRefreshing: Boolean = true,
    val isErrorState: Boolean = false,
    val showError: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val lazyListState: LazyListState = LazyListState(),
)

class BusBoundUiViewModel(
    busRouteId: String,
    busRouteName: String,
    bound: String,
    boundTitle: String,
    search: String,
    private val busService: BusService = BusService,
) : ViewModel() {
    var uiState by mutableStateOf(
        BusBoundUiState(
            busRouteId = busRouteId,
            busRouteName = busRouteName,
            bound = bound,
            boundTitle = boundTitle,
            search = search,
        )
    )
        private set

    init {
        loadBusStops()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun initModel(
        busRouteId: String,
        busRouteName: String,
        bound: String,
        boundTitle: String,
        search: String,
    ) {
        if (busRouteId != uiState.busRouteId || bound != uiState.bound) {
            uiState = uiState.copy(
                busRouteId = busRouteId,
                busRouteName = busRouteName,
                bound = bound,
                boundTitle = boundTitle,
                search = search,
                busStops = listOf(),
                searchBusStops = listOf(),
                isRefreshing = true,
                lazyListState = LazyListState()
            )
            loadBusStops()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun refresh() {
        Timber.d("Start Refreshing")
        uiState = uiState.copy(isRefreshing = true)
        loadBusStops()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateSearch(search: String) {
        uiState = uiState.copy(
            search = search,
            searchBusStops = uiState.busStops.filter { busStop -> busStop.description.contains(search, true) }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun loadBusStops() {
        busService.loadAllBusStopsForRouteBound(uiState.busRouteId, uiState.bound)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe(
                { result ->
                    val searchBusStops = if (uiState.search != "") {
                        result.filter { busStop -> busStop.description.contains(uiState.search, true) }
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

    @OptIn(ExperimentalMaterial3Api::class)
    fun resetShowError() {
        uiState = uiState.copy(showError = false)
    }

    companion object {
        fun provideFactory(
            busRouteId: String,
            busRouteName: String,
            bound: String,
            boundTitle: String,
            search: String,
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
                        search = search,
                    ) as T
                }
            }
    }
}
