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
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.BikeAdapter
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeFragment : Fragment(R.layout.fragment_filter_list), StoreSubscriber<State> {

    @BindView(R.id.fragment_bike_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.success)
    lateinit var successLayout: LinearLayout
    @BindView(R.id.failure)
    lateinit var failureLayout: RelativeLayout
    @BindView(R.id.list)
    lateinit var listView: ListView
    @BindView(R.id.filter)
    lateinit var filter: EditText
    @BindView(R.id.retry_button)
    lateinit var retryButton: Button

    private lateinit var bikeAdapter: BikeAdapter

    override fun onCreateView(savedInstanceState: Bundle?) {
        bikeAdapter = BikeAdapter()
        listView.adapter = bikeAdapter
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
        mainActivity.toolbar.setOnMenuItemClickListener { startRefreshing(); true }
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
                Util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
                showSuccessLayout()
            }
            Status.FULL_FAILURE -> {
                Util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
                showFailureLayout()
            }
            else -> {
                Timber.d("Unknown status on new state")
                Util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
                showFailureLayout()
            }
        }
        updateData(state.bikeStations)
        swipeRefreshLayout.isRefreshing = false
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

    private fun startRefreshing() {
        swipeRefreshLayout.isRefreshing = true
        store.dispatch(BikeStationAction())
    }

    companion object {

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return the fragment
         */
        fun newInstance(sectionNumber: Int): BikeFragment {
            return fragmentWithBundle(BikeFragment(), sectionNumber) as BikeFragment
        }
    }
}
