package fr.cph.chicago.rx.observable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import com.annimon.stream.Optional;
import fr.cph.chicago.app.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.entity.dto.FirstLoadDTO;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BikeServiceImpl;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ObservableUtil {

    private static final String TAG = ObservableUtil.class.getSimpleName();

    private static final TrainService TRAIN_SERVICE = new TrainServiceImpl();
    private static final BusService BUS_SERVICE = new BusServiceImpl();
    private static final BikeService BIKE_SERVICE = new BikeServiceImpl();

    private ObservableUtil() {
    }

    public static Observable<SparseArray<TrainArrival>> createTrainArrivals(@NonNull final Context context) {
        return Observable.create(
            (Subscriber<? super SparseArray<TrainArrival>> subscriber) -> {
                subscriber.onNext(TRAIN_SERVICE.loadFavoritesTrain(context));
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BusArrival>> createBusArrivals(@NonNull final Context context) {
        return Observable.create(
            (Subscriber<? super List<BusArrival>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadFavoritesBuses(context));
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<BikeStation>> createAllBikeStationsObservable(@NonNull final Context context) {
        return Observable.create(
            (Subscriber<? super List<BikeStation>> subscriber) -> {
                subscriber.onNext(BIKE_SERVICE.loadAllBikes(context));
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FavoritesDTO> createAllDataObservable(@NonNull final Context context) {
        // Train online favorites
        final Observable<SparseArray<TrainArrival>> trainArrivalsObservable = ObservableUtil.createTrainArrivals(context);
        // Bus online favorites
        final Observable<List<BusArrival>> busArrivalsObservable = ObservableUtil.createBusArrivals(context);
        // Bikes online all stations
        final Observable<List<BikeStation>> bikeStationsObservable = ObservableUtil.createAllBikeStationsObservable(context);
        return Observable.zip(trainArrivalsObservable, busArrivalsObservable, bikeStationsObservable,
            (trainArrivals, busArrivals, bikeStations) -> {
                App.modifyLastUpdate(Calendar.getInstance().getTime());
                final FavoritesDTO favoritesDTO = new FavoritesDTO();
                favoritesDTO.setTrainArrivals(trainArrivals);
                favoritesDTO.setBusArrivals(busArrivals);
                favoritesDTO.setBikeStations(bikeStations);
                if (trainArrivals == null) {
                    favoritesDTO.setTrainError(true);
                    favoritesDTO.setTrainArrivals(new SparseArray<>());
                }
                if (busArrivals == null) {
                    favoritesDTO.setBusError(true);
                    favoritesDTO.setBusArrivals(new ArrayList<>());
                }
                if (bikeStations == null) {
                    favoritesDTO.setBikeError(true);
                    favoritesDTO.setBikeStations(new ArrayList<>());
                }
                return favoritesDTO;
            });
    }

    public static Observable<List<BusStop>> createBusStopBoundObservable(@NonNull final Context context, @NonNull final String stopId, @NonNull final String bound) {
        return Observable.create(
            (Subscriber<? super List<BusStop>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadOneBusStop(context, stopId, bound));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BusDirections> createBusDirectionsObservable(@NonNull final Context context, @NonNull final String busRouteId) {
        return Observable.create(
            (Subscriber<? super BusDirections> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusDirections(context, busRouteId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FirstLoadDTO> createOnFirstLoadObservable(@NonNull final Context context) {
        final Observable<List<BusRoute>> busRoutesObs = Observable.create(
            (Subscriber<? super List<BusRoute>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusRoutes(context));
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
                subscriber.onNext(BIKE_SERVICE.loadAllBikes(context));
                subscriber.onCompleted();
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return null;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        return Observable.zip(busRoutesObs, bikeStationsObs, (busRoutes, bikeStations) -> {
            final FirstLoadDTO result = new FirstLoadDTO();
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

    public static Observable<List<BusArrival>> createFollowBusObservable(@NonNull final Context context, @NonNull final String busId) {
        return Observable.create(
            (Subscriber<? super List<BusArrival>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadFollowBus(context, busId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Optional<BusPattern>> createBusPatternObservable(@NonNull final Context context, @NonNull final String busRouteId, @NonNull final String bound) {
        return Observable.create(
            (Subscriber<? super Optional<BusPattern>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBusPattern(context, busRouteId, bound));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<Bus>> createBusListObservable(@NonNull final Context context, final int busId, @NonNull final String busRouteId) {
        return Observable.create(
            (Subscriber<? super List<Bus>> subscriber) -> {
                subscriber.onNext(BUS_SERVICE.loadBus(context, busId, busRouteId));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
