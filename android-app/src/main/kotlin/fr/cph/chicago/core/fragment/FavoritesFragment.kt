/**
 * Copyright 2020 Carl-Philipp Harmant
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
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.adapter.FavoritesAdapter
import fr.cph.chicago.redux.BusRoutesAction
import fr.cph.chicago.redux.BusRoutesAndBikeStationAction
import fr.cph.chicago.redux.FavoritesAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status.FAILURE
import fr.cph.chicago.redux.Status.FAILURE_NO_SHOW
import fr.cph.chicago.redux.Status.FULL_FAILURE
import fr.cph.chicago.redux.Status.SUCCESS
import fr.cph.chicago.redux.Status.UNKNOWN
import fr.cph.chicago.redux.UpdateStatus
import fr.cph.chicago.redux.store
import fr.cph.chicago.task.refreshTask
import fr.cph.chicago.util.RateUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlinx.android.synthetic.main.error.failureLayout
import kotlinx.android.synthetic.main.error.retryButton
import kotlinx.android.synthetic.main.fragment_main.favoritesListView
import kotlinx.android.synthetic.main.fragment_main.floatingButton
import kotlinx.android.synthetic.main.fragment_main.welcomeLayout
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Favorites Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class FavoritesFragment : RefreshFragment(R.layout.fragment_main), StoreSubscriber<State> {

    companion object {
        private val rateUtil = RateUtil

        fun newInstance(sectionNumber: Int): FavoritesFragment {
            return fragmentWithBundle(FavoritesFragment(), sectionNumber) as FavoritesFragment
        }
    }

    private lateinit var adapter: FavoritesAdapter
    private val refreshTask: Observable<Long> = refreshTask()
    private var disposable: Disposable? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = FavoritesAdapter(context!!)

        favoritesListView.adapter = adapter
        favoritesListView.layoutManager = LinearLayoutManager(context!!)
        floatingButton.setOnClickListener { activity?.startActivity(Intent(context!!, SearchActivity::class.java)) }

        favoritesListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        (activity as MainActivity).tb.setOnMenuItemClickListener { reloadData(); true }

        startRefreshTask()
        rateUtil.displayRateSnackBarIfNeeded(swipeRefreshLayout, activity!!)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
        store.unsubscribe(this)
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
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
        disposable?.run {
            if (this.isDisposed) {
                startRefreshTask()
            }
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
                if (state.bikeStationsStatus == FULL_FAILURE) {
                    displayErrorSnackBar(R.string.message_error_bike_favorites)
                } else {
                    displayErrorSnackBar(R.string.message_something_went_wrong)
                }
                stopRefreshing()
                store.dispatch(UpdateStatus(FAILURE_NO_SHOW))
            }
            FULL_FAILURE -> {
                showFullFailureUi()
                displayErrorSnackBar(R.string.message_something_went_wrong)
                stopRefreshing()
                store.dispatch(UpdateStatus(FAILURE_NO_SHOW))
            }
            FAILURE_NO_SHOW -> Timber.d("Something failed but it has been displayed to the user already")
            UNKNOWN -> Timber.d("Unknown status on new state")
            else -> Timber.d("Unknown status on new state")
        }
        adapter.update()
    }

    private fun showSuccessUi() {
        if (failureLayout.visibility != View.GONE) failureLayout.visibility = View.GONE
        welcomeLayout.visibility = if (preferenceService.hasFavorites()) View.GONE else View.VISIBLE
        if (favoritesListView.visibility != View.VISIBLE) favoritesListView.visibility = View.VISIBLE
    }

    private fun showFullFailureUi() {
        if (failureLayout.visibility != View.VISIBLE) failureLayout.visibility = View.VISIBLE
        if (welcomeLayout.visibility != View.GONE) welcomeLayout.visibility = View.GONE
        if (favoritesListView.visibility != View.GONE) favoritesListView.visibility = View.GONE
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

    private fun startRefreshTask() {
        disposable = refreshTask.subscribeWith(object : DisposableObserver<Long>() {
            override fun onNext(t: Long) {
                Timber.v("Update time. Thread id: %s", Thread.currentThread().id)
                adapter.update()
            }

            override fun onError(e: Throwable) {
                Timber.v(e, "Error with refresh task: %s", e.message)
            }

            override fun onComplete() {
                Timber.v("Refresh task complete")
            }
        })
        adapter.update()
    }
}
