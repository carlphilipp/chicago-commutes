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
import android.util.Log
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
import fr.cph.chicago.redux.Status
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
class BusFragment : Fragment(R.layout.fragment_filter_list), StoreSubscriber<AppState> {

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

    private lateinit var busAdapter: BusAdapter

    override fun onCreateView(savedInstanceState: Bundle?) {
        busAdapter = BusAdapter()
        listView.adapter = busAdapter
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
        Log.d(TAG, "Bus stops new state")
        when (state.busRoutesStatus) {
            Status.SUCCESS -> showSuccessLayout()
            Status.FAILURE -> {
                Util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
                showSuccessLayout()
            }
            Status.FULL_FAILURE -> {
                Util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
                showFailureLayout()
            }
            else -> {
                Log.d(TAG, "Unknown status on new state")
                Util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
                showFailureLayout()
            }
        }
        updateData(state.busRoutes)
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

    private fun updateData(busRoutes: List<BusRoute>) {
        busAdapter.updateBusRoutes(busRoutes)
        busAdapter.notifyDataSetChanged()
        filter.addTextChangedListener(object : TextWatcher {

            private var busRoutesLocal: List<BusRoute> = listOf()

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                busRoutesLocal = listOf()
            }

            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = c.toString().trim { it <= ' ' }
                this.busRoutesLocal = busRoutes.filter { (id, name) -> StringUtils.containsIgnoreCase(id, trimmed) || StringUtils.containsIgnoreCase(name, trimmed) }
            }

            override fun afterTextChanged(s: Editable) {
                busAdapter.updateBusRoutes(this.busRoutesLocal)
                busAdapter.notifyDataSetChanged()
            }
        })
        filter.text = filter.text
    }

    companion object {

        private val TAG = BusFragment::class.java.simpleName

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
