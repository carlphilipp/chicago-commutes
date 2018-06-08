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
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.listener.TrainOnClickListener
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.LayoutUtil

/**
 * Adapter that will handle trains
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainAdapter(line: TrainLine) : BaseAdapter() {

    private val trainService = TrainService
    private val layoutUtil = LayoutUtil

    private val trainStations: List<TrainStation> = trainService.getStationsForLine(line)

    override fun getCount(): Int {
        return trainStations.size
    }

    override fun getItem(position: Int): Any {
        return trainStations[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val station = getItem(position) as TrainStation

        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_train, parent, false)

            holder = ViewHolder(
                view.findViewById(R.id.station_name_value),
                view.findViewById(R.id.station_color)
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.stationNameView.text = station.name
        holder.stationColorView.removeAllViews()
        station.lines
            .map { line -> layoutUtil.createColoredRoundForMultiple(line) }
            .forEach { layout -> holder.stationColorView.addView(layout) }

        view?.setOnClickListener(TrainOnClickListener(parent.context, station.id, station.lines))
        return view
    }

    private class ViewHolder(val stationNameView: TextView, val stationColorView: LinearLayout)
}
