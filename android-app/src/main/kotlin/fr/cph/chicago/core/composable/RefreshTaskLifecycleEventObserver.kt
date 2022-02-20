package fr.cph.chicago.core.composable

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.task.refreshTask
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import timber.log.Timber

class RefreshTaskLifecycleEventObserver : LifecycleEventObserver {

    private var refreshTask: Observable<Long> = refreshTask()
    private var disposable: Disposable = refreshTask.subscribeWith(CustomerDisposableObserver())

    init {
        Timber.i("Create new RefreshTaskLifecycleEventObserver")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                Timber.i("on pause")
                disposable.dispose()
            }
            Lifecycle.Event.ON_STOP -> {
                Timber.i("on stop")
                disposable.dispose()
            }
            Lifecycle.Event.ON_RESUME -> {
                Timber.i("on resume")
                disposable.run {
                    if (this.isDisposed) {
                        Timber.i("on resume isDisposed")
                        refreshTask = refreshTask()
                        disposable = refreshTask.subscribeWith(CustomerDisposableObserver())
                    }
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                Timber.i("on destroy")
                disposable.dispose()
            }
            else -> {}
        }
    }
}

class CustomerDisposableObserver : DisposableObserver<Long>() {
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
}
