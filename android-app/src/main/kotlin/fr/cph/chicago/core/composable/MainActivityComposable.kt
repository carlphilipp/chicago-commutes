package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import fr.cph.chicago.core.composable.screen.screens
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.store
import fr.cph.chicago.task.refreshTask
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlin.random.Random
import org.rekotlin.StoreSubscriber
import timber.log.Timber

val isRefreshing = mutableStateOf(false)

class MainActivityComposable : ComponentActivity(), StoreSubscriber<State> {
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
    }

    override fun newState(state: State) {
        Timber.i("new state")
        Favorites.refreshFavorites()
        isRefreshing.value = false
    }

    private fun startRefreshTask() {
        val refreshTask: Observable<Long> = refreshTask()
        refreshTask.subscribeWith(object : DisposableObserver<Long>() {
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
