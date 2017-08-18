package fr.cph.chicago.data

import android.content.Context

import java.util.Date

import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine

interface Preferences {

    fun saveTrainFavorites(context: Context, favorites: List<Int>)

    fun hasFavorites(context: Context): Boolean

    fun saveBikeFavorites(context: Context, favorites: List<String>)

    fun getBikeFavorites(context: Context): MutableList<String>

    fun addBikeRouteNameMapping(context: Context, bikeId: String, bikeName: String)

    fun getBikeRouteNameMapping(context: Context, bikeId: String): String?

    fun saveBusFavorites(context: Context, favorites: List<String>)

    fun getBusFavorites(context: Context): MutableList<String>

    fun addBusRouteNameMapping(context: Context, busStopId: String, routeName: String)

    fun getBusRouteNameMapping(context: Context, busStopId: String): String?

    fun addBusStopNameMapping(context: Context, busStopId: String, stopName: String)

    fun getBusStopNameMapping(context: Context, busStopId: String): String?

    fun getTrainFavorites(context: Context): MutableList<Int>

    fun saveTrainFilter(context: Context, stationId: Int, line: TrainLine, direction: TrainDirection, value: Boolean)

    fun getTrainFilter(context: Context, stationId: Int, line: TrainLine, direction: TrainDirection): Boolean

    fun getRateLastSeen(context: Context): Date

    fun setRateLastSeen(context: Context)

    fun clearPreferences(context: Context)
}
