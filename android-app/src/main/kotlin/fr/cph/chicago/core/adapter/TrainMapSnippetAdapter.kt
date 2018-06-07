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
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import fr.cph.chicago.R
import fr.cph.chicago.entities.TrainEta

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainMapSnippetAdapter(private val trainEtas: List<TrainEta>) : BaseAdapter() {

    override fun getCount(): Int {
        return trainEtas.size
    }

    override fun getItem(position: Int): Any {
        return trainEtas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val eta = getItem(position) as TrainEta

        val holder: ViewHolder

        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_map_train, parent, false)

            holder = ViewHolder(
                view.findViewById(R.id.station_name),
                view.findViewById(R.id.time)
            )

            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.name.text = eta.station.name
        if (!(position == trainEtas.size - 1 && "0 min" == eta.timeLeftDueDelay)) {
            holder.time.text = eta.timeLeftDueDelay
            holder.name.setTextColor(ContextCompat.getColor(parent.context, R.color.grey_5))
            holder.name.gravity = Gravity.START
        } else {
            holder.name.setTextColor(ContextCompat.getColor(parent.context, R.color.grey))
            holder.name.gravity = Gravity.CENTER
        }
        return view
    }

    private class ViewHolder(val name: TextView, val time: TextView)
}
