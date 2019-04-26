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
import fr.cph.chicago.core.activity.station.TrainStationActivity
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.util.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class TrainArrivalObserver(private val trainStationActivity: TrainStationActivity) : SingleObserver<TrainArrival> {

    override fun onSubscribe(d: Disposable) {}

    override fun onError(throwable: Throwable) {
        handleError(throwable)
    }

    override fun onSuccess(trainArrival: TrainArrival) {
        try {
            trainStationActivity.hideAllArrivalViews()
            trainArrival.trainEtas.forEach { trainStationActivity.drawAllArrivalsTrain(it) }
        } catch (ex: Throwable) {
            handleOnNextError(ex)
        }
    }

    private fun handleOnNextError(throwable: Throwable) {
        handleError(throwable)
    }

    private fun handleError(throwable: Throwable) {
        Log.e(TAG, throwable.message, throwable)
        stopRefreshingIfNeeded()
        util.handleConnectOrParserException(throwable, trainStationActivity.swipeRefreshLayout)
    }

    private fun stopRefreshingIfNeeded() {
        if (trainStationActivity.swipeRefreshLayout.isRefreshing) {
            trainStationActivity.swipeRefreshLayout.isRefreshing = false
        }
    }

    companion object {
        private val TAG = TrainArrivalObserver::class.java.simpleName
        private val util = Util
    }
}
