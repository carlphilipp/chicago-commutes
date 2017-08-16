package fr.cph.chicago.rx

import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BikeStationActivity
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class BikeAllBikeStationsObserver(private val activity: BikeStationActivity, private val bikeStationId: Int, private val swipeRefreshLayout: SwipeRefreshLayout) : Observer<List<BikeStation>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(bikeStations: List<BikeStation>) {
        bikeStations
            .filter { station -> bikeStationId == station.id }
            .elementAtOrElse(0, { BikeStation.buildDefaultBikeStationWithName("error") })
            .also { station ->
                if (station.name != "error") {
                    activity.refreshStation(station)
                    activity.intent.extras.putParcelable(activity.getString(R.string.bundle_bike_station), station)
                } else {
                    Log.w(TAG, "Station id [$bikeStationId] not found")
                }
            }
    }

    override fun onError(throwable: Throwable) {
        Util.showOopsSomethingWentWrong(swipeRefreshLayout)
    }

    override fun onComplete() {
        swipeRefreshLayout.isRefreshing = false
    }

    companion object {
        private val TAG = BikeAllBikeStationsObserver::class.java.simpleName
    }
}
