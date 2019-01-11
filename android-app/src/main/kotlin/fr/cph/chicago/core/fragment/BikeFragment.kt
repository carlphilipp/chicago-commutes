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
import android.widget.RelativeLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.BikeAdapter
import fr.cph.chicago.core.model.BikeStation
import org.apache.commons.lang3.StringUtils

/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeFragment : Fragment(R.layout.fragment_bike) {

    @BindView(R.id.loading_relativeLayout)
    lateinit var loadingLayout: RelativeLayout
    @BindView(R.id.bike_list)
    lateinit var bikeListView: ListView
    @BindView(R.id.bike_filter)
    lateinit var filter: EditText
    @BindView(R.id.error_layout)
    lateinit var errorLayout: RelativeLayout

    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String

    private lateinit var bikeAdapter: BikeAdapter
    private lateinit var divvyStations: List<BikeStation>

    private var isFailure = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        loadingState()
    }

    override fun onResume() {
        super.onResume()
        divvyStations = mainActivity.intent.getParcelableArrayListExtra(bundleBikeStations) ?: listOf()
        if (isFailure) {
            errorState()
        } else if (divvyStations.isNotEmpty()) {
            loadList()
        }

    }

    private fun loadList() {
        bikeAdapter = BikeAdapter(divvyStations)
        bikeListView.adapter = bikeAdapter
        filter.addTextChangedListener(object : TextWatcher {

            private lateinit var divvyStations: List<BikeStation>

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                divvyStations = listOf()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                divvyStations = this@BikeFragment.divvyStations
                    .filter { station -> StringUtils.containsIgnoreCase(station.name, s.toString().trim { it <= ' ' }) }
            }

            override fun afterTextChanged(s: Editable) {
                bikeAdapter.updateBikeStations(this.divvyStations)
                bikeAdapter.notifyDataSetChanged()
            }
        })
        successState()
    }

    private fun successState() {
        filter.visibility = ListView.VISIBLE
        bikeListView.visibility = ListView.VISIBLE

        loadingLayout.visibility = RelativeLayout.INVISIBLE
        errorLayout.visibility = RelativeLayout.INVISIBLE
    }

    private fun loadingState() {
        loadingLayout.visibility = RelativeLayout.VISIBLE

        errorLayout.visibility = RelativeLayout.INVISIBLE
        filter.visibility = ListView.INVISIBLE
        bikeListView.visibility = ListView.INVISIBLE
    }

    private fun errorState() {
        errorLayout.visibility = RelativeLayout.VISIBLE

        loadingLayout.visibility = RelativeLayout.INVISIBLE
        filter.visibility = ListView.INVISIBLE
        bikeListView.visibility = ListView.INVISIBLE
    }

    fun setBikeStations(divvyStations: List<BikeStation>) {
        this.isFailure = divvyStations.isEmpty()
        this.divvyStations = divvyStations
        loadList()
    }

    fun setFailure() {
        isFailure = true
    }

    companion object {

        /**
         * Returns a new trainService of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return the fragment
         */
        fun newInstance(sectionNumber: Int): BikeFragment {
            return Fragment.fragmentWithBundle(BikeFragment(), sectionNumber) as BikeFragment
        }
    }
}
