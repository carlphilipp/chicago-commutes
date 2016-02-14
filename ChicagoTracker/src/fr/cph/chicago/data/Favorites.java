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
public class Favorites {
	/** The list of train arrival **/
	private SparseArray<TrainArrival> mTrainArrivals;
	/** The list of bus arrival **/
	private List<BusArrival> mBusArrivals;
	/** Bike stations **/
	private List<BikeStation> mBikeStations;
	/** The list of train favorites **/
	private List<Integer> mTrainFavorites;
	/** THe list of bus favorites **/
	private List<String> mBusFavorites;
	/** The list of bike favorites **/
	private List<String> mBikeFavorites;
	/** The list of fake bus favorites **/
	private List<String> mFakeBusFavorites;
	/** Train data **/
	private TrainData mTrainData;
	/** Bus data **/
	private BusData mBusData;

	/**
	 * Public constructor
	 */
	public Favorites() {
		this.mTrainArrivals = new SparseArray<TrainArrival>();
		this.mBusArrivals = new ArrayList<BusArrival>();
		this.mBikeStations = new ArrayList<BikeStation>();

		this.mTrainFavorites = new ArrayList<Integer>();
		this.mBusFavorites = new ArrayList<String>();
		this.mFakeBusFavorites = new ArrayList<String>();
		this.mBikeFavorites = new ArrayList<String>();

		this.mTrainData = DataHolder.getInstance().getTrainData();
		this.mBusData = DataHolder.getInstance().getBusData();
	}

