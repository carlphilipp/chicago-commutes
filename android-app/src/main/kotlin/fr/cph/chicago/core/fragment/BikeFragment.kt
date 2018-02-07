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

package fr.cph.chicago.core.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.BikeAdapter
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils


/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeFragment : AbstractFragment() {

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
    private lateinit var bikeStations: List<BikeStation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        Util.trackScreen(getString(R.string.analytics_bike_fragment))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_bike, container, false)
        if (!mainActivity.isFinishing) {
            setBinder(rootView)
            bikeStations = mainActivity.intent.getParcelableArrayListExtra(bundleBikeStations) ?: listOf()
            if (bikeStations.isEmpty()) {
                loadError()
            } else {
                loadList()
            }
        }
        return rootView
    }

    private fun loadList() {
        bikeAdapter = BikeAdapter(bikeStations)
        bikeListView.adapter = bikeAdapter
        filter.addTextChangedListener(object : TextWatcher {

            private lateinit var bikeStations: List<BikeStation>

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                bikeStations = listOf()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                bikeStations = this@BikeFragment.bikeStations
                    .filter { (_, name) -> StringUtils.containsIgnoreCase(name, s.toString().trim { it <= ' ' }) }
            }

            override fun afterTextChanged(s: Editable) {
                bikeAdapter.bikeStations = this.bikeStations.toList()
                bikeAdapter.notifyDataSetChanged()
            }
        })
        bikeListView.visibility = ListView.VISIBLE
        filter.visibility = ListView.VISIBLE
        loadingLayout.visibility = RelativeLayout.INVISIBLE
        errorLayout.visibility = RelativeLayout.INVISIBLE
    }

    private fun loadError() {
        loadingLayout.visibility = RelativeLayout.INVISIBLE
        errorLayout.visibility = RelativeLayout.VISIBLE
    }

    fun setBikeStations(bikeStations: List<BikeStation>) {
        this.bikeStations = bikeStations
        loadList()
    }

    companion object {

        /**
         * Returns a new trainService of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return the fragment
         */
        fun newInstance(sectionNumber: Int): BikeFragment {
            return AbstractFragment.Companion.fragmentWithBundle(BikeFragment(), sectionNumber) as BikeFragment
        }
    }
}
