package fr.cph.chicago.rx.observable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.cph.chicago.connection.CtaConnect;
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
import fr.cph.chicago.entity.dto.NearbyDTO;
import fr.cph.chicago.entity.dto.TrainArrivalDTO;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BikeServiceImpl;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;

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
                    final TrainArrivalDTO trainArrivalDTO = TrainArrivalDTO.builder()
                        .trainArrivalSparseArray(TRAIN_SERVICE.loadFavoritesTrain(context))
                        .error(false)
                        .build();
                    observableOnSubscribe.onNext(trainArrivalDTO);
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return TrainArrivalDTO.builder()
                    .trainArrivalSparseArray(new SparseArray<>())
                    .error(true)
                    .build();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Optional<TrainArrival>> createTrainArrivalsObservable(@NonNull final Context context, final int stationId) {
        return Observable.create(
            (ObservableEmitter<Optional<TrainArrival>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(TRAIN_SERVICE.loadStationTrainArrival(context, stationId));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Optional<TrainArrival>> createTrainArrivalsObservable(@NonNull final Context context, final List<Station> trainStations) {
        if (trainStations.isEmpty()) {
            return Observable.create((ObservableEmitter<Optional<TrainArrival>> observableOnSubscribe) -> {
                observableOnSubscribe.onNext(Optional.empty());
                observableOnSubscribe.onComplete();
            })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        } else {
            return createTrainArrivalsObservable(context, trainStations.get(0).getId());
        }
    }

    public static Observable<BusArrivalDTO> createFavoritesBusArrivalsObservable(@NonNull final Context context) {
        return Observable.create(
            (ObservableEmitter<BusArrivalDTO> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    final BusArrivalDTO busArrivalDTO = BusArrivalDTO.builder()
                        .busArrivals(BUS_SERVICE.loadFavoritesBuses(context))
                        .error(false)
                        .build();
                    observableOnSubscribe.onNext(busArrivalDTO);
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return BusArrivalDTO.builder()
                    .busArrivals(new ArrayList<>())
                    .error(true)
                    .build();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<SparseArray<Map<String, List<BusArrival>>>> createBusArrivalsObservable(String requestStopId, Context context, @NonNull final List<BusStop> bustStops) {
        return Observable.create(
            (ObservableEmitter<SparseArray<Map<String, List<BusArrival>>>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap = new SparseArray<>();
                    Stream.of(bustStops).forEach(busStop -> {
                        ObservableUtil.loadAroundBusArrivals(requestStopId, context, busStop, busArrivalsMap);
                    });
                    observableOnSubscribe.onNext(busArrivalsMap);
                    observableOnSubscribe.onComplete();
                }
            })
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new SparseArray<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    private static Map<String, List<BusArrival>> loadAroundBusArrivals(String requestStopId, Context context, @NonNull final BusStop busStop, @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap) {
        final Map<String, List<BusArrival>> result = new HashMap<>();
        try {
            int busStopId = busStop.getId();
            // Create
            final Map<String, List<BusArrival>> tempMap = busArrivalsMap.get(busStopId, new ConcurrentHashMap<>());
            if (!tempMap.containsKey(Integer.toString(busStopId))) {
                busArrivalsMap.put(busStopId, tempMap);
            }

            final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
            reqParams.put(requestStopId, Integer.toString(busStopId));
            final InputStream is = CtaConnect.INSTANCE.connect(BUS_ARRIVALS, reqParams, context);
            final List<BusArrival> busArrivals = XmlParser.INSTANCE.parseBusArrivals(is);
            for (final BusArrival busArrival : busArrivals) {
                final String direction = busArrival.getRouteDirection();
                if (tempMap.containsKey(direction)) {
                    final List<BusArrival> temp = tempMap.get(direction);
                    temp.add(busArrival);
                } else {
                    final List<BusArrival> temp = new ArrayList<>();
                    temp.add(busArrival);
                    tempMap.put(direction, temp);
                }
            }
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
        return result;
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

    public static Observable<List<BikeStation>> createBikeStationsObservable(@NonNull final List<Integer> ids) {
        if (ids.isEmpty()) {
            return Observable.create((ObservableEmitter<List<BikeStation>> observableOnSubscribe) -> new ArrayList<>());
        } else {
            return Observable.create(
                (ObservableEmitter<List<BikeStation>> observableOnSubscribe) -> {
                    if (!observableOnSubscribe.isDisposed()) {
                        observableOnSubscribe.onNext(BIKE_SERVICE.loadBikes(ids));
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
                return FavoritesDTO.builder()
                    .trainArrivalDTO(trainArrivalsDTO)
                    .busArrivalDTO(busArrivalsDTO)
                    .bikeStations(bikeStations)
                    .bikeError(bikeStations.isEmpty())
                    .build();
            });
    }

    public static Observable<NearbyDTO> createMarkerDataObservable(String requestStopId, @NonNull final List<BusStop> bustStops, @NonNull final Context context, @NonNull final List<Integer> bikeStationsIds) {
        // Train online favorites
        final Observable<TrainArrivalDTO> trainArrivalsObservable = ObservableUtil.createFavoritesTrainArrivalsObservable(context);
        // Bus online favorites
        final Observable<SparseArray<Map<String, List<BusArrival>>>> busArrivalsObservable = ObservableUtil.createBusArrivalsObservable(requestStopId, context, bustStops);
        // Bikes online all stations
        final Observable<List<BikeStation>> bikeStationsObservable = ObservableUtil.createBikeStationsObservable(bikeStationsIds);
        return Observable.zip(bikeStationsObservable, trainArrivalsObservable, busArrivalsObservable,
            (bikeStations, trainArrivalsDTO, busArrivalsDTO) -> NearbyDTO.builder()
                .trainArrivalDTO(trainArrivalsDTO)
                .busArrivalDTO(busArrivalsDTO)
                .bikeStations(bikeStations)
                .bikeError(bikeStations.isEmpty())
                .build());
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

    public static Observable<FirstLoadDTO> createOnFirstLoadObservable(@NonNull final Context context) {
        final Observable<List<BusRoute>> busRoutesObs = Observable.create(
            (ObservableEmitter<List<BusRoute>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(BUS_SERVICE.loadBusRoutes(context));
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

        return Observable.zip(busRoutesObs, bikeStationsObs, (busRoutes, bikeStations) -> {
            final FirstLoadDTO result = FirstLoadDTO.builder()
                .busRoutes(busRoutes)
                .bikeStations(bikeStations).build();
            if (busRoutes.isEmpty()) {
                result.setBusRoutesError(true);
            }
            if (bikeStations.isEmpty()) {
                result.setBikeStationsError(true);
            }
            return result;
        });
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
