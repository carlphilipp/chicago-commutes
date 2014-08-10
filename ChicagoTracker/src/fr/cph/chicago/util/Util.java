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
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;

/**
 * Util class
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Util {
	/** **/
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}. This value will not collide with ID values generated at build time by aapt for R.id.
	 * 
	 * @return a generated ID value
	 */
	public static final int generateViewId() {
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
	 * Get property from file
	 * 
	 * @param property
	 *            the property to get
	 * @return the value of the property
	 */
	public static final String getProperty(final String property) {
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
	 * Add to train favorites
	 * 
	 * @param stationId
	 *            the station id
	 * @param preference
	 *            the preference
	 */
	public static final void addToTrainFavorites(final Integer stationId, final String preference) {
		List<Integer> favorites = Preferences.getTrainFavorites(preference);
		if (!favorites.contains(stationId)) {
			favorites.add(stationId);
			Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove train from favorites
	 * 
	 * @param stationId
	 *            the station id
	 * @param preference
	 *            the preference
	 */
	public static final void removeFromTrainFavorites(final Integer stationId, final String preference) {
		List<Integer> favorites = Preferences.getTrainFavorites(preference);
		favorites.remove(stationId);
		Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove from bus favorites
	 * 
	 * @param busRouteId
	 *            the bus route id
	 * @param busStopId
	 *            the bus stop id
	 * @param bound
	 *            the bus bound
	 * @param preference
	 *            the preference
	 */
	public static final void removeFromBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		String id = busRouteId + "_" + busStopId + "_" + bound;
		List<String> favorites = Preferences.getBusFavorites(preference);
		favorites.remove(id);
		Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Add to bus favorites
	 * 
	 * @param busRouteId
	 *            the bus route id
	 * @param busStopId
	 *            the bus stop id
	 * @param bound
	 *            the bus bound
	 * @param preference
	 *            the preference
	 */
	public static final void addToBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		String id = busRouteId + "_" + busStopId + "_" + bound;
		List<String> favorites = Preferences.getBusFavorites(preference);
		if (!favorites.contains(id)) {
			favorites.add(id);
			Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static final void addToBikeFavorites(final int stationId, final String preference) {
		List<String> favorites = Preferences.getBikeFavorites(preference);
		if (!favorites.contains(String.valueOf(stationId))) {
			favorites.add(String.valueOf(stationId));
			Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static final void removeFromBikeFavorites(final int stationId, final String preference) {
		List<String> favorites = Preferences.getBikeFavorites(preference);
		favorites.remove(String.valueOf(stationId));
		Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Decode bus favorites
	 * 
	 * @param fav
	 *            the favorites
	 * @return a tab containing the route id, the stop id and the bound
	 */
	public static final String[] decodeBusFavorite(final String fav) {
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

	public static final Comparator<BikeStation> BIKE_COMPARATOR_NAME = new BikeStationComparator();

	private static final class BikeStationComparator implements Comparator<BikeStation> {
		@Override
		public int compare(BikeStation station1, BikeStation station2) {
			return station1.getName().compareTo(station2.getName());
		}

	}

	public static final boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) ChicagoTracker.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static final int[] getScreenSize() {
		WindowManager wm = (WindowManager) ChicagoTracker.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return new int[] { size.x, size.y };
	}

	/**
	 * Google analytics track screen
	 * 
	 * @param activity
	 *            the activity
	 * @param str
	 *            the label to send
	 */
	public static void trackScreen(Activity activity, int str) {
		Tracker t = ((ChicagoTracker) activity.getApplication()).getTracker();
		t.setScreenName(activity.getString(str));
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	public static void trackAction(Activity activity, int category, int action, int label, int value) {
		Tracker tracker = ((ChicagoTracker) activity.getApplication()).getTracker();
		tracker.send(new HitBuilders.EventBuilder().setCategory(activity.getString(category)).setAction(activity.getString(action))
				.setLabel(activity.getString(label)).setValue(value).build());
	}
}
