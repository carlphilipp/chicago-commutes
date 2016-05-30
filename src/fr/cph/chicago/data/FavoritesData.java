/**
 * Copyright 2016 Carl-Philipp Harmant
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
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fr.cph.chicago.app.App;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Vehicle Arrival. Hold data for favorites adapter.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public class FavoritesData {

    private static FavoritesData INSTANCE;

    private final Context context;
    private final TrainData trainData;
    private final BusData busData;

    private SparseArray<TrainArrival> trainArrivals;
    private List<BusArrival> busArrivals;
    private List<BikeStation> bikeStations;
    private List<Integer> trainFavorites;
    private List<String> busFavorites;
    private final List<String> bikeFavorites;
    private List<String> fakeBusFavorites;

    private FavoritesData(@NonNull final Context context) {
        this.context = context;
        this.trainArrivals = new SparseArray<>();
        this.busArrivals = new ArrayList<>();
        this.bikeStations = new ArrayList<>();
        this.trainFavorites = new ArrayList<>();
        this.busFavorites = new ArrayList<>();
        this.fakeBusFavorites = new ArrayList<>();
        this.bikeFavorites = new ArrayList<>();

        this.trainData = DataHolder.getInstance().getTrainData();
        this.busData = DataHolder.getInstance().getBusData();
    }

    public static FavoritesData getInstance(@NonNull final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FavoritesData(context.getApplicationContext());
        }
        return INSTANCE;
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
    @Nullable
    public final Object getObject(final int position) {
        if (position < trainFavorites.size()) {
            final Integer stationId = trainFavorites.get(position);
            return trainData.getStation(stationId);
        } else if (position < trainFavorites.size() + fakeBusFavorites.size() && (position - trainFavorites.size() < fakeBusFavorites.size())) {
            final int index = position - trainFavorites.size();
            final String res[] = Util.decodeBusFavorite(fakeBusFavorites.get(index));
            if (busData.containsRoute(res[0])) {
                return busData.getRoute(res[0]);
            } else {
                // Get name in the preferences if null
                final String routeName = Preferences.getBusRouteNameMapping(context, res[1]);
                final BusRoute busRoute = new BusRoute();
                busRoute.setId(res[0]);
                if (routeName == null) {
                    busRoute.setName("");
                } else {
                    busRoute.setName(routeName);
                }
                return busRoute;
            }
        } else {
            final int index = position - (trainFavorites.size() + fakeBusFavorites.size());
            final Optional<BikeStation> found = Stream.of(bikeStations)
                .filter(bikeStation -> Integer.toString(bikeStation.getId()).equals(bikeFavorites.get(index)))
                .findFirst();
            if (found.isPresent()) {
                return found.get();
            } else {
                final BikeStation bikeStation = new BikeStation();
                final String stationName = Preferences.getBikeRouteNameMapping(context, bikeFavorites.get(index));
                bikeStation.setName(stationName);
                return bikeStation;
            }
        }
    }

    /**
     * Get the train arrival
     *
     * @param stationId the station id
     * @return a train arrival
     */
    @NonNull
    private TrainArrival getTrainArrival(final int stationId) {
        return trainArrivals.get(stationId, new TrainArrival());
    }

    @NonNull
    public final Map<String, StringBuilder> getTrainArrivalByLine(final int stationId, @NonNull final TrainLine trainLine) {
        final List<Eta> etas = getTrainArrival(stationId).getEtas(trainLine);
        final Map<String, StringBuilder> result = new HashMap<>();
        for (final Eta eta : etas) {
            final String stopNameData = eta.getDestName();
            final String timingData = eta.getTimeLeftDueDelay();
            if (!result.containsKey(stopNameData)) {
                final StringBuilder list = new StringBuilder();
                list.append(timingData);
                result.put(stopNameData, list);
            } else {
                result.get(stopNameData).append(" ").append(timingData);
            }
        }
        return result;
    }

    /**
     * Get bus arrival mapped
     *
     * @param routeId the route id
     * @return a nice map
     */
    @NonNull
    public final Map<String, Map<String, List<BusArrival>>> getBusArrivalsMapped(@NonNull final String routeId) {
        final Map<String, Map<String, List<BusArrival>>> res = new TreeMap<>(String::compareTo);
        if (busArrivals != null) {
            if (busArrivals.size() == 0) {
                // Handle the case where no arrival train are there
                for (final String bus : busFavorites) {
                    final String fav[] = Util.decodeBusFavorite(bus);
                    final String routeIdFav = fav[0];
                    final Integer stopId = Integer.valueOf(fav[1]);
                    final String bound = fav[2];

                    final String stopName = Preferences.getBusStopNameMapping(context, String.valueOf(stopId));

                    final BusArrival busArrival = new BusArrival();
                    busArrival.setStopId(stopId);
                    busArrival.setRouteDirection(bound);
                    if (stopName != null) {
                        busArrival.setStopName(stopName);
                    } else {
                        busArrival.setStopName(stopId.toString());
                    }
                    busArrival.setRouteId(routeIdFav);

                    if (routeIdFav.equals(routeId)) {
                        if (res.containsKey(stopId.toString())) {
                            final Map<String, List<BusArrival>> tempMap = res.get(stopId.toString());
                            if (tempMap.containsKey(bound)) {
                                final List<BusArrival> arrivals = tempMap.get(bound);
                                arrivals.add(busArrival);
                            } else {
                                final List<BusArrival> arrivals = new ArrayList<>();
                                arrivals.add(busArrival);
                                tempMap.put(bound, arrivals);
                            }
                        } else {
                            final Map<String, List<BusArrival>> tempMap = new TreeMap<>();
                            final List<BusArrival> arrivals = new ArrayList<>();
                            arrivals.add(busArrival);
                            tempMap.put(bound, arrivals);
                            res.put(busArrival.getStopName(), tempMap);
                        }
                    }
                }
            } else {
                for (final BusArrival busArrival : busArrivals) {
                    final int stopId = busArrival.getStopId();
                    final String bound = busArrival.getRouteDirection();
                    if (isInFavorites(routeId, stopId, bound)) {
                        if (busArrival.getRouteId().equals(routeId)) {
                            if (res.containsKey(busArrival.getStopName())) {
                                final Map<String, List<BusArrival>> tempMap = res.get(busArrival.getStopName());
                                if (tempMap.containsKey(bound)) {
                                    final List<BusArrival> arrivals = tempMap.get(busArrival.getRouteDirection());
                                    arrivals.add(busArrival);
                                } else {
                                    final List<BusArrival> arrivals = new ArrayList<>();
                                    arrivals.add(busArrival);
                                    tempMap.put(bound, arrivals);
                                }
                            } else {
                                final Map<String, List<BusArrival>> tempMap = new TreeMap<>();
                                final List<BusArrival> arrivals = new ArrayList<>();
                                arrivals.add(busArrival);
                                tempMap.put(bound, arrivals);
                                res.put(busArrival.getStopName(), tempMap);
                            }
                        }
                    }
                }
            }

            // Put empty buses if one of the stop is missing from the answer
            for (final String bus : busFavorites) {
                final String fav[] = Util.decodeBusFavorite(bus);
                final String routeIdFav = fav[0];
                if (routeIdFav.equals(routeId)) {
                    final Integer stopId = Integer.valueOf(fav[1]);
                    final String bound = fav[2];

                    final String stopName = Preferences.getBusStopNameMapping(context, String.valueOf(stopId));

                    final BusArrival busArrival = new BusArrival();
                    busArrival.setStopId(stopId);
                    busArrival.setRouteDirection(bound);
                    if (stopName != null) {
                        busArrival.setStopName(stopName);
                    } else {
                        busArrival.setStopName(stopId.toString());
                    }
                    busArrival.setRouteId(routeIdFav);

                    if (!res.containsKey(busArrival.getStopName())) {
                        final Map<String, List<BusArrival>> tempMap = new TreeMap<>();
                        final List<BusArrival> arrivals = new ArrayList<>();
                        arrivals.add(busArrival);
                        tempMap.put(bound, arrivals);
                        res.put(busArrival.getStopName(), tempMap);
                    } else {
                        final Map<String, List<BusArrival>> tempMap = res.get(busArrival.getStopName());
                        if (!tempMap.containsKey(bound)) {
                            final List<BusArrival> arrivals = new ArrayList<>();
                            arrivals.add(busArrival);
                            tempMap.put(bound, arrivals);
                        }
                    }
                }
            }
        }
        return res;
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
            .filter(decoded -> routeId.equals(decoded[0]) && Integer.toString(stopId).equals(decoded[1]) && bound.equals(decoded[2]))
            .findFirst()
            .isPresent();
    }

    public final void setFavorites() {
        trainFavorites = Preferences.getTrainFavorites(context, App.PREFERENCE_FAVORITES_TRAIN);
        busFavorites = Preferences.getBusFavorites(context, App.PREFERENCE_FAVORITES_BUS);
        fakeBusFavorites = calculateActualRouteNumberBusFavorites();
        bikeFavorites.clear();
        final List<String> bikeFavoritesTemp = Preferences.getBikeFavorites(context, App.PREFERENCE_FAVORITES_BIKE);
        final List<BikeStation> bikeStationsFavoritesTemp = new ArrayList<>(bikeFavoritesTemp.size());
        if (bikeStations != null && bikeStations.size() != 0) {
            Stream.of(bikeFavoritesTemp)
                .flatMap(bikeStationId -> Stream.of(bikeStations)
                    .filter(station -> Integer.toString(station.getId()).equals(bikeStationId))
                )
                .findFirst()
                .map(bikeStationsFavoritesTemp::add);

            bikeFavorites.addAll(
                Stream.of(bikeStationsFavoritesTemp)
                    .sorted(Util.BIKE_COMPARATOR_NAME)
                    .map(station -> Integer.toString(station.getId()))
                    .collect(Collectors.toList())
            );
        } else {
            bikeFavorites.addAll(bikeFavoritesTemp);
        }
    }

    @NonNull
    private List<String> calculateActualRouteNumberBusFavorites() {
        // TODO find a good way to refactor with stream
        final List<String> found = new ArrayList<>(busFavorites.size());
        final List<String> favorites = new ArrayList<>(busFavorites.size());
        for (final String fav : busFavorites) {
            final String[] decoded = Util.decodeBusFavorite(fav);
            if (!found.contains(decoded[0])) {
                found.add(decoded[0]);
                favorites.add(fav);
            }
        }
        return favorites;
    }

    // TODO Do that when populating the list
    private void removeDuplicates(@NonNull final List<BusArrival> busArrivals) {
        final Set<BusArrival> stBusArrivals = new LinkedHashSet<>(busArrivals);
        busArrivals.clear();
        busArrivals.addAll(stBusArrivals);
    }

    public final void setBikeStations(@NonNull final List<BikeStation> bikeStations) {
        this.bikeStations.clear();
        this.bikeStations = bikeStations;
        Stream.of(this.bikeStations).sorted(Util.BIKE_COMPARATOR_NAME);
    }

    public final void setBusArrivals(@NonNull final List<BusArrival> busArrivals) {
        this.busArrivals.clear();
        removeDuplicates(busArrivals);
        this.busArrivals = busArrivals;
    }

    public void setTrainArrivals(@NonNull final SparseArray<TrainArrival> trainArrivals) {
        this.trainArrivals.clear();
        this.trainArrivals = trainArrivals;
    }
}
