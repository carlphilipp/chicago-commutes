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
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.Constants.Companion.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.Companion.TRAINS_ARRIVALS_URL
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.adapter.FavoritesAdapter
import fr.cph.chicago.data.FavoritesData
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.TrainArrival
import fr.cph.chicago.entity.dto.FavoritesDTO
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.task.RefreshTimingTask
import fr.cph.chicago.util.Util
import java.util.*

/**
 * FavoritesData Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class FavoritesFragment : AbstractFragment() {

    @BindView(R.id.welcome)
    lateinit var welcomeLayout: RelativeLayout
    @BindView(R.id.activity_main_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.favorites_list)
    lateinit var listView: RecyclerView
    @BindView(R.id.floating_button)
    lateinit var floatingButton: FloatingActionButton

    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStation: String

    private val util: Util = Util
    private val observableUtil: ObservableUtil = ObservableUtil
    private val favoritesData: FavoritesData = FavoritesData
    private val busService: BusService = BusService
    private val preferenceService: PreferenceService = PreferenceService

    private var favoritesAdapter: FavoritesAdapter? = null
    private var refreshTimingTask: RefreshTimingTask? = null
    private lateinit var bikeStations: List<BikeStation>

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (favoritesAdapter == null) {
            val busArrivals: List<BusArrival> = mainActivity.intent.getParcelableArrayListExtra(getString(R.string.bundle_bus_arrivals))
                ?: listOf()
            val trainArrivals: SparseArray<TrainArrival> = mainActivity.intent.extras.getSparseParcelableArray(getString(R.string.bundle_train_arrivals))
                ?: SparseArray()
            bikeStations = mainActivity.intent.getParcelableArrayListExtra(getString(R.string.bundle_bike_stations))
                ?: listOf()
            favoritesAdapter = FavoritesAdapter(mainActivity)
            favoritesData.trainArrivals = trainArrivals
            favoritesData.busArrivals = busArrivals
            favoritesData.bikeStations = bikeStations
            favoritesAdapter!!.refreshFavorites()
        }
        util.trackScreen(getString(R.string.analytics_favorites_fragment))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!mainActivity.isFinishing) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false)
            setBinder(rootView)
            val linearLayoutManager = LinearLayoutManager(mainActivity)
            listView.adapter = favoritesAdapter
            listView.layoutManager = linearLayoutManager
            floatingButton.setOnClickListener { _ ->
                if (bikeStations.isEmpty()) {
                    util.showMessage(mainActivity, R.string.message_too_fast)
                } else {
                    val intent = Intent(mainActivity, SearchActivity::class.java)
                    intent.putParcelableArrayListExtra(bundleBikeStation, bikeStations as ArrayList<BikeStation>?)
                    mainActivity.startActivity(intent)
                }
            }
            listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    if (dy > 0 && floatingButton.isShown) {
                        floatingButton.hide()
                    } else if (dy < 0 && !floatingButton.isShown) {
                        floatingButton.show()
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                swipeRefreshLayout.setColorSchemeColors(util.randomColor)
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL)
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL)
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_divvy, context!!.getString(R.string.analytics_action_get_divvy_all))
                util.trackAction(R.string.analytics_category_ui, R.string.analytics_action_press, context!!.getString(R.string.analytics_action_refresh_fav))

                if (busService.getBusRoutes().isEmpty()
                    || mainActivity.intent.getParcelableArrayListExtra<Parcelable>(bundleBikeStation) == null
                    || mainActivity.intent.getParcelableArrayListExtra<Parcelable>(bundleBikeStation).size == 0) {
                    mainActivity.loadFirstData()
                }

                if (util.isNetworkAvailable()) {
                    val zipped = observableUtil.createAllDataObservable(mainActivity.application)
                    zipped.subscribe(
                        {
                            this.reloadData(it)
                        },
                        { onError ->
                            Log.e(TAG, onError.message, onError)
                            this.displayError(R.string.message_something_went_wrong)
                        })
                } else {
                    this.displayError(R.string.message_network_error)
                }
            }

            startRefreshTask()
            util.displayRateSnackBarIfNeeded(swipeRefreshLayout, mainActivity)
        }
        return rootView
    }

    override fun onPause() {
        super.onPause()
        refreshTimingTask?.cancel(true)
    }

    override fun onStop() {
        super.onStop()
        refreshTimingTask?.cancel(true)

    }

    override fun onDestroy() {
        super.onDestroy()
        refreshTimingTask?.cancel(true)
    }

    override fun onResume() {
        super.onResume()
        favoritesAdapter?.refreshFavorites()
        favoritesAdapter?.notifyDataSetChanged()
        if (refreshTimingTask!!.status == Status.FINISHED) {
            startRefreshTask()
        }
        val hasFav = preferenceService.hasFavorites()
        welcomeLayout.visibility = if (hasFav) View.GONE else View.VISIBLE
    }

    fun reloadData(favoritesDTO: FavoritesDTO) {
        mainActivity.intent.putParcelableArrayListExtra(bundleBikeStation, favoritesDTO.bikeStations as ArrayList<BikeStation>)
        bikeStations = favoritesDTO.bikeStations
        favoritesData.busArrivals = favoritesDTO.busArrivalDTO.busArrivals
        favoritesData.trainArrivals = favoritesDTO.trainArrivalDTO.trainArrivalSparseArray

        favoritesAdapter?.refreshFavorites()
        favoritesAdapter?.resetLastUpdate()
        favoritesAdapter?.updateModel()
        favoritesAdapter?.notifyDataSetChanged()

        rootView.setBackgroundResource(R.drawable.highlight_selector)
        rootView.postDelayed({ rootView.setBackgroundResource(R.drawable.bg_selector) }, 100)
        stopRefreshing()
        if (util.isAtLeastTwoErrors(favoritesDTO.trainArrivalDTO.error, favoritesDTO.busArrivalDTO.error, favoritesDTO.bikeError)) {
            util.showMessage(mainActivity, R.string.message_something_went_wrong)
        } else if (favoritesDTO.trainArrivalDTO.error) {
            util.showMessage(mainActivity, R.string.message_error_train_favorites)
        } else if (favoritesDTO.busArrivalDTO.error) {
            util.showMessage(mainActivity, R.string.message_error_bus_favorites)
        } else if (favoritesDTO.bikeError) {
            util.showMessage(mainActivity, R.string.message_error_bike_favorites)
        }
    }

    /**
     * Display error
     *
     * @param message the message
     */
    fun displayError(message: Int) {
        util.showMessage(mainActivity, message)
        stopRefreshing()
    }

    fun setBikeStations(bikeStations: List<BikeStation>) {
        this.bikeStations = bikeStations
        favoritesData.bikeStations = bikeStations
        favoritesAdapter?.notifyDataSetChanged()
    }

    /**
     * Start refreshBusAndStation task
     */
    private fun startRefreshTask() {
        refreshTimingTask = RefreshTimingTask(favoritesAdapter!!).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) as RefreshTimingTask
        favoritesAdapter?.updateModel()
    }

    fun startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(util.randomColor)
        swipeRefreshLayout.isRefreshing = true
    }

    private fun stopRefreshing() {
        swipeRefreshLayout.isRefreshing = false
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
            return AbstractFragment.Companion.fragmentWithBundle(FavoritesFragment(), sectionNumber) as FavoritesFragment
        }
    }
}
