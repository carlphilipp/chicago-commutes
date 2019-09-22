package fr.cph.chicago.core.fragment

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.service.PreferenceService

abstract class RefreshFragment(layout: Int) : Fragment(layout) {

    companion object {
        private val preferenceService = PreferenceService
    }

    @BindView(R.id.swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(savedInstanceState: Bundle?) {
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
}
