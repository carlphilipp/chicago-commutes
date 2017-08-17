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
import fr.cph.chicago.entity.Eta

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainMapSnippetAdapter(private val etas: List<Eta>) : BaseAdapter() {

    override fun getCount(): Int {
        return etas.size
    }

    override fun getItem(position: Int): Any {
        return etas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val eta = getItem(position) as Eta

        val holder: ViewHolder

        if (convertView == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = vi.inflate(R.layout.list_map_train, parent, false)

            holder = ViewHolder()
            holder.name = convertView!!.findViewById(R.id.station_name)
            holder.time = convertView.findViewById(R.id.time)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.name!!.text = eta.station.name
        if (!(position == etas.size - 1 && "0 min" == eta.timeLeftDueDelay)) {
            holder.time!!.text = eta.timeLeftDueDelay
        } else {
            holder.name!!.setTextColor(ContextCompat.getColor(parent.context, R.color.grey))
            holder.name!!.gravity = Gravity.CENTER
        }
        return convertView
    }

    private class ViewHolder {
        internal var name: TextView? = null
        internal var time: TextView? = null
    }
}
