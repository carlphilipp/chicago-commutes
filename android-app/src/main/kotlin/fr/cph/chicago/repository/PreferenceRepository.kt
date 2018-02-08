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

package fr.cph.chicago.repository

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Class that store user preferences into the device
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object PreferenceRepository {

    private val TAG = PreferenceRepository::class.java.simpleName

    private val PATTERN = Pattern.compile("(\\d{1,3})")
    private val FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    private val PREFERENCE_FAVORITES = "ChicagoTrackerFavorites"
    private val PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain"
    private val PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus"
    private val PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING = "ChicagoTrackerFavoritesBusNameMapping"
    private val PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING = "ChicagoTrackerFavoritesBusStopNameMapping"
    private val PREFERENCE_FAVORITES_BIKE = "ChicagoTrackerFavoritesBike"
    private val PREFERENCE_FAVORITES_BIKE_NAME_MAPPING = "ChicagoTrackerFavoritesBikeNameMapping"

    private val trainService = TrainService
    private val util = Util

    fun saveTrainFavorites(favorites: List<Int>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = favorites.map { it.toString() }.toSet()
        Log.v(TAG, "Put train favorites: " + favorites.toString())
        editor.putStringSet(PREFERENCE_FAVORITES_TRAIN, set)
        editor.apply()
    }

    fun hasFavorites(): Boolean {
        val sharedPref = getPrivatePreferences()
        val setPref1 = sharedPref.getStringSet(PREFERENCE_FAVORITES_TRAIN, null)
        val setPref2 = sharedPref.getStringSet(PREFERENCE_FAVORITES_BUS, null)
        val setPref3 = sharedPref.getStringSet(PREFERENCE_FAVORITES_BIKE, null)
        return !((setPref1 == null || setPref1.size == 0) && (setPref2 == null || setPref2.size == 0) && (setPref3 == null || setPref3.size == 0))
    }

    fun saveBikeFavorites(favorites: List<String>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = favorites.toSet()
        Log.v(TAG, "Put bike favorites: $set")
        editor.putStringSet(PREFERENCE_FAVORITES_BIKE, set)
        editor.apply()
    }

    fun getBikeFavorites(): List<String> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_BIKE, LinkedHashSet())
        Log.v(TAG, "Read bike favorites : $setPref")
        return setPref.sorted()
    }

    fun addBikeRouteNameMapping(bikeId: String, bikeName: String) {
        val sharedPref = getPrivatePreferencesBikeMapping()
        val editor = sharedPref.edit()
        editor.putString(bikeId, bikeName)
        Log.v(TAG, "Add bike name mapping : $bikeId => $bikeName")
        editor.apply()
    }

    fun getBikeRouteNameMapping(bikeId: String): String? {
        val sharedPref = getPrivatePreferencesBikeMapping()
        val bikeName = sharedPref.getString(bikeId, null)
        Log.v(TAG, "Get bike name mapping : $bikeId => $bikeName")
        return bikeName
    }

    /**
     * Save bus into favorites
     *
     * @param favorites the list of favorites to save
     */
    fun saveBusFavorites(favorites: List<String>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = LinkedHashSet<String>()
        set.addAll(favorites)
        Log.v(TAG, "Put bus favorites: $favorites")
        editor.putStringSet(PREFERENCE_FAVORITES_BUS, set)
        editor.apply()
    }

    fun getBusFavorites(): List<String> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_BUS, LinkedHashSet())
        Log.v(TAG, "Read bus favorites : " + setPref!!.toString())
        return setPref.sortedWith(Comparator { str1, str2 ->
            val str1Decoded = util.decodeBusFavorite(str1).routeId
            val str2Decoded = util.decodeBusFavorite(str2).routeId
            val matcher1 = PATTERN.matcher(str1Decoded)
            val matcher2 = PATTERN.matcher(str2Decoded)
            if (matcher1.find() && matcher2.find()) {
                val one = Integer.parseInt(matcher1.group(1))
                val two = Integer.parseInt(matcher2.group(1))
                if (one < two) -1 else if (one == two) 0 else 1
            } else {
                str1Decoded.compareTo(str2Decoded)
            }
        })
    }

    fun addBusRouteNameMapping(busStopId: String, routeName: String) {
        val sharedPref = getPrivatePreferencesBusRouteMapping()
        val editor = sharedPref.edit()
        editor.putString(busStopId, routeName)
        Log.v(TAG, "Add bus route name mapping : $busStopId => $routeName")
        editor.apply()
    }

    fun getBusRouteNameMapping(busStopId: String): String? {
        val sharedPref = getPrivatePreferencesBusRouteMapping()
        val routeName = sharedPref.getString(busStopId, null)
        Log.v(TAG, "Get bus route name mapping : $busStopId => $routeName")
        return routeName
    }

    fun addBusStopNameMapping(busStopId: String, stopName: String) {
        val sharedPref = getPrivatePreferencesBusStopMapping()
        val editor = sharedPref.edit()
        editor.putString(busStopId, stopName)
        Log.v(TAG, "Add bus stop name mapping : $busStopId => $stopName")
        editor.apply()
    }

    fun getBusStopNameMapping(busStopId: String): String? {
        val sharedPref = getPrivatePreferencesBusStopMapping()
        val stopName = sharedPref.getString(busStopId, null)
        Log.v(TAG, "Get bus stop name mapping : $busStopId => $stopName")
        return stopName
    }

    fun getTrainFavorites(): List<Int> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_TRAIN, LinkedHashSet())
        Log.v(TAG, "Read train favorites : " + setPref)
        return setPref
            .map { it.toInt() }
            .map { trainService.getStation(it) }
            .sorted()
            .map { it.id }
            .toList()
    }

    fun saveTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection, value: Boolean) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        editor.putBoolean(stationId.toString() + "_" + line + "_" + direction, value)
        editor.apply()
    }

    fun getTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection): Boolean {
        val sharedPref = getPrivatePreferences()
        return sharedPref.getBoolean(stationId.toString() + "_" + line + "_" + direction, true)
    }

    fun getRateLastSeen(): Date {
        return try {
            val sharedPref = getPrivatePreferences()
            val defaultDate = FORMAT.format(Date())
            FORMAT.parse(sharedPref.getString("rateLastSeen", defaultDate))
        } catch (e: ParseException) {
            Date()
        }
    }

    fun setRateLastSeen() {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        editor.putString("rateLastSeen", FORMAT.format(Date()))
        editor.apply()
    }

    fun clearPreferences() {
        getPrivatePreferences().edit().clear().apply()
    }

    private fun getPrivatePreferences(): SharedPreferences {
        return App.instance.getSharedPreferences(PREFERENCE_FAVORITES, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBusStopMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBusRouteMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBikeMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, MODE_PRIVATE)
    }
}
