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
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import fr.cph.chicago.R

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class PopupTrainAdapter(context: Context, private val values: List<String>, private val colors: List<Int>) : ArrayAdapter<String>(context, R.layout.popup_train_cell, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View
        if (position == 0) {
            rowView = inflater.inflate(R.layout.popup_train_cell_0, parent, false)
        } else {
            rowView = inflater.inflate(R.layout.popup_train_cell, parent, false)
            val imageView: ImageView = rowView.findViewById(R.id.popup_train_map)
            imageView.setColorFilter(colors[position - 1])
        }
        val textView: TextView = rowView.findViewById(R.id.label)
        textView.text = values[position]
        return rowView
    }
}
