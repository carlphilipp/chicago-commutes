package fr.cph.chicago.rx.observable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.core.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.BusArrivalDTO;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.entity.dto.FirstLoadDTO;
import fr.cph.chicago.entity.dto.TrainArrivalDTO;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BikeServiceImpl;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public enum ObservableUtil {
    ;

    private static final String TAG = ObservableUtil.class.getSimpleName();

    private static final TrainService TRAIN_SERVICE = TrainServiceImpl.INSTANCE;
    private static final BusService BUS_SERVICE = BusServiceImpl.INSTANCE;
    private static final BikeService BIKE_SERVICE = BikeServiceImpl.INSTANCE;

    public static Observable<TrainArrivalDTO> createFavoritesTrainArrivalsObservable(@NonNull final Context context) {
        return Observable.create(
            (ObservableEmitter<TrainArrivalDTO> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(new TrainArrivalDTO(TRAIN_SERVICE.loadFavoritesTrain(context), false));
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new TrainArrivalDTO(new SparseArray<>(), true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Optional<TrainArrival>> createTrainArrivalsObservable(@NonNull final Context context, final Station station) {
        return Observable.create(
            (ObservableEmitter<Optional<TrainArrival>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(TRAIN_SERVICE.loadStationTrainArrival(context, station.getId()));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BusArrivalDTO> createFavoritesBusArrivalsObservable(@NonNull final Context context) {
        return Observable.create(
            (ObservableEmitter<BusArrivalDTO> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(new BusArrivalDTO(BUS_SERVICE.loadFavoritesBuses(context), false));
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new BusArrivalDTO(Collections.emptyList(), true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BusArrival>> createBusArrivalsObservable(@NonNull final Context context, @NonNull final BusStop busStop) {
        return Observable.create(
            (ObservableEmitter<List<BusArrival>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadAroundBusArrivals(context, busStop));
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new ArrayList<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BikeStation>> createAllBikeStationsObservable() {
        return Observable.create(
            (ObservableEmitter<List<BikeStation>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BIKE_SERVICE.loadAllBikes());
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new ArrayList<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BikeStation> createBikeStationsObservable(@NonNull final BikeStation bikeStation) {
        return Observable.create(
            (ObservableEmitter<BikeStation> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BIKE_SERVICE.loadBikes(bikeStation.getId()));
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return BikeStation.Companion.buildDefaultBikeStationWithName("error");
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FavoritesDTO> createAllDataObservable(@NonNull final Context context) {
        // Train online favorites
        final Observable<TrainArrivalDTO> trainArrivalsObservable = ObservableUtil.createFavoritesTrainArrivalsObservable(context);
        // Bus online favorites
        final Observable<BusArrivalDTO> busArrivalsObservable = ObservableUtil.createFavoritesBusArrivalsObservable(context);
        // Bikes online all stations
        final Observable<List<BikeStation>> bikeStationsObservable = ObservableUtil.createAllBikeStationsObservable();
        return Observable.zip(trainArrivalsObservable, busArrivalsObservable, bikeStationsObservable,
            (trainArrivalsDTO, busArrivalsDTO, bikeStations) -> {
                App.setLastUpdate(Calendar.getInstance().getTime());
                return new FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, bikeStations.isEmpty(), bikeStations);
            });
    }

    public static Observable<List<BusStop>> createBusStopBoundObservable(@NonNull final Context context, @NonNull final String stopId, @NonNull final String bound) {
        return Observable.create(
            (ObservableEmitter<List<BusStop>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadOneBusStop(context, stopId, bound));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BusDirections> createBusDirectionsObservable(@NonNull final Context context, @NonNull final String busRouteId) {
        return Observable.create(
            (ObservableEmitter<BusDirections> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadBusDirections(context, busRouteId));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FirstLoadDTO> createOnFirstLoadObservable() {
        final Observable<List<BusRoute>> busRoutesObs = Observable.create(
            (ObservableEmitter<List<BusRoute>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadBusRoutes());
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return Collections.emptyList();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        final Observable<List<BikeStation>> bikeStationsObs = Observable.create(
            (ObservableEmitter<List<BikeStation>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BIKE_SERVICE.loadAllBikes());
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new ArrayList<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        return Observable.zip(busRoutesObs, bikeStationsObs, (busRoutes, bikeStations) -> new FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations));
    }

    public static Observable<List<BusArrival>> createFollowBusObservable(@NonNull final Context context, @NonNull final String busId) {
        return Observable.create(
            (ObservableEmitter<List<BusArrival>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadFollowBus(context, busId));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Optional<BusPattern>> createBusPatternObservable(@NonNull final Context context, @NonNull final String busRouteId, @NonNull final String bound) {
        return Observable.create(
            (ObservableEmitter<Optional<BusPattern>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadBusPattern(context, busRouteId, bound));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<Bus>> createBusListObservable(@NonNull final Context context, final int busId, @NonNull final String busRouteId) {
        return Observable.create(
            (ObservableEmitter<List<Bus>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadBus(context, busId, busRouteId));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
