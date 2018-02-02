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

package fr.cph.chicago.core.activity

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import com.annimon.stream.Stream
import fr.cph.chicago.Constants.Companion.BUSES_PATTERN_URL
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.Position
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.exception.TrackerException
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import java.util.*

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusActivity : AbstractStationActivity() {

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
    @BindString(R.string.bus_activity_no_service)
    lateinit var busActivityNoService: String
    @BindString(R.string.analytics_bus_details)
    lateinit var analyticsBusDetails: String
    @BindString(R.string.request_rt)
    lateinit var requestRt: String
    @BindString(R.string.request_stop_id)
    lateinit var requestStopId: String

    @JvmField
    @BindColor(R.color.grey_5)
    @ColorInt
    internal var grey_5: Int = 0
    @JvmField
    @BindColor(R.color.grey)
    @ColorInt
    internal var grey: Int = 0
    @JvmField
    @BindColor(R.color.yellowLineDark)
    @ColorInt
    internal var yellowLineDark: Int = 0

    private val util = Util
    private val preferenceService = PreferenceService
    private val busService = BusService

    private var busArrivals: List<BusArrival>? = null
    private var busRouteId: String? = null
    private var bound: String? = null
    private var boundTitle: String? = null
    private var busStopId: Int? = null
    private var busStopName: String? = null
    private var busRouteName: String? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkBusData(this)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bus)
            ButterKnife.bind(this)

            if (busStopId == null || busRouteId == null || bound == null || busStopName == null || busRouteName == null || boundTitle == null) {
                val extras = intent.extras
                busStopId = extras!!.getInt(bundleBusStopId)
                busRouteId = extras.getString(bundleBusRouteId)
                bound = extras.getString(bundleBusBound)
                boundTitle = extras.getString(bundleBusBoundTitle)
                busStopName = extras.getString(bundleBusStopName)
                busRouteName = extras.getString(bundleBusRouteName)
                latitude = extras.getDouble(bundleBusLatitude)
                longitude = extras.getDouble(bundleBusLongitude)
            }

            val position = Position(latitude, longitude)

            isFavorite = isFavorite()

            mapImage.setColorFilter(grey_5)
            directionImage.setColorFilter(grey_5)
            favoritesImageContainer.setOnClickListener { v -> switchFavorite() }

            if (isFavorite) {
                favoritesImage.setColorFilter(yellowLineDark)
            } else {
                favoritesImage.setColorFilter(grey_5)
            }
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

            // Google analytics
            util.trackScreen(analyticsBusDetails)
        }
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
        toolbar.title = busRouteId + " - " + busStopName
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { _ -> finish() }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
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

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(bundleBusStopId, busStopId!!)
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putString(bundleBusBound, bound)
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        savedInstanceState.putString(bundleBusStopName, busStopName)
        savedInstanceState.putString(bundleBusRouteName, busRouteName)
        savedInstanceState.putDouble(bundleBusLatitude, latitude)
        savedInstanceState.putDouble(bundleBusLongitude, longitude)
        super.onSaveInstanceState(savedInstanceState)
    }

    fun setBusArrivals(busArrivals: List<BusArrival>) {
        this.busArrivals = busArrivals
    }

    /**
     * Draw arrivals in current layout
     */
    fun drawArrivals() {
        val tempMap = HashMap<String, MutableList<TextView>>()
        if (busArrivals!!.isEmpty()) {
            val arrivalView = TextView(applicationContext)
            arrivalView.setTextColor(grey)
            arrivalView.text = busActivityNoService
            tempMap[""] = mutableListOf(arrivalView)
        } else {
            Stream.of(busArrivals!!)
                .forEach { arrival ->
                    val destination = arrival.busDestination
                    if (tempMap.containsKey(destination)) {
                        val textViews = tempMap[destination]
                        val arrivalView = TextView(applicationContext)
                        arrivalView.text = if (arrival.isDelay) " Delay" else " " + arrival.timeLeft
                        textViews!!.add(arrivalView)
                    } else {
                        val textViews = ArrayList<TextView>()
                        val destinationView = TextView(applicationContext)
                        destinationView.text = destination + ":   "
                        destinationView.setTextColor(grey)
                        destinationView.setTypeface(null, Typeface.BOLD)
                        val arrivalView = TextView(applicationContext)
                        arrivalView.text = if (arrival.isDelay) " Delay" else " " + arrival.timeLeft
                        textViews.add(destinationView)
                        textViews.add(arrivalView)
                        tempMap[destination] = textViews
                    }
                }
        }
        stopsView.removeAllViews()
        tempMap.entries
            .flatMap { mutableEntry -> mutableEntry.value }
            .forEach { stopsView.addView(it) }
    }

    override fun isFavorite(): Boolean {
        return preferenceService.isStopFavorite(busRouteId!!, busStopId!!, boundTitle!!)
    }

    /**
     * Add or remove from favorites
     */
    private fun switchFavorite() {
        if (isFavorite) {
            preferenceService.removeFromBusFavorites(busRouteId!!, busStopId.toString(), boundTitle!!, scrollView!!)
            favoritesImage.setColorFilter(grey_5)
            isFavorite = false
        } else {
            preferenceService.addToBusFavorites(busRouteId!!, busStopId.toString(), boundTitle!!, scrollView!!)
            preferenceService.addBusRouteNameMapping(busStopId.toString(), busRouteName!!)
            preferenceService.addBusStopNameMapping(busStopId.toString(), busStopName!!)
            favoritesImage.setColorFilter(yellowLineDark)
            isFavorite = true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LoadStationDataTask : AsyncTask<Void, Void, List<BusArrival>>() {

        private var trackerException: TrackerException? = null

        override fun doInBackground(vararg params: Void): List<BusArrival>? {
            try {
                return busService.loadBusArrivals(requestRt, busRouteId!!, requestStopId, busStopId!!
                ) { (_, _, _, _, _, _, routeDirection) -> routeDirection == bound || routeDirection == boundTitle }
            } catch (e: ParserException) {
                this.trackerException = e
            } catch (e: ConnectException) {
                this.trackerException = e
            }

            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_PATTERN_URL)
            return null
        }

        override fun onProgressUpdate(vararg values: Void) {}

        override fun onPostExecute(result: List<BusArrival>) {
            if (trackerException == null) {
                setBusArrivals(result)
                drawArrivals()
            } else {
                util.showNetworkErrorMessage(scrollView)
            }
            scrollView.isRefreshing = false
        }
    }
}
