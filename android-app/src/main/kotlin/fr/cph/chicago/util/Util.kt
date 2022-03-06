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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.dto.BusFavoriteDTO
import java.io.Closeable
import java.io.IOException
import java.io.Reader
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

    fun checkIfPermissionGranted(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
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
}
