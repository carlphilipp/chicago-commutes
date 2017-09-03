package fr.cph.chicago.rx

import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import fr.cph.chicago.core.activity.StationActivity
import fr.cph.chicago.entity.TrainArrival
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class TrainArrivalObserver(private val activity: StationActivity, private val swipeRefreshLayout: SwipeRefreshLayout) : Observer<TrainArrival> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(trainArrival: TrainArrival) {
        activity.hideAllArrivalViews()
        trainArrival.etas.forEach { activity.drawAllArrivalsTrain(it) }
    }

    override fun onError(e: Throwable) {
        Log.e(TAG, "Error while getting trains arrival time: " + e.message, e)
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
        util.showNetworkErrorMessage(swipeRefreshLayout)
    }

    override fun onComplete() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    companion object {
        private val TAG = TrainArrivalObserver::class.java.simpleName
        private val util = Util
    }
}
