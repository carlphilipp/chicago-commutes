/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.ui.common.AnimatedErrorView
import fr.cph.chicago.core.ui.common.ShowErrorMessageSnackBar
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.viewmodel.settingsViewModel
import fr.cph.chicago.redux.BaseAction
import fr.cph.chicago.redux.DefaultSettingsAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * This class represents the base activity of the application It will load the loading screen and then the main
 * activityAlertActivity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = BaseViewModel()

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                BaseView(
                    viewModel = viewModel,
                )
            }
        }
        viewModel.initModel(context = this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseView(viewModel: BaseViewModel) {
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = viewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            LoadingView(show = !viewModel.uiState.isError)
            AnimatedErrorView(
                visible = viewModel.uiState.isError,
                onClick = { viewModel.setUpDefaultSettings() }
            )
            if (viewModel.uiState.showErrorSnackBar) {
                ShowErrorMessageSnackBar(
                    scope = scope,
                    snackbarHostState = viewModel.uiState.snackbarHostState,
                    showError = viewModel.uiState.showErrorSnackBar,
                    onComplete = {
                        viewModel.showHideSnackBar(false)
                    }
                )
            }
        }
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier, show: Boolean) {
    AnimatedVisibility(
        visible = show,
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        // Re-implemented Icon because the scale is not an option for some reason
        val vec = ImageVector.vectorResource(id = R.drawable.skyline_vector)
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .toolingGraphicsLayer()
                .paint(
                    painter = rememberVectorPainter(vec),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiaryContainer),
                    contentScale = ContentScale.FillBounds
                )
        )
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.padding(20.dp))
                Text(
                    text = stringResource(id = R.string.progress_message),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {}
        }
    }
}

data class BaseUiState(
    val isError: Boolean = false,
    val showErrorSnackBar: Boolean = false,
    val context: Context = App.instance.applicationContext,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)

@HiltViewModel
class BaseViewModel @Inject constructor(
    private val realmConfig: RealmConfig = RealmConfig
) : ViewModel(), StoreSubscriber<State> {
    var uiState by mutableStateOf(BaseUiState())
        private set

    fun initModel(context: Context): BaseViewModel {
        uiState = uiState.copy(context = context)
        setUpDefaultSettings()
        return this
    }

    fun showHideSnackBar(value: Boolean) {
        uiState = uiState.copy(showErrorSnackBar = value)
    }

    fun setUpDefaultSettings() {
        Single.fromCallable { realmConfig.setUpRealm() }
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .doOnSuccess {
                Timber.d("Realm setup, dispatching default settings action and base action")
                val defaultSettingsAction = DefaultSettingsAction(
                    ctaTrainKey = uiState.context.getString(R.string.cta_train_key),
                    ctaBusKey = uiState.context.getString(R.string.cta_bus_key),
                    googleStreetKey = uiState.context.getString(R.string.google_maps_api_key)
                )
                store.dispatch(defaultSettingsAction)
                store.dispatch(BaseAction())
            }
            .subscribe(
                {
                    Timber.d("Realm setup properly")
                },
                { throwable ->
                    Timber.e(throwable, "Could not setup realm")
                    uiState = uiState.copy(
                        isError = true,
                        showErrorSnackBar = true,
                    )
                }
            )
    }

    override fun newState(state: State) {
        when (state.status) {
            Status.SUCCESS -> {
                store.unsubscribe(this)
                startMainActivity()
            }
            Status.FAILURE -> {
                store.unsubscribe(this)
                startMainActivity()
            }
            Status.FULL_FAILURE -> {
                uiState = uiState.copy(
                    isError = true,
                    showErrorSnackBar = true,
                )
            }
            else -> Timber.d("Unknown status on new state")
        }
    }

    private fun startMainActivity() {
        val activity = (uiState.context as ComponentActivity)
        val intent = Intent(uiState.context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(activity, intent, null)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        activity.finish()
    }

    fun onStart() {
        store.subscribe(this)
    }

    fun onStop() {
        store.unsubscribe(this)
    }
}
