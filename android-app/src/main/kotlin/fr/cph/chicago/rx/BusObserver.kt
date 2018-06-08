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

import android.util.Log
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class BusObserver(private val activity: BusMapActivity, private val centerMap: Boolean, private val view: View) : Observer<List<Bus>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(buses: List<Bus>) {
        activity.drawBuses(buses)
        if (buses.isNotEmpty()) {
            if (centerMap) {
                activity.centerMapOnBus(buses)
            }
        } else {
            Util.showMessage(view, R.string.message_no_bus_found)
        }
    }

    override fun onError(throwable: Throwable) {
        Util.handleConnectOrParserException(throwable, null, view, view)
        Log.e(TAG, throwable.message, throwable)
    }

    override fun onComplete() {}

    companion object {
        private val TAG = BusObserver::class.java.simpleName
    }
}
