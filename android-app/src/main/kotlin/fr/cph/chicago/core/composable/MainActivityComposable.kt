package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.ResetBusRoutesFavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.task.refreshTask
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import org.rekotlin.StoreSubscriber
import timber.log.Timber

val isRefreshing = mutableStateOf(false)
var busRoutes = mutableStateListOf<BusRoute>()

class MainActivityComposable : ComponentActivity(), StoreSubscriber<State> {

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChicagoCommutesTheme {
                Navigation(screens = screens)
            }
        }
        startRefreshTask()
        store.subscribe(this)
        // FIXME not a fan of that maybe to that in base activity
        if (store.state.busRoutes.isEmpty() || store.state.bikeStations.isEmpty()) {
            store.dispatch(BusRoutesAndBikeStationAction())
        }

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE -> {
                    disposable?.dispose()
                    store.unsubscribe(this)
                }
                Lifecycle.Event.ON_STOP -> {
                    disposable?.dispose()
                }
                Lifecycle.Event.ON_RESUME -> {
                    store.subscribe(this)
                    disposable?.run {
                        if (this.isDisposed) {
                            startRefreshTask()
                        }
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    disposable?.dispose()
                    store.unsubscribe(this)
                }
                else -> {}
            }
        })
    }

    override fun newState(state: State) {
        Timber.i("new state")
        Favorites.refreshFavorites()
        isRefreshing.value = false
        if (state.busRoutesStatus == Status.SUCCESS) {
            busRoutes.clear()
            busRoutes.addAll(state.busRoutes)
            store.dispatch(ResetBusRoutesFavoritesAction())
        }
    }

    private fun startRefreshTask() {
        val refreshTask: Observable<Long> = refreshTask()
        disposable = refreshTask.subscribeWith(object : DisposableObserver<Long>() {
            override fun onNext(t: Long) {
                Timber.v("Update time. Thread id: %s", Thread.currentThread().id)
                Favorites.refreshTime()
            }

            override fun onError(e: Throwable) {
                Timber.v(e, "Error with refresh task: %s", e.message)
            }

            override fun onComplete() {
                Timber.v("Refresh task complete")
            }
        })
    }
}
