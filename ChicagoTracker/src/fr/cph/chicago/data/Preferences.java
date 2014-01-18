package fr.cph.chicago.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

public final class Preferences {

	/** Tag **/
	private static final String TAG = "Preferences";

	public static void saveFavorites(String name, List<Integer> favorites) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Set<String> set = new LinkedHashSet<String>();
		for (Integer favorite : favorites) {
			set.add(favorite.toString());
		}
		Log.v(TAG, "Put favorites: " + favorites.toString());
		editor.putStringSet(name, set);
		editor.commit();
	}

	public static List<Integer> getFavorites(String name) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		Set<String> setPref = sharedPref.getStringSet(name, null);
		List<Integer> favorites = new ArrayList<Integer>();
		if (setPref != null) {
			Iterator<String> it = setPref.iterator();
			while (it.hasNext()) {
				String value = it.next();
				favorites.add(Integer.valueOf(value));
			}
		}
		DataHolder dataHolder = DataHolder.getInstance();
		List<Station> stations = new ArrayList<Station>();
		for (Integer favorite : favorites) {
			Station station = dataHolder.getTrainData().getStation(favorite);
			stations.add(station);
		}
		Collections.sort(stations);
		List<Integer> res = new ArrayList<Integer>();
		for (Station station : stations) {
			res.add(station.getId());
		}
		Log.v(TAG, "Read favorites : " + res.toString());
		return res;
	}

	public static void saveFilter(Integer stationId, TrainLine line, TrainDirection direction, boolean value) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, value);
		editor.commit();
	}

	public static boolean getFilter(Integer stationId, TrainLine line, TrainDirection direction) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		boolean result = sharedPref.getBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, true);
		return result;
	}
}
