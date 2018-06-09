/**
 * Copyright 2018 Carl-Philipp Harmant
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
package fr.cph.chicago.rx

import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BikeStationActivity
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class BikeAllBikeStationsObserver(private val activity: BikeStationActivity, private val bikeStationId: Int, private val swipeRefreshLayout: SwipeRefreshLayout) : Observer<List<BikeStation>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(divvyStations: List<BikeStation>) {
        divvyStations
            .filter { station -> bikeStationId == station.id }
            .elementAtOrElse(0, { BikeStation.buildDefaultBikeStationWithName("error") })
            .also { station ->
                if (station.name != "error") {
                    activity.refreshStation(station)
                    activity.intent.extras.putParcelable(activity.getString(R.string.bundle_bike_station), station)
                } else {
                    Log.w(TAG, "Train station id [$bikeStationId] not found")
                }
            }
    }

    override fun onError(throwable: Throwable) {
        util.showOopsSomethingWentWrong(swipeRefreshLayout)
    }

    override fun onComplete() {
        swipeRefreshLayout.isRefreshing = false
    }

    companion object {
        private val TAG = BikeAllBikeStationsObserver::class.java.simpleName
        private val util = Util
    }
}
