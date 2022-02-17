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

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.dto.BusFavoriteDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.exception.CantLoadBusException
import fr.cph.chicago.exception.ConnectException
import java.io.Closeable
import java.io.IOException
import java.io.Reader
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object Util {

    val bikeStationComparator: Comparator<BikeStation> by lazy { BikeStationComparator() }
    val busStopComparatorByName: Comparator<BusRoute> by lazy { BusStopComparator() }

    private val PATTERN = Pattern.compile("(\\d{1,3})")
    private val nextGeneratedId = AtomicInteger(1)
    private val snackBarUtil = SnackBarUtil

    private val dpToPixel16: Int by lazy {
        convertDpToPixel(16)
    }

    fun generateViewId(): Int {
        while (true) {
            val result = nextGeneratedId.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 0x00FFFFFF)
                newValue = 1 // Roll over to 1, not 0.
            if (nextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }

    private class BikeStationComparator : Comparator<BikeStation> {
        override fun compare(station1: BikeStation, station2: BikeStation): Int {
            return station1.name.compareTo(station2.name)
        }
    }

    private class BusStopComparator : Comparator<BusRoute> {

        override fun compare(route1: BusRoute, route2: BusRoute): Int {
            val matcher1 = PATTERN.matcher(route1.id)
            val matcher2 = PATTERN.matcher(route2.id)
            return if (matcher1.find() && matcher2.find()) {
                val one = Integer.parseInt(matcher1.group(1)!!)
                val two = Integer.parseInt(matcher2.group(1)!!)
                if (one < two) -1 else if (one == two) 0 else 1
            } else {
                route1.id.compareTo(route2.id)
            }
        }
    }

    fun isAtLeastTwoErrors(isTrainError: Boolean, isBusError: Boolean, isBikeError: Boolean): Boolean {
        return isTrainError && (isBusError || isBikeError) || isBusError && isBikeError
    }

    /**
     * Decode bus favorites
     *
     * @param favorite the favorites
     * @return a dto containing the route id, the stop id and the bound
     */
    fun decodeBusFavorite(favorite: String): BusFavoriteDTO {
        val first = favorite.indexOf('_')
        val routeId = favorite.substring(0, first)
        val sec = favorite.indexOf('_', first + 1)
        val stopId = favorite.substring(first + 1, sec)
        val bound = favorite.substring(sec + 1, favorite.length)
        return BusFavoriteDTO(routeId, stopId, bound)
    }

    fun setWindowsColor(activity: Activity, toolbar: Toolbar, trainLine: TrainLine) {
        var backgroundColor = 0
        var statusBarColor = 0
        val textTitleColor = R.color.white
        when (trainLine) {
            TrainLine.BLUE -> {
                backgroundColor = R.color.blueLine
                statusBarColor = R.color.blueLineDark
            }
            TrainLine.BROWN -> {
                backgroundColor = R.color.brownLine
                statusBarColor = R.color.brownLineDark
            }
            TrainLine.GREEN -> {
                backgroundColor = R.color.greenLine
                statusBarColor = R.color.greenLineDark
            }
            TrainLine.ORANGE -> {
                backgroundColor = R.color.orangeLine
                statusBarColor = R.color.orangeLineDarker
            }
            TrainLine.PINK -> {
                backgroundColor = R.color.pinkLine
                statusBarColor = R.color.pinkLineDark
            }
            TrainLine.PURPLE -> {
                backgroundColor = R.color.purpleLine
                statusBarColor = R.color.purpleLineDark
            }
            TrainLine.RED -> {
                backgroundColor = R.color.redLine
                statusBarColor = R.color.redLineDark
            }
            TrainLine.YELLOW -> {
                backgroundColor = R.color.yellowLine
                statusBarColor = R.color.yellowLineDark
            }
            TrainLine.NA -> {
                backgroundColor = R.color.lightPrimaryColor
                statusBarColor = R.color.lightPrimaryColorDark
            }
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(App.instance, backgroundColor))
        toolbar.setTitleTextColor(ContextCompat.getColor(App.instance, textTitleColor))
        activity.window.statusBarColor = ContextCompat.getColor(activity, statusBarColor)
    }

    val randomColor: Int
        get() {
            val keys = TrainLine.values().map { it.color }.dropLast(1)
            return keys[Random().nextInt(keys.size)]
        }

    fun convertDpToPixel(dp: Int): Int {
        val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp.toFloat(), App.instance.resources.displayMetrics)
        return pixels.toInt()
    }

    fun getAttribute(context: Context, resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    fun showNetworkErrorMessage(view: View) {
        showSnackBar(view, R.string.message_network_error)
    }

    fun showOopsSomethingWentWrong(view: View) {
        showSnackBar(view, App.instance.getString(R.string.message_something_went_wrong))
    }

    fun showSnackBar(view: View, message: Int, forceShow: Boolean = false) {
        showSnackBar(view, App.instance.getString(message), forceShow)
    }

    fun showSnackBar(view: View, text: CharSequence, forceShow: Boolean = false) {
        if (forceShow) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        } else {
            snackBarUtil.smartShow(view, text)
        }
    }

    fun handleConnectOrParserException(throwable: Throwable, view: View) {
        when (throwable) {
            is ConnectException -> showNetworkErrorMessage(view)
            is CantLoadBusException -> showSnackBar(view, throwable.messageToShow)
            else -> showOopsSomethingWentWrong(view)
        }
    }

    fun trimBusStopNameIfNeeded(name: String): String {
        return if (name.length > 25)
            name.substring(0, 24).trim { it <= ' ' } + "..."
        else
            name
    }

    fun getCurrentVersion(): String {
        val packageInfo = App.instance.packageManager.getPackageInfo(App.instance.packageName, 0)
        val flavor = App.instance.getString(R.string.app_flavor)
        return "${packageInfo.versionName}-$flavor"
    }

    fun closeQuietly(inputStream: Reader?) {
        closeQuietly(inputStream as Closeable)
    }

    private fun closeQuietly(closable: Closeable?) {
        try {
            closable?.close()
        } catch (ioe: IOException) {
            // ignore
        }
    }

    fun formatBikesDocksValues(num: Int): String {
        return if (num >= 10) num.toString() else "  $num"
    }

    @Deprecated("Replaced by a method within BusArrival")
    fun formatArrivalTime(busArrival: BusArrival): String {
        return if (busArrival.isDelay) " Delay" else " " + busArrival.timeLeftMinutes
    }
}
