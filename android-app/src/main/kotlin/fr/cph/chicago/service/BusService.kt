package fr.cph.chicago.service

import android.content.Context
import fr.cph.chicago.data.BusData
import fr.cph.chicago.entity.*

interface BusService {

    fun loadFavoritesBuses(context: Context): List<BusArrival>

    fun loadOneBusStop(context: Context, stopId: String, bound: String): List<BusStop>

    fun loadLocalBusData(context: Context): BusData

    fun loadBusDirections(context: Context, busRouteId: String): BusDirections

    fun loadBusRoutes(): List<BusRoute>

    fun loadFollowBus(context: Context, busId: String): List<BusArrival>

    fun loadBusPattern(context: Context, busRouteId: String, bound: String): BusPattern

    fun loadBus(context: Context, busId: Int, busRouteId: String): List<Bus>

    fun loadAroundBusArrivals(context: Context, busStop: BusStop): List<BusArrival>
}
