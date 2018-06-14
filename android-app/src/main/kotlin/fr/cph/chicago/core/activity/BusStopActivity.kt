/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.exception.TrackerException
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusStopActivity : AbstractStationActivity(R.layout.activity_bus) {

    @BindView(R.id.activity_bus_stop_swipe_refresh_layout)
    lateinit var scrollView: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_bus_stops)
    lateinit var stopsView: LinearLayout
    @BindView(R.id.activity_bus_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.activity_bus_steetview_text)
    lateinit var streetViewText: TextView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.activity_map_direction)
    lateinit var directionImage: ImageView
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.activity_bus_station_value)
    lateinit var busRouteNameView2: TextView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

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

    @JvmField
    @BindColor(R.color.grey_5)
    internal var grey5: Int = 0
    @JvmField
    @BindColor(R.color.grey)
    internal var grey: Int = 0
    @JvmField
    @BindColor(R.color.yellowLineDark)
    internal var yellowLineDark: Int = 0

    private val util = Util
    private val preferenceService = PreferenceService
    private val busService = BusService

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

    override fun onCreate(savedInstanceState: Bundle?) {
        App.checkBusData(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreate() {
        busStopId = intent.getIntExtra(bundleBusStopId, 0)
        busRouteId = intent.getStringExtra(bundleBusRouteId)
        bound = intent.getStringExtra(bundleBusBound)
        boundTitle = intent.getStringExtra(bundleBusBoundTitle)
        busStopName = intent.getStringExtra(bundleBusStopName)
        busRouteName = intent.getStringExtra(bundleBusRouteName)
        latitude = intent.getDoubleExtra(bundleBusLatitude, 0.0)
        longitude = intent.getDoubleExtra(bundleBusLongitude, 0.0)

        val position = Position(latitude, longitude)

        isFavorite = isFavorite()

        mapImage.setColorFilter(grey5)
        directionImage.setColorFilter(grey5)
        favoritesImageContainer.setOnClickListener { _ -> switchFavorite() }

        favoritesImage.setColorFilter(if (isFavorite) yellowLineDark else grey5)

        scrollView.setOnRefreshListener { LoadStationDataTask().execute() }
        streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
        mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
        walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))

        val busRouteName2 = "$busRouteName ($boundTitle)"
        busRouteNameView2.text = busRouteName2

        // Load google street picture and data
        loadGoogleStreetImage(position, streetViewImage, streetViewText)
        LoadStationDataTask().execute()

        setToolBar()
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { _ ->
            scrollView.isRefreshing = true
            LoadStationDataTask().execute()
            false
        }
        util.setWindowsColor(this, toolbar, TrainLine.NA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = "$busRouteId - $busStopName"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { _ -> finish() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busStopId = savedInstanceState.getInt(bundleBusStopId)
        busRouteId = savedInstanceState.getString(bundleBusRouteId)
        bound = savedInstanceState.getString(bundleBusBound)
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle)
        busStopName = savedInstanceState.getString(bundleBusStopName)
        busRouteName = savedInstanceState.getString(bundleBusRouteName)
        latitude = savedInstanceState.getDouble(bundleBusLatitude)
        longitude = savedInstanceState.getDouble(bundleBusLongitude)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(bundleBusStopId, busStopId)
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putString(bundleBusBound, bound)
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        savedInstanceState.putString(bundleBusStopName, busStopName)
        savedInstanceState.putString(bundleBusRouteName, busRouteName)
        savedInstanceState.putDouble(bundleBusLatitude, latitude)
        savedInstanceState.putDouble(bundleBusLongitude, longitude)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Draw arrivals in current layout
     */
    fun refreshActivity(busArrivals: BusArrivalStopDTO) {
        this.busArrivals = busArrivals
        stopsView.removeAllViews()
        stopsView.addView(LayoutUtil.buildBusStopsLayout(busArrivals))
    }

    override fun isFavorite(): Boolean {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle)
    }

    /**
     * Add or remove from favorites
     */
    private fun switchFavorite() {
        isFavorite = if (isFavorite) {
            preferenceService.removeFromBusFavorites(busRouteId, busStopId.toString(), boundTitle, scrollView)
            favoritesImage.setColorFilter(grey5)
            false
        } else {
            preferenceService.addToBusFavorites(busRouteId, busStopId.toString(), boundTitle, scrollView)
            preferenceService.addBusRouteNameMapping(busStopId.toString(), busRouteName)
            preferenceService.addBusStopNameMapping(busStopId.toString(), busStopName)
            favoritesImage.setColorFilter(yellowLineDark)
            true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LoadStationDataTask : AsyncTask<Void, Void, BusArrivalStopDTO>() {

        private var trackerException: TrackerException? = null

        override fun doInBackground(vararg params: Void): BusArrivalStopDTO {
            try {
                return busService.loadBusArrivals(requestRt, busRouteId, requestStopId, busStopId, bound, boundTitle)
            } catch (e: ParserException) {
                this.trackerException = e
            } catch (e: ConnectException) {
                this.trackerException = e
            }
            return BusArrivalStopDTO()
        }

        override fun onProgressUpdate(vararg values: Void) {}

        override fun onPostExecute(result: BusArrivalStopDTO) {
            if (trackerException == null) {
                refreshActivity(result)
            } else {
                util.showNetworkErrorMessage(scrollView)
            }
            scrollView.isRefreshing = false
        }
    }
}
