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
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter
import fr.cph.chicago.core.model.BusArrival
import io.reactivex.rxjava3.functions.Function
import org.apache.commons.lang3.StringUtils
import java.lang.ref.WeakReference
import java.util.*

class BusesFunction(busMapActivity: BusMapActivity, private val feature: Feature, private val loadAll: Boolean) : Function<List<BusArrival>, View>, AFunction() {

    val activity: WeakReference<BusMapActivity> = WeakReference(busMapActivity)

    override fun apply(busArrivalsRes: List<BusArrival>): View {
        val view = createView(feature, activity)
        val arrivals = view.findViewById<ListView>(R.id.arrivals)
        val error = view.findViewById<TextView>(R.id.error)

        var busArrivals = busArrivalsRes.toMutableList()
        if (!loadAll && busArrivals.size > 7) {
            busArrivals = busArrivals.subList(0, 6)
            val busArrival = BusArrival(Date(), "added bus", view.context.getString(R.string.bus_all_results), 0, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Date(), false)
            busArrivals.add(busArrival)
        }
        if (busArrivals.isNotEmpty()) {
            val container = view.findViewById<RelativeLayout>(R.id.container)
            addParams(container, busArrivals.size)

            val ada = BusMapSnippetAdapter(busArrivals)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        return view
    }
}
