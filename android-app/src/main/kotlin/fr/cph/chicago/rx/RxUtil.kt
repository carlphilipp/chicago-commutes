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

package fr.cph.chicago.rx

import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Train
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.TrainStationPattern
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.FirstLoadDTO
import fr.cph.chicago.core.model.dto.LocalDTO
import fr.cph.chicago.core.model.dto.RouteAlertsDTO
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.MapUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Callable

object RxUtil {

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val alertService = AlertService
    private val positionUtil = MapUtil

    // Local
    fun local(): Single<LocalDTO> {
        // Train local data
        val trainLocalData: Single<Boolean> = Single.fromCallable { trainService.loadLocalTrainData() }
            .map { it.size() == 0 }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not create local train data")
                true
            }

        // Bus local data
        val busLocalData: Single<Boolean> = Single.fromCallable { busService.loadLocalBusData() }
            .map { false }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not create local bus data")
                true
            }
        return Single.zip(
            trainLocalData,
            busLocalData,
            BiFunction { trainLocalError: Boolean, busLocalError: Boolean ->
                LocalDTO(trainLocalError, busLocalError)
            }
        )
    }

    // Http call
    fun busArrivalsForStop(requestRt: String, busRouteId: String, requestStopId: String, busStopId: Int, bound: String, boundTitle: String): Single<BusArrivalStopDTO> {
        return createSingleFromCallable(Callable { busService.loadBusArrivals(requestRt, busRouteId, requestStopId, busStopId, bound, boundTitle) })
    }

    fun trainArrivals(trainStationId: Int): Single<TrainArrival> {
        return createSingleFromCallable(Callable { trainService.loadStationTrainArrival(trainStationId) })
    }

    fun busArrivals(busStop: BusStop): Single<List<BusArrival>> {
        return createSingleFromCallable(Callable { busService.loadAroundBusArrivals(busStop) })
            .onErrorReturn(handleError())
    }

    fun bikeAllStations(): Single<List<BikeStation>> {
        return createSingleFromCallable(Callable { bikeService.loadAllBikeStations() })
    }

    fun bikeOneStation(bikeStation: BikeStation): Single<BikeStation> {
        return createSingleFromCallable(Callable { bikeService.findBikeStation(bikeStation.id) })
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load bike stations")
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun busStopsForRouteBound(route: String, bound: String): Single<List<BusStop>> {
        return createSingleFromCallable(Callable { busService.loadAllBusStopsForRouteBound(route, bound) })
    }

    fun busDirections(busRouteId: String): Single<BusDirections> {
        return createSingleFromCallable(Callable { busService.loadBusDirections(busRouteId) })
    }

    fun followBus(busId: String): Single<List<BusArrival>> {
        return createSingleFromCallable(Callable { busService.loadFollowBus(busId) })
            .onErrorReturn(handleError())
    }

    fun busPatterns(busRouteId: String, bound: String): Single<BusPattern> {
        return createSingleFromCallable(Callable { busService.loadBusPattern(busRouteId, bound) })
    }

    fun busForRouteId(busRouteId: String): Single<List<Bus>> {
        return createSingleFromCallable(Callable { busService.loadBus(busRouteId) })
    }

    fun trainLocations(line: String): Single<List<Train>> {
        return createSingleFromCallable(Callable { trainService.getTrainLocation(line) })
            .onErrorReturn(handleError())
    }

    fun trainPatterns(line: String): Single<List<TrainStationPattern>> {
        return createSingleFromCallable(Callable { trainService.readPatterns(TrainLine.fromXmlString(line)) })
            .onErrorReturn(handleError())
    }

    fun trainStationAround(position: Position): Single<List<TrainStation>> {
        return createSingleFromCallable(Callable { trainService.readNearbyStation(position) })
            .onErrorReturn(handleError())
    }

    fun busStopsAround(position: Position): Single<List<BusStop>> {
        return createSingleFromCallable(Callable { busService.getBusStopsAround(position) })
            .onErrorReturn(handleError())
    }

    fun bikeStationAround(position: Position, bikeStations: List<BikeStation>): Single<List<BikeStation>> {
        return createSingleFromCallable(Callable { positionUtil.readNearbyStation(bikeStations, position) })
            .onErrorReturn(handleError())
    }

    fun trainEtas(runNumber: String, loadAll: Boolean): Single<List<TrainEta>> {
        return createSingleFromCallable(Callable { trainService.loadTrainEta(runNumber, loadAll) })
            .onErrorReturn(handleError())
    }

    fun alerts(): Single<List<RoutesAlertsDTO>> {
        return createSingleFromCallable(Callable { alertService.getAlerts() })
    }

    fun routeAlertForId(id: String): Single<List<RouteAlertsDTO>> {
        return createSingleFromCallable(Callable { alertService.getRouteAlert(id) })
    }

    private fun favoritesTrainArrivalDTO(): Single<TrainArrivalDTO> {
        return createSingleFromCallable(Callable { TrainArrivalDTO(trainService.loadFavoritesTrain(), false) })
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load favorites trains")
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    private fun favoritesBusArrivalDTO(): Single<BusArrivalDTO> {
        return createSingleFromCallable(Callable { BusArrivalDTO(busService.loadFavoritesBuses(), false) })
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load bus arrivals")
                BusArrivalDTO(listOf(), true)
            }
    }

    // Combined
    fun baseFavorites(): Single<FavoritesDTO> {
        // Train online favorites
        val favoritesTrainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())

        // Bus online favorites
        val favoritesBusArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())

        return Single.zip(
            favoritesTrainArrivals,
            favoritesBusArrivals,
            BiFunction { trainArrivalsDTO: TrainArrivalDTO, busArrivalsDTO: BusArrivalDTO ->
                FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf())
            })
    }

    fun favorites(): Single<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = favoritesTrainArrivalDTO()
        // Bus online favorites
        val busArrivalsObservable = favoritesBusArrivalDTO()
        // Bikes online all stations
        val bikeStationsObservable = bikeAllStations().onErrorReturn(handleError())
        return Single.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun busRoutes(): Single<List<BusRoute>> {
        return createSingleFromCallable(Callable { busService.loadBusRoutes() })
    }

    fun busRoutesAndBikeStation(): Single<FirstLoadDTO> {
        val busRoutesSingle = busRoutes()
            .onErrorReturn(handleError())

        val bikeStationsSingle = createSingleFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())

        return Single.zip(
            busRoutesSingle,
            bikeStationsSingle,
            BiFunction { busRoutes, bikeStations ->
                FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations)
            }
        )
    }

    private fun <T> createSingleFromCallable(supplier: Callable<T>): Single<T> {
        return Single.fromCallable(supplier)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> handleError(): (Throwable) -> List<T> = { throwable ->
        Timber.e(throwable)
        ArrayList()
    }
}
