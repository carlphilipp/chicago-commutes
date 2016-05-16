package fr.cph.chicago.util;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.service.DataService;
import fr.cph.chicago.service.DataServiceImpl;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
                return new SparseArray<>();
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
//            .onErrorResumeNext(Observable.create(
//                (Subscriber<? super List<BusArrival>> subscriber) -> {
//                    subscriber.onNext(null);
//                    subscriber.onCompleted();
//                }))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BikeStation>> createAllBikeStations(){
        return Observable.create(
            (Subscriber<? super List<BikeStation>> subscriber) -> {
                subscriber.onNext(SERVICE.loadAllBikes());
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new ArrayList<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
