/**
 * Copyright 2020 Carl-Philipp Harmant
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

import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class BusObserver(private val activity: BusMapActivity, private val centerMap: Boolean, private val view: View) : SingleObserver<List<Bus>> {

    companion object {
        private val util = Util
    }

    override fun onSubscribe(d: Disposable) {}

    override fun onSuccess(buses: List<Bus>) {
        activity.drawBuses(buses)
        if (buses.isNotEmpty()) {
            if (centerMap) {
                activity.centerMapOnBus(buses)
            }
        } else {
            util.showSnackBar(view, R.string.message_no_bus_found)
        }
    }

    override fun onError(throwable: Throwable) {
        util.handleConnectOrParserException(throwable, view)
        Timber.e(throwable, "Error while loading buses")
    }
}
