package fr.cph.chicago.rx

import android.app.Application
import android.content.Context
import android.util.Log
import android.util.SparseArray
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.*
import fr.cph.chicago.entity.dto.BusArrivalDTO
import fr.cph.chicago.entity.dto.FavoritesDTO
import fr.cph.chicago.entity.dto.FirstLoadDTO
import fr.cph.chicago.entity.dto.TrainArrivalDTO
import fr.cph.chicago.service.impl.BikeServiceImpl
import fr.cph.chicago.service.impl.BusServiceImpl
import fr.cph.chicago.service.impl.TrainServiceImpl
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.*

object ObservableUtil {

    private val TAG = ObservableUtil::class.java.simpleName

    private val TRAIN_SERVICE = TrainServiceImpl
    private val BUS_SERVICE = BusServiceImpl
    private val BIKE_SERVICE = BikeServiceImpl

    fun createFavoritesTrainArrivalsObservable(context: Context): Observable<TrainArrivalDTO> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<TrainArrivalDTO> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(TrainArrivalDTO(TRAIN_SERVICE.loadFavoritesTrain(context), false))
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

    fun createTrainArrivalsObservable(context: Context, station: Station): Observable<TrainArrival> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<TrainArrival> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(TRAIN_SERVICE.loadStationTrainArrival(context, station.id))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFavoritesBusArrivalsObservable(context: Context): Observable<BusArrivalDTO> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusArrivalDTO> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BusArrivalDTO(BUS_SERVICE.loadFavoritesBuses(context), false))
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                BusArrivalDTO(emptyList(), true)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusArrivalsObservable(context: Context, busStop: BusStop): Observable<List<BusArrival>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusArrival>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadAroundBusArrivals(context, busStop))
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
                observableOnSubscribe.onNext(BIKE_SERVICE.loadAllBikes())
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
                observableOnSubscribe.onNext(BIKE_SERVICE.loadBikes(bikeStation.id))
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
        val trainArrivalsObservable = createFavoritesTrainArrivalsObservable(application)
        // Bus online favorites
        val busArrivalsObservable = createFavoritesBusArrivalsObservable(application)
        // Bikes online all stations
        val bikeStationsObservable = createAllBikeStationsObservable()
        return Observable.zip(busArrivalsObservable, trainArrivalsObservable, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                (application as App).lastUpdate = Calendar.getInstance().time
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun createBusStopBoundObservable(context: Context, stopId: String, bound: String): Observable<List<BusStop>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusStop>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadOneBusStop(context, stopId, bound))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusDirectionsObservable(context: Context, busRouteId: String): Observable<BusDirections> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusDirections> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadBusDirections(context, busRouteId))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createOnFirstLoadObservable(): Observable<FirstLoadDTO> {
        val busRoutesObs = Observable.create { observableOnSubscribe: ObservableEmitter<List<BusRoute>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadBusRoutes())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                emptyList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        val bikeStationsObs = Observable.create { observableOnSubscribe: ObservableEmitter<List<BikeStation>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BIKE_SERVICE.loadAllBikes())
                observableOnSubscribe.onComplete()
            }
        }
            .onErrorReturn { throwable ->
                Log.e(TAG, throwable.message, throwable)
                ArrayList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        return Observable.zip(busRoutesObs, bikeStationsObs, BiFunction { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) })
    }

    fun createFollowBusObservable(context: Context, busId: String): Observable<List<BusArrival>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<BusArrival>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadFollowBus(context, busId))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusPatternObservable(context: Context, busRouteId: String, bound: String): Observable<BusPattern> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<BusPattern> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadBusPattern(context, busRouteId, bound))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createBusListObservable(context: Context, busId: Int, busRouteId: String): Observable<List<Bus>> {
        return Observable.create { observableOnSubscribe: ObservableEmitter<List<Bus>> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(BUS_SERVICE.loadBus(context, busId, busRouteId))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
