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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import java.util.ArrayList

import fr.cph.chicago.R
import fr.cph.chicago.entity.BusStop

/**
 * Adapter that will handle buses bound
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundAdapter : BaseAdapter() {

    private var busStops: List<BusStop>? = null

    init {
        this.busStops = ArrayList()
    }

    override fun getCount(): Int {
        return busStops!!.size
    }

    override fun getItem(position: Int): Any {
        return busStops!![position]
    }

    override fun getItemId(position: Int): Long {
        return busStops!![position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val busStop = busStops!![position]

        val routNameView: TextView?

        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_bus_bounds, parent, false)

            val holder = ViewHolder()

            routNameView = view!!.findViewById<View>(R.id.station_name) as TextView
            holder.routNameView = routNameView

            view.tag = holder
        } else {
            val viewHolder = view.tag as ViewHolder
            routNameView = viewHolder.routNameView
        }
        routNameView!!.text = busStop.name

        return view
    }

    /**
     * DP view holder
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    private class ViewHolder {
        internal var routNameView: TextView? = null
    }

    /**
     * Update of the bus stops
     */
    fun update(busStops: List<BusStop>) {
        this.busStops = busStops
    }
}
