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

import android.content.Intent
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
import fr.cph.chicago.core.activity.AlertActivity
import fr.cph.chicago.core.adapter.AlertAdapter
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.redux.AlertAction
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber

/**
 * Alert Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertFragment : Fragment(R.layout.fragment_filter_list), StoreSubscriber<AppState> {

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

    private lateinit var alertAdapter: AlertAdapter

    override fun onCreateView(savedInstanceState: Bundle?) {
        alertAdapter = AlertAdapter()
        listView.adapter = alertAdapter
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
        mainActivity.toolbar.setOnMenuItemClickListener { startRefreshing(); true }
        retryButton.setOnClickListener { startRefreshing() }
    }

    override fun onResume() {
        super.onResume()
        mainStore.subscribe(this)
        if (mainStore.state.alertsDTO.isEmpty()) {
            swipeRefreshLayout.isRefreshing = true
            mainStore.dispatch(AlertAction())
        }
    }

    override fun onPause() {
        super.onPause()
        mainStore.unsubscribe(this)
    }

    override fun newState(state: AppState) {
        when {
            state.lastAction is AlertAction && state.alertError && state.alertsDTO.isEmpty() -> {
                showFailureLayout()
            }
            state.lastAction is AlertAction && state.alertError -> {
                Util.showSnackBar(swipeRefreshLayout, state.alertErrorMessage)
            }
            state.alertError -> showFailureLayout()
            else -> showSuccessLayout()
        }
        updateData(state.alertsDTO)
        swipeRefreshLayout.isRefreshing = false
    }

    private fun updateData(alertDTO: List<RoutesAlertsDTO>) {
        alertAdapter.setAlerts(alertDTO)
        alertAdapter.notifyDataSetChanged()
        listView.setOnItemClickListener { _, _, position, _ ->
            val (id1, routeName, _, _, _, _, alertType) = alertAdapter.getItem(position)
            val intent = Intent(context, AlertActivity::class.java)
            val extras = Bundle()
            extras.putString("routeId", id1)
            extras.putString("title", if (alertType === AlertType.TRAIN)
                routeName
            else
                "$id1 - $routeName")
            intent.putExtras(extras)
            startActivity(intent)
        }
        filter.addTextChangedListener(object : TextWatcher {

            var routesAlertsDTOS: List<RoutesAlertsDTO> = listOf()

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                this.routesAlertsDTOS = listOf()
            }

            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = c.toString().trim { it <= ' ' }
                routesAlertsDTOS = alertDTO.filter { (id, routeName) ->
                    StringUtils.containsIgnoreCase(routeName, trimmed) || StringUtils.containsIgnoreCase(id, trimmed)
                }
            }

            override fun afterTextChanged(s: Editable) {
                alertAdapter.setAlerts(routesAlertsDTOS)
                alertAdapter.notifyDataSetChanged()
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
        mainStore.dispatch(AlertAction())
    }

    companion object {

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a train fragment
         */
        fun newInstance(sectionNumber: Int): AlertFragment {
            return fragmentWithBundle(AlertFragment(), sectionNumber) as AlertFragment
        }
    }
}
