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
import android.widget.ListView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.apache.commons.lang3.StringUtils
import java.util.Date

class BusFollowObserver(private val activity: BusMapActivity, private val layout: View, private val view: View, private val loadAll: Boolean) : Observer<List<BusArrival>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(busArrivalsParam: List<BusArrival>) {
        var busArrivals = busArrivalsParam.toMutableList()
        if (!loadAll && busArrivals.size > 7) {
            busArrivals = busArrivals.subList(0, 6)
            val busArrival = BusArrival(Date(), "added bus", view.context.getString(R.string.bus_all_results), 0, 0, "", "", StringUtils.EMPTY, Date(), false)
            busArrivals.add(busArrival)
        }
        val arrivals: ListView = view.findViewById(R.id.arrivals)
        val error: TextView = view.findViewById(R.id.error)
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
        util.handleConnectOrParserException(throwable, null, layout, layout)
        Log.e(TAG, throwable.message, throwable)
    }

    override fun onComplete() {}

    companion object {
        private val TAG = BusFollowObserver::class.java.simpleName
        private val util = Util
    }
}
