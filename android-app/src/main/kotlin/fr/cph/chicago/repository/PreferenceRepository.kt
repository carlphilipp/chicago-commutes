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

package fr.cph.chicago.repository

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.model.dto.PreferencesDTO
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.parseNotNull
import fr.cph.chicago.repository.PrefType.BIKE
import fr.cph.chicago.repository.PrefType.BIKE_NAME_MAPPING
import fr.cph.chicago.repository.PrefType.BUS
import fr.cph.chicago.repository.PrefType.BUS_ROUTE_NAME_MAPPING
import fr.cph.chicago.repository.PrefType.BUS_STOP_NAME_MAPPING
import fr.cph.chicago.repository.PrefType.FAVORITES
import fr.cph.chicago.repository.PrefType.THEME
import fr.cph.chicago.repository.PrefType.TRAIN
import fr.cph.chicago.repository.PrefType.TRAIN_FILTER
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import java.math.BigInteger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * Class that store user preferences into the device
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object PreferenceRepository {

    private val PATTERN = Pattern.compile("(\\d{1,3})")
    private val FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    private val trainService = TrainService
    private val util = Util

    // All
    fun hasFavorites(): Boolean {
        val sharedPref = getPrivatePreferences()
        val prefTrainFav = sharedPref.getStringSet(TRAIN.value, null)
        val prefBusFav = sharedPref.getStringSet(BUS.value, null)
        val prefBikeFav = sharedPref.getStringSet(BIKE.value, null)
        return !(prefTrainFav.isNullOrEmpty() && prefBusFav.isNullOrEmpty() && prefBikeFav.isNullOrEmpty())
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllPreferences(): PreferencesDTO {
        val preferencesDTO = PreferencesDTO()
        getAllPreferences(FAVORITES).forEach {
            val prefType = PrefType.of(it.key)
            if (prefType == null) {
                Timber.w("Preference not found ${it.key}")
            } else {
                if (it.value is String) {
                    preferencesDTO.addPreference(prefType, setOf(it.value))
                } else {
                    preferencesDTO.addPreference(prefType, it.value as Set<String>)
                }
            }
        }
        preferencesDTO.addPreference(TRAIN_FILTER, getAllPreferences(TRAIN_FILTER))
        preferencesDTO.addPreference(BUS_ROUTE_NAME_MAPPING, getAllPreferences(BUS_ROUTE_NAME_MAPPING))
        preferencesDTO.addPreference(BUS_STOP_NAME_MAPPING, getAllPreferences(BUS_STOP_NAME_MAPPING))
        preferencesDTO.addPreference(BIKE_NAME_MAPPING, getAllPreferences(BIKE_NAME_MAPPING))
        return preferencesDTO
    }

    private fun getAllPreferences(prefType: PrefType): Map<String, *> {
        val sharedPref = App.instance.getSharedPreferences(prefType.value, MODE_PRIVATE)
        return sharedPref.all ?: mapOf<String, Any?>()
    }

    fun clearPreferences() {
        getPrivatePreferences().edit().clear().apply()
        getPrivatePreferencesBusStopMapping().edit().clear().apply()
        getPrivatePreferencesBusRouteMapping().edit().clear().apply()
        getPrivatePreferencesBikeMapping().edit().clear().apply()
        getPrivatePreferencesTrainFilter().edit().clear().apply()
    }

    // Themes
    fun getTheme(): String {
        val sharedPref = getPrivatePreferences()
        return sharedPref.getString(THEME.value, Theme.AUTO.key)!!
    }

    fun saveTheme(theme: String) {
        val editor = getPrivatePreferences().edit()
        editor.putString(THEME.value, theme)
        editor.apply()
    }

    // Trains
    fun saveTrainFavorites(favorites: Set<BigInteger>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = favorites.map { it.toString() }.toSet()
        Timber.v("Put train favorites: %s", favorites)
        editor.putStringSet(TRAIN.value, set)
        editor.apply()
    }

    fun getTrainFavorites(): Set<BigInteger> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(TRAIN.value, LinkedHashSet()) ?: setOf()
        Timber.v("Read train favorites : %s", setPref)
        return setPref
            .asSequence()
            .map { BigInteger(it) }
            .map { trainService.getStation(it) }
            .sorted()
            .map { it.id }
            .toSet()
    }

    fun saveTrainFilter(stationId: BigInteger, line: TrainLine, direction: TrainDirection) {
        val sharedPref = getPrivatePreferencesTrainFilter()
        val editor = sharedPref.edit()
        editor.putBoolean(stationId.toString() + "_" + line + "_" + direction, false)
        editor.apply()
    }

    fun getTrainFilter(stationId: BigInteger, line: TrainLine, direction: TrainDirection): Boolean {
        val sharedPref = getPrivatePreferencesTrainFilter()
        return sharedPref.getBoolean(stationId.toString() + "_" + line + "_" + direction, true)
    }

    fun removeTrainFilter(stationId: BigInteger, line: TrainLine, direction: TrainDirection) {
        val sharedPref = getPrivatePreferencesTrainFilter()
        val editor = sharedPref.edit()
        editor.remove(stationId.toString() + "_" + line + "_" + direction)
        editor.apply()
    }

    // Buses
    fun saveBusFavorites(favorites: Set<String>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = LinkedHashSet<String>()
        set.addAll(favorites)
        Timber.v("Put bus favorites: %s", favorites)
        editor.putStringSet(BUS.value, set)
        editor.apply()
    }

    fun getBusFavorites(): Set<String> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(BUS.value, LinkedHashSet()) ?: setOf()
        Timber.v("Read bus favorites : %s", setPref)
        return setPref.sortedWith { str1, str2 ->
            val str1Decoded = util.decodeBusFavorite(str1).routeId
            val str2Decoded = util.decodeBusFavorite(str2).routeId
            val matcher1 = PATTERN.matcher(str1Decoded)
            val matcher2 = PATTERN.matcher(str2Decoded)
            if (matcher1.find() && matcher2.find()) {
                val one = Integer.parseInt(matcher1.group(1)!!)
                val two = Integer.parseInt(matcher2.group(1)!!)
                if (one < two) -1 else if (one == two) 0 else 1
            } else {
                str1Decoded.compareTo(str2Decoded)
            }
        }.toSet()
    }

    fun addBusRouteNameMapping(busStopId: String, routeName: String) {
        val sharedPref = getPrivatePreferencesBusRouteMapping()
        val editor = sharedPref.edit()
        editor.putString(busStopId, routeName)
        Timber.v("Add bus route name mapping : %s => %s", busStopId, routeName)
        editor.apply()
    }

    fun getBusRouteNameMapping(busStopId: String): String {
        val sharedPref = getPrivatePreferencesBusRouteMapping()
        val routeName = sharedPref.getString(busStopId, "?")!!
        Timber.v("Get bus route name mapping : %s => %s", busStopId, routeName)
        return routeName
    }

    fun removeBusRouteNameMapping(busStopId: String) {
        val sharedPref = getPrivatePreferencesBusRouteMapping()
        val editor = sharedPref.edit()
        editor.remove(busStopId)
        Timber.v("Delete bus route name mapping  : %s", busStopId)
        editor.apply()
    }

    fun addBusStopNameMapping(busStopId: String, stopName: String) {
        val sharedPref = getPrivatePreferencesBusStopMapping()
        val editor = sharedPref.edit()
        editor.putString(busStopId, stopName)
        Timber.v("Add bus stop name mapping : %s => %s", busStopId, stopName)
        editor.apply()
    }

    fun getBusStopNameMapping(busStopId: String): String? {
        val sharedPref = getPrivatePreferencesBusStopMapping()
        val stopName = sharedPref.getString(busStopId, null)
        Timber.v("Get bus stop name mapping : %s => %s", busStopId, stopName)
        return stopName
    }

    fun removeBusStopNameMapping(busStopId: String) {
        val sharedPref = getPrivatePreferencesBusStopMapping()
        val editor = sharedPref.edit()
        editor.remove(busStopId)
        Timber.v("Delete bus stop name mapping  : %s", busStopId)
        editor.apply()
    }

    // Bikes
    fun saveBikeFavorites(favorites: Set<BigInteger>) {
        val sharedPref = getPrivatePreferences()
        val editor = sharedPref.edit()
        val set = favorites.map { it.toString() }.toSet()
        Timber.v("Put bike favorites: %s", set)
        editor.putStringSet(BIKE.value, set)
        editor.apply()
    }

    fun getBikeFavorites(): Set<BigInteger> {
        val sharedPref = getPrivatePreferences()
        val setPref = sharedPref.getStringSet(BIKE.value, LinkedHashSet()) ?: setOf()
        Timber.v("Read bike favorites : %s", setPref)
        return setPref.map { it.toBigInteger() }.sorted().toSet()
    }

    fun addBikeRouteNameMapping(bikeId: BigInteger, bikeName: String) {
        val sharedPref = getPrivatePreferencesBikeMapping()
        val editor = sharedPref.edit()
        editor.putString(bikeId.toString(), bikeName)
        Timber.v("Add bike name mapping : %s => %s", bikeId, bikeName)
        editor.apply()
    }

    fun getBikeRouteNameMapping(bikeId: BigInteger): String {
        val sharedPref = getPrivatePreferencesBikeMapping()
        val bikeName = sharedPref.getString(bikeId.toString(), StringUtils.EMPTY)!!
        Timber.v("Get bike name mapping : %s => %s", bikeId, bikeName)
        return bikeName
    }

    fun removeBikeRouteNameMapping(bikeId: BigInteger) {
        val sharedPref = getPrivatePreferencesBikeMapping()
        val editor = sharedPref.edit()
        editor.remove(bikeId.toString())
        Timber.v("Delete bike name mapping : %s", bikeId.toString())
        editor.apply()
    }

    // Rate last seen
    fun getRateLastSeen(): Date {
        return try {
            val sharedPref = getPrivatePreferences()
            val defaultDate: String = FORMAT.format(Date())
            FORMAT.parseNotNull(sharedPref.getString("rateLastSeen", defaultDate)!!)
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

    private fun getPrivatePreferences(): SharedPreferences {
        return App.instance.getSharedPreferences(FAVORITES.value, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBusStopMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(BUS_STOP_NAME_MAPPING.name, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBusRouteMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(BUS_ROUTE_NAME_MAPPING.name, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesBikeMapping(): SharedPreferences {
        return App.instance.getSharedPreferences(BIKE_NAME_MAPPING.name, MODE_PRIVATE)
    }

    private fun getPrivatePreferencesTrainFilter(): SharedPreferences {
        return App.instance.getSharedPreferences(TRAIN_FILTER.value, MODE_PRIVATE)
    }
}

enum class PrefType(val value: String) {
    FAVORITES("ChicagoTrackerFavorites"),
    THEME("ChicagoTrackerTheme"),
    TRAIN("ChicagoTrackerFavoritesTrain"),
    TRAIN_FILTER("ChicagoTrackerFavoritesTrainFilter"),
    BUS("ChicagoTrackerFavoritesBus"),
    BUS_ROUTE_NAME_MAPPING("ChicagoTrackerFavoritesBusNameMapping"),
    BUS_STOP_NAME_MAPPING("ChicagoTrackerFavoritesBusStopNameMapping"),
    BIKE("ChicagoTrackerFavoritesBike"),
    BIKE_NAME_MAPPING("ChicagoTrackerFavoritesBikeNameMapping");

    companion object {
        private val values = values()
        fun of(value: String) = values.firstOrNull { it.value == value }
    }
}
