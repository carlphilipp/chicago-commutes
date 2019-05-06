/**
 * Copyright 2019 Carl-Philipp Harmant
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

import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.Date

object PreferenceService {

    private val repo = PreferenceRepository
    private val util = Util

    fun getTheme(): String {
        return repo.getTheme()
    }

    fun getCurrentTheme(): Int {
        val theme = repo.getTheme()
        return if (theme == "Light") R.style.AppTheme else R.style.AppThemeDark
    }

    fun saveTheme(theme: String) {
        if (theme != "Light" && theme != "Dark") {
            throw RuntimeException("The theme can only be Light or Dark")
        }
        repo.saveTheme(theme)
    }

    fun isTrainStationFavorite(trainStationId: Int): Boolean {
        return store.state.trainFavorites.any { it == trainStationId }
    }

    fun getTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection): Boolean {
        return repo.getTrainFilter(stationId, line, direction)
    }

    fun saveTrainFilter(stationId: Int, line: TrainLine, direction: TrainDirection, value: Boolean) {
        repo.saveTrainFilter(stationId, line, direction, value)
    }

    fun addBikeRouteNameMapping(bikeId: Int, bikeName: String) {
        repo.addBikeRouteNameMapping(bikeId, bikeName)
    }

    fun addBusRouteNameMapping(busStopId: String, routeName: String) {
        repo.addBusRouteNameMapping(busStopId, routeName)
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
            .firstOrNull { favorite -> favorite == busRouteId + "_" + busStopId + "_" + boundTitle }
            .isNullOrBlank()
    }

    fun isBikeStationFavorite(bikeStationId: Int): Boolean {
        return repo.getBikeFavorites().any { id -> id == bikeStationId }
    }

    fun addToBikeFavorites(stationId: Int, stationName: String): Single<List<Int>> {
        // FIXME: make it reactive
        val favorites = repo.getBikeFavorites().toMutableList()
        if (!favorites.contains(stationId)) {
            favorites.add(stationId)
            repo.saveBikeFavorites(favorites)
        }
        addBikeRouteNameMapping(stationId, stationName)
        return getBikeMatchingFavorites()
    }

    fun addToBusFavorites(busRouteId: String, busStopId: String, bound: String, busRouteName: String, busStopName: String): Single<List<String>> {
        // FIXME: Make it reactive
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites().toMutableList()
        if (!favorites.contains(id)) {
            favorites.add(id)
            repo.saveBusFavorites(favorites)
        }
        addBusRouteNameMapping(busStopId, busRouteName)
        addBusStopNameMapping(busStopId, busStopName)
        return getBusFavorites().subscribeOn(Schedulers.io())
    }

    fun addToTrainFavorites(stationId: Int): Single<List<Int>> {
        return Single
            .fromCallable {
                val favorites = repo.getTrainFavorites().toMutableList()
                if (!favorites.contains(stationId)) {
                    favorites.add(stationId)
                    repo.saveTrainFavorites(favorites)
                }
            }
            .subscribeOn(Schedulers.io())
            .flatMap { getTrainFavorites() }
    }

    fun getFavoritesBusParams(): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>(2)
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
        val favorites = repo.getTrainFavorites()
        val paramsTrain = ArrayListValuedHashMap<String, String>(1, favorites.size)
        favorites.forEach { favorite -> paramsTrain.put(App.instance.getString(R.string.request_map_id), favorite.toString()) }
        return paramsTrain
    }

    fun removeFromBusFavorites(busRouteId: String, busStopId: String, bound: String): Single<List<String>> {
        // FIXME: Make it reactive
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites().toMutableList()
        favorites.remove(id)
        repo.saveBusFavorites(favorites)
        return getBusFavorites().subscribeOn(Schedulers.io())
    }

    fun removeFromBikeFavorites(stationId: Int): Single<List<Int>> {
        val favorites = repo.getBikeFavorites().toMutableList()
        favorites.remove(stationId)
        repo.saveBikeFavorites(favorites)
        return getBikeMatchingFavorites()
    }

    fun removeFromTrainFavorites(stationId: Int): Single<List<Int>> {
        return Single
            .fromCallable {
                val favorites = repo.getTrainFavorites().toMutableList()
                favorites.remove(stationId)
                repo.saveTrainFavorites(favorites)
            }
            .subscribeOn(Schedulers.io())
            .flatMap { getTrainFavorites() }
    }

    fun getBikeFavorites(): Single<List<Int>> {
        return Single.fromCallable { repo.getBikeFavorites() }.subscribeOn(Schedulers.io())
    }

    private fun getBikeMatchingFavorites(): Single<List<Int>> {
        return Single.fromCallable {
            repo.getBikeFavorites()
                .flatMap { bikeStationId -> store.state.bikeStations.filter { station -> station.id == bikeStationId } }
                .sortedWith(util.bikeStationComparator)
                .map { station -> station.id }
        }.subscribeOn(Schedulers.io())
    }

    fun getTrainFavorites(): Single<List<Int>> {
        return Single.fromCallable { repo.getTrainFavorites() }.subscribeOn(Schedulers.io())
    }

    fun getBusFavorites(): Single<List<String>> {
        return Single.fromCallable { repo.getBusFavorites() }
    }

    fun getBusRouteNameMapping(busStopId: String): String {
        return repo.getBusRouteNameMapping(busStopId)
    }

    fun getBikeRouteNameMapping(bikeId: Int): String {
        return repo.getBikeRouteNameMapping(bikeId)
    }

    fun getBusStopNameMapping(busStopId: String): String? {
        return repo.getBusStopNameMapping(busStopId)
    }
}
