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
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.AlertActivity
import fr.cph.chicago.core.adapter.AlertAdapter
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.rx.ObservableUtil
import org.apache.commons.lang3.StringUtils

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertFragment : Fragment(R.layout.fragment_alert) {

    @BindView(R.id.alert_filter)
    lateinit var textFilter: EditText
    @BindView(R.id.alert_list)
    lateinit var listView: ListView
    @BindView(R.id.loading_relativeLayout)
    lateinit var loading: RelativeLayout

    override fun onCreateView(savedInstanceState: Bundle?) {
        loadingState()
        ObservableUtil.createAlertRoutesObservable().subscribe { routesAlerts ->
            val alertAdapter = AlertAdapter(routesAlerts)
            listView.adapter = alertAdapter
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
            textFilter.addTextChangedListener(object : TextWatcher {

                var routesAlertsDTOS: List<RoutesAlertsDTO> = listOf()

                override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                    this.routesAlertsDTOS = listOf()
                }

                override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                    val trimmed = c.toString().trim { it <= ' ' }
                    routesAlertsDTOS = routesAlerts.filter { (id, routeName) ->
                        StringUtils.containsIgnoreCase(routeName, trimmed) || StringUtils.containsIgnoreCase(id, trimmed)
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    alertAdapter.setAlerts(routesAlertsDTOS)
                    alertAdapter.notifyDataSetChanged()
                }
            })
            successState()
        }
    }

    override fun onResume() {
        super.onResume()
        textFilter.setText("")
    }

    private fun successState() {
        textFilter.visibility = ListView.VISIBLE
        listView.visibility = ListView.VISIBLE

        loading.visibility = RelativeLayout.INVISIBLE
    }

    private fun loadingState() {
        loading.visibility = RelativeLayout.VISIBLE

        textFilter.visibility = ListView.INVISIBLE
        listView.visibility = ListView.INVISIBLE
    }

    // TODO: Create an error state

    companion object {

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a train fragment
         */
        fun newInstance(sectionNumber: Int): AlertFragment {
            return Fragment.fragmentWithBundle(AlertFragment(), sectionNumber) as AlertFragment
        }
    }
}
