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

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.Toolbar
import android.text.TextUtils.TruncateAt
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.*
import com.annimon.stream.Stream
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.entity.Eta
import fr.cph.chicago.entity.Station
import fr.cph.chicago.entity.Stop
import fr.cph.chicago.entity.TrainArrival
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.rx.TrainArrivalObserver
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class StationActivity : AbstractStationActivity() {

    @BindView(android.R.id.content)
    lateinit var viewGroup: ViewGroup
    @BindView(R.id.activity_train_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.scrollViewTrainStation)
    lateinit var scrollView: ScrollView
    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_train_station_steetview_text)
    lateinit var streetViewText: TextView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.activity_map_direction)
    lateinit var directionImage: ImageView
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.activity_train_station_details)
    lateinit var stopsView: LinearLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindString(R.string.bundle_train_stationId)
    lateinit var bundleTrainStationId: String
    @BindString(R.string.analytics_train_details)
    lateinit var trainDetails: String

    @JvmField
    @BindDimen(R.dimen.activity_station_street_map_height)
    internal var height: Int = 0
    @JvmField
    @BindDimen(R.dimen.activity_station_stops_line3_padding_left)
    internal var line3PaddingLeft: Int = 0
    @JvmField
    @BindDimen(R.dimen.activity_station_stops_line3_padding_top)
    internal var line3PaddingTop: Int = 0

    @JvmField
    @BindColor(R.color.grey_5)
    internal var grey_5: Int = 0
    @JvmField
    @BindColor(R.color.grey)
    internal var grey: Int = 0
    @JvmField
    @BindColor(R.color.yellowLineDark)
    internal var yellowLineDark: Int = 0
    @JvmField
    @BindColor(R.color.yellowLine)
    internal var yellowLine: Int = 0
    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    private var paramsStop: LinearLayout.LayoutParams? = null
    private var isFavorite: Boolean = false
    private var stationId: Int = 0
    private var station: Station? = null
    private var ids: MutableMap<String, Int>? = null
    private var trainArrivalObservable: Observable<TrainArrival>? = null

    private val trainService: TrainService = TrainService
    private val preferenceService: PreferenceService = PreferenceService
    private val observableUtil: ObservableUtil = ObservableUtil
    private val util: Util = Util

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkTrainData(this)
        if (!this.isFinishing) {
            // Layout setup
            setContentView(R.layout.activity_station)
            ButterKnife.bind(this)
            // Get station id from bundle
            stationId = intent.extras!!.getInt(bundleTrainStationId, 0)
            if (stationId != 0) {
                // Get station
                station = trainService.getStation(stationId)

                paramsStop = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val layoutParams = streetViewImage.layoutParams as RelativeLayout.LayoutParams
                val position = station!!.stops[0].position
                val params = streetViewImage.layoutParams

                ids = HashMap()
                isFavorite = isFavorite()

                loadGoogleStreetImage(position, streetViewImage, streetViewText)
                createTrainArrivalObservableAndSubscribe()

                streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
                streetViewImage.layoutParams = params
                streetViewText.setTypeface(null, Typeface.BOLD)
                swipeRefreshLayout.setOnRefreshListener { trainArrivalObservable!!.subscribe(TrainArrivalObserver(this, swipeRefreshLayout)) }
                if (isFavorite) {
                    favoritesImage.setColorFilter(yellowLineDark)
                } else {
                    favoritesImage.setColorFilter(grey_5)
                }

                params.height = height
                params.width = layoutParams.width
                mapImage.setColorFilter(grey_5)
                directionImage.setColorFilter(grey_5)
                favoritesImageContainer.setOnClickListener { _ -> switchFavorite() }
                mapContainer.setOnClickListener(GoogleMapOnClickListener(position.latitude, position.longitude))
                walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(position.latitude, position.longitude))

                val stopByLines = station!!.stopByLines
                val randomTrainLine = getRandomLine(stopByLines)
                setUpStopLayouts(stopByLines)
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.color)
                setToolBar(randomTrainLine)

                util.trackScreen(trainDetails)
            }
        }
    }

    private fun setUpStopLayouts(stopByLines: Map<TrainLine, List<Stop>>) {
        stopByLines.entries
        stopByLines.entries.forEach { entry ->
            val line = entry.key
            val stops = entry.value
            val lineTitleView = layoutInflater.inflate(R.layout.activity_station_line_title, viewGroup, false)

            val testView = lineTitleView.findViewById<TextView>(R.id.train_line_title)
            testView.text = line.toStringWithLine()
            testView.setBackgroundColor(line.color)
            if (line === TrainLine.YELLOW) {
                testView.setBackgroundColor(yellowLine)
            }

            stopsView.addView(lineTitleView)

            stops.sorted().forEach { stop ->
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.HORIZONTAL
                linearLayout.layoutParams = paramsStop

                val checkBox = AppCompatCheckBox(this)
                checkBox.setOnCheckedChangeListener { _, isChecked -> preferenceService.saveTrainFilter(stationId, line, stop.direction, isChecked) }
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        trainArrivalObservable!!.subscribe(TrainArrivalObserver(this, swipeRefreshLayout))
                    }
                }
                checkBox.isChecked = preferenceService.getTrainFilter(stationId, line, stop.direction)
                checkBox.setTypeface(checkBox.typeface, Typeface.BOLD)
                checkBox.text = stop.direction.toString()
                checkBox.setTextColor(grey)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.backgroundTintList = ColorStateList.valueOf(line.color)
                    checkBox.buttonTintList = ColorStateList.valueOf(line.color)
                    if (line === TrainLine.YELLOW) {
                        checkBox.backgroundTintList = ColorStateList.valueOf(yellowLine)
                        checkBox.buttonTintList = ColorStateList.valueOf(yellowLine)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.foregroundTintList = ColorStateList.valueOf(line.color)
                    if (line === TrainLine.YELLOW) {
                        checkBox.foregroundTintList = ColorStateList.valueOf(yellowLine)
                    }
                }

                linearLayout.addView(checkBox)

                val arrivalTrainsLayout = LinearLayout(this)
                arrivalTrainsLayout.orientation = LinearLayout.VERTICAL
                arrivalTrainsLayout.layoutParams = paramsStop
                val id = util.generateViewId()
                arrivalTrainsLayout.id = id
                ids!![line.toString() + "_" + stop.direction.toString()] = id

                linearLayout.addView(arrivalTrainsLayout)
                stopsView.addView(linearLayout)
            }
        }
    }

    private fun setToolBar(randomTrainLine: TrainLine) {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { _ ->
            swipeRefreshLayout.isRefreshing = true
            trainArrivalObservable!!.subscribe(TrainArrivalObserver(this, swipeRefreshLayout))
            false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }

        util.setWindowsColor(this, toolbar, randomTrainLine)

        toolbar.title = station!!.name
        toolbar.navigationIcon = arrowBackWhite

        toolbar.setOnClickListener { _ -> finish() }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        stationId = savedInstanceState.getInt(getString(R.string.bundle_train_stationId))
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(getString(R.string.bundle_train_stationId), stationId)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (trainArrivalObservable != null) {
            trainArrivalObservable!!.unsubscribeOn(Schedulers.io())
        }
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isTrainStationFavorite(stationId)
    }

    // FIXME: delete view instead of hiding it
    fun hideAllArrivalViews() {
        station!!.lines
            .flatMap { trainLine ->
                TrainDirection.values().map { trainDirection -> trainLine.toString() + "_" + trainDirection.toString() }
            }
            .forEach { key ->
                if (ids!!.containsKey(key)) {
                    val id = ids!![key]
                    val line3View = findViewById<LinearLayout>(id!!)
                    if (line3View != null) {
                        line3View.visibility = View.GONE
                        if (line3View.childCount > 0) {
                            Stream.range(0, line3View.childCount).forEach { i ->
                                val view = line3View.getChildAt(i!!) as LinearLayout
                                val timing = view.getChildAt(1) as TextView
                                // FIXME
                                if (timing != null) {
                                    timing.text = ""
                                }
                            }
                        }
                    }
                }
            }
    }

    /**
     * Draw line
     *
     * @param eta the eta
     */
    fun drawAllArrivalsTrain(eta: Eta) {
        val line = eta.routeName
        val stop = eta.stop
        val key = line.toString() + "_" + stop.direction.toString()
        // viewId might be not there if CTA API provide wrong data
        if (ids!!.containsKey(key)) {
            val viewId = ids!![key]
            val line3View = findViewById<LinearLayout>(viewId!!)
            val id = ids!![line.toString() + "_" + stop.direction.toString() + "_" + eta.destName]
            if (id == null) {
                val insideLayout = LinearLayout(this)
                insideLayout.orientation = LinearLayout.HORIZONTAL
                insideLayout.layoutParams = paramsStop
                val newId = util.generateViewId()
                insideLayout.id = newId
                ids!![line.toString() + "_" + stop.direction.toString() + "_" + eta.destName] = newId

                val stopName = TextView(this)
                val stopNameData = eta.destName + ": "
                stopName.text = stopNameData
                stopName.setTextColor(grey)
                stopName.setPadding(line3PaddingLeft, line3PaddingTop, 0, 0)
                insideLayout.addView(stopName)

                val timing = TextView(this)
                val timingData = eta.timeLeftDueDelay + " "
                timing.text = timingData
                timing.setTextColor(grey)
                timing.setLines(1)
                timing.ellipsize = TruncateAt.END
                insideLayout.addView(timing)

                line3View.addView(insideLayout)
            } else {
                val insideLayout = findViewById<LinearLayout>(id)
                val timing = insideLayout.getChildAt(1) as TextView
                val timingData = timing.text.toString() + eta.timeLeftDueDelay + " "
                timing.text = timingData
            }
            line3View.visibility = View.VISIBLE
        }
    }

    /**
     * Add/remove favorites
     */
    private fun switchFavorite() {
        if (isFavorite) {
            preferenceService.removeFromTrainFavorites(stationId, scrollView)
            isFavorite = false
            favoritesImage.setColorFilter(grey)
        } else {
            preferenceService.addToTrainFavorites(stationId, scrollView)
            isFavorite = true
            favoritesImage.setColorFilter(yellowLineDark)
        }
    }

    private fun getRandomLine(stops: Map<TrainLine, List<Stop>>): TrainLine {
        val random = Random()
        val keys = ArrayList(stops.keys)
        return keys[random.nextInt(keys.size)]
    }

    private fun createTrainArrivalObservableAndSubscribe() {
        trainArrivalObservable = observableUtil.createTrainArrivalsObservable(station!!)
        trainArrivalObservable!!.subscribe(TrainArrivalObserver(this, swipeRefreshLayout))
    }
}
