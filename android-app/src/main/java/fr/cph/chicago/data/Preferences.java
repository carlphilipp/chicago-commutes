package fr.cph.chicago.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

public interface Preferences {

    void saveTrainFavorites(@NonNull Context context, @NonNull String name, @NonNull List<Integer> favorites);

    boolean hasFavorites(@NonNull Context context, @NonNull String trains, @NonNull String bus, @NonNull String bike);

    void saveBikeFavorites(@NonNull Context context, @NonNull String name, @NonNull List<String> favorites);

    @NonNull
    List<String> getBikeFavorites(@NonNull Context context, @NonNull String name);

    void addBikeRouteNameMapping(@NonNull Context context, @NonNull String bikeId, @NonNull String bikeName);

    @Nullable
    String getBikeRouteNameMapping(@NonNull Context context, @NonNull String bikeId);

    void saveBusFavorites(@NonNull Context context, @NonNull String name, @NonNull List<String> favorites);

    @NonNull
    List<String> getBusFavorites(@NonNull Context context, @NonNull String name);

    void addBusRouteNameMapping(@NonNull Context context, @NonNull String busStopId, @NonNull String routeName);

    @Nullable
    String getBusRouteNameMapping(@NonNull Context context, @NonNull String busStopId);

    void addBusStopNameMapping(@NonNull Context context, @NonNull String busStopId, @NonNull String stopName);

    @Nullable
    String getBusStopNameMapping(@NonNull Context context, @NonNull String busStopId);

    @NonNull
    List<Integer> getTrainFavorites(@NonNull Context context, @NonNull String name);

    void saveTrainFilter(@NonNull Context context, @NonNull Integer stationId, @NonNull TrainLine line, @NonNull TrainDirection direction, boolean value);

    boolean getTrainFilter(@NonNull Context context, @NonNull Integer stationId, @NonNull TrainLine line, @NonNull TrainDirection direction);

    void saveHideShowNearby(@NonNull Context context, boolean hide);

    boolean getHideShowNearby(@NonNull Context context);

    Date getRateLastSeen(@NonNull Context context);

    void setRateLastSeen(@NonNull Context context);
}
