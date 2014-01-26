/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.SparseArray;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.util.Util;

public class VehiculeArrival {

	/** Tag **/
	private static final String TAG = "VehiculeArrival";

	private SparseArray<TrainArrival> trainArrivals;
	private List<BusArrival> busArrivals;

	private List<Integer> trainFavorites;
	private List<String> busFavorites;
	private List<String> fakeBusFavorites;
//	private int actualRouteNumberBusFavorites;

	private TrainData trainData;
	private BusData busData;

	public VehiculeArrival() {
		this.trainArrivals = new SparseArray<TrainArrival>();
		this.busArrivals = new ArrayList<BusArrival>();

		this.trainFavorites = new ArrayList<Integer>();
		this.busFavorites = new ArrayList<String>();
		this.fakeBusFavorites = new ArrayList<String>();

		this.trainData = DataHolder.getInstance().getTrainData();
		this.busData = DataHolder.getInstance().getBusData();
	}

	public int size() {
		return trainFavorites.size() + fakeBusFavorites.size();
	}

	public Object getObject(int position) {
		Object result = null;
		if (position < trainFavorites.size()) {
			Integer stationId = trainFavorites.get(position);
			result = trainData.getStation(stationId);
		} else {
			int indice = position - trainFavorites.size();
			if (indice < fakeBusFavorites.size()) {
				String res[] = Util.decodeBusFavorite(fakeBusFavorites.get(indice));
				return busData.getRoute(res[0]);
			}
		}
		return result;
	}

	public TrainArrival getTrainArrival(Integer stationId) {
		return trainArrivals.get(stationId);
	}

	public List<BusArrival> getBusArrivals(String routeId) {
		List<BusArrival> res = new ArrayList<BusArrival>();
		for (BusArrival busArrival : busArrivals) {
			if (busArrival.getRouteId().equals(routeId)) {
				res.add(busArrival);
			}
		}
		return res;
	}

	public Map<String, Map<String, List<BusArrival>>> getBusArrivalsMapped(String routeId) {
		Map<String, Map<String, List<BusArrival>>> res = new HashMap<String, Map<String, List<BusArrival>>>();
		for (BusArrival busArrival : busArrivals) {
			if (busArrival.getRouteId().equals(routeId)) {
				if (res.containsKey(busArrival.getStopName())) {
					Map<String, List<BusArrival>> tempMap = res.get(busArrival.getStopName());
					if (tempMap.containsKey(busArrival.getRouteDirection())) {
						List<BusArrival> arrivals = tempMap.get(busArrival.getRouteDirection());
						arrivals.add(busArrival);
					} else {
						List<BusArrival> arrivals = new ArrayList<BusArrival>();
						arrivals.add(busArrival);
						tempMap.put(busArrival.getRouteDirection(), arrivals);
					}
				} else {
					Map<String, List<BusArrival>> tempMap = new HashMap<String, List<BusArrival>>();
					List<BusArrival> arrivals = new ArrayList<BusArrival>();
					arrivals.add(busArrival);
					tempMap.put(busArrival.getRouteDirection(), arrivals);
					res.put(busArrival.getStopName(), tempMap);
				}
			}
		}
		return res;
	}

	public void setTrainArrival(SparseArray<TrainArrival> trainArrival) {
		this.trainArrivals = trainArrival;
	}

	public void setBusArrivals(List<BusArrival> busArrivals) {
		this.busArrivals = busArrivals;
	}

	public void setFavorites() {
		this.trainFavorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		this.busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		this.fakeBusFavorites = calculateaActualRouteNumberBusFavorites();
	}

	public void setArrivals(SparseArray<TrainArrival> trainArrivals, List<BusArrival> busArrivals) {
		this.trainArrivals.clear();
		this.trainArrivals = trainArrivals;
		removeDuplicates(busArrivals);
		this.busArrivals.clear();
		this.busArrivals = busArrivals;
		setFavorites();
	}
	
	private void removeDuplicates(List<BusArrival> busArrivals){
		Set<BusArrival> stBusArrivals = new LinkedHashSet<BusArrival>(busArrivals);
		busArrivals.clear();
		busArrivals.addAll(stBusArrivals);
	}

	public List<String> calculateaActualRouteNumberBusFavorites() {
		List<String> found = new ArrayList<String>();
		List<String> favs = new ArrayList<String>();
		for (String fav : busFavorites) {
			String[] decoded = Util.decodeBusFavorite(fav);
			if (!found.contains(decoded[0])) {
				found.add(decoded[0]);
				favs.add(fav);
			}
		}
		return favs;
	}
}
