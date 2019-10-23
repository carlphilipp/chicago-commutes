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

import android.view.View
import android.widget.ListView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.util.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.Date

class BusFollowObserver(private val activity: BusMapActivity, private val layout: View, private val view: View, private val loadAll: Boolean) : SingleObserver<List<BusArrival>> {

    companion object {
        private val util = Util
    }

    override fun onSubscribe(d: Disposable) {}

    override fun onSuccess(busArrivalsParam: List<BusArrival>) {
        var busArrivals = busArrivalsParam.toMutableList()
        if (!loadAll && busArrivals.size > 7) {
            busArrivals = busArrivals.subList(0, 6)
            val busArrival = BusArrival(Date(), "added bus", view.context.getString(R.string.bus_all_results), 0, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Date(), false)
            busArrivals.add(busArrival)
        }
        val arrivals = view.findViewById<ListView>(R.id.arrivals)
        val error = view.findViewById<TextView>(R.id.error)
        if (busArrivals.isNotEmpty()) {
            val ada = BusMapSnippetAdapter(busArrivals)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        activity.refreshInfoWindow()
    }

    override fun onError(throwable: Throwable) {
        util.handleConnectOrParserException(throwable, layout)
        Timber.e(throwable, "Error while loading bus follow")
    }
}
