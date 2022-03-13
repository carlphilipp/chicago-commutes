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

package fr.cph.chicago.service

import android.content.res.Configuration
import android.graphics.Color
import fr.cph.chicago.Constants.REQUEST_ROUTE
import fr.cph.chicago.Constants.REQUEST_STOP_ID
import fr.cph.chicago.R
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.model.dto.PreferencesDTO
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.theme.ThemeColor
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Date
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap

object PreferenceService {

    private val repo = PreferenceRepository
    private val util = Util

    fun getTheme(): Theme {
        return Theme.convert(repo.getTheme())
    }

    fun saveTheme(theme: Theme) {
        repo.saveTheme(theme.key)
    }

    fun saveThemeColor(themeColor: ThemeColor) {
        repo.saveThemeColor(themeColor.name)
    }

    fun getThemeColor(): ThemeColor {
        val str = repo.getThemeColor()
        return ThemeColor.getThemeColor(str)
    }

    fun getDynamicColor(): Boolean {
        return repo.getDynamicColor()
    }

    fun saveDynamicColor(value: Boolean) {
        return repo.saveDynamicColor(value)
    }

    fun isTrainStationFavorite(trainStationId: String): Boolean {
        return store.state.trainFavorites.any { it == trainStationId }
    }

    fun getTrainFilter(stationId: String, line: TrainLine, direction: TrainDirection): Boolean {
        return repo.getTrainFilter(stationId, line, direction)
    }

    fun saveTrainFilter(stationId: String, line: TrainLine, direction: TrainDirection, isChecked: Boolean) {
        if (isChecked) {
            repo.removeTrainFilter(stationId, line, direction)
        } else {
            repo.saveTrainFilter(stationId, line, direction)
        }
    }

    fun hasFavorites(): Boolean {
        return repo.hasFavorites()
    }

    fun getAllFavorites(): Single<PreferencesDTO> {
        return Single
            .fromCallable { repo.getAllPreferences() }
            .subscribeOn(Schedulers.io())
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
    fun isStopFavorite(busRouteId: String, busStopId: String, boundTitle: String): Boolean {
        return !repo.getBusFavorites()
            .firstOrNull { favorite -> favorite == busRouteId + "_" + busStopId + "_" + boundTitle }
            .isNullOrBlank()
    }

    fun isBikeStationFavorite(bikeStationId: String): Boolean {
        return repo.getBikeFavorites().any { id -> id == bikeStationId }
    }

    fun addBikeToFavorites(stationId: String, stationName: String): Single<List<String>> {
        return Single.fromCallable { repo.getBikeFavorites().toMutableSet() }
            .map { favorites ->
                favorites.add(stationId)
                repo.saveBikeFavorites(favorites)
            }
            .flatMap { addBikeRouteNameMapping(stationId, stationName) }
            .flatMap { getBikeMatchingFavorites() }
            .subscribeOn(Schedulers.io())
    }

    private fun addBikeRouteNameMapping(bikeId: String, bikeName: String): Single<Unit> {
        return Single.fromCallable { repo.addBikeRouteNameMapping(bikeId, bikeName) }.subscribeOn(Schedulers.io())
    }

    fun addBusToFavorites(busRouteId: String, busStopId: String, bound: String, busRouteName: String, busStopName: String): Single<List<String>> {
        return Single.fromCallable { repo.getBusFavorites().toMutableSet() }
            .map { favorites ->
                val id = busRouteId + "_" + busStopId + "_" + bound
                favorites.add(id)
                repo.saveBusFavorites(favorites)
            }
            .flatMap { addBusRouteNameMapping(busStopId, busRouteName) }
            .flatMap { addBusStopNameMapping(busStopId, busStopName) }
            .flatMap { getBusFavorites() }
            .subscribeOn(Schedulers.io())
    }

    private fun addBusRouteNameMapping(busStopId: String, routeName: String): Single<Unit> {
        return Single.fromCallable { repo.addBusRouteNameMapping(busStopId, routeName) }.subscribeOn(Schedulers.io())
    }

    private fun addBusStopNameMapping(busStopId: String, stopName: String): Single<Unit> {
        return Single.fromCallable { repo.addBusStopNameMapping(busStopId, stopName) }.subscribeOn(Schedulers.io())
    }

    fun addTrainStationToFavorites(stationId: String): Single<List<String>> {
        return Single.fromCallable { repo.getTrainFavorites().toMutableSet() }
            .map { favorites ->
                if (!favorites.contains(stationId)) {
                    favorites.add(stationId)
                    repo.saveTrainFavorites(favorites)
                }
            }
            .flatMap { getTrainFavorites() }
            .subscribeOn(Schedulers.io())
    }

    fun getFavoritesBusParams(): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>(2)
        val busFavorites = repo.getBusFavorites()
        busFavorites
            .map { util.decodeBusFavorite(it) }
            .forEach { (routeId, stopId) ->
                paramsBus.put(REQUEST_ROUTE, routeId)
                paramsBus.put(REQUEST_STOP_ID, stopId)
            }
        return paramsBus
    }

