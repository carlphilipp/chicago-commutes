/**
 * Copyright 2021 Carl-Philipp Harmant
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
import fr.cph.chicago.core.model.BikeStation

/**
 * Adapter that will handle bikes
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeAdapter(private var bikeStations: List<BikeStation> = listOf()) : BaseAdapter() {

    override fun getCount(): Int {
        return bikeStations.size
    }

    override fun getItem(position: Int): Any {
        return bikeStations[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val station = getItem(position) as BikeStation

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

    fun updateBikeStations(bikeStations: List<BikeStation>) {
        this.bikeStations = bikeStations
    }

    private class ViewHolder(val stationNameView: TextView)
}
