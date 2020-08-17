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

package fr.cph.chicago.core.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.R
import fr.cph.chicago.core.fragment.NearbyFragment
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.dto.BusArrivalRouteDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util

class SlidingUpAdapter(private val nearbyFragment: NearbyFragment) {

    companion object {
        private val util = Util
        private val layoutUtil = LayoutUtil
    }

    private var nbOfLine = intArrayOf(0)
    private var headerHeight = 0

    fun updateTitleTrain(title: String) {
        createStationHeaderView(title, R.drawable.ic_train_white_24dp)
    }

    fun updateTitleBus(title: String) {
        createStationHeaderView(title, R.drawable.ic_directions_bus_white_24dp)
    }

    fun updateTitleBike(title: String) {
        createStationHeaderView(title, R.drawable.ic_directions_bike_white_24dp)
    }

    private fun createStationHeaderView(title: String, @DrawableRes drawable: Int) {
        val inflater = nearbyFragment.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(R.layout.nearby_station_main, nearbyFragment.slidingLayoutPanel, false)

        val titleView = convertView.findViewById<RelativeLayout>(R.id.title)
        val stationNameView = convertView.findViewById<TextView>(R.id.station_name)
        val imageView = convertView.findViewById<ImageView>(R.id.icon)

        stationNameView.text = title
        stationNameView.maxLines = 1
        stationNameView.ellipsize = TextUtils.TruncateAt.END
        imageView.setImageDrawable(ContextCompat.getDrawable(nearbyFragment.context!!, drawable))

        titleView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        headerHeight = titleView.measuredHeight * 2

        nearbyFragment.loadingLayout.addView(convertView)
    }

    fun addTrainStation(trainArrival: TrainArrival) {
        val linearLayout = nearbyResultsView

        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.childCount == 0) {
            var nbOfLine = 0
            val inflater = nearbyFragment.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val container = inflater.inflate(R.layout.fav_bus, linearLayout, false) as LinearLayout
            linearLayout.addView(container)
            for (trainLine in TrainLine.values()) {
                val etaResult = trainArrival.getEtas(trainLine)
                val etas = etaResult.fold(mutableMapOf<String, String>()) { accumulator, eta ->
                    val stopNameData = eta.destName
                    val timingData = eta.timeLeftDueDelay
                    val value = if (accumulator.containsKey(stopNameData))
                        accumulator[stopNameData] + " " + timingData
                    else
                        timingData
                    accumulator[stopNameData] = value
                    accumulator
                }

                val trainLayout = layoutUtil.layoutForTrainLine(nearbyFragment.context!!, linearLayout, etas, trainLine)
                container.addView(trainLayout)
                nbOfLine += etas.size
            }
            container.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            nearbyFragment.slidingLayoutPanel.panelHeight = getSlidingPanelHeight(container.measuredHeight)
            updatePanelState()
        }
    }

    fun addBusArrival(busArrivalRouteDTO: BusArrivalRouteDTO) {
        val linearLayout = nearbyResultsView

        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.childCount == 0) {
            nbOfLine = intArrayOf(0)

            val inflater = nearbyFragment.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val container = inflater.inflate(R.layout.fav_bus, linearLayout, false) as LinearLayout
            linearLayout.addView(container)
            busArrivalRouteDTO.entries.forEach { entry ->
                val stopNameTrimmed = util.trimBusStopNameIfNeeded(entry.key)
                val boundMap = entry.value

                for (entry2 in boundMap.entries) {
                    val busLayout = layoutUtil.createBusArrivalLine(nearbyFragment.context!!, linearLayout, stopNameTrimmed, BusDirection.fromString(entry2.key), entry2.value as MutableSet<out BusArrival>)
                    container.addView(busLayout)
                }
                nbOfLine[0] = nbOfLine[0] + boundMap.size
            }

            // Handle the case when we have no bus returned.
            if (busArrivalRouteDTO.size == 0) {
                handleNoResults(linearLayout)
                nbOfLine[0]++
            }
            linearLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            nearbyFragment.slidingLayoutPanel.panelHeight = getSlidingPanelHeight(linearLayout.measuredHeight)
            updatePanelState()
        }
    }

    fun addBike(bikeStation: BikeStation) {
        val linearLayout = nearbyResultsView
        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.childCount == 0 || "error" == bikeStation.name) {
            val bikeResultLayout = layoutUtil.buildBikeFavoritesLayout(nearbyFragment.context!!, linearLayout, bikeStation)
            linearLayout.addView(bikeResultLayout)
            linearLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            nearbyFragment.slidingLayoutPanel.panelHeight = getSlidingPanelHeight(linearLayout.measuredHeight)
            updatePanelState()
        }
    }

    private val nearbyResultsView: LinearLayout
        get() {
            val relativeLayout = nearbyFragment.loadingLayout.getChildAt(0) as RelativeLayout
            return relativeLayout.findViewById(R.id.nearby_results)
        }

    private fun handleNoResults(linearLayout: LinearLayout) {
        linearLayout.addView(layoutUtil.createBusArrivalLineNoResults(nearbyFragment.context!!, linearLayout))
    }

    private fun getSlidingPanelHeight(lineHeight: Int): Int {
        return lineHeight + headerHeight
    }

    private fun updatePanelState() {
        if (nearbyFragment.slidingLayoutPanel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            nearbyFragment.slidingLayoutPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
        nearbyFragment.showProgress(false)
    }
}