    fun getFavoritesTrainParams(): Set<String> {
        return repo.getTrainFavorites()
    }

    fun removeBusFromFavorites(busRouteId: String, busStopId: String, bound: String): Single<List<String>> {
        return Single.fromCallable { repo.getBusFavorites().toMutableSet() }
            .map { favorites ->
                val id = busRouteId + "_" + busStopId + "_" + bound
                favorites.remove(id)
                repo.removeBusRouteNameMapping(busStopId)
                repo.removeBusStopNameMapping(busStopId)
                repo.saveBusFavorites(favorites)
            }
            .flatMap { getBusFavorites() }
            .subscribeOn(Schedulers.io())
    }

    fun removeBikeFromFavorites(stationId: String): Single<List<String>> {
        return Single.fromCallable { repo.getBikeFavorites().toMutableSet() }
            .map { favorites ->
                favorites.remove(stationId)
                repo.removeBikeRouteNameMapping(stationId)
                repo.saveBikeFavorites(favorites)
            }
            .flatMap { getBikeMatchingFavorites() }
            .subscribeOn(Schedulers.io())
    }

    fun removeTrainFromFavorites(stationId: String): Single<List<String>> {
        return Single.fromCallable { repo.getTrainFavorites().toMutableSet() }
            .map { favorites ->
                favorites.remove(stationId)
                repo.saveTrainFavorites(favorites)
            }
            .flatMap { getTrainFavorites() }
            .subscribeOn(Schedulers.io())
    }

    fun getBikeFavorites(): Single<List<String>> {
        return Single.fromCallable { repo.getBikeFavorites().toList() }.subscribeOn(Schedulers.io())
    }

    private fun getBikeMatchingFavorites(): Single<List<String>> {
        return Single.fromCallable { repo.getBikeFavorites() }
            .observeOn(Schedulers.computation())
            .map { favorites ->
                favorites
                    .flatMap { bikeStationId -> store.state.bikeStations.filter { station -> station.id == bikeStationId } }
                    .sortedWith(util.bikeStationComparator)
                    .map { station -> station.id }
            }
            .subscribeOn(Schedulers.io())
    }

    fun getTrainFavorites(): Single<List<String>> {
        return Single.fromCallable { repo.getTrainFavorites().toList() }.subscribeOn(Schedulers.io())
    }

    fun getBusFavorites(): Single<List<String>> {
        return Single.fromCallable { repo.getBusFavorites().toList() }.subscribeOn(Schedulers.io())
    }

    fun getBusRouteNameMapping(busStopId: String): String {
        return repo.getBusRouteNameMapping(busStopId)
    }

    fun getBikeRouteNameMapping(bikeId: String): String {
        return repo.getBikeRouteNameMapping(bikeId)
    }

    fun getBusStopNameMapping(busStopId: String): String? {
        return repo.getBusStopNameMapping(busStopId)
    }
}
