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
import fr.cph.chicago.entity.*
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.parser.BusStopCsvParser
import fr.cph.chicago.parser.XmlParser
import fr.cph.chicago.repository.BusRepository
import fr.cph.chicago.util.Util
import io.reactivex.exceptions.Exceptions
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import java.util.*

object BusService {

    private val TAG = BusService::class.java.simpleName
    private val busStopCsvParser = BusStopCsvParser
    private val preferenceService = PreferenceService
    private val busRepository = BusRepository
    private val ctaClient = CtaClient
    private val xmlParser = XmlParser
    private val util = Util

    fun loadFavoritesBuses(): List<BusArrival> {
        // FIXME to refactor
        val busArrivals = LinkedHashSet<BusArrival>()
        val paramBus = preferenceService.getFavoritesBusParams()
        // Load bus
        try {
            val rts = mutableListOf<String>()
            val stpids = mutableListOf<String>()
            for ((key, value) in paramBus.asMap()) {
                var str = StringBuilder()
                var i = 0
                val values = value as List<*>
                for (v in values) {
                    str.append(v).append(",")
                    if (i == 9 || i == values.size - 1) {
                        if ("rt" == key) {
                            rts.add(str.toString())
                        } else if ("stpid" == key) {
                            stpids.add(str.toString())
                        }
                        str = StringBuilder()
                        i = -1
                    }
                    i++
                }
            }
            for (i in rts.indices) {
                val para = ArrayListValuedHashMap<String, String>()
                para.put(App.instance.getString(R.string.request_rt), rts[i])
                para.put(App.instance.getString(R.string.request_stop_id), stpids[i])
                val xmlResult = ctaClient.connect(BUS_ARRIVALS, para)
                busArrivals.addAll(xmlParser.parseBusArrivals(xmlResult))
            }
        } catch (e: Throwable) {
            throw Exceptions.propagate(e)
        }
        return busArrivals.toMutableList()
    }

    fun loadOneBusStop(stopId: String, bound: String): List<BusStop> {
        try {
            val params = ArrayListValuedHashMap<String, String>()
            params.put(App.instance.getString(R.string.request_rt), stopId)
            params.put(App.instance.getString(R.string.request_dir), bound)
            val xmlResult = ctaClient.connect(BUS_STOP_LIST, params)
            return xmlParser.parseBusBounds(xmlResult)
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
            val reqParams = ArrayListValuedHashMap<String, String>()
            reqParams.put(App.instance.getString(R.string.request_rt), busRouteId)
            val xmlResult = ctaClient.connect(BUS_DIRECTION, reqParams)
            return xmlParser.parseBusDirections(xmlResult, busRouteId)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBusRoutes(): List<BusRoute> {
        try {
            val params = ArrayListValuedHashMap<String, String>()
            val xmlResult = ctaClient.connect(BUS_ROUTES, params)
            return xmlParser.parseBusRoutes(xmlResult)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadFollowBus(busId: String): List<BusArrival> {
        try {
            val connectParam = ArrayListValuedHashMap<String, String>()
            connectParam.put(App.instance.getString(R.string.request_vid), busId)
            val content = ctaClient.connect(BUS_ARRIVALS, connectParam)
            return xmlParser.parseBusArrivals(content)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBusPattern(busRouteId: String, bound: String): BusPattern {
        return loadBusPattern(busRouteId, arrayOf(bound)).getOrElse(0, { BusPattern("error", mutableListOf()) })
    }

    fun loadBusPattern(busRouteId: String, bounds: Array<String>): List<BusPattern> {
        val connectParam = ArrayListValuedHashMap<String, String>()
        connectParam.put(App.instance.getString(R.string.request_rt), busRouteId)
        val boundIgnoreCase = bounds.map { bound -> bound.toLowerCase(Locale.US) }
        try {
            val content = ctaClient.connect(BUS_PATTERN, connectParam)
            val patterns = xmlParser.parsePatterns(content)
            return patterns
                .filter { pattern ->
                    val directionIgnoreCase = pattern.direction.toLowerCase(Locale.US)
                    boundIgnoreCase.contains(directionIgnoreCase)
                }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBus(busId: Int, busRouteId: String): List<Bus> {
        val connectParam = ArrayListValuedHashMap<String, String>()
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
            val reqParams = ArrayListValuedHashMap<String, String>(1, 1)
            reqParams.put(App.instance.getString(R.string.request_stop_id), Integer.toString(busStopId))
            val inputStream = ctaClient.connect(BUS_ARRIVALS, reqParams)
            return xmlParser.parseBusArrivals(inputStream)
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

    fun setBusRoutes(busRoutes: List<BusRoute>) {
        busRepository.setBusRoutes(busRoutes)
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
        val params = ArrayListValuedHashMap<String, String>()
        params.put(requestRt, busRouteId)
        params.put(requestStopId, busStopId.toString())
        val xmlResult = ctaClient.connect(BUS_ARRIVALS, params)
        return xmlParser.parseBusArrivals(xmlResult).filter(predicate)
    }
}
