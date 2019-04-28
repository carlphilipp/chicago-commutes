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
import fr.cph.chicago.core.adapter.BusAdapter
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.BusRoutesAction
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber

/**
 * Bus Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusFragment : Fragment(R.layout.fragment_bus_bike), StoreSubscriber<AppState> {

    @BindView(R.id.fragment_bike_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.success)
    lateinit var successLayout: LinearLayout
    @BindView(R.id.failure)
    lateinit var failureLayout: RelativeLayout
    @BindView(R.id.filter)
    lateinit var textFilter: EditText
    @BindView(R.id.list)
    lateinit var listView: ListView
    @BindView(R.id.retry_button)
    lateinit var retryButton: Button

    private lateinit var busAdapter: BusAdapter
    private var busRoutes = listOf<BusRoute>()

    override fun onCreateView(savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
        mainActivity.toolbar.setOnMenuItemClickListener { startRefreshing(); true }
        retryButton.setOnClickListener { startRefreshing() }
    }

    override fun onResume() {
        super.onResume()
        mainStore.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        mainStore.unsubscribe(this)
    }

    override fun newState(state: AppState) {
        when {
            state.lastAction is BusRoutesAction && state.busRoutesError && state.busRoutes.isEmpty() -> {
                Util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
                showFailureLayout()
            }
            state.lastAction is BusRoutesAction && state.busRoutesError -> {
                Util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
            }
            state.busRoutesError -> {
                showFailureLayout()
            }
            else -> {
                this.busRoutes = state.busRoutes
                showSuccessLayout()
                displayData()
            }
        }
        swipeRefreshLayout.isRefreshing = false
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
        mainStore.dispatch(BusRoutesAction())
    }

    private fun displayData() {
        busAdapter = BusAdapter(this.busRoutes)
        listView.adapter = busAdapter
        textFilter.addTextChangedListener(object : TextWatcher {

            private var busRoutes: List<BusRoute> = listOf()

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                busRoutes = listOf()
            }

            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val busRoutes = this@BusFragment.busRoutes
                val trimmed = c.toString().trim { it <= ' ' }
                this.busRoutes = busRoutes.filter { (id, name) -> StringUtils.containsIgnoreCase(id, trimmed) || StringUtils.containsIgnoreCase(name, trimmed) }
            }

            override fun afterTextChanged(s: Editable) {
                busAdapter.updateBusRoutes(this.busRoutes)
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
            return fragmentWithBundle(BusFragment(), sectionNumber) as BusFragment
        }
    }
}
