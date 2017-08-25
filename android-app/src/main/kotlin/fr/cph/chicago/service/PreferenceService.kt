package fr.cph.chicago.service

import android.content.Context
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.util.Util
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*

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
        val favorites = repo.getBikeFavorites()
        if (!favorites.contains(stationId.toString())) {
            favorites.add(stationId.toString())
            repo.saveBikeFavorites(favorites)
            util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites()
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

    fun getFavoritesBusParams(context: Context): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>()
        val busFavorites = repo.getBusFavorites()
        busFavorites
            .map { util.decodeBusFavorite(it) }
            .forEach { (routeId, stopId) ->
                paramsBus.put(context.getString(R.string.request_rt), routeId)
                paramsBus.put(context.getString(R.string.request_stop_id), stopId)
            }
        return paramsBus
    }

    fun getFavoritesTrainParams(context: Context): MultiValuedMap<String, String> {
        val paramsTrain = ArrayListValuedHashMap<String, String>()
        val favorites = repo.getTrainFavorites()
        favorites.forEach { favorite -> paramsTrain.put(context.getString(R.string.request_map_id), favorite.toString()) }
        return paramsTrain
    }

    fun removeFromBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites()
        favorites.remove(id)
        repo.saveBusFavorites(favorites)
        util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun removeFromBikeFavorites(stationId: Int, view: View) {
        val favorites = repo.getBikeFavorites()
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
}
