package fr.cph.chicago.core.composable.screen

import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.redux.AlertAction
import fr.cph.chicago.redux.ResetAlertsStatusAction
import fr.cph.chicago.redux.ResetBusStationStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import javax.inject.Inject
import org.rekotlin.StoreSubscriber
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Alerts(modifier: Modifier = Modifier, mainViewModel: MainViewModel, alertsViewModel: AlertsViewModel) {
    SwipeRefresh(
        modifier = modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(false), // FIXME
        onRefresh = {

        },
    ) {
        Scaffold(
            /*snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },*/ // FIXME
            content = {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(alertsViewModel.uiState.routesAlerts) { alert ->
                        Row {
                            if (alert.alertType == AlertType.TRAIN) {
                                val color = Color.parseColor(alert.routeBackgroundColor)
                                ColoredBox(color = androidx.compose.ui.graphics.Color(color))
                            } else {
                                ColoredBox()
                            }
                            Column {
                                val stationName = if (alert.alertType == AlertType.TRAIN) {
                                    alert.routeName
                                } else {
                                    alert.id + " - " + alert.routeName
                                }
                                Text(
                                    text = stationName,
                                )
                                Text(
                                    text = alert.routeStatus
                                )
                            }
                            if ("Normal Service" != alert.routeStatus) {
                                Row(horizontalArrangement = Arrangement.End) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_action_alert_warning),
                                        contentDescription = "alert",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    DisposableEffect(key1 = alertsViewModel) {
        alertsViewModel.onStart()
        onDispose { alertsViewModel.onStop() }
    }
}

data class AlertsState(
    val routesAlerts: List<RoutesAlertsDTO> = listOf()
)

@HiltViewModel
class AlertsViewModel @Inject constructor() : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(AlertsState())
        private set

    fun initModel(): AlertsViewModel {
        store.dispatch(AlertAction())
        return this
    }

    override fun newState(state: State) {
        Timber.d("Alert new state")
        when (state.alertStatus) {
            Status.SUCCESS -> {
                uiState = uiState.copy(routesAlerts = state.alertsDTO)
                store.dispatch(ResetAlertsStatusAction())
            }
            Status.FAILURE -> {

                store.dispatch(ResetAlertsStatusAction())
            }
            Status.FULL_FAILURE -> {

                store.dispatch(ResetAlertsStatusAction())
            }
            else -> Timber.d("Unknown status on new state")
        }
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
