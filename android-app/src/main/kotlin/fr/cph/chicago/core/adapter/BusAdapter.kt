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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.rx.BusDirectionObserver
import fr.cph.chicago.rx.RxUtil

/**
 * Adapter that will handle buses
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusAdapter(private var busRoutes: List<BusRoute> = listOf()) : BaseAdapter() {

    override fun getCount(): Int {
        return busRoutes.size
    }

    override fun getItem(position: Int): Any {
        return busRoutes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val route = getItem(position) as BusRoute
        val holder: ViewHolder
        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_bus, parent, false)
            holder = ViewHolder(
                routeNameView = view.findViewById(R.id.route_name),
                routeNumberView = view.findViewById(R.id.route_number),
                detailsLayout = view.findViewById(R.id.route_details)
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.routeNameView.text = route.name
        holder.routeNumberView.text = route.id

        view?.setOnClickListener {
            holder.detailsLayout.visibility = LinearLayout.VISIBLE
            RxUtil.busDirections(route.id)
                .subscribe(BusDirectionObserver(App.instance.screenWidth, parent, holder.detailsLayout, route))
        }
        return view
    }

    fun updateBusRoutes(busRoutes: List<BusRoute>) {
        this.busRoutes = busRoutes
    }

    private class ViewHolder(
        val routeNameView: TextView,
        val routeNumberView: TextView,
        val detailsLayout: LinearLayout
    )
}
