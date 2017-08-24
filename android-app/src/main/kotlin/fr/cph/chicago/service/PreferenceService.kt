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

    private val repo: PreferenceRepository = PreferenceRepository

    fun isTrainStationFavorite(context: Context, trainStationId: Int): Boolean {
        return repo.getTrainFavorites(context).any({ it == trainStationId })
    }

    fun getTrainFilter(context: Context, stationId: Int, line: TrainLine, direction: TrainDirection): Boolean {
        return repo.getTrainFilter(context, stationId, line, direction)
    }

    fun saveTrainFilter(context: Context, stationId: Int, line: TrainLine, direction: TrainDirection, value: Boolean) {
        repo.saveTrainFilter(context, stationId, line, direction, value)
    }

    fun addBikeRouteNameMapping(context: Context, bikeId: String, bikeName: String) {
        repo.addBikeRouteNameMapping(context, bikeId, bikeName)
    }

    fun addBusRouteNameMapping(context: Context, busStopId: String, routeName: String) {
        repo.addBikeRouteNameMapping(context, busStopId, routeName)
    }

    fun addBusStopNameMapping(context: Context, busStopId: String, stopName: String) {
        repo.addBusStopNameMapping(context, busStopId, stopName)
    }

    fun hasFavorites(context: Context): Boolean {
        return repo.hasFavorites(context)
    }

    fun clearPreferences(context: Context) {
        repo.clearPreferences(context)
    }

    fun getRateLastSeen(context: Context): Date {
        return repo.getRateLastSeen(context)
    }

    fun setRateLastSeen(context: Context) {
        repo.setRateLastSeen(context)
    }

    // Favorites
    fun isStopFavorite(context: Context, busRouteId: String, busStopId: Int, boundTitle: String): Boolean {
        return !repo.getBusFavorites(context)
            .firstOrNull({ favorite -> favorite == busRouteId + "_" + busStopId + "_" + boundTitle })
            .isNullOrBlank()
    }

    fun isBikeStationFavorite(context: Context, bikeStationId: Int): Boolean {
        return !repo.getBikeFavorites(context)
            .firstOrNull({ it.toInt() == bikeStationId })
            .isNullOrBlank()
    }

    fun addToBikeFavorites(stationId: Int, view: View) {
        val favorites = repo.getBikeFavorites(view.context)
        if (!favorites.contains(stationId.toString())) {
            favorites.add(stationId.toString())
            repo.saveBikeFavorites(view.context, favorites)
            Util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites(view.context)
        if (!favorites.contains(id)) {
            favorites.add(id)
            repo.saveBusFavorites(view.context, favorites)
            Util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun addToTrainFavorites(stationId: Int, view: View) {
        val favorites = repo.getTrainFavorites(view.context).toMutableList()
        if (!favorites.contains(stationId)) {
            favorites.add(stationId)
            repo.saveTrainFavorites(view.context, favorites)
            Util.showSnackBar(view, R.string.message_add_fav)
        }
    }

    fun getFavoritesBusParams(context: Context): MultiValuedMap<String, String> {
        val paramsBus = ArrayListValuedHashMap<String, String>()
        val busFavorites = repo.getBusFavorites(context)
        busFavorites
            .map { Util.decodeBusFavorite(it) }
            .forEach { (routeId, stopId) ->
                paramsBus.put(context.getString(R.string.request_rt), routeId)
                paramsBus.put(context.getString(R.string.request_stop_id), stopId)
            }
        return paramsBus
    }

    fun getFavoritesTrainParams(context: Context): MultiValuedMap<String, String> {
        val paramsTrain = ArrayListValuedHashMap<String, String>()
        val favorites = repo.getTrainFavorites(context)
        favorites.forEach { favorite -> paramsTrain.put(context.getString(R.string.request_map_id), favorite.toString()) }
        return paramsTrain
    }

    fun removeFromBusFavorites(busRouteId: String, busStopId: String, bound: String, view: View) {
        val id = busRouteId + "_" + busStopId + "_" + bound
        val favorites = repo.getBusFavorites(view.context)
        favorites.remove(id)
        repo.saveBusFavorites(view.context, favorites)
        Util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun removeFromBikeFavorites(stationId: Int, view: View) {
        val favorites = repo.getBikeFavorites(view.context)
        favorites.remove(Integer.toString(stationId))
        repo.saveBikeFavorites(view.context, favorites)
        Util.showSnackBar(view, R.string.message_remove_fav)
    }

    fun removeFromTrainFavorites(stationId: Int, view: View) {
        val favorites = repo.getTrainFavorites(view.context).toMutableList()
        favorites.remove(stationId)
        repo.saveTrainFavorites(view.context, favorites)
        Util.showSnackBar(view, R.string.message_remove_fav)
    }
}
