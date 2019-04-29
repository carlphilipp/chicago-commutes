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
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.BusStopArrivalsAction
import fr.cph.chicago.redux.ForceUpdateFavorites
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.rx.RxUtil
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.rekotlin.StoreSubscriber

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusStopActivity : StationActivity(R.layout.activity_bus), StoreSubscriber<AppState> {

    @BindView(R.id.activity_bus_stop_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.left_layout)
    lateinit var leftLayout: RelativeLayout
    @BindView(R.id.right_layout)
    lateinit var rightLayout: RelativeLayout
    @BindView(R.id.activity_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.street_view_progress_bar)
    lateinit var streetViewProgressBar: ProgressBar
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.activity_bus_station_value)
    lateinit var busRouteNameView: TextView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.destination)
    lateinit var destinationTextView: TextView
    @BindView(R.id.arrivals)
    lateinit var arrivalsTextView: TextView

    @BindString(R.string.bundle_bus_stop_id)
    lateinit var bundleBusStopId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bound)
    lateinit var bundleBusBound: String
    @BindString(R.string.bundle_bus_bound_title)
    lateinit var bundleBusBoundTitle: String
    @BindString(R.string.bundle_bus_stop_name)
    lateinit var bundleBusStopName: String
    @BindString(R.string.bundle_bus_route_name)
    lateinit var bundleBusRouteName: String
    @BindString(R.string.bundle_bus_latitude)
    lateinit var bundleBusLatitude: String
    @BindString(R.string.bundle_bus_longitude)
    lateinit var bundleBusLongitude: String
    @BindString(R.string.request_rt)
    lateinit var requestRt: String
    @BindString(R.string.request_stop_id)
    lateinit var requestStopId: String

    private val util = Util
    private val preferenceService = PreferenceService

    private var busArrivals: BusArrivalStopDTO = BusArrivalStopDTO()
    private lateinit var busRouteId: String
    private lateinit var bound: String
    private lateinit var boundTitle: String
    private var busStopId: Int = 0
    private lateinit var busStopName: String
    private lateinit var busRouteName: String
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var isFavorite: Boolean = false
    private lateinit var busStopArrivalsAction: BusStopArrivalsAction

    override fun create(savedInstanceState: Bundle?) {
        busStopId = intent.getIntExtra(bundleBusStopId, 0)
        busRouteId = intent.getStringExtra(bundleBusRouteId)
        bound = intent.getStringExtra(bundleBusBound)
        boundTitle = intent.getStringExtra(bundleBusBoundTitle)
        busRouteName = intent.getStringExtra(bundleBusRouteName)

        busStopArrivalsAction = BusStopArrivalsAction(
            requestRt = requestRt,
            busRouteId = busRouteId,
            requestStopId = requestStopId,
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)

        isFavorite = isFavorite()

        favoritesImageContainer.setOnClickListener { switchFavorite() }

        if (isFavorite) {
            favoritesImage.setColorFilter(Color.yellowLineDark)
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (latitude == 0.0 && longitude == 0.0) {
                loadStopDetailsAndStreetImage()
            } else {
                mainStore.dispatch(busStopArrivalsAction)
                // FIXME: Identify if it's the place holder or not. This is not great
                if (streetViewImage.scaleType == ImageView.ScaleType.CENTER) {
                    loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewProgressBar)
                }
            }
        }

        val busRouteNameDisplay = "$busRouteName ($boundTitle)"
        busRouteNameView.text = busRouteNameDisplay

        setToolBar()

        loadStopDetailsAndStreetImage()
    }

    override fun onPause() {
        super.onPause()
        mainStore.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        mainStore.subscribe(this)
        mainStore.dispatch(busStopArrivalsAction)
    }

    override fun newState(state: AppState) {
        if (state.busStopError) {
            util.showSnackBar(swipeRefreshLayout, state.busStopErrorMessage)
        } else {
            refreshActivity(state.busArrivalStopDTO)
        }
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    @SuppressLint("CheckResult")
    private fun loadStopDetailsAndStreetImage() {
        // Load bus stop details and google street image
        RxUtil.busStopsForRouteBound(busRouteId, boundTitle)
            .observeOn(Schedulers.computation())
            .flatMap { stops ->
                val busStop: BusStop? = stops.firstOrNull { busStop -> busStop.id == busStopId }
                Single.just(busStop!!)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .map { busStop ->
                toolbar.title = "$busRouteId - ${busStop.name}"
                busStop
            }
            .map { busStop ->
                loadGoogleStreetImage(busStop.position, streetViewImage, streetViewProgressBar)
                busStop
            }
            .doOnError { throwable ->
                Log.e(TAG, throwable.message, throwable)
                onError()
            }
            .subscribe(
                { busStop ->
                    latitude = busStop.position.latitude
                    longitude = busStop.position.longitude
                    busStopName = busStop.name
                    streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
                    mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
                    walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))
                },
                { throwable ->
                    Log.e(TAG, throwable.message, throwable)
                    onError()
                })
    }

    private fun onError() {
        util.showOopsSomethingWentWrong(swipeRefreshLayout)
        failStreetViewImage(streetViewImage)
        streetViewProgressBar.visibility = View.GONE
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            swipeRefreshLayout.isRefreshing = true
            mainStore.dispatch(busStopArrivalsAction)
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = busRouteId
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busStopId = savedInstanceState.getInt(bundleBusStopId)
        busRouteId = savedInstanceState.getString(bundleBusRouteId) ?: ""
        bound = savedInstanceState.getString(bundleBusBound) ?: ""
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle) ?: ""
        busStopName = savedInstanceState.getString(bundleBusStopName) ?: ""
        busRouteName = savedInstanceState.getString(bundleBusRouteName) ?: ""
        latitude = savedInstanceState.getDouble(bundleBusLatitude)
        longitude = savedInstanceState.getDouble(bundleBusLongitude)
        busStopArrivalsAction = BusStopArrivalsAction(
            requestRt = requestRt,
            busRouteId = busRouteId,
            requestStopId = requestStopId,
            busStopId = busStopId,
            bound = bound,
            boundTitle = boundTitle)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(bundleBusStopId, busStopId)
        if(::busRouteId.isInitialized)
            savedInstanceState.putString(bundleBusRouteId, busRouteId)
        if(::bound.isInitialized)
            savedInstanceState.putString(bundleBusBound, bound)
        if(::boundTitle.isInitialized)
            savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        if(::busStopName.isInitialized)
            savedInstanceState.putString(bundleBusStopName, busStopName)
        if(::busRouteName.isInitialized)
            savedInstanceState.putString(bundleBusRouteName, busRouteName)
        savedInstanceState.putDouble(bundleBusLatitude, latitude)
        savedInstanceState.putDouble(bundleBusLongitude, longitude)
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
            arrivalsTextView.text = ""
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
        destinationTextView.text = ""
        arrivalsTextView.text = ""
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

    /**
     * Add or remove from favorites
     */
    private fun switchFavorite() {
        isFavorite = if (isFavorite) {
            preferenceService.removeFromBusFavorites(busRouteId, busStopId.toString(), boundTitle, swipeRefreshLayout)
            favoritesImage.colorFilter = mapImage.colorFilter
            false
        } else {
            preferenceService.addToBusFavorites(busRouteId, busStopId.toString(), boundTitle, swipeRefreshLayout)
            preferenceService.addBusRouteNameMapping(busStopId.toString(), busRouteName)
            preferenceService.addBusStopNameMapping(busStopId.toString(), busStopName)
            favoritesImage.setColorFilter(Color.yellowLineDark)
            mainStore.dispatch(ForceUpdateFavorites(forceUpdate = true))
            true
        }
    }

    companion object {
        private val TAG = BusStopActivity::class.java.simpleName
    }
}
