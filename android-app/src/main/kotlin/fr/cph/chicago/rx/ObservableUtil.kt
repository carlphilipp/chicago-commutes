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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import java.util.concurrent.Callable

object ObservableUtil {

    private val TAG = ObservableUtil::class.java.simpleName

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val alertService = AlertService
    private val positionUtil = MapUtil

    fun createFavoritesTrainArrivalsObservable(): Observable<TrainArrivalDTO> {
        return createObservableFromCallable(Callable { TrainArrivalDTO(trainService.loadFavoritesTrain(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    fun createTrainArrivalsObservable(trainStation: TrainStation): Observable<TrainArrival> {
        return createObservableFromCallable(Callable { trainService.loadStationTrainArrival(trainStation.id) })
    }

    fun createFavoritesBusArrivalsObservable(): Observable<BusArrivalDTO> {
        return createObservableFromCallable(Callable { BusArrivalDTO(busService.loadFavoritesBuses(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BusArrivalDTO()
            }
    }

    fun createBusArrivalsObservable(busStop: BusStop): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadAroundBusArrivals(busStop) })
            .onErrorReturn(handleError())
    }

    fun createAllBikeStationsObservable(): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())
    }

    fun createBikeStationsObservable(divvyStation: BikeStation): Observable<BikeStation> {
        return createObservableFromCallable(Callable { bikeService.findBikeStation(divvyStation.id) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun createAllDataObservable(): Observable<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = createFavoritesTrainArrivalsObservable()
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsObservable()
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsObservable()
        return Observable.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, divvyStations: List<BikeStation>
                ->
                App.instance.lastUpdate = Calendar.getInstance().time
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, divvyStations.isEmpty(), divvyStations)
            })
    }

    fun createBusStopBoundObservable(stopId: String, bound: String): Observable<List<BusStop>> {
        return createObservableFromCallable(Callable { busService.loadOneBusStop(stopId, bound) })
    }

    fun createBusDirectionsObservable(busRouteId: String): Observable<BusDirections> {
        return createObservableFromCallable(Callable { busService.loadBusDirections(busRouteId) })
    }

    fun createOnFirstLoadObservable(): Observable<FirstLoadDTO> {
        val busRoutesObs = createObservableFromCallable(Callable { busService.loadBusRoutes() })
            .onErrorReturn(handleError())

        val bikeStationsObs = createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn(handleError())

        return Observable.zip(busRoutesObs, bikeStationsObs, BiFunction { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) })
    }

    fun createFollowBusObservable(busId: String): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadFollowBus(busId) })
            .onErrorReturn(handleError())
    }

    fun createBusPatternObservable(busRouteId: String, bound: String): Observable<BusPattern> {
        return createObservableFromCallable(Callable { busService.loadBusPattern(busRouteId, bound) })
    }

    fun createBusListObservable(busRouteId: String): Observable<List<Bus>> {
        return createObservableFromCallable(Callable { busService.loadBus(busRouteId) })
            .onErrorReturn(handleError())
    }

    fun createTrainLocationObservable(line: String): Observable<List<Train>> {
        return createObservableFromCallable(Callable { trainService.getTrainLocation(line) })
            .onErrorReturn(handleError())
    }

    fun createTrainPatternObservable(line: String): Observable<List<TrainStationPattern>> {
        return createObservableFromCallable(Callable { trainService.readPatterns(TrainLine.fromXmlString(line)) })
            .onErrorReturn(handleError())
    }

    fun createTrainStationAroundObservable(position: Position): Observable<List<TrainStation>> {
        return createObservableFromCallable(Callable { trainService.readNearbyStation(position) })
            .onErrorReturn(handleError())
    }

    fun createBusStopsAroundObservable(position: Position): Observable<List<BusStop>> {
        return createObservableFromCallable(Callable { busService.getBusStopsAround(position) })
            .onErrorReturn(handleError())
    }

    fun createBikeStationAroundObservable(position: Position, divvyStations: List<BikeStation>): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { positionUtil.readNearbyStation(divvyStations, position) })
            .onErrorReturn(handleError())
    }

    fun createLoadTrainEtaObservable(runNumber: String, loadAll: Boolean): Observable<List<TrainEta>> {
        return createObservableFromCallable(Callable { trainService.loadTrainEta(runNumber, loadAll) })
            .onErrorReturn(handleError())
    }

    fun createAlertRoutesObservable(): Observable<List<RoutesAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getAlerts() })
            .onErrorReturn(handleError())
    }

    fun createAlertRouteObservable(id: String): Observable<List<RouteAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getRouteAlert(id) })
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