	/**
	 * Get the size of the current model
	 *
	 * @return a size
	 */
	public final int size() {
		return mTrainFavorites.size() + mFakeBusFavorites.size() + mBikeFavorites.size();
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
		if (position < mTrainFavorites.size()) {
			Integer stationId = mTrainFavorites.get(position);
			result = mTrainData.getStation(stationId);
		} else if (position < mTrainFavorites.size() + mFakeBusFavorites.size()) {
			int indice = position - mTrainFavorites.size();
			if (indice < mFakeBusFavorites.size()) {
				String res[] = Util.decodeBusFavorite(mFakeBusFavorites.get(indice));
				if (mBusData.containsRoute(res[0])) {
					return mBusData.getRoute(res[0]);
				} else {
					// Get name in the preferences if null
					String routeName = Preferences.getBusRouteNameMapping(res[1]);
					BusRoute busRoute = new BusRoute();
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
			int indice = position - (mTrainFavorites.size() + mFakeBusFavorites.size());
			Collections.sort(mBikeStations, Util.BIKE_COMPARATOR_NAME);
			for (BikeStation bikeStation : mBikeStations) {
				if (String.valueOf(bikeStation.getId()).equals(mBikeFavorites.get(indice))) {
					return bikeStation;
				}
			}
			BikeStation bikeStation = new BikeStation();
			String stationName = Preferences.getBikeRouteNameMapping(mBikeFavorites.get(indice));
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
		return mTrainArrivals.get(stationId);
	}

	/**
	 * A list of bus arrival
	 *
	 * @param routeId
	 *            the route id
	 * @return a listof bus arrival
	 */
	public final List<BusArrival> getBusArrivals(final String routeId) {
		List<BusArrival> res = new ArrayList<BusArrival>();
		for (BusArrival busArrival : mBusArrivals) {
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
		for (BusArrival busArrival : mBusArrivals) {
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
		Map<String, Map<String, List<BusArrival>>> res = new TreeMap<String, Map<String, List<BusArrival>>>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		if (mBusArrivals != null) {
			if (mBusArrivals.size() == 0) {
				// Handle the case where no arrival train are there
				for (String bus : mBusFavorites) {
					String fav[] = Util.decodeBusFavorite(bus);
					String routeIdFav = fav[0];
					Integer stopId = Integer.valueOf(fav[1]);
					String bound = fav[2];

					String stopName = Preferences.getBusStopNameMapping(String.valueOf(stopId));

					BusArrival busArrival = new BusArrival();
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
							Map<String, List<BusArrival>> tempMap = res.get(stopId.toString());
							if (tempMap.containsKey(bound)) {
								List<BusArrival> arrivals = tempMap.get(bound);
								arrivals.add(busArrival);
							} else {
								List<BusArrival> arrivals = new ArrayList<BusArrival>();
								arrivals.add(busArrival);
								tempMap.put(bound, arrivals);
							}
						} else {
							Map<String, List<BusArrival>> tempMap = new TreeMap<String, List<BusArrival>>();
							List<BusArrival> arrivals = new ArrayList<BusArrival>();
							arrivals.add(busArrival);
							tempMap.put(bound, arrivals);
							res.put(busArrival.getStopName(), tempMap);
						}
					}
				}
			} else {
				for (BusArrival busArrival : mBusArrivals) {
					Integer stopId = busArrival.getStopId();
					String bound = busArrival.getRouteDirection();
					if (isInFavorites(routeId, stopId, bound)) {
						if (busArrival.getRouteId().equals(routeId)) {
							if (res.containsKey(busArrival.getStopName())) {
								Map<String, List<BusArrival>> tempMap = res.get(busArrival.getStopName());
								if (tempMap.containsKey(bound)) {
									List<BusArrival> arrivals = tempMap.get(busArrival.getRouteDirection());
									arrivals.add(busArrival);
								} else {
									List<BusArrival> arrivals = new ArrayList<BusArrival>();
									arrivals.add(busArrival);
									tempMap.put(bound, arrivals);
								}
							} else {
								Map<String, List<BusArrival>> tempMap = new TreeMap<String, List<BusArrival>>();
								List<BusArrival> arrivals = new ArrayList<BusArrival>();
								arrivals.add(busArrival);
								tempMap.put(bound, arrivals);
								res.put(busArrival.getStopName(), tempMap);
							}
						}
					}
				}
			}

			// Put empty buses if one of the stop is missing from the answer
			for (String bus : mBusFavorites) {
				String fav[] = Util.decodeBusFavorite(bus);
				String routeIdFav = fav[0];
				if (routeIdFav.equals(routeId)) {
					Integer stopId = Integer.valueOf(fav[1]);
					String bound = fav[2];

					String stopName = Preferences.getBusStopNameMapping(String.valueOf(stopId));

					BusArrival busArrival = new BusArrival();
					busArrival.setStopId(stopId);
					busArrival.setRouteDirection(bound);
					if (stopName != null) {
						busArrival.setStopName(stopName);
					} else {
						busArrival.setStopName(stopId.toString());
					}
					busArrival.setRouteId(routeIdFav);

					if (!res.containsKey(busArrival.getStopName())) {
						Map<String, List<BusArrival>> tempMap = new TreeMap<String, List<BusArrival>>();
						List<BusArrival> arrivals = new ArrayList<BusArrival>();
						arrivals.add(busArrival);
						tempMap.put(bound, arrivals);
						res.put(busArrival.getStopName(), tempMap);
					} else {
						Map<String, List<BusArrival>> tempMap = res.get(busArrival.getStopName());
						if (!tempMap.containsKey(bound)) {
							List<BusArrival> arrivals = new ArrayList<BusArrival>();
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
	private final boolean isInFavorites(final String routeId, final Integer stopId, final String bound) {
		boolean res = false;
		for (String fav : mBusFavorites) {
			String decoded[] = Util.decodeBusFavorite(fav);
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
		this.mTrainArrivals = trainArrival;
	}

	/**
	 *
	 * @param busArrivals
	 */
	public final void setBusArrivals(final List<BusArrival> busArrivals) {
		this.mBusArrivals = busArrivals;
	}

	/**
	 *
	 */
	public final void setFavorites() {
		this.mTrainFavorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		this.mBusFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		this.mFakeBusFavorites = calculateActualRouteNumberBusFavorites();
		this.mBikeFavorites.clear();
		List<String> bikeFavoritesTemp = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		List<BikeStation> bikeStationsFavoritesTemp = new ArrayList<BikeStation>();
		if (this.mBikeStations.size() != 0) {
			for (String bikeStationId : bikeFavoritesTemp) {
				for (BikeStation station : mBikeStations) {
					if (String.valueOf(station.getId()).equals(bikeStationId)) {
						bikeStationsFavoritesTemp.add(station);
						break;
					}
				}
			}
			Collections.sort(bikeStationsFavoritesTemp, Util.BIKE_COMPARATOR_NAME);
			for (BikeStation station : bikeStationsFavoritesTemp) {
				this.mBikeFavorites.add(String.valueOf(station.getId()));
			}
		} else {
			mBikeFavorites.addAll(bikeFavoritesTemp);
		}
	}

	/**
	 *
	 * @param trainArrivals
	 * @param busArrivals
	 */
	public final void setArrivalsAndBikeStations(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations) {
		this.mTrainArrivals.clear();
		this.mTrainArrivals = trainArrivals;
		removeDuplicates(busArrivals);
		this.mBusArrivals.clear();
		this.mBusArrivals = busArrivals;
		this.mBikeStations.clear();
		this.mBikeStations = bikeStations;
	}

	public final void setBikeStations(List<BikeStation> bikeStations) {
		this.mBikeStations.clear();
		this.mBikeStations = bikeStations;
		setFavorites();
	}

	/**
	 *
	 * @return
	 */
	private List<String> calculateActualRouteNumberBusFavorites() {
		List<String> found = new ArrayList<String>();
		List<String> favs = new ArrayList<String>();
		for (String fav : mBusFavorites) {
			String[] decoded = Util.decodeBusFavorite(fav);
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
	private final void removeDuplicates(final List<BusArrival> busArrivals) {
		Set<BusArrival> stBusArrivals = new LinkedHashSet<BusArrival>(busArrivals);
		busArrivals.clear();
		busArrivals.addAll(stBusArrivals);
	}
}
