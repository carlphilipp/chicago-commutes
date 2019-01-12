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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.cph.chicago.R

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class PopupBusAdapter(context: Context, private val values: List<String>) : ArrayAdapter<String>(context, R.layout.popup_bus_cell, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View
        view = if (position != values.size - 1) {
            vi.inflate(R.layout.popup_bus_cell_0, parent, false)
        } else {
            vi.inflate(R.layout.popup_bus_cell, parent, false)
        }
        val textView: TextView = view.findViewById(R.id.label)
        textView.text = values[position]
        return view
    }
}
