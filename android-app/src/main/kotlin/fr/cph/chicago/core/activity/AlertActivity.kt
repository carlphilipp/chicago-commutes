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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.AlertRouteAdapter
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_alert.listView
import kotlinx.android.synthetic.main.activity_alert.scrollView
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class AlertActivity : AppCompatActivity() {

    companion object {
        private val alertService = AlertService
        private val util = Util
    }

    private lateinit var routeId: String
    private lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_alert)
            routeId = intent.getStringExtra("routeId") ?: StringUtils.EMPTY
            title = intent.getStringExtra("title") ?: StringUtils.EMPTY
            scrollView.setOnRefreshListener { this.refreshData() }
            setToolBar()
            refreshData()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        routeId = savedInstanceState.getString("routeId") ?: StringUtils.EMPTY
        title = savedInstanceState.getString("title") ?: StringUtils.EMPTY
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::routeId.isInitialized) savedInstanceState.putString("routeId", routeId)
        if (::title.isInitialized) savedInstanceState.putString("title", title)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            scrollView.isRefreshing = true
            refreshData()
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = title
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }

    @SuppressLint("CheckResult")
    private fun refreshData() {
        alertService.routeAlertForId(routeId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { routeAlertsDTOS ->
                    val ada = AlertRouteAdapter(routeAlertsDTOS)
                    listView.adapter = ada
                    if (routeAlertsDTOS.isEmpty()) {
                        util.showSnackBar(view = listView, text = this@AlertActivity.getString(R.string.message_no_alerts))
                    }
                    hideAnimation()
                },
                { error ->
                    Timber.e(error, "Error while refreshing data")
                    util.showOopsSomethingWentWrong(listView)
                    hideAnimation()
                })
    }

    private fun hideAnimation() {
        if (scrollView.isRefreshing) {
            scrollView.isRefreshing = false
        }
    }
}
