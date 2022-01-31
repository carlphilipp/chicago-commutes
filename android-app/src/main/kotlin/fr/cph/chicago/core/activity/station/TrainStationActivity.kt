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

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.TextUtils.TruncateAt
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import fr.cph.chicago.R
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.listener.OpenMapDirectionOnClickListener
import fr.cph.chicago.core.listener.OpenMapOnClickListener
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.databinding.ActivityStationBinding
import fr.cph.chicago.redux.AddTrainFavoriteAction
import fr.cph.chicago.redux.RemoveTrainFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.TrainStationAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Color
import java.math.BigInteger
import java.util.Random
import org.apache.commons.lang3.StringUtils
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainStationActivity : StationActivity(), StoreSubscriber<State> {

    private lateinit var trainStation: TrainStation
    private lateinit var binding: ActivityStationBinding

    private var stationId: BigInteger = BigInteger.ZERO
    private var ids: MutableMap<String, Int> = mutableMapOf()
    private var randomTrainLine = TrainLine.NA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            binding = ActivityStationBinding.inflate(layoutInflater)
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

            // Get train station id from bundle
            stationId = BigInteger(intent.extras?.getString(getString(R.string.bundle_train_stationId), "0")!!)

            if (stationId != BigInteger.ZERO) {
                // Get trainStation
                trainStation = TrainService.getStation(stationId)
                position = trainStation.stops[0].position

                loadGoogleStreetImage(position)

                streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))

                handleFavorite()

                binding.header.favorites.mapContainer.setOnClickListener(OpenMapOnClickListener(position.latitude, position.longitude))
                binding.header.favorites.walkContainer.setOnClickListener(OpenMapDirectionOnClickListener(position.latitude, position.longitude))

                val stopByLines = trainStation.stopByLines
                randomTrainLine = getRandomLine(stopByLines)
                setUpStopLayouts(stopByLines)
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.color)
                buildToolbar(binding.included.toolbar)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
        store.dispatch(TrainStationAction(trainStation.id))
    }

    override fun newState(state: State) {
        Timber.d("New state")
        when (state.trainStationStatus) {
            Status.SUCCESS -> {
                hideAllArrivalViews()
                state.trainStationArrival.trainEtas.forEach { drawAllArrivalsTrain(it) }
            }
            Status.FAILURE -> util.showSnackBar(swipeRefreshLayout, state.trainStationErrorMessage)
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
        store.dispatch(TrainStationAction(trainStation.id))
        loadGoogleStreetImage(position)
    }

    private fun setUpStopLayouts(stopByLines: Map<TrainLine, List<Stop>>) {
        stopByLines.entries.forEach { entry ->
            val line = entry.key
            val stops = entry.value
            val lineTitleView = layoutInflater.inflate(R.layout.activity_station_line_title, binding.stopsView, false)

            val testView = lineTitleView.findViewById<TextView>(R.id.train_line_title)
            testView.text = line.toStringWithLine()
            testView.setBackgroundColor(line.color)
            if (line === TrainLine.YELLOW) {
                testView.setBackgroundColor(Color.yellowLine)
            }

            binding.stopsView.addView(lineTitleView)

            stops.sorted().forEach { stop ->
                val view = View.inflate(this, R.layout.activity_train_station_direction, null)
                val checkBox = view.findViewById<MaterialCheckBox>(R.id.checkbox)
                checkBox.setOnCheckedChangeListener { _, isChecked -> preferenceService.saveTrainFilter(stationId, line, stop.direction, isChecked) }
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        store.dispatch(TrainStationAction(trainStation.id))
                    }
                }
                checkBox.isChecked = preferenceService.getTrainFilter(stationId, line, stop.direction)
                checkBox.text = stop.direction.toString()
                checkBox.backgroundTintList = ColorStateList.valueOf(line.color)
                checkBox.buttonTintList = ColorStateList.valueOf(line.color)
                if (line === TrainLine.YELLOW) {
                    checkBox.backgroundTintList = ColorStateList.valueOf(Color.yellowLine)
                    checkBox.buttonTintList = ColorStateList.valueOf(Color.yellowLine)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.foregroundTintList = ColorStateList.valueOf(line.color)
                    if (line === TrainLine.YELLOW) {
                        checkBox.foregroundTintList = ColorStateList.valueOf(Color.yellowLine)
                    }
                }

                val arrivalTrainsLayout = view.findViewById<LinearLayout>(R.id.arrivalsTextView)
                val id = util.generateViewId()
                arrivalTrainsLayout.id = id
                ids[line.toString() + "_" + stop.direction.toString()] = id
                binding.stopsView.addView(view)
            }
        }
    }

    override fun buildToolbar(toolbar: MaterialToolbar) {
        super.buildToolbar(toolbar)
        toolbar.title = trainStation.name
        util.setWindowsColor(this, toolbar, randomTrainLine)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        stationId = BigInteger(savedInstanceState.getString(getString(R.string.bundle_train_stationId))!!)
        position = savedInstanceState.getParcelable(getString(R.string.bundle_position)) as Position? ?: Position()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(getString(R.string.bundle_train_stationId), stationId.toString())
        savedInstanceState.putParcelable(getString(R.string.bundle_position), position)
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

    private fun hideAllArrivalViews() {
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
                                    timing.text = StringUtils.EMPTY
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
    private fun drawAllArrivalsTrain(trainEta: TrainEta) {
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
    override fun switchFavorite() {
        super.switchFavorite()
        if (isFavorite()) {
            store.dispatch(RemoveTrainFavoriteAction(stationId))
        } else {
            store.dispatch(AddTrainFavoriteAction(stationId))
        }
    }

    private fun getRandomLine(stops: Map<TrainLine, List<Stop>>): TrainLine {
        val random = Random()
        val keys = stops.keys
        return keys.elementAt(random.nextInt(keys.size))
    }
}
