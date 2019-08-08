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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.adapter.AlertRouteAdapter
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class AlertActivity : ButterKnifeActivity(R.layout.activity_alert) {

    companion object {
        private val alertService = AlertService
        private val util = Util
    }

    @BindView(R.id.activity_alerts_swipe_refresh_layout)
    lateinit var scrollView: SwipeRefreshLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.alert_route_list)
    lateinit var listView: ListView

    private lateinit var routeId: String
    private lateinit var title: String

    override fun create(savedInstanceState: Bundle?) {
        routeId = intent.getStringExtra("routeId") ?: StringUtils.EMPTY
        title = intent.getStringExtra("title") ?: StringUtils.EMPTY
        scrollView.setOnRefreshListener { this.refreshData() }
        setToolBar()
        refreshData()
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
