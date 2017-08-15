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

package fr.cph.chicago.util

import android.content.Context
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
import fr.cph.chicago.entity.BikeStation
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

    // FIXME Find a way to not use context everywhere here
    fun createColoredRoundForFavorites(context: Context, trainLine: TrainLine): RelativeLayout {
        val lineIndication = RelativeLayout(context)
        val params = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        params.height = context.resources.getDimensionPixelSize(R.dimen.layout_round_height)
        params.width = context.resources.getDimensionPixelSize(R.dimen.layout_round_width)
        params.addRule(RelativeLayout.CENTER_VERTICAL)
        lineIndication.setBackgroundColor(trainLine.color)
        lineIndication.layoutParams = params
        return lineIndication
    }

    fun createColoredRoundForMultiple(context: Context, trainLine: TrainLine): LinearLayout {
        val lineIndication = LinearLayout(context)
        val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        params.height = context.resources.getDimensionPixelSize(R.dimen.layout_round_height)
        params.width = context.resources.getDimensionPixelSize(R.dimen.layout_round_width)
        params.setMargins(10, 0, 0, 0)
        lineIndication.setBackgroundColor(trainLine.color)
        lineIndication.layoutParams = params
        return lineIndication
    }

    fun getInsideParams(context: Context, newLine: Boolean, lastLine: Boolean): LinearLayout.LayoutParams {
        val pixels = Util.convertDpToPixel(context, 16)
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

    fun createBusArrivalsNoResult(context: Context, containParams: LinearLayout.LayoutParams): LinearLayout {
        return createBusArrivalsLayout(context, containParams, "No Results", null, mutableListOf())
    }

    // TODO Create XML files instead of doing all those methods in Java
    fun createBusArrivalsLayout(context: Context, containParams: LinearLayout.LayoutParams, stopNameTrimmed: String, busDirection: BusDirection.BusDirectionEnum?, buses: MutableList<out BusArrival>): LinearLayout {
        val pixels = Util.convertDpToPixel(context, 16)
        val pixelsHalf = pixels / 2
        val marginLeftPixel = Util.convertDpToPixel(context, 10)
        val grey5 = ContextCompat.getColor(context, R.color.grey_5)

        val container = LinearLayout(context)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left
        val leftParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(context)
        left.layoutParams = leftParams

        val lineIndication = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA)
        val lineId = Util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val leftString = if (busDirection == null) stopNameTrimmed else stopNameTrimmed + " " + busDirection.shortLowerCase
        val destinationSpannable = SpannableString(leftString)
        destinationSpannable.setSpan(RelativeSizeSpan(0.65f), stopNameTrimmed.length, leftString.length, 0) // set size
        destinationSpannable.setSpan(ForegroundColorSpan(grey5), 0, leftString.length, 0) // set color

        val boundCustomTextView = TextView(context)
        boundCustomTextView.text = destinationSpannable
        boundCustomTextView.setSingleLine(true)
        boundCustomTextView.layoutParams = destinationParams

        left.addView(lineIndication)
        left.addView(boundCustomTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(context)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = StringBuilder()
        buses.forEach { arri -> currentEtas.append(" ").append(arri.timeLeftDueDelay) }

        val arrivalText = TextView(context)
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

    fun createTrainArrivalsLayout(context: Context, containParams: LinearLayout.LayoutParams, entry: Map.Entry<String, String>, trainLine: TrainLine): LinearLayout {
        val pixels = Util.convertDpToPixel(context, 16)
        val pixelsHalf = pixels / 2
        val marginLeftPixel = Util.convertDpToPixel(context, 10)
        val grey5 = ContextCompat.getColor(context, R.color.grey_5)

        val container = LinearLayout(context)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = containParams

        // Left
        val leftParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(context)
        left.layoutParams = leftParam

        val lineIndication = LayoutUtil.createColoredRoundForFavorites(context, trainLine)
        val lineId = Util.generateViewId()
        lineIndication.id = lineId

        val destinationParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId)
        destinationParams.setMargins(pixelsHalf, 0, 0, 0)

        val destination = entry.key
        val destinationTextView = TextView(context)
        destinationTextView.setTextColor(grey5)
        destinationTextView.setText(destination)
        destinationTextView.setLines(1)
        destinationTextView.layoutParams = destinationParams

        left.addView(lineIndication)
        left.addView(destinationTextView)

        // Right
        val rightParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rightParams.setMargins(marginLeftPixel, 0, 0, 0)
        val right = LinearLayout(context)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = rightParams

        val currentEtas = entry.value
        val arrivalText = TextView(context)
        arrivalText.setText(currentEtas)
        arrivalText.gravity = Gravity.END
        arrivalText.setSingleLine(true)
        arrivalText.setTextColor(grey5)
        arrivalText.ellipsize = TextUtils.TruncateAt.END

        right.addView(arrivalText)

        container.addView(left)
        container.addView(right)

        return container
    }

    fun createBikeLayout(context: Context, bikeStation: BikeStation): LinearLayout {
        val containerParams = getInsideParams(context, true, true)
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = containerParams

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(createBikeLine(context, bikeStation, true))
        linearLayout.addView(createBikeLine(context, bikeStation, false))

        container.addView(linearLayout)
        return container
    }

    private fun createBikeLine(context: Context, bikeStation: BikeStation, firstLine: Boolean): LinearLayout {
        val pixels = Util.convertDpToPixel(context, 16)
        val pixelsHalf = pixels / 2
        val grey5 = ContextCompat.getColor(context, R.color.grey_5)


        val lineParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val line = LinearLayout(context)
        line.orientation = LinearLayout.HORIZONTAL
        line.layoutParams = lineParams

        // Left
        val leftParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = RelativeLayout(context)
        left.layoutParams = leftParam

        val lineIndication = createColoredRoundForFavorites(context, TrainLine.NA)
        val lineId = Util.generateViewId()
        lineIndication.id = lineId

        val availableParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId)
        availableParam.setMargins(pixelsHalf, 0, 0, 0)

        val boundCustomTextView = TextView(context)
        boundCustomTextView.text = context.getString(R.string.bike_available_docks)
        boundCustomTextView.setSingleLine(true)
        boundCustomTextView.layoutParams = availableParam
        boundCustomTextView.setTextColor(grey5)
        val availableId = Util.generateViewId()
        boundCustomTextView.id = availableId

        val availableValueParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId)
        availableValueParam.setMargins(pixelsHalf, 0, 0, 0)

        val amountBike = TextView(context)
        val text = if (firstLine) context.getString(R.string.bike_available_bikes) else context.getString(R.string.bike_available_docks)
        boundCustomTextView.text = text
        val data = if (firstLine) bikeStation.availableBikes else bikeStation.availableDocks
        if (data == -1) {
            amountBike.text = "?"
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.orange))
        } else {
            amountBike.text = data.toString()
            val color = if (data == 0) R.color.red else R.color.green
            amountBike.setTextColor(ContextCompat.getColor(context, color))
        }
        amountBike.layoutParams = availableValueParam

        left.addView(lineIndication)
        left.addView(boundCustomTextView)
        left.addView(amountBike)
        line.addView(left)
        return line
    }
}
