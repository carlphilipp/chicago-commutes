package fr.cph.chicago.rx.observable;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BikeServiceImpl;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import fr.cph.chicago.web.FavoritesResult;
import fr.cph.chicago.web.FirstLoadResult;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ObservableUtil {

    private static final String TAG = ObservableUtil.class.getSimpleName();

    private static final TrainService TRAIN_SERVICE = new TrainServiceImpl();
    private static final BusService BUS_SERVICE = new BusServiceImpl();
    private static final BikeService BIKE_SERVICE = new BikeServiceImpl();

    private ObservableUtil() {
    }

    public static Observable<SparseArray<TrainArrival>> createTrainArrivals() {
        return Observable.create(
            (Subscriber<? super SparseArray<TrainArrival>> subscriber) -> {
                subscriber.onNext(TRAIN_SERVICE.loadFavoritesTrain());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BusArrival>> createBusArrivals() {
        return Observable.create(
            (Subscriber<? super List<BusArrival>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadFavoritesBuses());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BikeStation>> createAllBikeStationsObservable() {
        return Observable.create(
            (Subscriber<? super List<BikeStation>> subscriber) -> {
                subscriber.onNext(BIKE_SERVICE.loadAllBikes());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FavoritesResult> createAllDataObservable() {
        // Train online favorites
        final Observable<SparseArray<TrainArrival>> trainArrivalsObservable = ObservableUtil.createTrainArrivals();
        // Bus online favorites
        final Observable<List<BusArrival>> busArrivalsObservable = ObservableUtil.createBusArrivals();
        // Bikes online all stations
        final Observable<List<BikeStation>> bikeStationsObservable = ObservableUtil.createAllBikeStationsObservable();
        return Observable.zip(trainArrivalsObservable, busArrivalsObservable, bikeStationsObservable,
            (trainArrivals, busArrivals, bikeStations) -> {
                App.modifyLastUpdate(Calendar.getInstance().getTime());
                final FavoritesResult favoritesResult = new FavoritesResult();
                favoritesResult.setTrainArrivals(trainArrivals);
                favoritesResult.setBusArrivals(busArrivals);
                favoritesResult.setBikeStations(bikeStations);
                if (trainArrivals == null) {
                    favoritesResult.setTrainError(true);
                    favoritesResult.setTrainArrivals(new SparseArray<>());
                }
                if (busArrivals == null) {
                    favoritesResult.setBusError(true);
                    favoritesResult.setBusArrivals(new ArrayList<>());
                }
                if (bikeStations == null) {
                    favoritesResult.setBikeError(true);
                    favoritesResult.setBikeStations(new ArrayList<>());
                }
                return favoritesResult;
            });
    }

    public static Observable<List<BusStop>> createBusStopBoundObservable(@NonNull final String stopId, @NonNull final String bound) {
        return Observable.create(
            (Subscriber<? super List<BusStop>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadOneBusStop(stopId, bound));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BusDirections> createBusDirectionsObservable(@NonNull final String busRouteId) {
        return Observable.create(
            (Subscriber<? super BusDirections> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusDirections(busRouteId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FirstLoadResult> createOnFirstLoadObservable() {
        final Observable<List<BusRoute>> busRoutesObs = Observable.create(
            (Subscriber<? super List<BusRoute>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusRoutes());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        final Observable<List<BikeStation>> bikeStationsObs = Observable.create(
            (Subscriber<? super List<BikeStation>> subscriber) -> {
                subscriber.onNext(BIKE_SERVICE.loadAllBikes());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        return Observable.zip(busRoutesObs, bikeStationsObs, (busRoutes, bikeStations) -> {
            final FirstLoadResult result = new FirstLoadResult();
            if (busRoutes == null) {
                busRoutes = new ArrayList<>();
                result.setBusRoutesError(true);
            }
            if (bikeStations == null) {
                bikeStations = new ArrayList<>();
                result.setBikeStationsError(true);
            }
            result.setBusRoutes(busRoutes);
            result.setBikeStations(bikeStations);
            return result;
        });
    }

    public static Observable<List<BusArrival>> createFollowBusObservable(@NonNull final String busId) {
        return Observable.create(
            (Subscriber<? super List<BusArrival>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadFollowBus(busId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BusPattern> createBusPatternObservable(@NonNull final String busRouteId, @NonNull final String bound) {
        return Observable.create(
            (Subscriber<? super BusPattern> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusPattern(busRouteId, bound));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<Bus>> createBusList(final int busId, @NonNull final String busRouteId) {
        return Observable.create(
            (Subscriber<? super List<Bus>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBus(busId, busRouteId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
