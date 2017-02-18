/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.annimon.stream.Collector;
import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Supplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.collector.CommutesCollectors;
import fr.cph.chicago.core.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.BusArrivalMappedDTO;
import fr.cph.chicago.entity.dto.BusFavoriteDTO;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;
import lombok.AccessLevel;
import lombok.Setter;

/**
 * Vehicle Arrival. Hold data for favorites adapter.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public enum FavoritesData {
    INSTANCE;

    private final TrainData trainData;
    private final BusData busData;

    @Setter
    private SparseArray<TrainArrival> trainArrivals;
    @Setter
    private List<BusArrival> busArrivals;
    @Setter
    private List<BikeStation> bikeStations;
    private List<Integer> trainFavorites;
    @Setter(AccessLevel.PACKAGE)
    private List<String> busFavorites;
    private final List<String> bikeFavorites;
    private List<String> fakeBusFavorites;
    @Setter(AccessLevel.PACKAGE)
    private Preferences preferences;

    FavoritesData() {
        this.trainArrivals = new SparseArray<>();
        this.busArrivals = new ArrayList<>();
        this.bikeStations = new ArrayList<>();
        this.trainFavorites = new ArrayList<>();
        this.busFavorites = new ArrayList<>();
        this.fakeBusFavorites = new ArrayList<>();
        this.bikeFavorites = new ArrayList<>();

        this.trainData = DataHolder.INSTANCE.getTrainData();
        this.busData = DataHolder.INSTANCE.getBusData();
        this.preferences = PreferencesImpl.INSTANCE;
    }

    /**
     * Get the size of the current model
     *
     * @return a size
     */
    public final int size() {
        return trainFavorites.size() + fakeBusFavorites.size() + bikeFavorites.size();
    }

    /**
     * Get the object depending on position
     *
     * @param position the position
     * @return an object, station or bus route
     */
    @NonNull
    public final Optional<?> getObject(final int position, @NonNull final Context context) {
        if (position < trainFavorites.size()) {
            final Integer stationId = trainFavorites.get(position);
            return trainData.getStation(stationId);
        } else if (position < trainFavorites.size() + fakeBusFavorites.size() && (position - trainFavorites.size() < fakeBusFavorites.size())) {
            final int index = position - trainFavorites.size();
            final String routeId = fakeBusFavorites.get(index);
            final Optional<BusRoute> busRouteOptional = busData.getRoute(routeId);
            if (busRouteOptional.isPresent()) {
                return busRouteOptional;
            } else {
                // Get name in the preferences if null
                final String routeName = preferences.getBusRouteNameMapping(context, routeId);
                final BusRoute busRoute = BusRoute.builder()
                    .id(routeId)
                    .name(routeName == null ? "" : routeName)
                    .build();
                busRoute.setId(routeId);
                return Optional.of(busRoute);
            }
        } else {
            final int index = position - (trainFavorites.size() + fakeBusFavorites.size());
            if (bikeStations != null) {
                final Optional<BikeStation> found = Stream.of(bikeStations)
                    .filter(bikeStation -> Integer.toString(bikeStation.getId()).equals(bikeFavorites.get(index)))
                    .findFirst();
                return found.isPresent() ? found : createEmptyBikeStation(index, context);
            } else {
                return createEmptyBikeStation(index, context);
            }
        }
    }

    @NonNull
    private Optional<BikeStation> createEmptyBikeStation(final int index, @NonNull final Context context) {
        final BikeStation bikeStation = BikeStation.builder().build();
        final String stationName = preferences.getBikeRouteNameMapping(context, bikeFavorites.get(index));
        bikeStation.setName(stationName);
        return Optional.of(bikeStation);
    }

    /**
     * Get the train arrival
     *
     * @param stationId the station id
     * @return a train arrival
     */
    @NonNull
    private TrainArrival getTrainArrival(final int stationId) {
        return trainArrivals.get(stationId, TrainArrival.builder().build());
    }

    @NonNull
    public final Map<String, String> getTrainArrivalByLine(final int stationId, @NonNull final TrainLine trainLine) {
        final List<Eta> etas = getTrainArrival(stationId).getEtas(trainLine);
        return Stream.of(etas).collect(CommutesCollectors.toTrainArrivalByLine());
    }

    /**
     * Get bus arrival mapped
     *
     * @param routeId the route id
     * @return a nice map
     */
    @NonNull
    public final BusArrivalMappedDTO getBusArrivalsMapped(@NonNull final String routeId, @NonNull final Context context) {
        // TODO check why (and if?) this method is called several time
        final BusArrivalMappedDTO busArrivalDTO = new BusArrivalMappedDTO();
        Stream.of(busArrivals)
            .filter(busArrival -> busArrival.getRouteId().equals(routeId))
            .filter(busArrival -> isInFavorites(routeId, busArrival.getStopId(), busArrival.getRouteDirection()))
            .forEach(busArrivalDTO::addBusArrival);

        // Put empty buses if one of the stop is missing from the response
        addNoServiceBusIfNeeded(busArrivalDTO, routeId, context);
        return busArrivalDTO;
    }

    private void addNoServiceBusIfNeeded(@NonNull final BusArrivalMappedDTO busArrivalDTO, @NonNull final String routeId, @NonNull final Context context) {
        for (final String bus : busFavorites) {
            final BusFavoriteDTO busFavorite = Util.decodeBusFavorite(bus);
            final String routeIdFav = busFavorite.getRouteId();
            if (routeIdFav.equals(routeId)) {
                final Integer stopId = Integer.valueOf(busFavorite.getStopId());
                final String bound = busFavorite.getBound();

                String stopName = preferences.getBusStopNameMapping(context, stopId.toString());
                stopName = stopName != null ? stopName : stopId.toString();

                if (!busArrivalDTO.containsStopNameAndBound(stopName, bound)) {
                    final BusArrival busArrival = BusArrival.builder()
                        .stopId(stopId)
                        .routeDirection(bound)
                        .stopName(stopName)
                        .routeId(routeIdFav)
                        .build();
                    busArrivalDTO.addBusArrival(busArrival);
                }
            }
        }
    }

    /**
     * Is in favorites
     *
     * @param routeId the route id
     * @param stopId  the stop id
     * @param bound   the bound
     * @return a boolean
     */
    private boolean isInFavorites(@NonNull final String routeId, final int stopId, @NonNull final String bound) {
        return Stream.of(busFavorites)
            .map(Util::decodeBusFavorite)
            // TODO: Is that correct ? maybe remove stopId
            .filter(decoded -> routeId.equals(decoded.getRouteId()) && Integer.toString(stopId).equals(decoded.getStopId()) && bound.equals(decoded.getBound()))
            .findFirst()
            .isPresent();
    }

    public final void setFavorites(@NonNull final Context context) {
        trainFavorites = preferences.getTrainFavorites(context);
        busFavorites = preferences.getBusFavorites(context);
        fakeBusFavorites = calculateActualRouteNumberBusFavorites();
        bikeFavorites.clear();
        final List<String> bikeFavoritesTemp = preferences.getBikeFavorites(context);
        if (bikeStations != null && bikeStations.size() != 0) {
            Stream.of(bikeFavoritesTemp)
                .flatMap(bikeStationId -> Stream.of(bikeStations).filter(station -> Integer.toString(station.getId()).equals(bikeStationId)))
                .sorted(Util.BIKE_COMPARATOR_NAME)
                .map(station -> Integer.toString(station.getId()))
                .forEach(bikeFavorites::add);
        } else {
            bikeFavorites.addAll(bikeFavoritesTemp);
        }
    }

    @NonNull
    private List<String> calculateActualRouteNumberBusFavorites() {
        return Stream.of(busFavorites)
            .map(Util::decodeBusFavorite)
            .map(BusFavoriteDTO::getRouteId)
            .distinct()
            .collect(Collectors.toList());
    }
}
