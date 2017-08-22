/**
 * Copyright 2017 Carl-Philipp Harmant
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
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.entity.BusArrival

/**
 * Adapter that will handle bus map
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusMapSnippetAdapter(private val arrivals: List<BusArrival>) : BaseAdapter() {

    override fun getCount(): Int {
        return arrivals.size
    }

    override fun getItem(position: Int): Any {
        return arrivals[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val arrival = getItem(position) as BusArrival
        println("${arrivals.size - 1} $position $arrival")
        val viewHolder: ViewHolder
        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_map_train, parent, false)!!
            viewHolder = ViewHolder(
                view.findViewById(R.id.station_name),
                view.findViewById(R.id.time)
            )
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.stationName.text = arrival.stopName
        println("${position == (arrivals.size - 1)}")
        if (position == (arrivals.size - 1) && "No service" == arrival.timeLeftDueDelay) {
            viewHolder.stationName.setTextColor(ContextCompat.getColor(parent.context, R.color.grey))
            viewHolder.stationName.gravity = Gravity.CENTER
        } else {
            viewHolder.time.text = arrival.timeLeftDueDelay
            viewHolder.stationName.gravity = Gravity.START
            viewHolder.stationName.setTextColor(ContextCompat.getColor(parent.context, R.color.grey_5))
        }
        return view
    }

    private class ViewHolder(val stationName: TextView, val time: TextView)
}
