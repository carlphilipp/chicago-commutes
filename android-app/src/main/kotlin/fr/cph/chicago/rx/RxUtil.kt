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
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusPattern
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
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import java.util.concurrent.Callable

object RxUtil {

    private val TAG = RxUtil::class.java.simpleName

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val alertService = AlertService
    private val positionUtil = MapUtil

    // Local
    fun createLocalTrainDataObs(): Single<SparseArray<TrainStation>> {
        return createSingleFromCallable(Callable { trainService.loadLocalTrainData() })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not create local train data", throwable)
                SparseArray()
            }
    }

    fun createLocalBusDataObs(): Single<Any> {
        return createSingleFromCallable(Callable { busService.loadLocalBusData() })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not create local bus data", throwable)
                Any()
            }
    }

    // Http call
    fun createBusArrivalObs(requestRt: String, busRouteId: String, requestStopId: String, busStopId: Int, bound: String, boundTitle: String): Observable<BusArrivalStopDTO> {
        return createObservableFromCallable(Callable { busService.loadBusArrivals(requestRt, busRouteId, requestStopId, busStopId, bound, boundTitle) })
    }

    fun createFavoritesTrainArrivalsObs(): Observable<TrainArrivalDTO> {
        return createObservableFromCallable(Callable { TrainArrivalDTO(trainService.loadFavoritesTrain(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load favorites trains", throwable)
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    fun createTrainArrivalsObs(trainStation: TrainStation): Observable<TrainArrival> {
        return createObservableFromCallable(Callable { trainService.loadStationTrainArrival(trainStation.id) })
    }

    fun createFavoritesBusArrivalsObs(): Observable<BusArrivalDTO> {
        return createObservableFromCallable(Callable { BusArrivalDTO(busService.loadFavoritesBuses(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load bus arrivals", throwable)
                BusArrivalDTO()
            }
    }

    fun createBusArrivalsObs(busStop: BusStop): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadAroundBusArrivals(busStop) })
            .onErrorReturn(handleError())
    }

    fun createAllBikeStationsObs(): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())
    }

    fun createBikeStationsObs(bikeStation: BikeStation): Observable<BikeStation> {
        return createObservableFromCallable(Callable { bikeService.findBikeStation(bikeStation.id) })
            .onErrorReturn { throwable ->
                Log.e(TAG, "Could not load bike stations", throwable)
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun createAllDataObs(): Observable<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = createFavoritesTrainArrivalsObs()
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsObs()
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsObs()
        return Observable.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                App.instance.lastUpdate = Calendar.getInstance().time
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun createBusStopsForRouteBoundObs(route: String, bound: String): Observable<List<BusStop>> {
        return createObservableFromCallable(Callable { busService.loadAllBusStopsForRouteBound(route, bound) })
    }

    fun createBusDirectionsObs(busRouteId: String): Observable<BusDirections> {
        return createObservableFromCallable(Callable { busService.loadBusDirections(busRouteId) })
    }

    fun createOnFirstLoadObs(): Observable<FirstLoadDTO> {
        val busRoutesObs = createObservableFromCallable(Callable { busService.loadBusRoutes() })
            .onErrorReturn(handleError())

        val bikeStationsObs = createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())

        return Observable.zip(busRoutesObs, bikeStationsObs, BiFunction { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) })
    }

    fun createFollowBusObs(busId: String): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadFollowBus(busId) })
            .onErrorReturn(handleError())
    }

    fun createBusPatternObs(busRouteId: String, bound: String): Observable<BusPattern> {
        return createObservableFromCallable(Callable { busService.loadBusPattern(busRouteId, bound) })
    }

    fun createBusListObs(busRouteId: String): Observable<List<Bus>> {
        return createObservableFromCallable(Callable { busService.loadBus(busRouteId) })
            .onErrorReturn(handleError())
    }

    fun createTrainLocationObs(line: String): Observable<List<Train>> {
        return createObservableFromCallable(Callable { trainService.getTrainLocation(line) })
            .onErrorReturn(handleError())
    }

    fun createTrainPatternObs(line: String): Observable<List<TrainStationPattern>> {
        return createObservableFromCallable(Callable { trainService.readPatterns(TrainLine.fromXmlString(line)) })
            .onErrorReturn(handleError())
    }

    fun createTrainStationAroundObs(position: Position): Observable<List<TrainStation>> {
        return createObservableFromCallable(Callable { trainService.readNearbyStation(position) })
            .onErrorReturn(handleError())
    }

    fun createBusStopsAroundObs(position: Position): Observable<List<BusStop>> {
        return createObservableFromCallable(Callable { busService.getBusStopsAround(position) })
            .onErrorReturn(handleError())
    }

    fun createBikeStationAroundObs(position: Position, bikeStations: List<BikeStation>): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { positionUtil.readNearbyStation(bikeStations, position) })
            .onErrorReturn(handleError())
    }

    fun createLoadTrainEtaObs(runNumber: String, loadAll: Boolean): Observable<List<TrainEta>> {
        return createObservableFromCallable(Callable { trainService.loadTrainEta(runNumber, loadAll) })
            .onErrorReturn(handleError())
    }

    fun createAlertRoutesObs(): Observable<List<RoutesAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getAlerts() })
            .onErrorReturn(handleError())
    }

    fun createAlertRouteObs(id: String): Observable<List<RouteAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getRouteAlert(id) })
    }

    private fun <T> createSingleFromCallable(supplier: Callable<T>): Single<T> {
        return Single.fromCallable(supplier)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> createObservableFromCallable(supplier: Callable<T>): Observable<T> {
        return Observable.fromCallable(supplier)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> handleError(): (Throwable) -> List<T> = { throwable ->
        Log.e(TAG, throwable.message, throwable)
        ArrayList()
    }
}
