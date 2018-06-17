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

package fr.cph.chicago.util

import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
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
        val paramsLeft = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    fun createFavoritesBusArrivalsNoResult(containParams: LinearLayout.LayoutParams): LinearLayout {
        return createFavoritesBusArrivalsLayout(containParams, "No Results", null, mutableListOf())
    }

    // TODO Create XML files instead of doing all those methods in Java
    fun createFavoritesBusArrivalsLayout(containParams: LinearLayout.LayoutParams, stopNameTrimmed: String, busDirection: BusDirection?, buses: MutableList<out BusArrival>): LinearLayout {
        val pixelsHalf = util.dpToPixel16 / 2
        val marginLeftPixel = util.convertDpToPixel(10)

        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left layout
        val left = createLeftLayout()

        val lineIndication = createColoredRoundForFavorites(TrainLine.NA)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val leftString = if (busDirection == null) stopNameTrimmed else stopNameTrimmed + " " + busDirection.shortLowerCase
        val destinationSpannable = SpannableString(leftString)
        destinationSpannable.setSpan(RelativeSizeSpan(0.65f), stopNameTrimmed.length, leftString.length, 0) // set size
        destinationSpannable.setSpan(ForegroundColorSpan(util.grey5), 0, leftString.length, 0) // set color

        val boundCustomTextView = TextView(App.instance)
        boundCustomTextView.text = destinationSpannable
        boundCustomTextView.setSingleLine(true)
        boundCustomTextView.layoutParams = destinationParams

        left.addView(lineIndication)
        left.addView(boundCustomTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(App.instance)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = StringBuilder()
        buses.forEach { arri -> currentEtas.append(" ").append(arri.timeLeftDueDelay) }

        val arrivalText = TextView(App.instance)
        arrivalText.text = currentEtas
        arrivalText.gravity = Gravity.END
        arrivalText.setSingleLine(true)
        arrivalText.setTextColor(util.grey5)
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)
        return container
    }

    fun createTrainArrivalsLayout(containParams: LinearLayout.LayoutParams, entry: Map.Entry<String, String>, trainLine: TrainLine): LinearLayout {
        val pixels = util.dpToPixel16
        val pixelsHalf = pixels / 2
        val marginLeftPixel = util.convertDpToPixel(10)

        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left layout
        val left = createLeftLayout()

        val lineIndication = createColoredRoundForFavorites(trainLine)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val destination = entry.key
        val destinationTextView = TextView(App.instance)
        destinationTextView.setTextColor(util.grey5)
        destinationTextView.text = destination
        destinationTextView.setLines(1)
        destinationTextView.layoutParams = destinationParams

        left.addView(lineIndication)
        left.addView(destinationTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(App.instance)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = entry.value
        val arrivalText = TextView(App.instance)
        arrivalText.text = currentEtas
        arrivalText.gravity = Gravity.END
        arrivalText.setSingleLine(true)
        arrivalText.setTextColor(util.grey5)
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)

        return container
    }

    fun buildBikeFavoritesLayout(bikeStation: BikeStation): LinearLayout {
        val container = buildBusBikeLayout()
        val linearLayout = (container.getChildAt(0) as LinearLayout)
        linearLayout.addView(createBikeLine(App.instance.getString(R.string.bike_available_bikes), bikeStation.availableBikes, true))
        linearLayout.addView(createBikeLine(App.instance.getString(R.string.bike_available_docks), bikeStation.availableDocks, true))
        return container
    }

    fun buildBikeStationLayout(bikeStation: BikeStation): LinearLayout {
        val container = buildBusBikeLayout()
        val linearLayout = (container.getChildAt(0) as LinearLayout)
        linearLayout.addView(createBikeLine(App.instance.getString(R.string.bike_available_bikes), bikeStation.availableBikes, false))
        linearLayout.addView(createBikeLine(App.instance.getString(R.string.bike_available_docks), bikeStation.availableDocks, false))
        return container
    }

    fun createLineBelowLayoutParams(id: Int): RelativeLayout.LayoutParams {
        val arrivalLayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    private fun createBikeLine(lineTitle: String, lineValue: Int, withDots: Boolean): LinearLayout {
        // Create line
        val line = createLineLayout()

        // Left layout
        val left = createLeftLayout()
        val lineId = util.generateViewId()

        val lineTitleTextView = createLineTitle(
            lineTitle,
            if (withDots) createTitleParamsWithId(lineId) else createTitleParams(DEFAULT_SPACE_DP)
        )

        val amountBike = TextView(App.instance)
        if (lineValue == -1) {
            amountBike.text = "?"
            amountBike.setTextColor(ContextCompat.getColor(App.instance, R.color.orange))
        } else {
            amountBike.text = formatBikesDocksValues(lineValue)
            val color = if (lineValue == 0) R.color.red else R.color.green
            amountBike.setTextColor(ContextCompat.getColor(App.instance, color))
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

    private fun createLineLayout(): LinearLayout {
        val lineParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val line = LinearLayout(App.instance)
        line.orientation = LinearLayout.HORIZONTAL
        line.layoutParams = lineParams
        return line
    }

    private fun createLeftLayout(): RelativeLayout {
        val leftParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(App.instance)
        left.layoutParams = leftParam
        return left
    }

    private fun createTitleParams(dp: Int): RelativeLayout.LayoutParams {
        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
        val arrivalLayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        arrivalLayoutParams.setMargins(util.dpToPixel16d, 0, 0, 0)
        arrivalLayoutParams.addRule(RelativeLayout.RIGHT_OF, id)
        return arrivalLayoutParams
    }

    private fun createLineTitle(title: String, layoutParams: RelativeLayout.LayoutParams): TextView {
        val lineTitleTextView = TextView(App.instance)
        lineTitleTextView.text = title
        lineTitleTextView.layoutParams = layoutParams
        lineTitleTextView.id = util.generateViewId()
        lineTitleTextView.setSingleLine(true)
        lineTitleTextView.setTextColor(util.grey5)
        lineTitleTextView.measure(0, 0)
        return lineTitleTextView
    }

    private fun formatBikesDocksValues(num: Int): String {
        return if (num >= 10) num.toString() else "  $num"
    }

    // TODO move to appropriate utility class
    fun formatArrivalTime(busArrival: BusArrival): String {
        return if (busArrival.isDelay) " Delay" else " " + busArrival.timeLeft
    }
}
