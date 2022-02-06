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

package fr.cph.chicago.util

import android.content.Context
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine

/**
 * Layout util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object LayoutUtil {

    fun createColoredRoundForMultiple(trainLine: TrainLine): LinearLayout {
        val lineIndication = LinearLayout(App.instance)
        val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        params.height = App.instance.resources.getDimensionPixelSize(R.dimen.layout_round_height)
        params.width = App.instance.resources.getDimensionPixelSize(R.dimen.layout_round_width)
        params.setMargins(10, 0, 0, 0)
        lineIndication.setBackgroundColor(trainLine.color)
        lineIndication.layoutParams = params
        return lineIndication
    }

    fun createBusArrivalLine(context: Context, viewGroup: ViewGroup, stopNameTrimmed: String, busDirection: BusDirection?, buses: MutableSet<out BusArrival>): RelativeLayout {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate bus line and populate texts.
        val line = inflater.inflate(R.layout.fav_bus_line, viewGroup, false) as RelativeLayout
        val stop = line.findViewById<TextView>(R.id.stop)
        val stopNameDisplay = if (busDirection == null) stopNameTrimmed else "$stopNameTrimmed ${busDirection.shortLowerCase}"
        val destinationSpannable = SpannableString(stopNameDisplay)
        destinationSpannable.setSpan(RelativeSizeSpan(0.65f), stopNameTrimmed.length, stopNameDisplay.length, 0)
        stop.text = destinationSpannable
        val arrivalTimes = line.findViewById<TextView>(R.id.value)
        arrivalTimes.text = buses.joinToString(separator = " ") { busArrival -> busArrival.timeLeftDueDelay }

        return line
    }

    fun createBusArrivalLineNoResults(context: Context, viewGroup: ViewGroup): RelativeLayout {
        return createBusArrivalLine(context, viewGroup, "No Results", null, mutableSetOf())
    }

    fun layoutForTrainLine(context: Context, viewGroup: ViewGroup, map: Map<String, List<String>>, trainLine: TrainLine): LinearLayout {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        for (e in map.entries) {
            // Inflate bus line and populate texts.
            val line = inflater.inflate(R.layout.fav_bus_line, viewGroup, false) as RelativeLayout

            val square = line.findViewById<View>(R.id.square)
            square.setBackgroundColor(trainLine.color)

            val stop = line.findViewById<TextView>(R.id.stop)
            stop.text = e.key

            val arrivalTimes = line.findViewById<TextView>(R.id.value)
            arrivalTimes.text = e.value.joinToString(" ")

            container.addView(line)
        }
        return container
    }

    fun buildBikeFavoritesLayout(context: Context, parent: ViewGroup, bikeStation: BikeStation): LinearLayout {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.fav_bike, parent, false) as LinearLayout
        if (bikeStation.availableBikes != -1) {
            val availableBikesValue = layout.findViewById<TextView>(R.id.available_bikes_value)
            availableBikesValue.text = formatBikeNumber(bikeStation.availableBikes)
            availableBikesValue.setTextColor(if (bikeStation.availableBikes == 0) Color.red else Color.green)
        }
        if (bikeStation.availableDocks != -1) {
            val availableDocksValue = layout.findViewById<TextView>(R.id.available_docks_value)
            availableDocksValue.text = formatBikeNumber(bikeStation.availableDocks)
            availableDocksValue.setTextColor(if (bikeStation.availableDocks == 0) Color.red else Color.green)
        }
        return layout
    }

    private fun formatBikeNumber(number: Int): String {
        return if (number > 9) {
            number.toString()
        } else {
            "\u0020\u0020" + number.toString()
        }
    }
}
