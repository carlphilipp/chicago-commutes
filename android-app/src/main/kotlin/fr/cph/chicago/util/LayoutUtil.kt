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
import fr.cph.chicago.entity.bike.DivvyStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.enumeration.BusDirection
import fr.cph.chicago.entity.enumeration.TrainLine

/**
 * Layout util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object LayoutUtil {

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
        val pixels = util.convertDpToPixel(16)
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

    fun createBusArrivalsNoResult(containParams: LinearLayout.LayoutParams): LinearLayout {
        return createBusArrivalsLayout(containParams, "No Results", null, mutableListOf())
    }

    // TODO Create XML files instead of doing all those methods in Java
    fun createBusArrivalsLayout(containParams: LinearLayout.LayoutParams, stopNameTrimmed: String, busDirection: BusDirection?, buses: MutableList<out BusArrival>): LinearLayout {
        val pixels = util.convertDpToPixel(16)
        val pixelsHalf = pixels / 2
        val marginLeftPixel = util.convertDpToPixel(10)
        val grey5 = ContextCompat.getColor(App.instance, R.color.grey_5)

        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left
        val leftParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(App.instance)
        left.layoutParams = leftParams

        val lineIndication = createColoredRoundForFavorites(TrainLine.NA)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val leftString = if (busDirection == null) stopNameTrimmed else stopNameTrimmed + " " + busDirection.shortLowerCase
        val destinationSpannable = SpannableString(leftString)
        destinationSpannable.setSpan(RelativeSizeSpan(0.65f), stopNameTrimmed.length, leftString.length, 0) // set size
        destinationSpannable.setSpan(ForegroundColorSpan(grey5), 0, leftString.length, 0) // set color

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
        arrivalText.setTextColor(grey5)
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)
        return container
    }

    fun createTrainArrivalsLayout(containParams: LinearLayout.LayoutParams, entry: Map.Entry<String, String>, trainLine: TrainLine): LinearLayout {
        val pixels = util.convertDpToPixel(16)
        val pixelsHalf = pixels / 2
        val marginLeftPixel = util.convertDpToPixel(10)
        val grey5 = ContextCompat.getColor(App.instance, R.color.grey_5)

        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left
        val leftParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(App.instance)
        left.layoutParams = leftParam

        val lineIndication = createColoredRoundForFavorites(trainLine)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val destination = entry.key
        val destinationTextView = TextView(App.instance)
        destinationTextView.setTextColor(grey5)
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
        arrivalText.setTextColor(grey5)
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)

        return container
    }

    fun createBikeLayout(divvyStation: DivvyStation): LinearLayout {
        val containerParams = getInsideParams(true, true)
        val container = LinearLayout(App.instance)
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = containerParams

        val linearLayout = LinearLayout(App.instance)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(createBikeLine(divvyStation, true))
        linearLayout.addView(createBikeLine(divvyStation, false))

        container.addView(linearLayout)
        return container
    }

    private fun createBikeLine(divvyStation: DivvyStation, firstLine: Boolean): LinearLayout {
        val pixels = util.convertDpToPixel(16)
        val pixelsHalf = pixels / 2
        val grey5 = ContextCompat.getColor(App.instance, R.color.grey_5)


        val lineParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val line = LinearLayout(App.instance)
        line.orientation = LinearLayout.HORIZONTAL
        line.layoutParams = lineParams

        // Left
        val leftParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(App.instance)
        left.layoutParams = leftParam

        val lineIndication = createColoredRoundForFavorites(TrainLine.NA)
        val lineId = util.generateViewId()
        lineIndication.id = lineId

        val availableParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId)
        availableParam.setMargins(pixelsHalf, 0, 0, 0)

        val boundCustomTextView = TextView(App.instance)
        boundCustomTextView.text = App.instance.resources.getString(R.string.bike_available_docks)
        boundCustomTextView.setSingleLine(true)
        boundCustomTextView.layoutParams = availableParam
        boundCustomTextView.setTextColor(grey5)
        val availableId = util.generateViewId()
        boundCustomTextView.id = availableId

        val availableValueParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId)
        availableValueParam.setMargins(pixelsHalf, 0, 0, 0)

        val amountBike = TextView(App.instance)
        val text = if (firstLine) App.instance.resources.getString(R.string.bike_available_bikes) else App.instance.resources.getString(R.string.bike_available_docks)
        boundCustomTextView.text = text
        val data = if (firstLine) divvyStation.availableBikes else divvyStation.availableDocks
        if (data == -1) {
            amountBike.text = "?"
            amountBike.setTextColor(ContextCompat.getColor(App.instance, R.color.orange))
        } else {
            amountBike.text = data.toString()
            val color = if (data == 0) R.color.red else R.color.green
            amountBike.setTextColor(ContextCompat.getColor(App.instance, color))
        }
        amountBike.layoutParams = availableValueParam

        left.addView(lineIndication)
        left.addView(boundCustomTextView)
        left.addView(amountBike)
        line.addView(left)
        return line
    }
}
