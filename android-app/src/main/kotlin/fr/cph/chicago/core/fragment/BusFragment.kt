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

package fr.cph.chicago.core.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.adapter.BusAdapter
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.service.BusService
import org.apache.commons.lang3.StringUtils

/**
 * Bus Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusFragment : Fragment(R.layout.fragment_bus) {

    @BindView(R.id.bus_filter)
    lateinit var textFilter: EditText
    @BindView(R.id.bus_list)
    lateinit var listView: ListView

    private val busService: BusService = BusService

    private lateinit var busAdapter: BusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        App.checkBusData(mainActivity)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        addView()
    }

    fun update() {
        addView()
    }

    private fun addView() {
        busAdapter = BusAdapter()
        listView.adapter = busAdapter
        textFilter.visibility = TextView.VISIBLE
        textFilter.addTextChangedListener(object : TextWatcher {

            private var busRoutes: List<BusRoute> = listOf()

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                busRoutes = listOf()
            }

            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val busRoutes = busService.getBusRoutes()
                val trimmed = c.toString().trim { it <= ' ' }
                this.busRoutes = busRoutes.filter { (id, name) -> StringUtils.containsIgnoreCase(id, trimmed) || StringUtils.containsIgnoreCase(name, trimmed) }
            }

            override fun afterTextChanged(s: Editable) {
                busAdapter.busRoutes = this.busRoutes
                busAdapter.notifyDataSetChanged()
            }
        })
    }

    companion object {

        /**
         * Returns a new trainService of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return the fragment
         */
        fun newInstance(sectionNumber: Int): BusFragment {
            return Fragment.fragmentWithBundle(BusFragment(), sectionNumber) as BusFragment
        }
    }
}
