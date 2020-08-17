/**
 * Copyright 2020 Carl-Philipp Harmant
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
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import fr.cph.chicago.R

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
internal class PopupFavoritesTrainAdapter(context: Context, private val values: List<String>, private val colors: List<Int>) : ArrayAdapter<String>(context, R.layout.popup_train_cell, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.popup_train_cell, parent, false)
            holder = ViewHolder(view.findViewById(R.id.popup_train_map), view.findViewById(R.id.label))
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.label.text = values[position]
        holder.imageView.setColorFilter(colors[position])
        return view!!
    }

    private class ViewHolder(val imageView: ImageView, val label: TextView)
}
