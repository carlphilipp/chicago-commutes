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

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.cph.chicago.core.activity.AlertActivity
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.adapter.AlertAdapter
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.databinding.FragmentFilterListBinding
import fr.cph.chicago.redux.AlertAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Alert Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertFragment : RefreshFragment(), StoreSubscriber<State> {

    companion object {
        fun newInstance(sectionNumber: Int): AlertFragment {
            return fragmentWithBundle(AlertFragment(), sectionNumber) as AlertFragment
        }
    }

    private lateinit var adapter: AlertAdapter
    private var _binding: FragmentFilterListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFilterListBinding.inflate(inflater, container, false)
        setUpSwipeRefreshLayout(binding.swipeRefreshLayout)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = AlertAdapter()
        binding.listView.adapter = adapter
        (activity as MainActivity).toolBar.setOnMenuItemClickListener { startRefreshing(); true }
        binding.error.retryButton.setOnClickListener { startRefreshing() }
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        if (store.state.alertsDTO.isEmpty()) {            startRefreshing()
            store.dispatch(AlertAction())
        }
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun newState(state: State) {
        Timber.d("Alert new state")
        when (state.alertStatus) {
            Status.SUCCESS -> showSuccessLayout()
            Status.FAILURE -> util.showSnackBar(swipeRefreshLayout, state.alertErrorMessage)
            Status.FULL_FAILURE -> {
                util.showSnackBar(swipeRefreshLayout, state.alertErrorMessage)
                showFailureLayout()
            }
            else -> Timber.d("Unknown status on new state")
        }
        stopRefreshing()
        updateData(state.alertsDTO)
    }

    override fun startRefreshing() {
        super.startRefreshing()
        store.dispatch(AlertAction())
    }

    private fun updateData(alertDTO: List<RoutesAlertsDTO>) {
        adapter.setAlerts(alertDTO)
        adapter.notifyDataSetChanged()
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val (id1, routeName, _, _, _, _, alertType) = adapter.getItem(position)
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
        binding.filter.addTextChangedListener(object : TextWatcher {

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
                adapter.setAlerts(routesAlertsDTOS)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun showSuccessLayout() {
        binding.successLayout.visibility = View.VISIBLE
        binding.error.failureLayout.visibility = View.GONE
    }

    private fun showFailureLayout() {
        binding.successLayout.visibility = View.GONE
        binding.error.failureLayout.visibility = View.VISIBLE
    }
}
