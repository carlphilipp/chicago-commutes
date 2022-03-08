package fr.cph.chicago.core.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.AlertActivity
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.AnimatedPlaceHolderList
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {
    val navController = LocalNavController.current
    val load = remember { mutableStateOf(true) }
    if (load.value) {
        mainViewModel.shouldLoadAlerts()
        load.value = false
    }

    val uiState = mainViewModel.uiState
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    var searchRoutesAlerts by remember { mutableStateOf<List<RoutesAlertsDTO>>(listOf()) }
    searchRoutesAlerts = uiState.routesAlerts

    SwipeRefresh(
        state = rememberSwipeRefreshState(mainViewModel.uiState.isRefreshing),
        onRefresh = { mainViewModel.loadAlerts() },
    ) {
        Scaffold(
            modifier = modifier.fillMaxWidth(),
            snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
            content = {
                if (uiState.isRefreshing && uiState.routesAlerts.isEmpty()) {
                    AnimatedPlaceHolderList(isLoading = uiState.isRefreshing)
                } else {
                    if (!uiState.routeAlertErrorState) {
                        Column {
                            TextFieldMaterial3(
                                modifier = Modifier.fillMaxWidth(),
                                text = textSearch,
                                onValueChange = { value ->
                                    textSearch = value
                                    searchRoutesAlerts = uiState.routesAlerts.filter { alert ->
                                        alert.id.contains(value.text, true) || alert.routeName.contains(value.text, true)
                                    }
                                }
                            )
                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                items(searchRoutesAlerts) { alert ->
                                    TextButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        onClick = {
                                            val intent = Intent(context, AlertActivity::class.java)
                                            val extras = Bundle()
                                            extras.putString(App.instance.getString(R.string.bundle_alerts_route_id), alert.id)
                                            extras.putString(
                                                App.instance.getString(R.string.bundle_title),
                                                if (alert.alertType === AlertType.TRAIN)
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
                                                    ColoredBox(modifier = Modifier.padding(end = 20.dp), color = Color(color))
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
                                                Icons.Filled.Warning
                                                Image(
                                                    imageVector = Icons.Filled.Warning,
                                                    contentDescription = "alert",
                                                    colorFilter = ColorFilter.tint(Color.Red),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        AnimatedErrorView(
                            onClick = {
                                mainViewModel.loadAlerts()
                            }
                        )
                        if (uiState.routeAlertShowError) {
                            ShowErrorMessageSnackBar(
                                scope = scope,
                                snackbarHostState = uiState.snackbarHostState,
                                showError = uiState.routeAlertShowError,
                                onComplete = {
                                    mainViewModel.resetAlertsShowError()
                                }
                            )
                        }
                    }
                }
            })
    }
    BackHandler {
        navController.navigateBack()
    }
}