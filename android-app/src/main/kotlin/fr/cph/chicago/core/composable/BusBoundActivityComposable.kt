package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.composable.viewmodel.settingsViewModel
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.startBusDetailActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

private val busService = BusService

class BusBoundActivityComposable : ComponentActivity() {

    private lateinit var busRouteId: String
    private lateinit var busRouteName: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private val isRefreshing = mutableStateOf(false)
    private val snackbarHostState = mutableStateOf(SnackbarHostState())
    private var busStops = mutableStateListOf<BusStop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: ""
        busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: ""
        bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: ""
        boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: ""

        loadData()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                BusBoundView(
                    busStops = busStops,
                    busRouteId = busRouteId,
                    busRouteName = busRouteName,
                    bound = bound,
                    boundTitle = boundTitle,
                    isRefreshing = isRefreshing.value,
                    snackbarHostState = snackbarHostState.value,
                    onRefresh = {
                        isRefreshing.value = true
                        Timber.d("Start Refreshing")
                        loadData()
                    },
                )
            }
        }
    }

    private fun loadData() {
        busService.loadAllBusStopsForRouteBound(busRouteId, bound)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    busStops.clear()
                    busStops.addAll(result)
                    isRefreshing.value = false
                },
                { throwable ->
                    Timber.e(throwable, "Error while getting bus stops for route bound")
                    isRefreshing.value = false
                    // FIXME: handle exception
                })
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusBoundView(
    modifier: Modifier = Modifier,
    busRouteId: String,
    busRouteName: String,
    bound: String,
    boundTitle: String,
    busStops: List<BusStop>,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var searchBusStops by remember { mutableStateOf(busStops) }

    Scaffold(
        modifier = modifier,
        topBar = { RefreshTopBar("$busRouteId - $boundTitle") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = onRefresh,
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        TextFieldMaterial3(
                            text = searchText,
                            onValueChange = { textFieldValue ->
                                searchText = textFieldValue
                                searchBusStops = busStops.filter { busStop ->
                                    busStop.description.contains(textFieldValue.text, true)
                                }
                            }
                        )
                    }
                    items(searchBusStops) { busStop ->
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
                                        bound = bound,
                                        boundTitle = boundTitle,
                                        busRouteId = busRouteId,
                                        routeName = busRouteName,
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
    )
}

