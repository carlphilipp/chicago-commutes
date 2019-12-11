package fr.cph.chicago.core.fragment

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fr.cph.chicago.R
import fr.cph.chicago.service.PreferenceService

abstract class RefreshFragment(layout: Int) : Fragment(layout) {

    companion object {
        @JvmStatic
        protected val preferenceService = PreferenceService
    }

    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeColors(preferenceService.getColorSchemeColors(resources.configuration))
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(preferenceService.getProgressBackgroundColorSchemeResource(resources.configuration))
    }

    protected open fun startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(util.randomColor)
        swipeRefreshLayout.isRefreshing = true
    }

    protected fun stopRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun hasActionBar(): Boolean {
        return true
    }
}
