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
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter
import fr.cph.chicago.core.model.TrainEta
import io.reactivex.functions.Function
import java.lang.ref.WeakReference

class TrainsFunction(trainMapActivity: TrainMapActivity, private val feature: Feature) : Function<List<TrainEta>, View>, AFunction() {

    val activity: WeakReference<TrainMapActivity> = WeakReference(trainMapActivity)

    override fun apply(trains: List<TrainEta>): View {
        val view = createView(feature, activity)
        val arrivals = view.findViewById<ListView>(R.id.arrivals)
        val error = view.findViewById<TextView>(R.id.error)

        if (trains.isNotEmpty()) {
            val container = view.findViewById<RelativeLayout>(R.id.container)
            addParams(container, trains.size)

            val ada = TrainMapSnippetAdapter(trains)
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
