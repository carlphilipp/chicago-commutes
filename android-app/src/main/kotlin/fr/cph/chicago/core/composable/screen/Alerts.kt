package fr.cph.chicago.core.composable.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.ColoredBox
import fr.cph.chicago.core.composable.common.TextFieldMaterial3
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Alerts(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {
    mainViewModel.loadAlertsIfNeeded()

    var textSearch by remember { mutableStateOf(TextFieldValue("")) }
    var searchRoutesAlerts by remember { mutableStateOf<List<RoutesAlertsDTO>>(listOf()) }
    searchRoutesAlerts = mainViewModel.uiState.routesAlerts

    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(mainViewModel.uiState.isRefreshing),
        onRefresh = {
            // TODO
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
                                searchRoutesAlerts = mainViewModel.uiState.routesAlerts.filter { alert ->
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
                                // TODO
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
}
