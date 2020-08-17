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

package fr.cph.chicago.core.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.adapter.BikeAdapter
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import kotlinx.android.synthetic.main.error.failureLayout
import kotlinx.android.synthetic.main.error.retryButton
import kotlinx.android.synthetic.main.fragment_filter_list.filter
import kotlinx.android.synthetic.main.fragment_filter_list.listView
import kotlinx.android.synthetic.main.fragment_filter_list.successLayout
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeFragment : RefreshFragment(R.layout.fragment_filter_list), StoreSubscriber<State> {

    companion object {
        fun newInstance(sectionNumber: Int): BikeFragment {
            return fragmentWithBundle(BikeFragment(), sectionNumber) as BikeFragment
        }
    }

    private lateinit var bikeAdapter: BikeAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bikeAdapter = BikeAdapter()
        listView.adapter = bikeAdapter
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
        (activity as MainActivity).tb.setOnMenuItemClickListener { startRefreshing(); true }
        retryButton.setOnClickListener { startRefreshing() }
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        if (store.state.bikeStations.isEmpty()) {
            store.dispatch(BikeStationAction())
        }
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun newState(state: State) {
        Timber.d("Bike stations new state")
        when (state.bikeStationsStatus) {
            Status.SUCCESS, Status.ADD_FAVORITES, Status.REMOVE_FAVORITES -> showSuccessLayout()
            Status.FAILURE -> {
                util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
                showSuccessLayout()
            }
            Status.FULL_FAILURE -> showFailureLayout()
            else -> {
                Timber.d("Unknown status on new state")
                util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
                showFailureLayout()
            }
        }
        updateData(state.bikeStations)
        stopRefreshing()
    }

    private fun updateData(bikeStations: List<BikeStation>) {
        bikeAdapter.updateBikeStations(bikeStations)
        bikeAdapter.notifyDataSetChanged()

        filter.addTextChangedListener(object : TextWatcher {

            private lateinit var bikeStationsLocal: List<BikeStation>

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                bikeStationsLocal = listOf()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                bikeStationsLocal = bikeStations
                    .filter { station -> StringUtils.containsIgnoreCase(station.name, s.toString().trim { it <= ' ' }) }
            }

            override fun afterTextChanged(s: Editable) {
                bikeAdapter.updateBikeStations(this.bikeStationsLocal)
                bikeAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun showSuccessLayout() {
        successLayout.visibility = View.VISIBLE
        failureLayout.visibility = View.GONE
    }

    private fun showFailureLayout() {
        successLayout.visibility = View.GONE
        failureLayout.visibility = View.VISIBLE
    }

    override fun startRefreshing() {
        super.startRefreshing()
        store.dispatch(BikeStationAction())
    }
}
