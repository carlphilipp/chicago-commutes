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
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.cph.chicago.R
import fr.cph.chicago.R.string
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.adapter.FavoritesAdapter
import fr.cph.chicago.redux.BusRoutesAction
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status.FAILURE
import fr.cph.chicago.redux.Status.FULL_FAILURE
import fr.cph.chicago.redux.Status.SUCCESS
import fr.cph.chicago.redux.Status.UNKNOWN
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.task.RefreshTimingTask
import fr.cph.chicago.util.RateUtil
import fr.cph.chicago.util.Util
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Favorites Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class FavoritesFragment : Fragment(R.layout.fragment_main), StoreSubscriber<State> {

    companion object {

        private val rateUtil = RateUtil
        private val util = Util
        private val preferenceService = PreferenceService

        /**
         * Returns a new service of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a favorite fragment
         */
        fun newInstance(sectionNumber: Int): FavoritesFragment {
            return fragmentWithBundle(FavoritesFragment(), sectionNumber) as FavoritesFragment
        }
    }

    @BindView(R.id.welcome)
    lateinit var welcomeLayout: RelativeLayout
    @BindView(R.id.activity_main_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.favorites_list)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.floating_button)
    lateinit var floatingButton: FloatingActionButton
    @BindView(R.id.failure)
    lateinit var failureLayout: RelativeLayout
    @BindView(R.id.retry_button)
    lateinit var retryButton: Button

    private lateinit var adapter: FavoritesAdapter
    private lateinit var refreshTimingTask: RefreshTimingTask

    override fun onCreateView(savedInstanceState: Bundle?) {
        adapter = FavoritesAdapter(mainActivity)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        floatingButton.setOnClickListener { mainActivity.startActivity(Intent(mainActivity, SearchActivity::class.java)) }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && floatingButton.isShown) {
                    floatingButton.hide()
                } else if (dy < 0 && !floatingButton.isShown) {
                    floatingButton.show()
                }
            }
        })
        swipeRefreshLayout.setOnRefreshListener { reloadData() }
        retryButton.setOnClickListener { reloadData() }
        mainActivity.toolbar.setOnMenuItemClickListener { reloadData(); true }

        startRefreshTask()
        rateUtil.displayRateSnackBarIfNeeded(swipeRefreshLayout, mainActivity)
    }

    override fun onPause() {
        super.onPause()
        refreshTimingTask.cancel(true)
        store.unsubscribe(this)
    }

    override fun onStop() {
        super.onStop()
        refreshTimingTask.cancel(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshTimingTask.cancel(true)
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        if (store.state.busRoutes.isEmpty() || store.state.bikeStations.isEmpty()) {
            store.dispatch(BusRoutesAndBikeStationAction())
        }
        if (App.instance.refresh) {
            App.instance.refresh = false
            store.dispatch(FavoritesAction())
        }
        adapter.refreshFavorites()
        adapter.notifyDataSetChanged()
        if (refreshTimingTask.status == Status.FINISHED) {
            startRefreshTask()
        }
    }

    override fun newState(state: State) {
        Timber.d("Favorites new state with status %s", state.status)
        when (state.status) {
            SUCCESS -> {
                showSuccessUi()
                stopRefreshing()
            }
            FAILURE -> {
                showSuccessUi()
                displayErrorSnackBar(string.message_something_went_wrong)
                stopRefreshing()
            }
            FULL_FAILURE -> {
                showFullFailureUi()
                displayErrorSnackBar(string.message_something_went_wrong)
                stopRefreshing()
            }
            UNKNOWN -> Timber.d("Unknown status on new state")
            else -> Timber.d("Unknown status on new state")
        }
        adapter.update()
    }

    private fun showSuccessUi() {
        if (failureLayout.visibility != View.GONE) failureLayout.visibility = View.GONE
        welcomeLayout.visibility = if (preferenceService.hasFavorites()) View.GONE else View.VISIBLE
        if (recyclerView.visibility != View.VISIBLE) recyclerView.visibility = View.VISIBLE
    }

    private fun showFullFailureUi() {
        if (failureLayout.visibility != View.VISIBLE) failureLayout.visibility = View.VISIBLE
        if (welcomeLayout.visibility != View.GONE) welcomeLayout.visibility = View.GONE
        if (recyclerView.visibility != View.GONE) recyclerView.visibility = View.GONE
    }

    private fun reloadData() {
        startRefreshing()
        store.dispatch(FavoritesAction())
        if (store.state.busRoutes.isEmpty()) { // Bike station is done already in the previous action
            store.dispatch(BusRoutesAction())
        }
    }

    private fun displayErrorSnackBar(message: Int) {
        util.showSnackBar(swipeRefreshLayout, message)
        stopRefreshing()
    }

    private fun startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(util.randomColor)
        swipeRefreshLayout.isRefreshing = true
    }

    private fun stopRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    /**
     * Start refreshBusAndStation task
     */
    private fun startRefreshTask() {
        refreshTimingTask = RefreshTimingTask(adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) as RefreshTimingTask
        adapter.update()
    }
}
