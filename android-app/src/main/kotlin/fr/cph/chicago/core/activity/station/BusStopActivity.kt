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

package fr.cph.chicago.core.activity.station

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.listener.OpenMapDirectionOnClickListener
import fr.cph.chicago.core.listener.OpenMapOnClickListener
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.databinding.ActivityBusBinding
import fr.cph.chicago.redux.AddBusFavoriteAction
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.RemoveBusFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.Color
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigInteger
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusStopActivity : StationActivity(), StoreSubscriber<State> {

    companion object {
        private val busService = BusService
    }

    private lateinit var busRouteId: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private var busStopId: BigInteger = BigInteger.ZERO
    private lateinit var busStopName: String
    private lateinit var busRouteName: String
    private lateinit var action: BusStopArrivalsAction
    private lateinit var binding: ActivityBusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView(
            swipeRefreshLayout = binding.activityStationSwipeRefreshLayout,
            streetViewImage = binding.header.streetViewImage,
            streetViewProgressBar = binding.header.streetViewProgressBar,
            streetViewText = binding.header.streetViewText,
            favoritesImage = binding.header.favorites.favoritesImage,
            mapImage = binding.header.favorites.mapImage,
            favoritesImageContainer = binding.header.favorites.favoritesImageContainer
        )

        busStopId = BigInteger(intent.getStringExtra(getString(R.string.bundle_bus_stop_id)) ?: "0")
        busRouteId = intent.getStringExtra(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        bound = intent.getStringExtra(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        boundTitle = intent.getStringExtra(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY
        busRouteName = intent.getStringExtra(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY

        action = BusStopArrivalsAction(
            busRouteId = busRouteId,
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)

        handleFavorite()

        val busRouteNameDisplay = "$busRouteName ($boundTitle)"
        binding.busRouteNameView.text = busRouteNameDisplay

        buildToolbar(binding.included.toolbar)

        loadStopDetailsAndStreetImage()
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        store.dispatch(action)
    }

    override fun newState(state: State) {
        Timber.d("New state")
        when (state.busStopStatus) {
            Status.SUCCESS -> refreshActivity(state.busArrivalStopDTO)
            Status.FAILURE -> util.showSnackBar(swipeRefreshLayout, state.busStopErrorMessage)
            Status.ADD_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_add_fav, true)
                    applyFavorite = false
                    favoritesImage.setColorFilter(Color.yellowLineDark)
                }
            }
            Status.REMOVE_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_remove_fav, true)
                    applyFavorite = false
                    favoritesImage.drawable.colorFilter = mapImage.drawable.colorFilter
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
            store.dispatch(action)
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
                    binding.header.favorites.mapContainer.setOnClickListener(OpenMapOnClickListener(position.latitude, position.longitude))
                    binding.header.favorites.walkContainer.setOnClickListener(OpenMapDirectionOnClickListener(position.latitude, position.longitude))
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

    override fun buildToolbar(toolbar: Toolbar) {
        super.buildToolbar(toolbar)
        toolbar.title = busRouteId
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busStopId = BigInteger(savedInstanceState.getString(getString(R.string.bundle_bus_stop_id)) ?: "0")
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id)) ?: StringUtils.EMPTY
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound)) ?: StringUtils.EMPTY
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title)) ?: StringUtils.EMPTY
        busStopName = savedInstanceState.getString(getString(R.string.bundle_bus_stop_name)) ?: StringUtils.EMPTY
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name)) ?: StringUtils.EMPTY
        position = savedInstanceState.getParcelable(getString(R.string.bundle_position)) as Position? ?: Position()
        action = BusStopArrivalsAction(
            busRouteId = busRouteId,
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(getString(R.string.bundle_bus_stop_id), busStopId.toString())
        if (::busRouteId.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId)
        if (::bound.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound)
        if (::boundTitle.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle)
        if (::busStopName.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_stop_name), busStopName)
        if (::busRouteName.isInitialized) savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName)
        savedInstanceState.putParcelable(getString(R.string.bundle_position), position)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Draw arrivals in current layout
     */
    private fun refreshActivity(busArrivals: BusArrivalStopDTO) {
        cleanLayout()
        if (busArrivals.isEmpty()) {
            binding.destinationTextView.text = App.instance.getString(R.string.bus_activity_no_service)
            binding.arrivalsTextView.text = StringUtils.EMPTY
        } else {
            val key1 = busArrivals.keys.iterator().next()
            binding.destinationTextView.text = key1
            binding.arrivalsTextView.text = busArrivals[key1]!!.joinToString(separator = " ") { util.formatArrivalTime(it) }

            var idBelowTitle = binding.destinationTextView.id
            var idBellowArrival = binding.arrivalsTextView.id
            busArrivals.entries.drop(1).forEach {
                val belowTitle = with(TextView(this)) {
                    text = it.key
                    id = util.generateViewId()
                    isSingleLine = true
                    layoutParams = createLineBelowLayoutParams(idBelowTitle)
                    this
                }

                idBelowTitle = belowTitle.id

                val belowArrival = with(TextView(this)) {
                    text = it.value.joinToString(separator = " ") { util.formatArrivalTime(it) }
                    id = util.generateViewId()
                    isSingleLine = true
                    layoutParams = createLineBelowLayoutParams(idBellowArrival)
                    this
                }
                idBellowArrival = belowArrival.id

                binding.leftLayout.addView(belowTitle)
                binding.rightLayout.addView(belowArrival)
            }
        }
    }

    private fun cleanLayout() {
        binding.destinationTextView.text = StringUtils.EMPTY
        binding.arrivalsTextView.text = StringUtils.EMPTY
        while (binding.leftLayout.childCount >= 2) {
            val view = binding.leftLayout.getChildAt(binding.leftLayout.childCount - 1)
            binding.leftLayout.removeView(view)
        }

        while (binding.rightLayout.childCount >= 2) {
            val view2 = binding.rightLayout.getChildAt(binding.rightLayout.childCount - 1)
            binding.rightLayout.removeView(view2)
        }
    }


    private fun createLineBelowLayoutParams(id: Int): RelativeLayout.LayoutParams {
        val arrivalLayoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        arrivalLayoutParams.addRule(RelativeLayout.BELOW, id)
        return arrivalLayoutParams
    }

    override fun isFavorite(): Boolean {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
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
}
