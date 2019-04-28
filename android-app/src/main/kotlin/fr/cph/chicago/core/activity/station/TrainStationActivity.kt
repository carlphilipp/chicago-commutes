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

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils.TruncateAt
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindDimen
import butterknife.BindDrawable
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util
import org.rekotlin.StoreSubscriber
import java.util.Random

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainStationActivity : StationActivity(R.layout.activity_station), StoreSubscriber<AppState> {

    @BindView(android.R.id.content)
    lateinit var viewGroup: ViewGroup
    @BindView(R.id.activity_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.street_view_progress_bar)
    lateinit var streetViewProgressBar: ProgressBar
    @BindView(R.id.scrollViewStation)
    lateinit var scrollView: ScrollView
    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
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

    @JvmField
    @BindDimen(R.dimen.activity_station_street_map_height)
    internal var height: Int = 0
    @JvmField
    @BindDimen(R.dimen.activity_station_stops_line3_padding_left)
    internal var line3PaddingLeft: Int = 0
    @JvmField
    @BindDimen(R.dimen.activity_station_stops_line3_padding_top)
    internal var line3PaddingTop: Int = 0

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    private lateinit var paramsStop: LinearLayout.LayoutParams
    private lateinit var trainStation: TrainStation

    private var isFavorite: Boolean = false
    private var stationId: Int = 0
    private var ids: MutableMap<String, Int> = mutableMapOf()

    private val preferenceService: PreferenceService = PreferenceService
    private val util: Util = Util

    override fun create(savedInstanceState: Bundle?) {
        // Get train station id from bundle
        stationId = intent.extras?.getInt(bundleTrainStationId, 0) ?: 0
        if (stationId != 0) {
            // Get trainStation
            trainStation = TrainService.getStation(stationId)

            paramsStop = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val layoutParams = streetViewImage.layoutParams as RelativeLayout.LayoutParams
            val position = trainStation.stops[0].position
            val params = streetViewImage.layoutParams

            isFavorite = isFavorite()

            loadGoogleStreetImage(position, streetViewImage, streetViewProgressBar)

            streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
            streetViewImage.layoutParams = params
            swipeRefreshLayout.setOnRefreshListener {
                mainStore.dispatch(TrainStationAction(trainStation))
                // FIXME: Identify if it's the place holder or not. This is not great
                if (streetViewImage.scaleType == ImageView.ScaleType.CENTER) {
                    loadGoogleStreetImage(position, streetViewImage, streetViewProgressBar)
                }
            }
            if (isFavorite) {
                favoritesImage.setColorFilter(Color.yellowLineDark)
            }

            params.height = height
            params.width = layoutParams.width
            favoritesImageContainer.setOnClickListener { switchFavorite() }
            mapContainer.setOnClickListener(GoogleMapOnClickListener(position.latitude, position.longitude))
            walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(position.latitude, position.longitude))

            val stopByLines = trainStation.stopByLines
            val randomTrainLine = getRandomLine(stopByLines)
            setUpStopLayouts(stopByLines)
            swipeRefreshLayout.setColorSchemeColors(randomTrainLine.color)
            setToolBar(randomTrainLine)
        }
    }

    override fun newState(state: AppState) {
        if (state.trainStationError) {
            util.showSnackBar(swipeRefreshLayout, state.trainStationErrorMessage)
        } else {
            hideAllArrivalViews()
            state.trainStationArrival.trainEtas.forEach { drawAllArrivalsTrain(it) }
        }
        stopRefreshing()
    }

    private fun setUpStopLayouts(stopByLines: Map<TrainLine, List<Stop>>) {
        stopByLines.entries.forEach { entry ->
            val line = entry.key
            val stops = entry.value
            val lineTitleView = layoutInflater.inflate(R.layout.activity_station_line_title, viewGroup, false)

            val testView = lineTitleView.findViewById<TextView>(R.id.train_line_title)
            testView.text = line.toStringWithLine()
            testView.setBackgroundColor(line.color)
            if (line === TrainLine.YELLOW) {
                testView.setBackgroundColor(Color.yellowLine)
            }

            stopsView.addView(lineTitleView)

            stops.sorted().forEach { stop ->
                val view = View.inflate(this, R.layout.activity_train_station_direction, null)
                val checkBox = view.findViewById<AppCompatCheckBox>(R.id.checkbox)
                checkBox.setOnCheckedChangeListener { _, isChecked -> preferenceService.saveTrainFilter(stationId, line, stop.direction, isChecked) }
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        mainStore.dispatch(TrainStationAction(trainStation))
                    }
                }
                checkBox.isChecked = preferenceService.getTrainFilter(stationId, line, stop.direction)
                checkBox.text = stop.direction.toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.backgroundTintList = ColorStateList.valueOf(line.color)
                    checkBox.buttonTintList = ColorStateList.valueOf(line.color)
                    if (line === TrainLine.YELLOW) {
                        checkBox.backgroundTintList = ColorStateList.valueOf(Color.yellowLine)
                        checkBox.buttonTintList = ColorStateList.valueOf(Color.yellowLine)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.foregroundTintList = ColorStateList.valueOf(line.color)
                    if (line === TrainLine.YELLOW) {
                        checkBox.foregroundTintList = ColorStateList.valueOf(Color.yellowLine)
                    }
                }

                val arrivalTrainsLayout = view.findViewById<LinearLayout>(R.id.arrivals)
                val id = util.generateViewId()
                arrivalTrainsLayout.id = id
                ids[line.toString() + "_" + stop.direction.toString()] = id
                stopsView.addView(view)
            }
        }
    }

    private fun setToolBar(randomTrainLine: TrainLine) {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            swipeRefreshLayout.isRefreshing = true
            mainStore.dispatch(TrainStationAction(trainStation))
            false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }

        util.setWindowsColor(this, toolbar, randomTrainLine)

        toolbar.title = trainStation.name
        toolbar.navigationIcon = arrowBackWhite

        toolbar.setOnClickListener { finish() }
    }

    override fun onPause() {
        super.onPause()
        mainStore.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        mainStore.subscribe(this)
        mainStore.dispatch(TrainStationAction(trainStation))
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        stationId = savedInstanceState.getInt(getString(R.string.bundle_train_stationId))
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(getString(R.string.bundle_train_stationId), stationId)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the trainStation is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isTrainStationFavorite(stationId)
    }

    private fun stopRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    fun hideAllArrivalViews() {
        trainStation.lines
            .flatMap { trainLine -> TrainDirection.values().map { trainDirection -> trainLine.toString() + "_" + trainDirection.toString() } }
            .forEach { key ->
                if (ids.containsKey(key)) {
                    val id = ids[key]
                    val arrivalTrainsLayout = findViewById<LinearLayout>(id!!)
                    if (arrivalTrainsLayout != null) {
                        arrivalTrainsLayout.visibility = View.GONE
                        if (arrivalTrainsLayout.childCount > 0) {
                            (0 until arrivalTrainsLayout.childCount).forEach { i ->
                                val view = arrivalTrainsLayout.getChildAt(i) as LinearLayout
                                val timing = view.getChildAt(1) as TextView?
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
     * @param trainEta the trainEta
     */
    fun drawAllArrivalsTrain(trainEta: TrainEta) {
        val line = trainEta.routeName
        val stop = trainEta.stop
        val key = line.toString() + "_" + stop.direction.toString()
        // viewId might be not there if CTA API provide wrong data
        if (ids.containsKey(key)) {
            val viewId = ids[key]
            val arrivalTrainsLayout = findViewById<LinearLayout>(viewId!!)
            val id = ids[line.toString() + "_" + stop.direction.toString() + "_" + trainEta.destName]
            if (id == null) {
                val insideLayout = LinearLayout(this)
                insideLayout.orientation = LinearLayout.HORIZONTAL
                insideLayout.layoutParams = paramsStop
                val newId = util.generateViewId()
                insideLayout.id = newId
                ids[line.toString() + "_" + stop.direction.toString() + "_" + trainEta.destName] = newId

                val stopName = TextView(this)
                val stopNameData = trainEta.destName + ": "
                stopName.text = stopNameData
                insideLayout.addView(stopName)

                val timing = TextView(this)
                val timingData = trainEta.timeLeftDueDelay + " "
                timing.text = timingData
                timing.setLines(1)
                timing.ellipsize = TruncateAt.END
                insideLayout.addView(timing)
                arrivalTrainsLayout.addView(insideLayout)
            } else {
                val insideLayout = findViewById<LinearLayout>(id)
                val timing = insideLayout.getChildAt(1) as TextView
                val timingData = timing.text.toString() + trainEta.timeLeftDueDelay + " "
                timing.text = timingData
            }
            arrivalTrainsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Add/remove favorites
     */
    private fun switchFavorite() {
        if (isFavorite) {
            preferenceService.removeFromTrainFavorites(stationId, scrollView)
            favoritesImage.colorFilter = mapImage.colorFilter
        } else {
            preferenceService.addToTrainFavorites(stationId, scrollView)
            favoritesImage.setColorFilter(Color.yellowLineDark)
            App.instance.refresh = true
        }
        isFavorite = !isFavorite
    }

    private fun getRandomLine(stops: Map<TrainLine, List<Stop>>): TrainLine {
        val random = Random()
        val keys = stops.keys
        return keys.elementAt(random.nextInt(keys.size))
    }
}
