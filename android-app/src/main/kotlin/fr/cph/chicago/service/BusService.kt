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

import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.*
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.*
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.entity.*
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.parser.BusStopCsvParser
import fr.cph.chicago.parser.XmlParser
import fr.cph.chicago.repository.BusRepository
import fr.cph.chicago.util.Util
import io.reactivex.exceptions.Exceptions
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import java.text.SimpleDateFormat
import java.util.*

object BusService {

    private val TAG = BusService::class.java.simpleName
    private val busStopCsvParser = BusStopCsvParser
    private val preferenceService = PreferenceService
    private val busRepository = BusRepository
    private val ctaClient = CtaClient
    private val xmlParser = XmlParser
    private val util = Util
    private val simpleDateFormatBus: SimpleDateFormat = SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)

    fun loadFavoritesBuses(): List<BusArrival> {
        try {
            val favoritesBusParams = preferenceService.getFavoritesBusParams()
            if (favoritesBusParams.isEmpty) return mutableListOf()
            val params = ArrayListValuedHashMap<String, String>(2, 1)
            val routeIdParam = App.instance.getString(R.string.request_rt)
            val stopIdParam = App.instance.getString(R.string.request_stop_id)
            params.put(routeIdParam, favoritesBusParams.get(routeIdParam).joinToString(separator = ","))
            params.put(stopIdParam, favoritesBusParams.get(stopIdParam).joinToString(separator = ","))
            // Return mutable list because this need to be serializable
            return getBusArrivals(params).toMutableList()
        } catch (e: Throwable) {
            throw Exceptions.propagate(e)
        }
    }

    fun loadOneBusStop(stopId: String, bound: String): List<BusStop> {
        try {
            val params = ArrayListValuedHashMap<String, String>(2, 1)
            params.put(App.instance.getString(R.string.request_rt), stopId)
            params.put(App.instance.getString(R.string.request_dir), bound)
            val result = ctaClient.get(BUS_STOP_LIST, params, BusStopsResponse::class.java)
            if (result.bustimeResponse.stops == null) {
                val error = result.bustimeResponse.error?.joinToString { error -> "${error.rt} ${error.dir} ${error.msg}" }
                Log.e(TAG, error)
                return listOf()
            }
            return result.bustimeResponse.stops!!.map { stop ->
                BusStop(
                    id = stop.stpid.toInt(),
                    name = stop.stpnm,
                    description = stop.stpnm,
                    position = Position(stop.lat, stop.lon)
                )
            }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadLocalBusData(): Any {
        if (busRepository.hasBusStopsEmpty()) {
            Log.d(TAG, "Load bus stop from CSV")
            busStopCsvParser.parse()
        }
        return Any()
    }

    fun loadBusDirections(busRouteId: String): BusDirections {
        try {
            val params = ArrayListValuedHashMap<String, String>(1, 1)
            params.put(App.instance.getString(R.string.request_rt), busRouteId)
            val busDirections = BusDirections(busRouteId)
            val result = ctaClient.get(BUS_DIRECTION, params, BusDirectionResponse::class.java)
            if (result.bustimeResponse.directions == null) {
                val error = result.bustimeResponse.error?.joinToString { error -> "${error.rt} ${error.msg}" }
                Log.e(TAG, error)
                return busDirections
            }
            result
                .bustimeResponse
                .directions!!
                .map { direction -> BusDirection.fromString(direction.dir) }
                .forEach { busDirections.addBusDirection(it) }
            return busDirections
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBusRoutes(): List<BusRoute> {
        try {
            return ctaClient.get(BUS_ROUTES, ArrayListValuedHashMap<String, String>(), BusRoutesResponse::class.java)
                .bustimeResponse
                .routes
                .map { route -> BusRoute(route.routeId, route.routeName) }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadFollowBus(busId: String): List<BusArrival> {
        try {
            val params = ArrayListValuedHashMap<String, String>(1, 1)
            params.put(App.instance.getString(R.string.request_vid), busId)
            return getBusArrivals(params)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBusPattern(busRouteId: String, bound: String): BusPattern {
        return loadBusPattern(busRouteId, arrayOf(bound)).getOrElse(0, { BusPattern("error", mutableListOf()) })
    }

    fun loadBusPattern(busRouteId: String, bounds: Array<String>): List<BusPattern> {
        try {
            val connectParam = ArrayListValuedHashMap<String, String>(1, 1)
            connectParam.put(App.instance.getString(R.string.request_rt), busRouteId)
            val boundIgnoreCase = bounds.map { bound -> bound.toLowerCase(Locale.US) }
            val result = ctaClient.get(BUS_PATTERN, connectParam, BusPatternResponse::class.java)
            if (result.bustimeResponse.ptr == null) {
                val error = result.bustimeResponse.error?.joinToString { error -> "${error.rt} ${error.msg}" }
                Log.e(TAG, error)
                return listOf()
            }
            return result
                .bustimeResponse
                .ptr!!
                .map { ptr ->
                    BusPattern(
                        direction = ptr.rtdir,
                        points = ptr.pt.map { pt ->
                            val stopName = pt.stpnm ?: ""
                            PatternPoint(Position(pt.lat, pt.lon), pt.typ, stopName)
                        }.toMutableList()
                    )
                }
                .filter { pattern ->
                    val directionIgnoreCase = pattern.direction.toLowerCase(Locale.US)
                    boundIgnoreCase.contains(directionIgnoreCase)
                }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBus(busId: Int, busRouteId: String): List<Bus> {
        val connectParam = ArrayListValuedHashMap<String, String>(1, 1)
        if (busId != 0) {
            connectParam.put(App.instance.getString(R.string.request_vid), busId.toString())
        } else {
            connectParam.put(App.instance.getString(R.string.request_rt), busRouteId)
        }
        try {
            val content = ctaClient.connect(BUS_VEHICLES, connectParam)
            return xmlParser.parseVehicles(content)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadAroundBusArrivals(busStop: BusStop): List<BusArrival> {
        try {
            val busStopId = busStop.id
            val params = ArrayListValuedHashMap<String, String>(1, 1)
            params.put(App.instance.getString(R.string.request_stop_id), Integer.toString(busStopId))
            return getBusArrivals(params)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun getBusStopsAround(position: Position): List<BusStop> {
        return busRepository.getBusStopsAround(position)
    }

    fun saveBusStops(busStops: List<BusStop>) {
        return busRepository.saveBusStops(busStops)
    }

    fun getBusRoutes(): MutableList<BusRoute> {
        return busRepository.inMemoryBusRoutes
    }

    fun saveBusRoutes(busRoutes: List<BusRoute>) {
        busRepository.saveBusRoutes(busRoutes)
    }

    fun getBusRoute(routeId: String): BusRoute {
        return busRepository.getBusRoute(routeId)
    }

    fun busRouteError(): Boolean {
        return busRepository.busRouteError
    }

    fun setBusRouteError(value: Boolean) {
        busRepository.busRouteError = value
    }

    fun searchBusRoutes(query: String): List<BusRoute> {
        return getBusRoutes()
            .filter { (id, name) -> containsIgnoreCase(id, query) || containsIgnoreCase(name, query) }
            .distinct()
            .sortedWith(util.busStopComparatorByName)
    }

    @Throws(ParserException::class, ConnectException::class)
    fun loadBusArrivals(requestRt: String, busRouteId: String, requestStopId: String, busStopId: Int, predicate: (BusArrival) -> (Boolean)): List<BusArrival> {
        try {
            val params = ArrayListValuedHashMap<String, String>(2, 1)
            params.put(requestRt, busRouteId)
            params.put(requestStopId, busStopId.toString())
            return getBusArrivals(params).filter(predicate)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    private fun getBusArrivals(params: MultiValuedMap<String, String>): List<BusArrival> {
        val result = ctaClient.get(BUS_ARRIVALS, params, BusArrivalResponse::class.java)
        if (result.bustimeResponse.prd == null) {
            val error = result.bustimeResponse.error?.joinToString { error -> "${error.stpid} ${error.msg}" }
            Log.e(TAG, error)
            return listOf()
        }
        return result.bustimeResponse
            .prd!!
            .map { prd ->
                BusArrival(
                    timeStamp = simpleDateFormatBus.parse(prd.tmstmp),
                    errorMessage = StringUtils.EMPTY, // TODO evaluate why there is this field
                    stopName = prd.stpnm,
                    stopId = prd.stpid.toInt(),
                    busId = prd.vid.toInt(),
                    routeId = prd.rt,
                    routeDirection = BusDirection.fromString(prd.rtdir).text,
                    busDestination = prd.des,
                    predictionTime = simpleDateFormatBus.parse(prd.prdtm),
                    isDelay = prd.dly)
            }
    }
}
