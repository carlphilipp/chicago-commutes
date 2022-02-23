package fr.cph.chicago.core.composable.screen

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.AlertActivityComposable
import fr.cph.chicago.core.composable.AlertDetailsUiState
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.common.ErrorView
import fr.cph.chicago.core.composable.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.redux.AlertAction
import fr.cph.chicago.redux.ResetAlertsStatusAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import javax.inject.Inject
import org.rekotlin.StoreSubscriber
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Alerts(modifier: Modifier = Modifier, mainViewModel: MainViewModel, viewModel: AlertsViewModel = AlertsViewModel().initModel()) {
    //mainViewModel.loadAlertsIfNeeded()

    Timber.i("View model%s", System.identityHashCode(viewModel))

    val uiState = mainViewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    var searchRoutesAlerts by remember { mutableStateOf<List<RoutesAlertsDTO>>(listOf()) }
    searchRoutesAlerts = viewModel.uiState.routesAlerts

    //if (!mainViewModel.uiState.routeAlertErrorState) {
        SwipeRefresh(
            modifier = modifier,
            state = rememberSwipeRefreshState(mainViewModel.uiState.isRefreshing),
            onRefresh = {
                viewModel.loadAlerts()
            },
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
                content = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            TextFieldMaterial3(
                                text = textSearch,
                                onValueChange = { value ->
                                    textSearch = value
                                    searchRoutesAlerts = viewModel.uiState.routesAlerts.filter { alert ->
                                        alert.id.contains(value.text, true) || alert.routeName.contains(value.text, true)
                                    }
                                }
                            )
                        }
                        items(searchRoutesAlerts) { alert ->
                            TextButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                onClick = {
                                    val intent = Intent(context, AlertActivityComposable::class.java)
                                    val extras = Bundle()
                                    extras.putString("routeId", alert.id)
                                    extras.putString(
                                        "title", if (alert.alertType === AlertType.TRAIN)
                                            alert.routeName
                                        else
                                            "${alert.id} - ${alert.routeName}"
                                    )
                                    intent.putExtras(extras)
                                    startActivity(context, intent, null)
                                }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (alert.alertType == AlertType.TRAIN) {
                                            val color = android.graphics.Color.parseColor(alert.routeBackgroundColor)
                                            ColoredBox(modifier = Modifier.padding(end = 20.dp), color = androidx.compose.ui.graphics.Color(color))
                                        } else {
                                            ColoredBox(modifier = Modifier.padding(end = 20.dp))
                                        }
                                        Column {
                                            val stationName = if (alert.alertType == AlertType.TRAIN) {
                                                alert.routeName
                                            } else {
                                                alert.id + " - " + alert.routeName
                                            }
                                            Text(
                                                text = stationName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = alert.routeStatus,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }

                                    if ("Normal Service" != alert.routeStatus) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_action_alert_warning),
                                            contentDescription = "alert",
                                            colorFilter = ColorFilter.tint(Color.Red),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
   /* } else {
        ErrorView(
            onClick = {
                mainViewModel.loadAlerts()
            }
        )
    }*/

    if (viewModel.uiState.routeAlertShowError) {
        ShowErrorMessageSnackBar(
            scope = scope,
            snackbarHostState = uiState.snackbarHostState,
            showErrorMessage = viewModel.uiState.routeAlertShowError
        )
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

data class AlertsUiState(
    val routesAlerts: List<RoutesAlertsDTO> = listOf(),
    val routeAlertErrorState: Boolean = false,
    val routeAlertShowError: Boolean = false,
)

@HiltViewModel
class AlertsViewModel @Inject constructor () : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(AlertsUiState())
        private set

    init {
        Timber.i("*************************** NEW MODEL")
    }

    fun initModel(): AlertsViewModel {
        Timber.i("Init model")
        loadAlerts()
        return this
    }

    override fun newState(state: State) {
        Timber.i("new state ${state.alertStatus}")
        if (state.alertStatus == Status.SUCCESS) {
            uiState = uiState.copy(routesAlerts = state.alertsDTO)
            //uiState = uiState.copy(
            //    routesAlerts = state.alertsDTO,
                //routeAlertErrorState = false,
              //  routeAlertShowError = false,
            //)
            //store.dispatch(ResetAlertsStatusAction())
        }
        if (state.alertStatus == Status.FAILURE || state.alertStatus == Status.FULL_FAILURE) {
            uiState = uiState.copy(
                routeAlertErrorState = true,
                routeAlertShowError = true,
            )
            store.dispatch(ResetAlertsStatusAction())
        }
    }

    fun loadAlertsIfNeeded() {
        if (uiState.routesAlerts.isEmpty() && !uiState.routeAlertErrorState) {
            loadAlerts()
        }
    }

    fun loadAlerts() {
        //uiState = uiState.copy(isRefreshing = true)
        store.dispatch(AlertAction())
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
