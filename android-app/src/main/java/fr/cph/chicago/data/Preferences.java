package fr.cph.chicago.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

public interface Preferences {

    void saveTrainFavorites(Context context, List<Integer> favorites);

    boolean hasFavorites(Context context);

    void saveBikeFavorites(Context context, List<String> favorites);

    @NonNull
    List<String> getBikeFavorites(Context context);

    void addBikeRouteNameMapping(Context context, String bikeId, String bikeName);

    @Nullable
    String getBikeRouteNameMapping(Context context, String bikeId);

    void saveBusFavorites(Context context, List<String> favorites);

    @NonNull
    List<String> getBusFavorites(Context context);

    void addBusRouteNameMapping(Context context, String busStopId, String routeName);

    @Nullable
    String getBusRouteNameMapping(Context context, String busStopId);

    void addBusStopNameMapping(Context context, String busStopId, String stopName);

    @Nullable
    String getBusStopNameMapping(Context context, String busStopId);

    @NonNull
    List<Integer> getTrainFavorites(Context context);

    void saveTrainFilter(Context context, Integer stationId, TrainLine line, TrainDirection direction, boolean value);

    boolean getTrainFilter(Context context, Integer stationId, TrainLine line, TrainDirection direction);

    Date getRateLastSeen(Context context);

    void setRateLastSeen(Context context);

    void clearPreferences(final Context context);
}
