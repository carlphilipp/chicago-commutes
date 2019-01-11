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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO

/**
 * Adapter that handle alert lists
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertAdapter(private var routesAlertsDTOS: List<RoutesAlertsDTO>) : BaseAdapter() {

    override fun getCount(): Int {
        return routesAlertsDTOS.size
    }

    override fun getItem(position: Int): RoutesAlertsDTO {
        return routesAlertsDTOS[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_alerts, parent, false)
        val item = getItem(position)

        val color: LinearLayout = view.findViewById(R.id.station_color_value)
        color.setBackgroundColor(
            if (item.alertType == AlertType.TRAIN)
                Color.parseColor(item.routeBackgroundColor)
            else
                Color.GRAY
        )

        val stationName: TextView = view.findViewById(R.id.station_name_value)
        stationName.text =
            if (item.alertType === AlertType.TRAIN)
                item.routeName
            else
                item.id + " - " + item.routeName

        val status: TextView = view.findViewById(R.id.status)
        status.text = item.routeStatus

        if ("Normal Service" != item.routeStatus) {
            val imageView: ImageView = view.findViewById(R.id.alert_warning)
            imageView.visibility = View.VISIBLE
        }
        return view
    }

    fun setAlerts(routesAlertsDTOS: List<RoutesAlertsDTO>) {
        this.routesAlertsDTOS = routesAlertsDTOS
    }
}
