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

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.AlertActivity
import fr.cph.chicago.core.adapter.AlertAdapter
import fr.cph.chicago.entity.dto.AlertType
import fr.cph.chicago.entity.dto.RoutesAlertsDTO
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertFragment : AbstractFragment() {

    @BindView(R.id.alert_filter)
    lateinit var textFilter: EditText
    @BindView(R.id.alert_list)
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Util.trackScreen(getString(R.string.analytics_cta_alert_fragment))
    }

    override fun onResume() {
        super.onResume()
        textFilter.setText("")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_alert, container, false)
        setBinder(rootView)
        ObservableUtil.createAlertRoutesObservable()
            .subscribe { routesAlertsDTOS ->
                val alertAdapter = AlertAdapter(routesAlertsDTOS)
                listView.adapter = alertAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val (id1, routeName, _, _, _, _, alertType) = alertAdapter.getItem(position)
                    val intent = Intent(context, AlertActivity::class.java)
                    val extras = Bundle()
                    extras.putString("routeId", id1)
                    extras.putString("title", if (alertType === AlertType.TRAIN)
                        routeName
                    else
                        id1 + " - " + routeName)
                    intent.putExtras(extras)
                    startActivity(intent)
                }
                textFilter.addTextChangedListener(object : TextWatcher {

                    var routesAlertsDTOS: MutableList<RoutesAlertsDTO> = mutableListOf()

                    override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                        this.routesAlertsDTOS = mutableListOf()
                    }

                    override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                        val trimmed = c.toString().trim { it <= ' ' }
                        this.routesAlertsDTOS.addAll(
                            routesAlertsDTOS
                                .filter { (id, routeName) -> StringUtils.containsIgnoreCase(routeName, trimmed) || StringUtils.containsIgnoreCase(id, trimmed) }
                        )
                    }

                    override fun afterTextChanged(s: Editable) {
                        alertAdapter.setAlerts(routesAlertsDTOS!!)
                        alertAdapter.notifyDataSetChanged()
                    }
                })
            }
        return rootView
    }

    companion object {

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a train fragment
         */
        fun newInstance(sectionNumber: Int): AlertFragment {
            return AbstractFragment.fragmentWithBundle(AlertFragment(), sectionNumber) as AlertFragment
        }
    }
}
