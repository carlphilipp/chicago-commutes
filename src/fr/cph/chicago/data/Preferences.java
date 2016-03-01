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
import android.content.SharedPreferences;
import android.util.Log;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that store user preferences into phoneO
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Preferences {

	/**
	 * Tag
	 **/
	private static final String TAG = Preferences.class.getSimpleName();

	/**
	 * Check if the user has favorites already
	 *
	 * @param trains the trains preference string
	 * @param bus    the bus preference string
	 * @return a boolean
	 */
	public static boolean hasFavorites(final String trains, final String bus, final String bike) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final Set<String> setPref1 = sharedPref.getStringSet(trains, null);
		final Set<String> setPref2 = sharedPref.getStringSet(bus, null);
		final Set<String> setPref3 = sharedPref.getStringSet(bike, null);
		boolean res = true;
		if ((setPref1 == null || setPref1.size() == 0) && (setPref2 == null || setPref2.size() == 0) && (setPref3 == null || setPref3.size() == 0)) {
			res = false;
		}
		return res;
	}

	public static void saveBikeFavorites(final String name, final List<String> favorites) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		final Set<String> set = new LinkedHashSet<>();
		for (final String fav : favorites) {
			set.add(fav);
		}
		Log.v(TAG, "Put bike favorites: " + set.toString());
		editor.putStringSet(name, set);
		editor.apply();
	}

	public static List<String> getBikeFavorites(final String name) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final Set<String> setPref = sharedPref.getStringSet(name, null);
		final List<String> favorites = new ArrayList<>();
		if (setPref != null) {
			for (final String value : setPref) {
				favorites.add(value);
			}
		}
		Collections.sort(favorites);
		Log.v(TAG, "Read bike favorites : " + favorites);
		return favorites;
	}

	public static void addBikeRouteNameMapping(final String bikeId, final String bikeName) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context
				.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(bikeId, bikeName);
		Log.v(TAG, "Add bike name mapping : " + bikeId + " => " + bikeName);
		editor.apply();
	}

	public static String getBikeRouteNameMapping(final String bikeId) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context
				.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, Context.MODE_PRIVATE);
		final String bikeName = sharedPref.getString(bikeId, null);
		Log.v(TAG, "Get bike name mapping : " + bikeId + " => " + bikeName);
		return bikeName;
	}

	/**
	 * Save bus into favorites
	 *
	 * @param name      the name of the bus preference string
	 * @param favorites the list of favorites to save
	 */
	public static void saveBusFavorites(final String name, final List<String> favorites) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		final Set<String> set = new LinkedHashSet<>();
		for (final String fav : favorites) {
			set.add(fav);
		}
		Log.v(TAG, "Put bus favorites: " + favorites.toString());
		editor.putStringSet(name, set);
		editor.apply();
	}

	/**
	 * Get favorites bus
	 *
	 * @param name the name of the bus preference string
	 * @return a list of favorites bus
	 */
	public static List<String> getBusFavorites(final String name) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final Set<String> setPref = sharedPref.getStringSet(name, null);
		final List<String> favorites = new ArrayList<>();
		if (setPref != null) {
			for (final String value : setPref) {
				favorites.add(value);
			}
		}
		Collections.sort(favorites, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				final String str1Decoded = Util.decodeBusFavorite(str1)[0];
				final String str2Decoded = Util.decodeBusFavorite(str2)[0];
				Integer int1;
				Integer int2;
				// TODO Refactor that delete exception
				try {
					int1 = Integer.valueOf(str1Decoded);
				} catch (NumberFormatException e) {
					int1 = Integer.valueOf(str1Decoded.substring(0, str1Decoded.length() - 1));
				}
				try {
					int2 = Integer.valueOf(str2Decoded);
				} catch (NumberFormatException e) {
					int2 = Integer.valueOf(str2Decoded.substring(0, str2Decoded.length() - 1));
				}
				return int1.compareTo(int2);
			}
		});
		Log.v(TAG, "Read bus favorites : " + favorites.toString());
		return favorites;
	}

	public static void addBusRouteNameMapping(final String busStopId, final String routeName) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(busStopId, routeName);
		Log.v(TAG, "Add bus route name mapping : " + busStopId + " => " + routeName);
		editor.apply();
	}

	public static String getBusRouteNameMapping(final String busStopId) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, Context.MODE_PRIVATE);
		final String routeName = sharedPref.getString(busStopId, null);
		Log.v(TAG, "Get bus route name mapping : " + busStopId + " => " + routeName);
		return routeName;
	}

	public static void addBusStopNameMapping(final String busStopId, final String stopName) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(busStopId, stopName);
		Log.v(TAG, "Add bus stop name mapping : " + busStopId + " => " + stopName);
		editor.apply();
	}

	public static String getBusStopNameMapping(final String busStopId) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context
				.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, Context.MODE_PRIVATE);
		final String stopName = sharedPref.getString(busStopId, null);
		Log.v(TAG, "Get bus stop name mapping : " + busStopId + " => " + stopName);
		return stopName;
	}

	/**
	 * Save train favorites
	 *
	 * @param name      the name of the train preference string
	 * @param favorites the favorites
	 */
	public static void saveTrainFavorites(final String name, final List<Integer> favorites) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		final Set<String> set = new LinkedHashSet<>();
		for (final Integer favorite : favorites) {
			set.add(favorite.toString());
		}
		Log.v(TAG, "Put train favorites: " + favorites.toString());
		editor.putStringSet(name, set);
		editor.apply();
	}

	/**
	 * Get train favorites
	 *
	 * @param name the name of the train preference string
	 * @return the favorites
	 */
	public static List<Integer> getTrainFavorites(final String name) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final Set<String> setPref = sharedPref.getStringSet(name, null);
		final List<Integer> favorites = new ArrayList<>();
		if (setPref != null) {
			for (final String value : setPref) {
				favorites.add(Integer.valueOf(value));
			}
		}
		final DataHolder dataHolder = DataHolder.getInstance();
		final List<Station> stations = new ArrayList<>();
		for (final Integer favorite : favorites) {
			final Station station = dataHolder.getTrainData().getStation(favorite);
			stations.add(station);
		}
		Collections.sort(stations);
		final List<Integer> res = new ArrayList<>();
		for (final Station station : stations) {
			res.add(station.getId());
		}
		Log.v(TAG, "Read train favorites : " + res.toString());
		return res;
	}

	/**
	 * Save train filter
	 *
	 * @param stationId the station id
	 * @param line      the line
	 * @param direction the direction
	 * @param value     the value
	 */
	public static void saveTrainFilter(final Integer stationId, final TrainLine line, final TrainDirection direction, final boolean value) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, value);
		editor.apply();
	}

	/**
	 * Get train filter
	 *
	 * @param stationId the station id
	 * @param line      the line
	 * @param direction the direction
	 * @return if a train is filtered
	 */
	public static boolean getTrainFilter(final Integer stationId, final TrainLine line, final TrainDirection direction) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, true);
	}

	public static void saveHideShowNearby(boolean hide) {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean("hideNearby", hide);
		editor.apply();
	}

	public static boolean getHideShowNearby() {
		final Context context = ChicagoTracker.getContext();
		final SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		return sharedPref.getBoolean("hideNearby", true);
	}
}
