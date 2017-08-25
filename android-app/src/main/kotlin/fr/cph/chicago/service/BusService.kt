package fr.cph.chicago.service

import android.content.Context
import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.*
import fr.cph.chicago.entity.*
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

    fun loadFavoritesBuses(context: Context): List<BusArrival> {
        // FIXME to refactor
        val busArrivals = LinkedHashSet<BusArrival>()
        val paramBus = preferenceService.getFavoritesBusParams(context)
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
                para.put(context.getString(R.string.request_rt), rts[i])
                para.put(context.getString(R.string.request_stop_id), stpids[i])
                val xmlResult = ctaClient.connect(BUS_ARRIVALS, para)
                busArrivals.addAll(xmlParser.parseBusArrivals(xmlResult))
            }
        } catch (e: Throwable) {
            throw Exceptions.propagate(e)
        }
        return busArrivals.toMutableList()
    }

    fun loadOneBusStop(context: Context, stopId: String, bound: String): List<BusStop> {
        try {
            val params = ArrayListValuedHashMap<String, String>()
            params.put(context.getString(R.string.request_rt), stopId)
            params.put(context.getString(R.string.request_dir), bound)
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

    fun loadBusDirections(context: Context, busRouteId: String): BusDirections {
        try {
            val reqParams = ArrayListValuedHashMap<String, String>()
            reqParams.put(context.getString(R.string.request_rt), busRouteId)
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

    fun loadFollowBus(context: Context, busId: String): List<BusArrival> {
        try {
            val connectParam = ArrayListValuedHashMap<String, String>()
            connectParam.put(context.getString(R.string.request_vid), busId)
            val content = ctaClient.connect(BUS_ARRIVALS, connectParam)
            return xmlParser.parseBusArrivals(content)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBusPattern(context: Context, busRouteId: String, bound: String): BusPattern {
        val connectParam = ArrayListValuedHashMap<String, String>()
        connectParam.put(context.getString(R.string.request_rt), busRouteId)
        val boundIgnoreCase = bound.toLowerCase(Locale.US)
        try {
            val content = ctaClient.connect(BUS_PATTERN, connectParam)
            val patterns = xmlParser.parsePatterns(content)
            return patterns
                .filter { pattern ->
                    val directionIgnoreCase = pattern.direction.toLowerCase(Locale.US)
                    pattern.direction == bound || boundIgnoreCase.contains(directionIgnoreCase)
                }
                .getOrElse(0, { BusPattern("error", mutableListOf()) })
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadBus(context: Context, busId: Int, busRouteId: String): List<Bus> {
        val connectParam = ArrayListValuedHashMap<String, String>()
        if (busId != 0) {
            connectParam.put(context.getString(R.string.request_vid), Integer.toString(busId))
        } else {
            connectParam.put(context.getString(R.string.request_rt), busRouteId)
        }
        try {
            val content = ctaClient.connect(BUS_VEHICLES, connectParam)
            return xmlParser.parseVehicles(content)
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun loadAroundBusArrivals(context: Context, busStop: BusStop): List<BusArrival> {
        try {
            val busStopId = busStop.id
            val reqParams = ArrayListValuedHashMap<String, String>(1, 1)
            reqParams.put(context.getString(R.string.request_stop_id), Integer.toString(busStopId))
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
        return busRepository.busRoutes
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
            .sortedWith(Util.busStopComparatorByName)
    }
}
