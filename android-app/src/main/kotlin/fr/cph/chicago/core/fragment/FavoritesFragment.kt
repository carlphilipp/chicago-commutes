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
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.adapter.FavoritesAdapter
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.LoadFavoritesDataAction
import fr.cph.chicago.redux.LoadFirstDataAction
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.task.RefreshTimingTask
import fr.cph.chicago.util.RateUtil
import fr.cph.chicago.util.Util
import org.rekotlin.StoreSubscriber

/**
 * Favorites Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class FavoritesFragment : Fragment(R.layout.fragment_main), StoreSubscriber<AppState> {

    @BindView(R.id.welcome)
    lateinit var welcomeLayout: RelativeLayout
    @BindView(R.id.activity_main_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.favorites_list)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.floating_button)
    lateinit var floatingButton: FloatingActionButton

    private val rateUtil: RateUtil = RateUtil
    private val util: Util = Util
    private val preferenceService: PreferenceService = PreferenceService

    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var refreshTimingTask: RefreshTimingTask

    override fun onCreateView(savedInstanceState: Bundle?) {
        favoritesAdapter = FavoritesAdapter(mainActivity)

        recyclerView.adapter = favoritesAdapter
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

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.setColorSchemeColors(util.randomColor)
            mainStore.dispatch(LoadFavoritesDataAction())
        }

        mainActivity.toolbar.setOnMenuItemClickListener {
            startRefreshing()
            mainStore.dispatch(LoadFavoritesDataAction())
            true
        }

        startRefreshTask()
        rateUtil.displayRateSnackBarIfNeeded(swipeRefreshLayout, mainActivity)
    }

    override fun onPause() {
        super.onPause()
        refreshTimingTask.cancel(true)
        mainStore.unsubscribe(this)
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
        mainStore.subscribe(this)
        if (mainStore.state.busRoutes.isEmpty() || mainStore.state.bikeStations.isEmpty()) {
            Log.e(TAG, "Load bus routes and bike stations")
            mainStore.dispatch(LoadFirstDataAction())
        }
        if (App.instance.refresh) {
            App.instance.refresh = false
            mainStore.dispatch(LoadFavoritesDataAction())
        }
        favoritesAdapter.refreshFavorites()
        favoritesAdapter.notifyDataSetChanged()
        if (refreshTimingTask.status == Status.FINISHED) {
            startRefreshTask()
        }
        val hasFav = preferenceService.hasFavorites()
        welcomeLayout.visibility = if (hasFav) View.GONE else View.VISIBLE
    }

    override fun newState(state: AppState) {
        Log.e(TAG, "Favorites new state")
        if (state.error != null && state.error) {
            displayError(R.string.message_something_went_wrong)
        } else {
            favoritesAdapter.updateData(
                date = state.lastUpdate,
                trainArrivals = state.trainArrivalsDTO.trainArrivalSparseArray,
                busArrivals = state.busArrivalsDTO.busArrivals,
                bikeStations = state.bikeStations
            )
            if (state.highlightBackground) {
                highlightBackground()
            }
        }
        stopRefreshing()
    }

    /**
     * Display error
     *
     * @param message the message
     */
    private fun displayError(message: Int) {
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

    private fun highlightBackground() {
        val currentBackground = rootView.background
        rootView.setBackgroundResource(R.drawable.highlight_selector)
        rootView.postDelayed({ rootView.background = currentBackground }, 100)
    }

    /**
     * Start refreshBusAndStation task
     */
    private fun startRefreshTask() {
        refreshTimingTask = RefreshTimingTask(favoritesAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) as RefreshTimingTask
        favoritesAdapter.refreshLastUpdateView()
    }

    companion object {

        private val TAG = FavoritesFragment::class.java.simpleName

        /**
         * Returns a new trainService of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a favorite fragment
         */
        fun newInstance(sectionNumber: Int): FavoritesFragment {
            return fragmentWithBundle(FavoritesFragment(), sectionNumber) as FavoritesFragment
        }
    }
}
