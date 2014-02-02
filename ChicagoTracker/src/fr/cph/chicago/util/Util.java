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

package fr.cph.chicago.util;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.data.Preferences;

public class Util {
	/** **/
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}. This value will not collide with ID values generated at build time by aapt for R.id.
	 * 
	 * @return a generated ID value
	 */
	public static int generateViewId() {
		for (;;) {
			final int result = sNextGeneratedId.get();
			// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
			int newValue = result + 1;
			if (newValue > 0x00FFFFFF)
				newValue = 1; // Roll over to 1, not 0.
			if (sNextGeneratedId.compareAndSet(result, newValue)) {
				return result;
			}
		}
	}

	/**
	 * 
	 * @param property
	 * @return
	 */
	public static String getProperty(String property) {
		Properties prop = new Properties();
		try {
			prop.load(ChicagoTracker.getAppContext().getAssets().open("app.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return prop.getProperty(property, null);
	}

	/**
	 * 
	 * @param stationId
	 * @param preference
	 */
	public static void addToTrainFavorites(Integer stationId, String preference) {
		List<Integer> favorites = Preferences.getTrainFavorites(preference);
		if (!favorites.contains(stationId)) {
			favorites.add(stationId);
			Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param stationId
	 * @param preference
	 */
	public static void removeFromTrainFavorites(Integer stationId, String preference) {
		List<Integer> favorites = Preferences.getTrainFavorites(preference);
		favorites.remove(stationId);
		Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param busRouteId
	 * @param busStopId
	 * @param bound
	 * @param preference
	 */
	public static void removeFromBusFavorites(String busRouteId, String busStopId, String bound, String preference) {
		String id = busRouteId + "_" + busStopId + "_" + bound;
		List<String> favorites = Preferences.getBusFavorites(preference);
		favorites.remove(id);
		Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param busRouteId
	 * @param busStopId
	 * @param bound
	 * @param preference
	 */
	public static void addToBusFavorites(String busRouteId, String busStopId, String bound, String preference) {
		String id = busRouteId + "_" + busStopId + "_" + bound;
		List<String> favorites = Preferences.getBusFavorites(preference);
		if (!favorites.contains(id)) {
			favorites.add(id);
			Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param fav
	 * @return
	 */
	public static String[] decodeBusFavorite(String fav) {
		String[] res = new String[3];
		int first = fav.indexOf('_');
		String routeId = fav.substring(0, first);
		int sec = fav.indexOf('_', first + 1);
		String stopId = fav.substring(first + 1, sec);
		String bound = fav.substring(sec + 1, fav.length());
		res[0] = routeId;
		res[1] = stopId;
		res[2] = bound;
		return res;
	}
}
