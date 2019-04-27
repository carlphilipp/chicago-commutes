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

import android.util.Log
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
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.*

object RxUtil {

    private val TAG = RxUtil::class.java.simpleName

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val alertService = AlertService
    private val positionUtil = MapUtil

    // Local
    private fun createLocalTrainDataSingle(): Single<SparseArray<TrainStation>> {
        return Single.fromCallable { trainService.loadLocalTrainData() }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not create local train data", throwable)
                SparseArray()
            }
    }

    private fun createLocalBusDataSingle(): Single<Any> {
        return Single.fromCallable { busService.loadLocalBusData() }
            .subscribeOn(Schedulers.computation())
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not create local bus data", throwable)
                Any()
            }
    }

    // Http call
    fun createBusArrivalObs(requestRt: String, busRouteId: String, requestStopId: String, busStopId: Int, bound: String, boundTitle: String): Single<BusArrivalStopDTO> {
        return createSingleFromCallable(Callable { busService.loadBusArrivals(requestRt, busRouteId, requestStopId, busStopId, bound, boundTitle) })
    }

    fun createTrainArrivalsSingle(trainStation: TrainStation): Single<TrainArrival> {
        return createSingleFromCallable(Callable { trainService.loadStationTrainArrival(trainStation.id) })
    }

    fun createBusArrivalsObs(busStop: BusStop): Single<List<BusArrival>> {
        return createSingleFromCallable(Callable { busService.loadAroundBusArrivals(busStop) })
            .onErrorReturn(handleError())
    }

    fun createAllBikeStationsSingle(): Single<List<BikeStation>> {
        return createSingleFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())
    }

    fun createBikeStationsSingle(bikeStation: BikeStation): Single<BikeStation> {
        return createSingleFromCallable(Callable { bikeService.findBikeStation(bikeStation.id) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load bike stations", throwable)
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun createBusStopsForRouteBoundSingle(route: String, bound: String): Single<List<BusStop>> {
        return createSingleFromCallable(Callable { busService.loadAllBusStopsForRouteBound(route, bound) })
    }

    fun createBusDirectionsSingle(busRouteId: String): Single<BusDirections> {
        return createSingleFromCallable(Callable { busService.loadBusDirections(busRouteId) })
    }

    fun createFollowBusSingle(busId: String): Single<List<BusArrival>> {
        return createSingleFromCallable(Callable { busService.loadFollowBus(busId) })
            .onErrorReturn(handleError())
    }

    fun createBusPatternSingle(busRouteId: String, bound: String): Single<BusPattern> {
        return createSingleFromCallable(Callable { busService.loadBusPattern(busRouteId, bound) })
    }

    fun createBusListSingle(busRouteId: String): Single<List<Bus>> {
        return createSingleFromCallable(Callable { busService.loadBus(busRouteId) })
            .onErrorReturn(handleError())
    }

    fun createTrainLocationSingle(line: String): Single<List<Train>> {
        return createSingleFromCallable(Callable { trainService.getTrainLocation(line) })
            .onErrorReturn(handleError())
    }

    fun createTrainPatternSingle(line: String): Single<List<TrainStationPattern>> {
        return createSingleFromCallable(Callable { trainService.readPatterns(TrainLine.fromXmlString(line)) })
            .onErrorReturn(handleError())
    }

    fun createTrainStationAroundSingle(position: Position): Single<List<TrainStation>> {
        return createSingleFromCallable(Callable { trainService.readNearbyStation(position) })
            .onErrorReturn(handleError())
    }

    fun createBusStopsAroundSingle(position: Position): Single<List<BusStop>> {
        return createSingleFromCallable(Callable { busService.getBusStopsAround(position) })
            .onErrorReturn(handleError())
    }

    fun createBikeStationAroundSingle(position: Position, bikeStations: List<BikeStation>): Single<List<BikeStation>> {
        return createSingleFromCallable(Callable { positionUtil.readNearbyStation(bikeStations, position) })
            .onErrorReturn(handleError())
    }

    fun createLoadTrainEtaSingle(runNumber: String, loadAll: Boolean): Single<List<TrainEta>> {
        return createSingleFromCallable(Callable { trainService.loadTrainEta(runNumber, loadAll) })
            .onErrorReturn(handleError())
    }

    fun createAlertRoutesSingle(): Single<List<RoutesAlertsDTO>> {
        return createSingleFromCallable(Callable { alertService.getAlerts() })
            .onErrorReturn(handleError())
    }

    fun createAlertRouteSingle(id: String): Single<List<RouteAlertsDTO>> {
        return createSingleFromCallable(Callable { alertService.getRouteAlert(id) })
    }

    private fun createFavoritesTrainArrivalsSingle(): Single<TrainArrivalDTO> {
        return createSingleFromCallable(Callable { TrainArrivalDTO(trainService.loadFavoritesTrain(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load favorites trains", throwable)
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    private fun createFavoritesBusArrivalsSingle(): Single<BusArrivalDTO> {
        return createSingleFromCallable(Callable { BusArrivalDTO(busService.loadFavoritesBuses(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load bus arrivals", throwable)
                BusArrivalDTO()
            }
    }

    // Combined
    fun createLocalAndFavoritesDataSingle(): Single<FavoritesDTO> {
        // Train local data
        val trainLocalData = createLocalTrainDataSingle()

        // Bus local data
        val busLocalData = createLocalBusDataSingle()

        // Train online favorites
        val trainOnlineFavorites = createFavoritesTrainArrivalsSingle()

        // Bus online favorites
        val busOnlineFavorites = createFavoritesBusArrivalsSingle()

        // Run local first and then online: Ensure that local data is loaded first
        return Single.zip(trainLocalData, busLocalData, trainOnlineFavorites, busOnlineFavorites, Function4 { _: Any, _: Any, trainArrivalsDTO: TrainArrivalDTO, busArrivalsDTO: BusArrivalDTO ->
            FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf())
        })
    }

    fun createAllDataSingle(): Single<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = createFavoritesTrainArrivalsSingle()
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsSingle()
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsSingle()
        return Single.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun createBusRoutesSingle(): Single<List<BusRoute>> {
        return createSingleFromCallable(Callable { busService.loadBusRoutes() })
    }

    fun createOnFirstLoadObs(): Single<FirstLoadDTO> {
        val busRoutesSingle = createBusRoutesSingle()
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
        Log.e(TAG, throwable.message, throwable)
        ArrayList()
    }
}
