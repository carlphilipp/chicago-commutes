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

import android.util.ArrayMap
import fr.cph.chicago.Constants.REQUEST_ROUTE
import fr.cph.chicago.Constants.REQUEST_STOP_ID
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.BusStopPattern
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.exception.CantLoadBusException
import fr.cph.chicago.exception.CtaException
import fr.cph.chicago.parseNotNull
import fr.cph.chicago.parser.BusStopCsvParser
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.BusRepository
import fr.cph.chicago.rx.RxUtil.handleListError
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Callable
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import org.apache.commons.text.WordUtils
import timber.log.Timber

object BusService {

    private val busStopCsvParser = BusStopCsvParser
    private val preferenceService = PreferenceService
    private val busRepository = BusRepository
    private val ctaClient = CtaClient
    private val util = Util
    private val simpleDateFormatBus: SimpleDateFormat = SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)

    fun loadFavoritesBuses(): Single<BusArrivalDTO> {
        return Single.fromCallable { preferenceService.getFavoritesBusParams() }
            .flatMap { favoritesBusParams ->
                if (favoritesBusParams.isEmpty) {
                    Single.just(listOf())
                } else {
                    val params = ArrayListValuedHashMap<String, String>(2, 1)
                    params.put(REQUEST_ROUTE, favoritesBusParams.get(REQUEST_ROUTE).joinToString(separator = ","))
                    params.put(REQUEST_STOP_ID, favoritesBusParams.get(REQUEST_STOP_ID).joinToString(separator = ","))
                    getBusArrivals(
                        routes = favoritesBusParams.get(REQUEST_ROUTE).toList(),
                        stopIds = favoritesBusParams.get(REQUEST_STOP_ID).map { it.toBigInteger() }.toList()
                    )
                }
            }
            .subscribeOn(Schedulers.computation())
            .map { favoriteBuses -> BusArrivalDTO(favoriteBuses, false) }
    }

    fun loadAllBusStopsForRouteBound(route: String, bound: String): Single<List<BusStop>> {
        return ctaClient.getBusStops(route, bound)
            .map { busStopsResponse ->
                if (busStopsResponse.bustimeResponse.stops == null) {
                    throw CtaException(busStopsResponse)
                }
                busStopsResponse.bustimeResponse.stops!!.map { stop ->
                    BusStop(
                        id = stop.stpid.toBigInteger(),
                        name = WordUtils.capitalizeFully(stop.stpnm),
                        description = stop.stpnm,
                        position = Position(stop.lat, stop.lon)
                    )
                }
            }
    }

    fun getStopPosition(route: String, bound: String, stopId: BigInteger): @NonNull Single<Position> {
        return ctaClient.getBusStops(route, bound)
            .map { busStopsResponse ->
                if (busStopsResponse.bustimeResponse.stops == null) {
                    throw CtaException(busStopsResponse)
                }
                busStopsResponse.bustimeResponse.stops!!.map { stop ->
                    BusStop(
                        id = stop.stpid.toBigInteger(),
                        name = WordUtils.capitalizeFully(stop.stpnm),
                        description = stop.stpnm,
                        position = Position(stop.lat, stop.lon)
                    )
                }
            }
            .toObservable()
            .flatMapIterable { busStop -> busStop }
            .filter { busStop -> busStop.id == stopId }
            .map { busStop -> busStop.position }
            .firstOrError()
    }

    fun loadLocalBusData(): Single<Boolean> {
        return Single
            .fromCallable {
                if (busRepository.hasBusStopsEmpty()) {
                    Timber.d("Load bus stop from CSV")
                    busStopCsvParser.parse()
                }
            }
            .map { false }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not create local bus data")
                true
            }
    }

    fun loadBusDirectionsSingle(busRouteId: String): Single<BusDirections> {
        return ctaClient.getBusDirections(busRouteId)
            .map { response ->
                if (response.bustimeResponse.directions == null) {
                    throw CtaException(response)
                } else {
                    response
                }
            }
            .map { response ->
                val busDirections = BusDirections(busRouteId)
                response
                    .bustimeResponse
                    .directions!!
                    .map { direction -> BusDirection.fromString(direction.dir) }
                    .forEach { busDirections.addBusDirection(it) }
                busDirections
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun busRoutes(): Single<List<BusRoute>> {
        return ctaClient.getBusRoutes()
            .map { bustimeResponse ->
                bustimeResponse.bustimeResponse
                    .routes
                    .map { route -> BusRoute(route.routeId, route.routeName) }
            }
    }

    fun loadFollowBus(busId: String): Single<List<BusArrival>> {
        return getBusArrivals(busId = busId)
            .onErrorReturn(handleListError())
            .subscribeOn(Schedulers.computation())
    }

    fun loadBusPattern(busRouteId: String, bound: String): Single<BusPattern> {
        return loadBusPattern(busRouteId, arrayOf(bound)).map { busPatterns -> busPatterns[0] }
    }

    fun loadBusPattern(busRouteId: String, bounds: Array<String>): Single<List<BusPattern>> {
        return ctaClient.getBusPatterns(busRouteId)
            .map { response ->
                if (response.bustimeResponse.ptr == null) throw CtaException(response)
                response
                    .bustimeResponse
                    .ptr!!
                    .map { ptr ->
                        BusPattern(
                            direction = ptr.rtdir,
                            busStopsPatterns = ptr.pt
                                .map { pt -> BusStopPattern(Position(pt.lat, pt.lon), pt.typ, pt.stpnm ?: StringUtils.EMPTY) }
                                .toMutableList())
                    }
                    .filter { pattern ->
                        val directionIgnoreCase = pattern.direction.lowercase()
                        val boundIgnoreCase = bounds.map { bound -> bound.lowercase() }
                        boundIgnoreCase.contains(directionIgnoreCase)
                    }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun busForRouteId(busRouteId: String): Single<List<Bus>> {
        return ctaClient.getBusVehicles(busRouteId)
            .map { result ->
                if (result.bustimeResponse.vehicle == null) throw CantLoadBusException(result)
                result.bustimeResponse.vehicle!!
                    .map { vehicle ->
                        val position = Position(vehicle.lat.toDouble(), vehicle.lon.toDouble())
                        Bus(vehicle.vid.toInt(), position, vehicle.hdg.toInt(), vehicle.des)
                    }
            }
            .subscribeOn(Schedulers.computation())
    }

    fun loadBusArrivals(busStop: BusStop): Single<List<BusArrival>> {
        return getBusArrivals(listOf(busStop.id))
            .onErrorReturn(handleListError())
            .subscribeOn(Schedulers.computation())
    }

    fun busStopsAround(position: Position): Single<List<BusStop>> {
        return singleFromCallable(Callable { busRepository.getBusStopsAround(position) })
            .onErrorReturn(handleListError())
    }

    fun saveBusStops(busStops: List<BusStop>) {
        return busRepository.saveBusStops(busStops)
    }

    /**
     *  We can't guaranty that the repo will be populated when we call that method
     */
    fun getBusRoute(routeId: String): BusRoute {
        return if (store.state.busRoutes.isEmpty()) {
            getBusRouteFromFavorites(routeId)
        } else {
            return store.state.busRoutes
                .filter { (id) -> id == routeId }
                .getOrNull(0) ?: getBusRouteFromFavorites(routeId)
        }
    }

    private fun getBusRouteFromFavorites(routeId: String): BusRoute {
        val routeName = preferenceService.getBusRouteNameMapping(routeId)
        return BusRoute(routeId, routeName)
    }

    fun searchBusRoutes(query: String): Single<List<BusRoute>> {
        return Single
            .fromCallable {
                store.state.busRoutes
                    .filter { (id, name) -> containsIgnoreCase(id, query) || containsIgnoreCase(name, query) }
                    .distinct()
                    .sortedWith(util.busStopComparatorByName)
            }
            .subscribeOn(Schedulers.computation())
    }

    fun loadBusArrivals(busRouteId: String, busStopId: BigInteger, bound: String, boundTitle: String): Single<BusArrivalStopDTO> {
        return getBusArrivals(listOf(busStopId), listOf(busRouteId))
            .map { busArrivals ->
                val result = busArrivals
                    .filter { (_, _, _, _, _, _, routeDirection) -> routeDirection == bound || routeDirection == boundTitle }
                    .fold(BusArrivalStopDTO()) { accumulator, busArrival ->
                        accumulator.getOrPut(busArrival.busDestination) { mutableListOf() }.add(busArrival)
                        accumulator
                    }
                if (result.size == 0) {
                    val noService = ArrayMap<String, MutableList<BusArrival>>()
                    noService["No Service"] = mutableListOf()
                    BusArrivalStopDTO(noService)
                } else {
                    result
                }
            }
            .subscribeOn(Schedulers.computation())
    }

    fun extractBusRouteFavorites(busFavorites: List<String>): List<String> {
        return busFavorites
            .map { util.decodeBusFavorite(it) }
            .map { it.routeId }
            .distinct()
    }

    private fun getBusArrivals(stopIds: List<BigInteger>? = null, routes: List<String>? = null, busId: String? = null): Single<List<BusArrival>> {
        return ctaClient.getBusArrivals(stopIds?.map { it.toString() }, routes, busId)
            .map { result ->
                when (result.bustimeResponse.prd) {
                    null -> {
                        var res: List<BusArrival>? = null
                        if (result.bustimeResponse.error != null && result.bustimeResponse.error!!.isNotEmpty()) {
                            if (result.bustimeResponse.error!![0].noServiceScheduled()) {
                                res = listOf()
                            }
                        }
                        res ?: throw CtaException(result)
                    }
                    else -> {
                        result.bustimeResponse
                            .prd!!
                            .map { prd ->
                                BusArrival(
                                    timeStamp = simpleDateFormatBus.parseNotNull(prd.tmstmp),
                                    stopName = WordUtils.capitalizeFully(prd.stpnm),
                                    stopId = prd.stpid.toInt(),
                                    busId = prd.vid.toInt(),
                                    routeId = prd.rt,
                                    routeDirection = BusDirection.fromString(prd.rtdir).text,
                                    busDestination = prd.des,
                                    predictionTime = simpleDateFormatBus.parseNotNull(prd.prdtm),
                                    isDelay = prd.dly
                                )
                            }
                            .sortedBy { it.timeLeftMilli }
                    }
                }
            }
    }
}
