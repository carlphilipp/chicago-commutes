/**
 * Copyright 2019 Carl-Philipp Harmant
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

import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.station.BikeStationActivity
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.util.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class BikeAllBikeStationsObserver(private val activity: BikeStationActivity, private val bikeStationId: Int) : SingleObserver<List<BikeStation>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onSuccess(bikeStations: List<BikeStation>) {
        try {
            bikeStations
                .filter { station -> bikeStationId == station.id }
                .elementAtOrElse(0) { BikeStation.buildDefaultBikeStationWithName("error") }
                .also { station ->
                    if (station.name != "error") {
                        activity.refreshStation(station)
                        activity.intent.extras?.putParcelable(activity.getString(R.string.bundle_bike_station), station)
                    } else {
                        Log.w(TAG, "Train station id [$bikeStationId] not found")
                        util.showOopsSomethingWentWrong(activity.swipeRefreshLayout)
                    }
                }
            stopRefreshingIfNeeded()
        } catch (ex: Throwable) {
            handleOnNextError(ex)
        }
    }

    override fun onError(throwable: Throwable) {
        handleError(throwable)
    }

    private fun handleOnNextError(throwable: Throwable) {
        handleError(throwable)
    }

    private fun handleError(throwable: Throwable) {
        Log.e(TAG, throwable.message, throwable)
        stopRefreshingIfNeeded()
        util.showOopsSomethingWentWrong(activity.swipeRefreshLayout)
    }

    private fun stopRefreshingIfNeeded() {
        if (activity.swipeRefreshLayout.isRefreshing) {
            activity.swipeRefreshLayout.isRefreshing = false
        }
    }

    companion object {
        private val TAG = BikeAllBikeStationsObserver::class.java.simpleName
        private val util = Util
    }
}
