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

import android.app.Application
import android.util.Log
import android.util.SparseArray
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.*
import fr.cph.chicago.core.model.dto.*
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.entity.TrainEta
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
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

    fun createFavoritesTrainArrivalsObservable(): Observable<TrainArrivalDTO> {
        return createObservableFromCallable(Callable { TrainArrivalDTO(trainService.loadFavoritesTrain(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    fun createTrainArrivalsObservable(station: Station): Observable<TrainArrival> {
        return createObservableFromCallable(Callable { trainService.loadStationTrainArrival(station.id) })
    }

    fun createFavoritesBusArrivalsObservable(): Observable<BusArrivalDTO> {
        return createObservableFromCallable(Callable { BusArrivalDTO(busService.loadFavoritesBuses(), false) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BusArrivalDTO(mutableListOf(), true)
            }
    }

    fun createBusArrivalsObservable(busStop: BusStop): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadAroundBusArrivals(busStop) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                mutableListOf()
            }
    }

    fun createAllBikeStationsObservable(): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                mutableListOf()
            }
    }

    fun createBikeStationsObservable(divvyStation: BikeStation): Observable<BikeStation> {
        return createObservableFromCallable(Callable { bikeService.findBikeStation(divvyStation.id) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BikeStation.buildDefaultBikeStationWithName("error")
            }
    }

    fun createAllDataObservable(application: Application): Observable<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = createFavoritesTrainArrivalsObservable()
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsObservable()
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsObservable()
        return Observable.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, divvyStations: List<BikeStation>
                ->
                (application as App).lastUpdate = Calendar.getInstance().time
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
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                mutableListOf()
            }

        val bikeStationsObs = createObservableFromCallable(Callable { bikeService.loadAllBikeStations() })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }

        return Observable.zip(busRoutesObs, bikeStationsObs, BiFunction { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) })
    }

    fun createFollowBusObservable(busId: String): Observable<List<BusArrival>> {
        return createObservableFromCallable(Callable { busService.loadFollowBus(busId) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createBusPatternObservable(busRouteId: String, bound: String): Observable<BusPattern> {
        return createObservableFromCallable(Callable { busService.loadBusPattern(busRouteId, bound) })
    }

    fun createBusListObservable(busId: Int, busRouteId: String): Observable<List<Bus>> {
        return createObservableFromCallable(Callable { busService.loadBus(busId, busRouteId) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createTrainLocationObservable(line: String): Observable<List<Train>> {
        return createObservableFromCallable(Callable { trainService.getTrainLocation(line) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createTrainPatternObservable(line: String): Observable<List<Position>> {
        return createObservableFromCallable(Callable { trainService.readPattern(TrainLine.fromXmlString(line)) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createTrainStationAroundObservable(position: Position): Observable<List<Station>> {
        return createObservableFromCallable(Callable { trainService.readNearbyStation(position) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createBusStopsAroundObservable(position: Position): Observable<List<BusStop>> {
        return createObservableFromCallable(Callable { busService.getBusStopsAround(position) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createBikeStationAroundObservable(position: Position, divvyStations: List<BikeStation>): Observable<List<BikeStation>> {
        return createObservableFromCallable(Callable { BikeStation.readNearbyStation(divvyStations, position) })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                mutableListOf()
            }
    }

    fun createLoadTrainEtaObservable(runNumber: String, loadAll: Boolean): Observable<List<TrainEta>> {
        return createObservableFromCallable(Callable { trainService.loadTrainEta(runNumber, loadAll) })
            .onErrorReturn { mutableListOf() }
    }

    fun createAlertRoutesObservable(): Observable<List<RoutesAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getAlerts() })
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                mutableListOf()
            }
    }

    fun createAlertRouteObservable(id: String): Observable<List<RouteAlertsDTO>> {
        return createObservableFromCallable(Callable { alertService.getRouteAlert(id) })
    }

    private fun <T> createObservableFromCallable(supplier: Callable<T>): Observable<T> {
        return Observable.fromCallable(supplier)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
