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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.data.PreferencesImpl
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.Position
import fr.cph.chicago.entity.dto.BusFavoriteDTO
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object Util {

    val BIKE_COMPARATOR_NAME: Comparator<BikeStation> = BikeStationComparator()
    val BUS_STOP_COMPARATOR_NAME: Comparator<BusRoute> = BusStopComparator()

    private val PATTERN = Pattern.compile("(\\d{1,3})")
    private val sNextGeneratedId = AtomicInteger(1)

    fun chicago(): LatLng {
        return LatLng(41.8819, -87.6278)
    }

    fun generateViewId(): Int {
        while (true) {
            val result = sNextGeneratedId.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 0x00FFFFFF)
                newValue = 1 // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
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
            if (matcher1.find() && matcher2.find()) {
                val one = Integer.parseInt(matcher1.group(1))
                val two = Integer.parseInt(matcher2.group(1))
                return if (one < two) -1 else if (one == two) 0 else 1
            } else {
                return route1.id.compareTo(route2.id)
            }
        }
    }

    fun isAtLeastTwoErrors(isTrainError: Boolean, isBusError: Boolean, isBikeError: Boolean): Boolean {
        return isTrainError && (isBusError || isBikeError) || isBusError && isBikeError
    }

    /**
     * Add to train favorites
     *
     * @param stationId the station id
     * @param view      the view
     */
    fun addToTrainFavorites(stationId: Int, view: View) {
        val favorites = PreferencesImpl.getTrainFavorites(view.context)
        if (!favorites.contains(stationId)) {
            favorites.add(stationId)
            PreferencesImpl.saveTrainFavorites(view.context, favorites)
            showSnackBar(view, R.string.message_add_fav)
        }
    }

    /**
     * Remove train from favorites
     *
     * @param stationId the station id
     * @param view      the view
     */
    fun removeFromTrainFavorites(stationId: Int, view: View) {
        val favorites = PreferencesImpl.getTrainFavorites(view.context)
        favorites.remove(stationId)
        PreferencesImpl.saveTrainFavorites(view.context, favorites)
        showSnackBar(view, R.string.message_remove_fav)
    }

    /**
     * Remove from bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param view       the view
     */
    fun removeFromBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = PreferencesImpl.getBusFavorites(view.context)
        favorites.remove(id)
        PreferencesImpl.saveBusFavorites(view.context, favorites)
        showSnackBar(view, R.string.message_remove_fav)
    }

    /**
     * Add to bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param view       the view
     */
    fun addToBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = PreferencesImpl.getBusFavorites(view.context)
        if (!favorites.contains(id)) {
            favorites.add(id)
            PreferencesImpl.saveBusFavorites(view.context, favorites)
            showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToBikeFavorites(stationId: Int, view: View) {
        val favorites = PreferencesImpl.getBikeFavorites(view.context)
        if (!favorites.contains(Integer.toString(stationId))) {
            favorites.add(Integer.toString(stationId))
            PreferencesImpl.saveBikeFavorites(view.context, favorites)
            showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun removeFromBikeFavorites(stationId: Int, view: View) {
        val favorites = PreferencesImpl.getBikeFavorites(view.context)
        favorites.remove(Integer.toString(stationId))
        PreferencesImpl.saveBikeFavorites(view.context, favorites)
        showSnackBar(view, R.string.message_remove_fav)
    }

    /**
     * Decode bus favorites
     *
     * @param favorite the favorites
     * @return a tab containing the route id, the stop id and the bound
     */
    fun decodeBusFavorite(favorite: String): BusFavoriteDTO {
        val first = favorite.indexOf('_')
        val routeId = favorite.substring(0, first)
        val sec = favorite.indexOf('_', first + 1)
        val stopId = favorite.substring(first + 1, sec)
        val bound = favorite.substring(sec + 1, favorite.length)
        return BusFavoriteDTO(routeId, stopId, bound)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * Google analytics track screen
     *
     * @param screen the screen name
     */
    fun trackScreen(app: App, screen: String) {
        val tracker = app.tracker
        tracker.setScreenName(screen)
        tracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    fun trackAction(app: App, category: Int, action: Int, label: String) {
        Thread {
            app.tracker.send(HitBuilders.EventBuilder()
                .setCategory(app.applicationContext.getString(category))
                .setAction(app.applicationContext.getString(action))
                .setLabel(label)
                .setValue(0).build())
        }.start()
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
        toolbar.setBackgroundColor(ContextCompat.getColor(activity.applicationContext, backgroundColor))
        toolbar.setTitleTextColor(ContextCompat.getColor(activity.applicationContext, textTitleColor))
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
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chicago(), 10f))
            }
        }
    }

    fun getFavoritesTrainParams(context: Context): MultiValuedMap<String, String> {
        val paramsTrain = ArrayListValuedHashMap<String, String>()
        val favorites = PreferencesImpl.getTrainFavorites(context)
        favorites.forEach { favorite -> paramsTrain.put(context.getString(R.string.request_map_id), favorite.toString()) }
        return paramsTrain
    }

    fun getFavoritesBusParams(context: Context): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>()
        val busFavorites = PreferencesImpl.getBusFavorites(context)
        busFavorites
            .map { decodeBusFavorite(it) }
            .forEach { (routeId, stopId) ->
                paramsBus.put(context.getString(R.string.request_rt), routeId)
                paramsBus.put(context.getString(R.string.request_stop_id), stopId)
            }
        return paramsBus
    }

    fun convertDpToPixel(context: Context, dp: Int): Int {
        val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)
        return pixels.toInt()
    }

    fun showNetworkErrorMessage(activity: Activity) {
        showSnackBar(activity, R.string.message_network_error)
    }

    fun showNetworkErrorMessage(view: View) {
        showSnackBar(view, R.string.message_network_error)
    }

    fun showMessage(activity: Activity, message: Int) {
        showSnackBar(activity, message)
    }

    fun showMessage(view: View, message: Int) {
        showSnackBar(view, message)
    }

    fun showSnackBar(activity: Activity, message: Int) {
        if (activity.currentFocus != null) {
            Snackbar.make(activity.currentFocus!!, activity.getString(message), Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, activity.getString(message), Toast.LENGTH_LONG).show()
        }
    }

    private fun showSnackBar(view: View, message: Int) {
        Snackbar.make(view, view.context.getString(message), Snackbar.LENGTH_SHORT).show()
    }

    fun showOopsSomethingWentWrong(view: View) {
        Snackbar.make(view, view.context.getString(R.string.message_something_went_wrong), Snackbar.LENGTH_SHORT).show()
    }

    private fun showRateSnackBar(view: View, activity: Activity) {
        val textColor = ContextCompat.getColor(view.context, R.color.greenLineDark)
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
            val lastSeen = PreferencesImpl.getRateLastSeen(view.context)
            // if it has been more than 30 days or if it's the first time
            if (now.time - lastSeen.time > 2592000000L || now.time - lastSeen.time < 1000L) {
                showRateSnackBar(view, activity)
                PreferencesImpl.setRateLastSeen(view.context)
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

    fun getCurrentVersion(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }
}
