package fr.cph.chicago.core.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.model.dto.RouteAlertsDTO
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.AnimatedPlaceHolderList
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.ui.common.SnackbarHostInsets
import fr.cph.chicago.service.AlertService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertDetailsViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    Column {
        DisplayTopBar(
            title = title,
            viewModel = navigationViewModel,
        )
        Scaffold(
            snackbarHost = { SnackbarHostInsets(state = uiState.snackbarHostState) },
            content = {
                SwipeRefresh(
                    modifier = modifier,
                    state = rememberSwipeRefreshState(uiState.isRefreshing),
                    onRefresh = { viewModel.loadAlertDetails() },
                ) {
                    if (uiState.isRefreshing && uiState.routeAlertsDTOS.isEmpty()) {
                        AnimatedPlaceHolderList(isLoading = uiState.isRefreshing)
                    } else {
                        if (!uiState.isError) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.routeAlertsDTOS) { alert ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
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
                                        if (alert.end != "") {
                                            Text(
                                                text = "To: " + alert.end,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            AnimatedErrorView(
                                onClick = {
                                    viewModel.loadAlertDetails()
                                }
                            )
                            if (uiState.showErrorSnackBar) {

                                ShowErrorMessageSnackBar(
                                    scope = scope,
                                    snackbarHostState = uiState.snackbarHostState,
                                    showError = uiState.showErrorSnackBar,
                                    onComplete = { viewModel.resetShowErrorSnackBar() }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

data class AlertDetailsUiState(
    val routeId: String = "",
    val title: String = "",
    val isRefreshing: Boolean = false,
    val routeAlertsDTOS: List<RouteAlertsDTO> = listOf(),
    val isError: Boolean = false,
    val showErrorSnackBar: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class AlertDetailsViewModel @Inject constructor(
    routeId: String,
    title: String,
    private val alertService: AlertService = AlertService
) : ViewModel() {
    var uiState by mutableStateOf(AlertDetailsUiState())
        private set

    init {
        uiState = AlertDetailsUiState(
            routeId = routeId,
            title = title,
        )
        loadAlertDetails()
    }

    fun loadAlertDetails() {
        uiState = uiState.copy(isRefreshing = true)
        alertService.routeAlertForId(uiState.routeId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { routeAlertsDTOS ->
                    uiState = uiState.copy(
                        routeAlertsDTOS = routeAlertsDTOS,
                        isError = false,
                        showErrorSnackBar = false,
                        isRefreshing = false,
                    )
                },
                { error ->
                    Timber.e(error, "Error while refreshing data")
                    uiState = uiState.copy(
                        isRefreshing = false,
                        isError = true,
                        showErrorSnackBar = true,
                    )
                })
    }

    fun resetShowErrorSnackBar() {
        uiState = uiState.copy(showErrorSnackBar = false)
    }

    companion object {
        fun provideFactory(
            routeId: String,
            title: String,
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
                    return AlertDetailsViewModel(
                        routeId = routeId,
                        title = title,
                    ) as T
                }
            }
    }
}
