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

package fr.cph.chicago.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.RouteAlertsDTO

/**
 * Adapter that handle alert lists
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertRouteAdapter(private val routeAlertsDTOS: List<RouteAlertsDTO>) : BaseAdapter() {

    override fun getCount(): Int {
        return routeAlertsDTOS.size
    }

    override fun getItem(position: Int): RouteAlertsDTO {
        return routeAlertsDTOS[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_alert_route, parent, false)
        val item = getItem(position)

        val headline: TextView = view.findViewById(R.id.headline)
        headline.text = item.headLine

        val description: TextView = view.findViewById(R.id.description)
        description.text = item.description

        val impact: TextView = view.findViewById(R.id.impact)
        impact.text = item.impact

        val start: TextView = view.findViewById(R.id.start)
        start.text = "From: " + item.start

        val end: TextView = view.findViewById(R.id.end)
        if (item.end == "") {
            end.visibility = LinearLayout.GONE
        } else {
            end.text = "To: " + item.end
        }

        return view
    }
}
