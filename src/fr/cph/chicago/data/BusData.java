/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.parser.BusStopCsvParser;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that handle bus data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusData {

    private static BusData BUS_DATA;

    private final Context context;

    @Getter
    @Setter
    private List<BusRoute> busRoutes;
    private List<BusStop> busStops;

    private final BusStopCsvParser parser;

    private BusData(@NonNull final Context context) {
        this.context = context.getApplicationContext();
        this.busRoutes = new ArrayList<>();
        this.busStops = new ArrayList<>();
        this.parser = new BusStopCsvParser();
    }

    /**
     * Get instance of the class
     *
     * @return a bus data instance
     */
    @NonNull
    public static BusData getInstance(@NonNull final Context context) {
        if (BUS_DATA == null) {
            BUS_DATA = new BusData(context);
        }
        return BUS_DATA;
    }

    /**
     * Method that read bus stops from CSV
     */
    public final void readBusStops() {
        if (busStops.isEmpty()) {
            busStops = parser.parse(context);
        }
    }

    /**
     * Get a route
     *
     * @param routeId the id of the bus route
     * @return a bus route
     */
    @Nullable
    final BusRoute getRoute(@NonNull final String routeId) {
        final Optional<BusRoute> busRoute = Stream.of(busRoutes)
            .filter(busR -> busR.getId().equals(routeId))
            .findFirst();
        return busRoute.isPresent() ? busRoute.get() : null;
    }

    final boolean containsRoute(@NonNull final String routeId) {
        return Stream.of(busRoutes)
            .filter(busR -> busR.getId().equals(routeId))
            .findFirst()
            .isPresent();
    }

    /**
     * Get all bus stops from CSV
     *
     * @return a list of bus stops
     */
    @NonNull
    public final List<BusStop> readAllBusStops() {
        return busStops;
    }

    /**
     * Get a list of bus stop within a a distance and position
     *
     * @param position the position
     * @return a list of bus stop
     */
    @NonNull
    public final List<BusStop> readNearbyStops(@NonNull final Position position) {
        final double dist = 0.004472;

        final double latitude = position.getLatitude();
        final double longitude = position.getLongitude();

        final double latMax = latitude + dist;
        final double latMin = latitude - dist;
        final double lonMax = longitude + dist;
        final double lonMin = longitude - dist;

        return Stream.of(busStops)
            .filter(busStop -> {
                final double busLatitude = busStop.getPosition().getLatitude();
                final double busLongitude = busStop.getPosition().getLongitude();
                return busLatitude <= latMax && busLatitude >= latMin && busLongitude <= lonMax && busLongitude >= lonMin;
            })
            .sorted((left, right) -> left.getName().compareTo(right.getName()))
            .collect(Collectors.toList());
    }
}
