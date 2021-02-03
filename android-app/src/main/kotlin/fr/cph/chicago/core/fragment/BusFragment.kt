/**
 * Copyright 2021 Carl-Philipp Harmant
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
import fr.cph.chicago.core.adapter.BusAdapter
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.redux.BusRoutesAction
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
 * Bus Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusFragment : RefreshFragment(R.layout.fragment_filter_list), StoreSubscriber<State> {

    companion object {
        fun newInstance(sectionNumber: Int): BusFragment {
            return fragmentWithBundle(BusFragment(), sectionNumber) as BusFragment
        }
    }

    private lateinit var busAdapter: BusAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        busAdapter = BusAdapter()
        listView.adapter = busAdapter
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
        (activity as MainActivity).tb.setOnMenuItemClickListener { startRefreshing(); true }
        retryButton.setOnClickListener { startRefreshing() }
    }


    override fun onResume() {
        super.onResume()
        store.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun newState(state: State) {
        Timber.d("Bus stops new state")
        when (state.busRoutesStatus) {
            Status.SUCCESS -> showSuccessLayout()
            Status.FAILURE -> {
                showSuccessLayout()
            }
            Status.FULL_FAILURE -> {
                showFailureLayout()
            }
            else -> {
                Timber.d("Unknown status on new state")
                util.showSnackBar(swipeRefreshLayout, state.busRoutesErrorMessage)
                showFailureLayout()
            }
        }
        updateData(state.busRoutes)
        stopRefreshing()
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
        store.dispatch(BusRoutesAction())
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
}
