package fr.cph.chicago.rx

import android.app.Application
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.common.api.GoogleApiClient
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.*
import fr.cph.chicago.entity.dto.*
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.service.AlertService
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.GPSUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.*

object ObservableUtil {

    private val TAG = ObservableUtil::class.java.simpleName

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val alertService = AlertService

    fun createFavoritesTrainArrivalsObservable(): Observable<TrainArrivalDTO> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<TrainArrivalDTO> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(TrainArrivalDTO(trainService.loadFavoritesTrain(), false))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                TrainArrivalDTO(SparseArray(), true)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createTrainArrivalsObservable(station: Station): Observable<TrainArrival> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<TrainArrival> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.loadStationTrainArrival(station.id))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFavoritesBusArrivalsObservable(): Observable<BusArrivalDTO> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusArrivalDTO> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BusArrivalDTO(busService.loadFavoritesBuses(), false))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BusArrivalDTO(ArrayList(), true)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusArrivalsObservable(busStop: BusStop): Observable<List<BusArrival>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusArrival>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadAroundBusArrivals(busStop))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createAllBikeStationsObservable(): Observable<List<BikeStation>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BikeStation>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(bikeService.loadAllBikeStations())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBikeStationsObservable(bikeStation: BikeStation): Observable<BikeStation> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BikeStation> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(bikeService.findBikeStation(bikeStation.id))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BikeStation.buildDefaultBikeStationWithName("error")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createAllDataObservable(application: Application): Observable<FavoritesDTO> {
        // Train online favorites
        val trainArrivalsObservable = createFavoritesTrainArrivalsObservable()
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsObservable()
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsObservable()
        return Observable.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                (application as App).lastUpdate = Calendar.getInstance().time
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun createBusStopBoundObservable(stopId: String, bound: String): Observable<List<BusStop>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusStop>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadOneBusStop(stopId, bound))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusDirectionsObservable(busRouteId: String): Observable<BusDirections> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusDirections> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadBusDirections(busRouteId))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createOnFirstLoadObservable(): Observable<FirstLoadDTO> {
        val busRoutesObs = Observable.create { observableOnSubscribe: ObservableEmitter<List<BusRoute>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadBusRoutes())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        val bikeStationsObs = Observable.create { observableOnSubscribe: ObservableEmitter<List<BikeStation>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(bikeService.loadAllBikeStations())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        return Observable.zip(busRoutesObs, bikeStationsObs, BiFunction { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) })
    }

    fun createFollowBusObservable(busId: String): Observable<List<BusArrival>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusArrival>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadFollowBus(busId))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusPatternObservable(busRouteId: String, bound: String): Observable<BusPattern> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusPattern> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadBusPattern(busRouteId, bound))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusListObservable(busId: Int, busRouteId: String): Observable<List<Bus>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Bus>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadBus(busId, busRouteId))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createTrainLocationObservable(line: String): Observable<List<Train>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Train>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.getTrainLocation(line))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createTrainPatternObservable(line: String): Observable<List<Position>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Position>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.readPattern(TrainLine.fromXmlString(line)))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createPositionObservable(googleApiClient: GoogleApiClient): Observable<Position> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<Position> ->
            if (!observableOnSubscribe.isDisposed) {
                if (!googleApiClient.isConnected) {
                    googleApiClient.blockingConnect()
                }
                observableOnSubscribe.onNext(GPSUtil(googleApiClient).location)
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                Position()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createTrainStationAroundObservable(position: Position): Observable<List<Station>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Station>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.readNearbyStation(position))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusStopsAroundObservable(position: Position): Observable<List<BusStop>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusStop>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.getBusStopsAround(position))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBikeStationAroundObservable(position: Position, bikeStations: List<BikeStation>): Observable<List<BikeStation>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BikeStation>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BikeStation.readNearbyStation(bikeStations, position))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                // Do not change that to listOf().
                // It needs to be ArrayList for being parcelable
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createLoadTrainEtaObservable(runNumber: String, loadAll: Boolean): Observable<List<Eta>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Eta>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.loadTrainEta(runNumber, loadAll))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { ArrayList() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createAlertRoutesObservable(): Observable<List<RoutesAlertsDTO>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<RoutesAlertsDTO>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(alertService.getAlerts())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createAlertRouteObservable(id: String): Observable<List<RouteAlertsDTO>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<RouteAlertsDTO>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(alertService.getRouteAlert(id))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
