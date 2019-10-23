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
import fr.cph.chicago.core.model.enumeration.TrainLine

/**
 * Adapter that will handle Train station list
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainStationAdapter : BaseAdapter() {

    override fun getCount(): Int {
        return TrainLine.size() - 1 // Do not display "NA"
    }

    override fun getItem(position: Int): Any {
        return TrainLine.values()[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_train_line, parent, false)

        val color = view.findViewById<LinearLayout>(R.id.station_color_value)
        color.setBackgroundColor(TrainLine.values()[position].color)

        val stationName = view.findViewById<TextView>(R.id.station_name_value)
        stationName.text = TrainLine.values()[position].toString()
        return view
    }
}
