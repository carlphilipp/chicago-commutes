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

package fr.cph.chicago.util

import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.R.layout
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

    private const val DEFAULT_SPACE_DP = 110
    private val util = Util

    private fun createColoredRoundForFavorites(trainLine: TrainLine): RelativeLayout {
        val lineIndication = RelativeLayout(App.instance)
        val params = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        params.height = App.instance.resources.getDimensionPixelSize(R.dimen.layout_round_height)
        params.width = App.instance.resources.getDimensionPixelSize(R.dimen.layout_round_width)
        params.addRule(RelativeLayout.CENTER_VERTICAL)
        lineIndication.setBackgroundColor(trainLine.color)
        lineIndication.layoutParams = params
        return lineIndication
    }

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

    fun getInsideParams(newLine: Boolean, lastLine: Boolean): LinearLayout.LayoutParams {
        val pixels = util.dpToPixel16
        val pixelsQuarter = pixels / 4
        val paramsLeft = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT)
        if (newLine && lastLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, pixelsQuarter)
        } else if (newLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, 0)
        } else if (lastLine) {
            paramsLeft.setMargins(pixels, 0, pixels, pixelsQuarter)
        } else {
            paramsLeft.setMargins(pixels, 0, pixels, 0)
        }
        return paramsLeft
    }

    fun createFavoritesBusArrivalsNoResult(context: Context, containParams: LinearLayout.LayoutParams): LinearLayout {
        return createFavoritesBusArrivalsLayout(context, containParams, "No Results", null, mutableSetOf())
    }

    // TODO Create XML files instead of doing all those methods in Java
    fun createFavoritesBusArrivalsLayout(context: Context, containParams: LinearLayout.LayoutParams, stopNameTrimmed: String, busDirection: BusDirection?, buses: MutableSet<out BusArrival>): LinearLayout {
        val pixelsHalf = util.dpToPixel16 / 2
        val marginLeftPixel = util.convertDpToPixel(10)

        val container = LinearLayout(context)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left layout
        val left = createLeftLayout(context)

        val lineIndication = createColoredRoundForFavorites(TrainLine.NA)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val leftString = if (busDirection == null) stopNameTrimmed else stopNameTrimmed + " " + busDirection.shortLowerCase
        val destinationSpannable = SpannableString(leftString)
        destinationSpannable.setSpan(RelativeSizeSpan(0.65f), stopNameTrimmed.length, leftString.length, 0) // set size

        val boundCustomTextView = TextView(context)
        boundCustomTextView.text = destinationSpannable
        boundCustomTextView.isSingleLine = true
        boundCustomTextView.layoutParams = destinationParams

        left.addView(lineIndication)
        left.addView(boundCustomTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(context)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = StringBuilder()
        buses.forEach { arri -> currentEtas.append(" ").append(arri.timeLeftDueDelay) }

        val arrivalText = TextView(context)
        arrivalText.text = currentEtas
        arrivalText.gravity = Gravity.END
        arrivalText.isSingleLine = true
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)
        return container
    }

    fun createTrainArrivalsLayout(context: Context, containParams: LinearLayout.LayoutParams, entry: Map.Entry<String, String>, trainLine: TrainLine): LinearLayout {
        val pixels = util.dpToPixel16
        val pixelsHalf = pixels / 2
        val marginLeftPixel = util.convertDpToPixel(10)

        val container = LinearLayout(context)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left layout
        val left = createLeftLayout(context)

        val lineIndication = createColoredRoundForFavorites(trainLine)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val destination = entry.key
        val destinationTextView = TextView(context)
        destinationTextView.text = destination
        destinationTextView.setLines(1)
        destinationTextView.layoutParams = destinationParams
        //destinationTextView.setTextAppearance(textAppearance, App.instance)

        left.addView(lineIndication)
        left.addView(destinationTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(context)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = entry.value
        val arrivalText = TextView(context)
        arrivalText.text = currentEtas
        arrivalText.gravity = Gravity.END
        arrivalText.isSingleLine = true
        arrivalText.ellipsize = TextUtils.TruncateAt.END
        //arrivalText.setTextAppearance(textAppearance, App.instance)

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)

        return container
    }

    fun buildBikeFavoritesLayout(context: Context, bikeStation: BikeStation): LinearLayout {
        //val container = buildBusBikeLayout()
        //val linearLayout = (container.getChildAt(0) as LinearLayout)
        //linearLayout.addView(createBikeLine(context, App.instance.getString(R.string.bike_available_bikes), bikeStation.availableBikes, true))
        //linearLayout.addView(createBikeLine(context, App.instance.getString(R.string.bike_available_docks), bikeStation.availableDocks, true))
        //return container
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = vi.inflate(layout.fav_bike, null) as LinearLayout
        if (bikeStation.availableBikes != -1) {
            val availableBikes = layout.findViewById<TextView>(R.id.available_bikes)
            availableBikes.text = formatNumber(bikeStation.availableBikes)
            availableBikes.setTextColor(if (bikeStation.availableBikes == 0) Color.red else Color.green)
        }
        if (bikeStation.availableDocks != -1) {
            val availableDocks = layout.findViewById<TextView>(R.id.available_docks)
            availableDocks.text = formatNumber(bikeStation.availableDocks)
            availableDocks.setTextColor(if (bikeStation.availableDocks == 0) Color.red else Color.green)
        }
        return layout
    }

    private fun formatNumber(number: Int): String {
        return if (number > 9) {
            number.toString()
        } else {
            "\u0020\u0020" + number.toString()
        }
    }

    fun createLineBelowLayoutParams(id: Int): RelativeLayout.LayoutParams {
        val arrivalLayoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        arrivalLayoutParams.addRule(RelativeLayout.BELOW, id)
        return arrivalLayoutParams
    }

    private fun buildBusBikeLayout(): LinearLayout {
        val containerParams = getInsideParams(true, true)
        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = containerParams

        val linearLayout = LinearLayout(App.instance)
        linearLayout.orientation = LinearLayout.VERTICAL

        container.addView(linearLayout)
        return container
    }

    private fun createBikeLine(context: Context, lineTitle: String, lineValue: Int, withDots: Boolean): LinearLayout {
        // Create line
        val line = createLineLayout(context)

        // Left layout
        val left = createLeftLayout(context)
        val lineId = util.generateViewId()

        val lineTitleTextView = createLineTitle(
            context,
            lineTitle,
            if (withDots) createTitleParamsWithId(lineId) else createTitleParams(DEFAULT_SPACE_DP)
        )

        val amountBike = TextView(App.instance)
        if (lineValue == -1) {
            amountBike.text = "?"
            amountBike.setTextColor(Color.orange)
        } else {
            amountBike.text = Util.formatBikesDocksValues(lineValue)
            val color = if (lineValue == 0) Color.red else Color.green
            amountBike.setTextColor(color)
        }
        amountBike.layoutParams = createLineValueLayoutParams(lineTitleTextView.id)

        if (withDots) {
            val lineIndication = createColoredRoundForFavorites(TrainLine.NA)
            lineIndication.id = lineId
            left.addView(lineIndication)
        }
        left.addView(lineTitleTextView)
        left.addView(amountBike)
        line.addView(left)
        return line
    }

    private fun createLineLayout(context: Context): LinearLayout {
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return vi.inflate(layout.fav_line, null) as LinearLayout
    }

    private fun createLeftLayout(context: Context): RelativeLayout {
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return vi.inflate(layout.fav_left, null) as RelativeLayout
    }

    private fun createTitleParams(dp: Int): RelativeLayout.LayoutParams {
        val destinationParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        destinationParams.setMargins(util.dpToPixel16d, 0, 0, 0)
        destinationParams.width = util.convertDpToPixel(dp)
        return destinationParams
    }

    private fun createTitleParamsWithId(id: Int): RelativeLayout.LayoutParams {
        val params = createTitleParams(DEFAULT_SPACE_DP)
        params.addRule(RelativeLayout.RIGHT_OF, id)
        return params
    }

    private fun createLineValueLayoutParams(id: Int): RelativeLayout.LayoutParams {
        val arrivalLayoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        arrivalLayoutParams.setMargins(util.dpToPixel16d, 0, 0, 0)
        arrivalLayoutParams.addRule(RelativeLayout.RIGHT_OF, id)
        return arrivalLayoutParams
    }

    private fun createLineTitle(context: Context, title: String, layoutParams: RelativeLayout.LayoutParams): TextView {
        val lineTitleTextView = TextView(context)
        lineTitleTextView.text = title
        lineTitleTextView.layoutParams = layoutParams
        lineTitleTextView.id = util.generateViewId()
        lineTitleTextView.isSingleLine = true
        lineTitleTextView.measure(0, 0)
        return lineTitleTextView
    }
}
