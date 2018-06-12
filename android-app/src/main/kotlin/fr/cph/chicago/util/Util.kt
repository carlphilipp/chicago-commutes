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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusFavoriteDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.service.PreferenceService
import java.io.Closeable
import java.io.IOException
import java.io.Reader
import java.util.Arrays
import java.util.Date
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

    val DIVVY_COMPARATOR_BY_NAME: Comparator<BikeStation> by lazy { BikeStationComparator() }
    val busStopComparatorByName: Comparator<BusRoute> by lazy { BusStopComparator() }
    private val preferenceService = PreferenceService

    private val PATTERN = Pattern.compile("(\\d{1,3})")
    private val nextGeneratedId = AtomicInteger(1)

    val chicago: LatLng by lazy {
        LatLng(41.8819, -87.6278)
    }

    val chicagoPosition: Position by lazy {
        Position(chicago.latitude, chicago.longitude)
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
                val one = Integer.parseInt(matcher1.group(1))
                val two = Integer.parseInt(matcher2.group(1))
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

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun setWindowsColor(activity: Activity, toolbar: Toolbar, trainLine: TrainLine) {
        var backgroundColor = 0
        var statusBarColor = 0
        //int navigationBarColor = 0;
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
                backgroundColor = R.color.primaryColor
                statusBarColor = R.color.primaryColorDark
            }
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(App.instance, backgroundColor))
        toolbar.setTitleTextColor(ContextCompat.getColor(App.instance, textTitleColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = ContextCompat.getColor(activity, statusBarColor)
            activity.window.navigationBarColor = ContextCompat.getColor(activity, R.color.primaryColorDarker)
        }
    }

    val randomColor: Int
        get() {
            val random = Random()
            val keys = Arrays.asList(*TrainLine.values())
            return keys[random.nextInt(keys.size)].color
        }

    @Throws(SecurityException::class)
    fun centerMap(mapFragment: SupportMapFragment, position: Position?) {
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            if (position != null) {
                val latLng = LatLng(position.latitude, position.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chicago, 10f))
            }
        }
    }

    fun convertDpToPixel(dp: Int): Int {
        val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), App.instance.resources.displayMetrics)
        return pixels.toInt()
    }

    fun showNetworkErrorMessage(activity: Activity) {
        showSnackBar(activity, R.string.message_network_error, Snackbar.LENGTH_SHORT)
    }

    fun showNetworkErrorMessage(view: View) {
        showSnackBar(view, R.string.message_network_error)
    }

    fun showMessage(activity: Activity, message: Int) {
        showSnackBar(activity, message, Snackbar.LENGTH_SHORT)
    }

    fun showMessage(view: View, message: Int) {
        showSnackBar(view, message)
    }

    fun showSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    fun showSnackBar(activity: Activity, message: Int, length: Int) {
        if (activity.currentFocus != null) {
            Snackbar.make(activity.currentFocus, activity.getString(message), length).show()
        } else {
            Toast.makeText(activity, activity.getString(message), length).show()
        }
    }

    fun showSnackBar(view: View, message: Int) {
        Snackbar.make(view, App.instance.getString(message), Snackbar.LENGTH_SHORT).show()
    }

    fun showOopsSomethingWentWrong(view: View) {
        Snackbar.make(view, App.instance.getString(R.string.message_something_went_wrong), Snackbar.LENGTH_SHORT).show()
    }

    private fun showRateSnackBar(view: View, activity: Activity) {
        val textColor = ContextCompat.getColor(App.instance, R.color.greenLineDark)
        val snackBar1 = Snackbar.make(view, "Do you like this app?", Snackbar.LENGTH_LONG)
            .setAction("YES") { view1 ->
                val snackBar2 = Snackbar.make(view1, "Rate this app on the market", Snackbar.LENGTH_LONG)
                    .setAction("OK") { _ -> rateThisApp(activity) }
                    .setActionTextColor(textColor)
                    .setDuration(10000)
                snackBar2.show()
            }
            .setActionTextColor(textColor)
            .setDuration(10000)
        snackBar1.show()
    }

    fun displayRateSnackBarIfNeeded(view: View, activity: Activity) {
        val handler = Handler()
        val r = {
            val now = Date()
            val lastSeen = preferenceService.getRateLastSeen()
            // if it has been more than 30 days or if it's the first time
            if (now.time - lastSeen.time > 2592000000L || now.time - lastSeen.time < 1000L) {
                showRateSnackBar(view, activity)
                preferenceService.setRateLastSeen()
            }
        }
        handler.postDelayed(r, 2500L)
    }

    fun rateThisApp(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=fr.cph.chicago")
        activity.startActivity(intent)
    }

    fun handleConnectOrParserException(throwable: Throwable, activity: Activity?, connectView: View?, parserView: View) {
        if (throwable.cause is ConnectException) {
            if (activity != null) {
                showNetworkErrorMessage(activity)
            } else if (connectView != null) {
                showNetworkErrorMessage(connectView)
            }
        } else if (throwable.cause is ParserException) {
            showOopsSomethingWentWrong(parserView)
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
        return packageInfo.versionName
    }

    fun closeQuietly(inputStream: Reader?) {
        closeQuietly(inputStream as Closeable)
    }

    fun closeQuietly(closable: Closeable?) {
        try {
            closable?.close()
        } catch (ioe: IOException) {
            // ignore
        }
    }
}
