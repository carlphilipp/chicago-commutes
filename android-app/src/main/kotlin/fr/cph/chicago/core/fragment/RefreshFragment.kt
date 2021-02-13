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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fr.cph.chicago.service.PreferenceService

abstract class RefreshFragment : Fragment() {

    companion object {
        @JvmStatic
        protected val preferenceService = PreferenceService
    }

    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    protected fun setUpSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener { startRefreshing() }
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
