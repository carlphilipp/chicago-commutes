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

package fr.cph.chicago.service

import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.util.Util
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.Date

object PreferenceService {

    private val repo = PreferenceRepository
    private val util = Util

    fun isTrainStationFavorite(trainStationId: Int): Boolean {
        return repo.getTrainFavorites().any({ it == trainStationId })
    }

    fun getTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection): Boolean {
        return repo.getTrainFilter(stationId, line, direction)
    }

    fun saveTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection, value: Boolean) {
        repo.saveTrainFilter(stationId, line, direction, value)
    }

    fun addBikeRouteNameMapping(bikeId: String, bikeName: String) {
        repo.addBikeRouteNameMapping(bikeId, bikeName)
    }

    fun addBusRouteNameMapping(busStopId: String, routeName: String) {
        repo.addBikeRouteNameMapping(busStopId, routeName)
    }

    fun addBusStopNameMapping(busStopId: String, stopName: String) {
        repo.addBusStopNameMapping(busStopId, stopName)
    }

    fun hasFavorites(): Boolean {
        return repo.hasFavorites()
    }

    fun clearPreferences() {
        repo.clearPreferences()
    }

    fun getRateLastSeen(): Date {
        return repo.getRateLastSeen()
    }

    fun setRateLastSeen() {
        repo.setRateLastSeen()
    }

    // Favorites
    fun isStopFavorite(busRouteId: String, busStopId: Int, boundTitle: String): Boolean {
        return !repo.getBusFavorites()
            .firstOrNull({ favorite -> favorite == busRouteId + "_" + busStopId + "_" + boundTitle })
            .isNullOrBlank()
    }

    fun isBikeStationFavorite(bikeStationId: Int): Boolean {
        return !repo.getBikeFavorites()
            .firstOrNull({ it.toInt() == bikeStationId })
            .isNullOrBlank()
    }

    fun addToBikeFavorites(stationId: Int, view: View) {
        val favorites = repo.getBikeFavorites().toMutableList()
        if (!favorites.contains(stationId.toString())) {
            favorites.add(stationId.toString())
            repo.saveBikeFavorites(favorites)
            util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites().toMutableList()
        if (!favorites.contains(id)) {
            favorites.add(id)
            repo.saveBusFavorites(favorites)
            util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToTrainFavorites(stationId: Int, view: View) {
        val favorites = repo.getTrainFavorites().toMutableList()
        if (!favorites.contains(stationId)) {
            favorites.add(stationId)
            repo.saveTrainFavorites(favorites)
            util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun getFavoritesBusParams(): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>()
        val busFavorites = repo.getBusFavorites()
        busFavorites
            .map { util.decodeBusFavorite(it) }
            .forEach { (routeId, stopId) ->
                paramsBus.put(App.instance.getString(R.string.request_rt), routeId)
                paramsBus.put(App.instance.getString(R.string.request_stop_id), stopId)
            }
        return paramsBus
    }

    fun getFavoritesTrainParams(): MultiValuedMap<String, String> {
        val paramsTrain = ArrayListValuedHashMap<String, String>()
        val favorites = repo.getTrainFavorites()
        favorites.forEach { favorite -> paramsTrain.put(App.instance.getString(R.string.request_map_id), favorite.toString()) }
        return paramsTrain
    }

    fun removeFromBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites().toMutableList()
        favorites.remove(id)
        repo.saveBusFavorites(favorites)
        util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun removeFromBikeFavorites(stationId: Int, view: View) {
        val favorites = repo.getBikeFavorites().toMutableList()
        favorites.remove(Integer.toString(stationId))
        repo.saveBikeFavorites(favorites)
        util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun removeFromTrainFavorites(stationId: Int, view: View) {
        val favorites = repo.getTrainFavorites().toMutableList()
        favorites.remove(stationId)
        repo.saveTrainFavorites(favorites)
        util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun getBikeFavorites(): List<String> {
        return repo.getBikeFavorites()
    }

    fun getTrainFavorites(): List<Int> {
        return repo.getTrainFavorites()
    }

    fun getBusFavorites(): List<String> {
        return repo.getBusFavorites()
    }

    fun getBusRouteNameMapping(busStopId: String): String? {
        return repo.getBusRouteNameMapping(busStopId)
    }

    fun getBikeRouteNameMapping(bikeId: String): String? {
        return repo.getBikeRouteNameMapping(bikeId)
    }

    fun getBusStopNameMapping(busStopId: String): String? {
        return repo.getBusStopNameMapping(busStopId)
    }
}
