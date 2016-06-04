package fr.cph.chicago.service;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import id.ridsatrio.optio.Optional;

public interface BusService {

    List<BusArrival> loadFavoritesBuses(@NonNull final Context context);

    List<BusStop> loadOneBusStop(@NonNull final Context context, @NonNull final String stopId, @NonNull final String bound);

    BusData loadLocalBusData(@NonNull final Context context);

    BusDirections loadBusDirections(@NonNull final Context context, @NonNull final String busRouteId);

    List<BusRoute> loadBusRoutes(@NonNull final Context context);

    List<BusArrival> loadFollowBus(@NonNull final Context context, @NonNull final String busId);

    Optional<BusPattern> loadBusPattern(@NonNull final Context context, @NonNull final String busRouteId, @NonNull final String bound);

    List<Bus> loadBus(@NonNull final Context context, final int busId, @NonNull final String busRouteId);
}
