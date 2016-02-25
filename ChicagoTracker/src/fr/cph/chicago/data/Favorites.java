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

import android.util.SparseArray;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Vehicle Arrival. Hold data for favorites adapter.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public class Favorites {

	private TrainData trainData;
	private BusData busData;

	private SparseArray<TrainArrival> trainArrivals;
	private List<BusArrival> busArrivals;
	private List<BikeStation> bikeStations;
	private List<Integer> trainFavorites;
	private List<String> busFavorites;
	private List<String> bikeFavorites;
	private List<String> fakeBusFavorites;


	/**
	 * Public constructor
	 */
	public Favorites() {
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
	 * @param position
	 *            the position
	 * @return an object, station or bus route
	 */
	public final Object getObject(final int position) {
		Object result = null;
		if (position < trainFavorites.size()) {
			final Integer stationId = trainFavorites.get(position);
			result = trainData.getStation(stationId);
		} else if (position < trainFavorites.size() + fakeBusFavorites.size()) {
			final int indice = position - trainFavorites.size();
			if (indice < fakeBusFavorites.size()) {
				final String res[] = Util.decodeBusFavorite(fakeBusFavorites.get(indice));
				if (busData.containsRoute(res[0])) {
					return busData.getRoute(res[0]);
				} else {
					// Get name in the preferences if null
					final String routeName = Preferences.getBusRouteNameMapping(res[1]);
					final BusRoute busRoute = new BusRoute();
					busRoute.setId(res[0]);
					if (routeName == null) {
						busRoute.setName("");
					} else {
						busRoute.setName(routeName);
					}
					return busRoute;
				}

			}
		} else {
			final int index = position - (trainFavorites.size() + fakeBusFavorites.size());
			Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
			for (final BikeStation bikeStation : bikeStations) {
				if (String.valueOf(bikeStation.getId()).equals(bikeFavorites.get(index))) {
					return bikeStation;
				}
			}
			final BikeStation bikeStation = new BikeStation();
			final String stationName = Preferences.getBikeRouteNameMapping(bikeFavorites.get(index));
			bikeStation.setName(stationName);
			return bikeStation;
		}
		return result;
	}

	/**
	 * Get the train arrival
	 *
	 * @param stationId
	 *            the station id
	 * @return a train arrival
	 */
	public final TrainArrival getTrainArrival(final Integer stationId) {
		return trainArrivals.get(stationId);
	}

	/**
	 * A list of bus arrival
	 *
	 * @param routeId
	 *            the route id
	 * @return a listof bus arrival
	 */
	public final List<BusArrival> getBusArrivals(final String routeId) {
		final List<BusArrival> res = new ArrayList<>();
		for (final BusArrival busArrival : busArrivals) {
			if (busArrival.getRouteId().equals(routeId)) {
				res.add(busArrival);
			}
		}
		return res;
	}

	/**
	 * Get on bus arrival
	 *
	 * @param routeId
	 *            the route id
	 * @return the bus arrival
	 */
	public final BusArrival getOneBusArrival(final String routeId) {
		BusArrival bus = null;
		for (final BusArrival busArrival : busArrivals) {
			if (busArrival.getRouteId().equals(routeId)) {
				bus = busArrival;
				break;
			}
		}
		return bus;
	}

	/**
	 * Get bus arrival mapped
	 *
	 * @param routeId
	 *            the route id
	 * @return a nice map
	 */
	public final Map<String, Map<String, List<BusArrival>>> getBusArrivalsMapped(final String routeId) {
		final Map<String, Map<String, List<BusArrival>>> res = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		if (busArrivals != null) {
			if (busArrivals.size() == 0) {
				// Handle the case where no arrival train are there
				for (final String bus : busFavorites) {
					final String fav[] = Util.decodeBusFavorite(bus);
					final String routeIdFav = fav[0];
					final Integer stopId = Integer.valueOf(fav[1]);
					final String bound = fav[2];

					final String stopName = Preferences.getBusStopNameMapping(String.valueOf(stopId));

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
					final Integer stopId = busArrival.getStopId();
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

					final String stopName = Preferences.getBusStopNameMapping(String.valueOf(stopId));

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
	 * @param routeId
	 *            the route id
	 * @param stopId
	 *            the stop id
	 * @param bound
	 *            the bound
	 * @return a boolean
	 */
	private boolean isInFavorites(final String routeId, final Integer stopId, final String bound) {
		boolean res = false;
		for (final String fav : busFavorites) {
			final String decoded[] = Util.decodeBusFavorite(fav);
			// TODO: Is that correct ? maybe remove stopId
			if (routeId.equals(decoded[0]) && String.valueOf(stopId).equals(decoded[1]) && bound.equals(decoded[2])) {
				res = true;
				break;
			}
		}
		return res;
	}

	/**
	 *
	 * @param trainArrival
	 */
	public final void setTrainArrival(final SparseArray<TrainArrival> trainArrival) {
		this.trainArrivals = trainArrival;
	}

	/**
	 *
	 * @param busArrivals
	 */
	public final void setBusArrivals(final List<BusArrival> busArrivals) {
		this.busArrivals = busArrivals;
	}

	/**
	 *
	 */
	public final void setFavorites() {
		trainFavorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		fakeBusFavorites = calculateActualRouteNumberBusFavorites();
		bikeFavorites.clear();
		final List<String> bikeFavoritesTemp = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		final List<BikeStation> bikeStationsFavoritesTemp = new ArrayList<>();
		if (this.bikeStations.size() != 0) {
			for (final String bikeStationId : bikeFavoritesTemp) {
				for (final BikeStation station : bikeStations) {
					if (String.valueOf(station.getId()).equals(bikeStationId)) {
						bikeStationsFavoritesTemp.add(station);
						break;
					}
				}
			}
			Collections.sort(bikeStationsFavoritesTemp, Util.BIKE_COMPARATOR_NAME);
			for (final BikeStation station : bikeStationsFavoritesTemp) {
				this.bikeFavorites.add(String.valueOf(station.getId()));
			}
		} else {
			bikeFavorites.addAll(bikeFavoritesTemp);
		}
	}

	/**
	 *
	 * @param trainArrivals
	 * @param busArrivals
	 */
	public final void setArrivalsAndBikeStations(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations) {
		this.trainArrivals.clear();
		this.trainArrivals = trainArrivals;
		removeDuplicates(busArrivals);
		this.busArrivals.clear();
		this.busArrivals = busArrivals;
		this.bikeStations.clear();
		this.bikeStations = bikeStations;
	}

	public final void setBikeStations(List<BikeStation> bikeStations) {
		this.bikeStations.clear();
		this.bikeStations = bikeStations;
		setFavorites();
	}

	/**
	 *
	 * @return
	 */
	private List<String> calculateActualRouteNumberBusFavorites() {
		final List<String> found = new ArrayList<>();
		final List<String> favs = new ArrayList<>();
		for (final String fav : busFavorites) {
			final String[] decoded = Util.decodeBusFavorite(fav);
			if (!found.contains(decoded[0])) {
				found.add(decoded[0]);
				favs.add(fav);
			}
		}
		return favs;
	}

	/**
	 *
	 * @param busArrivals
	 */
	// TODO Do that when populating the list
	private void removeDuplicates(final List<BusArrival> busArrivals) {
		final Set<BusArrival> stBusArrivals = new LinkedHashSet<>(busArrivals);
		busArrivals.clear();
		busArrivals.addAll(stBusArrivals);
	}
}
