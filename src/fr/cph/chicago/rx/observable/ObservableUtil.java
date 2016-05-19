package fr.cph.chicago.rx.observable;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BikeServiceImpl;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import fr.cph.chicago.web.FavoritesResult;
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

    public static Observable<List<BikeStation>> createAllBikeStations() {
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
        final Observable<List<BikeStation>> bikeStationsObservable = ObservableUtil.createAllBikeStations();
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

    public static Observable<BusDirections> createBusDirections(final String busRouteId) {
        return Observable.create(
            (Subscriber<? super BusDirections> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusDirections(busRouteId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
