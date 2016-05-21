package fr.cph.chicago.service;

import android.support.annotation.NonNull;

import java.util.List;

import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;

public interface BusService {

    List<BusArrival> loadFavoritesBuses();

    List<BusStop> loadOneBusStop(@NonNull final String stopId, @NonNull final String bound);

    BusData loadLocalBusData();

    BusDirections loadBusDirections(@NonNull final String busRouteId);

    List<BusRoute> loadBusRoutes();

    List<BusArrival> loadFollowBus(@NonNull final String busId);

    BusPattern loadBusPattern(@NonNull final String busRouteId, @NonNull final String bound);

    List<Bus> loadBus(final int busId, @NonNull final String busRouteId);
}
