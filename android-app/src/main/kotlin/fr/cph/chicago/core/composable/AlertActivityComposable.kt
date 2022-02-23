package fr.cph.chicago.core.composable

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.dto.RouteAlertsDTO
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.AlertService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import timber.log.Timber

class AlertActivityComposable : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = intent.getStringExtra("routeId") ?: StringUtils.EMPTY
        val title = intent.getStringExtra("title") ?: StringUtils.EMPTY
        val viewModel = AlertDetailsViewModel().initModel(routeId = routeId, title = title)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                AlertDetails(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetails(
    modifier: Modifier = Modifier,
    viewModel: AlertDetailsViewModel
) {
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(uiState.isRefreshing),
        onRefresh = { viewModel.loadAlerts() },
    ) {
        Scaffold(
            topBar = { TopBar(title = uiState.title) },
            snackbarHost = { SnackbarHost(hostState = uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
            content = {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.routeAlertsDTOS) { alert ->
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = alert.headLine,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = alert.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = alert.impact,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "From: " + alert.start,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (alert.end != StringUtils.EMPTY) {
                                Text(
                                    text = "To: " + alert.end,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        )
    }
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

data class AlertDetailsUiState(
    val routeId: String = StringUtils.EMPTY,
    val title: String = StringUtils.EMPTY,
    val isRefreshing: Boolean = false,
    val routeAlertsDTOS: List<RouteAlertsDTO> = listOf(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class AlertDetailsViewModel @Inject constructor(private val alertService: AlertService = AlertService) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(AlertDetailsUiState())
        private set

    fun initModel(routeId: String, title: String): AlertDetailsViewModel {
        uiState = uiState.copy(
            routeId = routeId,
            title = title,
        )
        loadAlerts()

        return this
    }

    override fun newState(state: State) {

    }

    fun loadAlerts() {
        uiState = uiState.copy(isRefreshing = true)
        alertService.routeAlertForId(uiState.routeId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { routeAlertsDTOS ->
                    uiState = uiState.copy(
                        routeAlertsDTOS = routeAlertsDTOS,
                        isRefreshing = false
                    )
                },
                { error ->
                    Timber.e(error, "Error while refreshing data")
                    uiState = uiState.copy(isRefreshing = false)
                })
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
