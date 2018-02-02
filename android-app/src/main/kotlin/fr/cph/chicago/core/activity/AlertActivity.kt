package fr.cph.chicago.core.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.widget.ListView

import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.AlertRouteAdapter
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.Util

class AlertActivity : Activity() {

    @BindView(R.id.activity_alerts_swipe_refresh_layout)
    lateinit var scrollView: SwipeRefreshLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.alert_route_list)
    lateinit var listView: ListView

    private var routeId: String? = null
    private var title: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_alert)
            ButterKnife.bind(this)
            val extras = intent.extras
            routeId = extras!!.getString("routeId", "")
            title = extras.getString("title", "<Template title>")
            scrollView.setOnRefreshListener({ this.refreshData() })
            setToolBar()
            refreshData()
        }
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { item ->
            scrollView.isRefreshing = true
            refreshData()
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = title
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { v -> finish() }
    }

    private fun refreshData() {
        ObservableUtil.createAlertRouteObservable(routeId!!).subscribe { routeAlertsDTOS ->
            val ada = AlertRouteAdapter(routeAlertsDTOS)
            listView.adapter = ada
            if (scrollView.isRefreshing) {
                scrollView.isRefreshing = false
            }
            if (routeAlertsDTOS.isEmpty()) {
                Util.showSnackBar(listView, this@AlertActivity.getString(R.string.message_no_alerts))
            }
        }
    }
}
