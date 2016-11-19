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
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.parser.BusStopCsvParser;
import io.realm.Realm;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that handle bus data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum BusData {
    INSTANCE;

    private static final String TAG = BusData.class.getSimpleName();

    @Getter
    private final List<BusRoute> busRoutes;

    BusData() {
        this.busRoutes = new ArrayList<>();
    }

    /**
     * Method that read bus stops from CSV
     */
    public final void readBusStopsIfNeeded(@NonNull final Context context) {
        final Realm realm = Realm.getDefaultInstance();
        if (realm.where(BusStop.class).findFirst() == null) {
            Log.d(TAG, "Load bus stop from CSV");
            BusStopCsvParser.INSTANCE.parse(context);
        }
        realm.close();
    }

    /**
     * Get a route
     *
     * @param routeId the id of the bus route
     * @return a bus route
     */
    @NonNull
    final Optional<BusRoute> getRoute(@NonNull final String routeId) {
        return Stream.of(busRoutes)
            .filter(busRoute -> busRoute.getId().equals(routeId))
            .findFirst();
    }

    /**
     * Get a list of bus stop within a a distance and position
     *
     * @param position the position
     * @return a list of bus stop
     */
    @NonNull
    public final List<BusStop> readNearbyStops(@NonNull final Realm realm, @NonNull final Position position) {
        final double dist = 0.004472;

        final double latitude = position.getLatitude();
        final double longitude = position.getLongitude();

        final double latMax = latitude + dist;
        final double latMin = latitude - dist;
        final double lonMax = longitude + dist;
        final double lonMin = longitude - dist;

        return Stream.of(realm.where(BusStop.class)
            // TODO use between when child object is supported by Realm
            .greaterThan("position.latitude", latMin)
            .lessThan("position.latitude", latMax)
            .greaterThan("position.longitude", lonMin)
            .lessThan("position.longitude", lonMax)
            .findAllSorted("name"))
            .map(currentBusStop -> {
                final BusStop busStop = new BusStop();
                busStop.setName(currentBusStop.getName());
                final Position pos = new Position();
                pos.setLatitude(currentBusStop.getPosition().getLatitude());
                pos.setLongitude(currentBusStop.getPosition().getLongitude());
                busStop.setPosition(pos);
                busStop.setId(currentBusStop.getId());
                return busStop;
            })
            .collect(Collectors.toList());
    }
}
