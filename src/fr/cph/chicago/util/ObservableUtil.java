package fr.cph.chicago.util;

import android.util.Log;
import android.util.SparseArray;
import fr.cph.chicago.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.service.DataService;
import fr.cph.chicago.service.DataServiceImpl;
import fr.cph.chicago.web.FavoritesResult;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ObservableUtil {

    private static final String TAG = ObservableUtil.class.getSimpleName();

    private static final DataService SERVICE = new DataServiceImpl();

    private ObservableUtil() {
    }

    public static Observable<SparseArray<TrainArrival>> createTrainArrivals() {
        return Observable.create(
            (Subscriber<? super SparseArray<TrainArrival>> subscriber) -> {
                subscriber.onNext(SERVICE.loadFavoritesTrain());
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
                subscriber.onNext(SERVICE.loadFavoritesBuses());
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
                subscriber.onNext(SERVICE.loadAllBikes());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FavoritesResult> createFavoritesObservables() {
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
}
