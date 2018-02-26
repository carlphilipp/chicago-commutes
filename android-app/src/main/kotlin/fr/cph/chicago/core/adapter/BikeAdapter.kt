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

package fr.cph.chicago.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import fr.cph.chicago.R
import fr.cph.chicago.core.listener.BikeStationOnClickListener
import fr.cph.chicago.entity.bike.DivvyStation

/**
 * Adapter that will handle bikes
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeAdapter(var divvyStations: List<DivvyStation>) : BaseAdapter() {

    override fun getCount(): Int {
        return divvyStations.size
    }

    override fun getItem(position: Int): Any {
        return divvyStations[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val station = getItem(position) as DivvyStation

        val holder: ViewHolder

        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_bike, parent, false)
            holder = ViewHolder(view.findViewById(R.id.station_name))
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.stationNameView.text = station.name

        view?.setOnClickListener(BikeStationOnClickListener(station))
        return view
    }

    /**
     * DP view holder
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    private class ViewHolder(val stationNameView: TextView)
}
