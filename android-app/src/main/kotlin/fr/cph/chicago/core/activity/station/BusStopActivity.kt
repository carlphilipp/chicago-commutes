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

package fr.cph.chicago.core.activity.station

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusStopActivity : StationActivity(R.layout.activity_bus), StoreSubscriber<State> {

    @BindView(R.id.left_layout)
    lateinit var leftLayout: RelativeLayout
    @BindView(R.id.right_layout)
    lateinit var rightLayout: RelativeLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.activity_bus_station_value)
    lateinit var busRouteNameView: TextView
    @BindView(R.id.destination)
    lateinit var destinationTextView: TextView
    @BindView(R.id.arrivals)
    lateinit var arrivalsTextView: TextView

    private var busArrivals: BusArrivalStopDTO = BusArrivalStopDTO()
    private lateinit var busRouteId: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private var busStopId: Int = 0
    private lateinit var busStopName: String
    private lateinit var busRouteName: String
    private lateinit var busStopArrivalsAction: BusStopArrivalsAction

    override fun create(savedInstanceState: Bundle?) {
        busStopId = intent.getIntExtra(getString(R.string.bundle_bus_stop_id), 0)
        busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id))
        bound = intent.getStringExtra(getString(R.string.bundle_bus_bound))
        boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title))
        busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name))

        busStopArrivalsAction = BusStopArrivalsAction(
            requestRt = getString(R.string.request_rt),
            busRouteId = busRouteId,
            requestStopId = getString(R.string.request_stop_id),
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)

        handleFavorite()

        val busRouteNameDisplay = "$busRouteName ($boundTitle)"
        busRouteNameView.text = busRouteNameDisplay

        setToolbar()

        loadStopDetailsAndStreetImage()
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        store.dispatch(busStopArrivalsAction)
        swipeRefreshLayout.isRefreshing = true
    }

    override fun newState(state: State) {
        Timber.d("New state")
        when (state.busStopStatus) {
            Status.SUCCESS -> refreshActivity(state.busArrivalStopDTO)
            Status.FAILURE -> util.showSnackBar(swipeRefreshLayout, state.busStopErrorMessage)
            Status.ADD_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_add_fav)
                    applyFavorite = false
                    favoritesImage.setColorFilter(Color.yellowLineDark)
                }
            }
            Status.REMOVE_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_remove_fav)
                    applyFavorite = false
                    favoritesImage.colorFilter = mapImage.colorFilter
                }
            }
            else -> Timber.d("Status not handled")
        }
        stopRefreshing()
    }

    override fun refresh() {
        super.refresh()
        if (position.latitude == 0.0 && position.longitude == 0.0) {
            loadStopDetailsAndStreetImage()
        } else {
            store.dispatch(busStopArrivalsAction)
            loadGoogleStreetImage(position)
        }
    }

    @SuppressLint("CheckResult")
    private fun loadStopDetailsAndStreetImage() {
        // Load bus stop details and google street image
        busService.loadAllBusStopsForRouteBound(busRouteId, boundTitle)
            .observeOn(Schedulers.computation())
            .flatMap { stops ->
                val busStop: BusStop? = stops.firstOrNull { busStop -> busStop.id == busStopId }
                Single.just(busStop!!)
            }
            .map { busStop ->
                loadGoogleStreetImage(busStop.position)
                position = Position(busStop.position.latitude, busStop.position.longitude)
                busStopName = busStop.name
                busStop
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { throwable ->
                Timber.e(throwable, "Error while loading street image and stop details")
                onError()
            }
            .subscribe(
                { busStop ->
                    toolbar.title = "$busRouteId - ${busStop.name}"
                    streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
                    mapContainer.setOnClickListener(GoogleMapOnClickListener(position.latitude, position.longitude))
                    walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(position.latitude, position.longitude))
                },
                { throwable ->
                    Timber.e(throwable, "Error while loading street image and stop details")
                    onError()
                })
    }

    private fun onError() {
        util.showOopsSomethingWentWrong(swipeRefreshLayout)
        failStreetViewImage(streetViewImage)
        streetViewProgressBar.visibility = View.GONE
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.title = busRouteId
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busStopId = savedInstanceState.getInt(getString(R.string.bundle_bus_stop_id))
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id))
            ?: StringUtils.EMPTY
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound))
            ?: StringUtils.EMPTY
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title))
            ?: StringUtils.EMPTY
        busStopName = savedInstanceState.getString(getString(R.string.bundle_bus_stop_name))
            ?: StringUtils.EMPTY
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name))
            ?: StringUtils.EMPTY
        position = savedInstanceState.getParcelable(getString(R.string.bundle_position)) as Position
        busStopArrivalsAction = BusStopArrivalsAction(
            requestRt = getString(R.string.request_rt),
            busRouteId = busRouteId,
            requestStopId = getString(R.string.request_stop_id),
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(getString(R.string.bundle_bus_stop_id), busStopId)
        if (::busRouteId.isInitialized)
            savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId)
        if (::bound.isInitialized)
            savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound)
        if (::boundTitle.isInitialized)
            savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle)
        if (::busStopName.isInitialized)
            savedInstanceState.putString(getString(R.string.bundle_bus_stop_name), busStopName)
        if (::busRouteName.isInitialized)
            savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName)
        savedInstanceState.putParcelable(getString(R.string.bundle_position), position)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Draw arrivals in current layout
     */
    private fun refreshActivity(busArrivals: BusArrivalStopDTO) {
        this.busArrivals = busArrivals
        cleanLayout()
        if (busArrivals.isEmpty()) {
            destinationTextView.text = App.instance.getString(R.string.bus_activity_no_service)
            arrivalsTextView.text = StringUtils.EMPTY
        } else {
            val key1 = busArrivals.keys.iterator().next()
            destinationTextView.text = key1
            arrivalsTextView.text = busArrivals[key1]!!.joinToString(separator = " ") { util.formatArrivalTime(it) }

            var idBelowTitle = destinationTextView.id
            var idBellowArrival = arrivalsTextView.id
            busArrivals.entries.drop(1).forEach {
                val belowTitle = with(TextView(App.instance)) {
                    text = it.key
                    id = util.generateViewId()
                    setSingleLine(true)
                    layoutParams = LayoutUtil.createLineBelowLayoutParams(idBelowTitle)
                    this
                }
                idBelowTitle = belowTitle.id

                val belowArrival = with(TextView(App.instance)) {
                    text = it.value.joinToString(separator = " ") { util.formatArrivalTime(it) }
                    id = util.generateViewId()
                    setSingleLine(true)
                    layoutParams = LayoutUtil.createLineBelowLayoutParams(idBellowArrival)
                    this
                }
                idBellowArrival = belowArrival.id

                leftLayout.addView(belowTitle)
                rightLayout.addView(belowArrival)
            }
        }
    }

    private fun cleanLayout() {
        destinationTextView.text = StringUtils.EMPTY
        arrivalsTextView.text = StringUtils.EMPTY
        while (leftLayout.childCount >= 2) {
            val view = leftLayout.getChildAt(leftLayout.childCount - 1)
            leftLayout.removeView(view)
        }

        while (rightLayout.childCount >= 2) {
            val view2 = rightLayout.getChildAt(rightLayout.childCount - 1)
            rightLayout.removeView(view2)
        }
    }

    override fun isFavorite(): Boolean {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
    }

    private fun handleFavorite() {
        if (isFavorite()) {
            favoritesImage.setColorFilter(Color.yellowLineDark)
        }
    }

    /**
     * Add or remove from favorites
     */
    override fun switchFavorite() {
        super.switchFavorite()
        App.instance.refresh = true // because the state is not updated
        if (isFavorite()) {
            store.dispatch(RemoveBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle))
        } else {
            store.dispatch(AddBusFavoriteAction(busRouteId, busStopId.toString(), boundTitle, busRouteName, busStopName))
        }
    }

    companion object {
        private val busService = BusService
        private val util = Util
        private val preferenceService = PreferenceService
    }
}
