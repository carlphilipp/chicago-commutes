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

package fr.cph.chicago.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.GoogleMapAbility;
import fr.cph.chicago.task.GlobalConnectTask;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Util {

	private static final String TAG = Util.class.getSimpleName();

	public static final LatLng CHICAGO = new LatLng(41.8819, -87.6278);

	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	public static int generateViewId() {
		for (; ; ) {
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
	 * @param property the property to get
	 * @return the value of the property
	 */
	public static String getProperty(final String property) {
		final Properties prop = new Properties();
		try {
			prop.load(ChicagoTracker.getContext().getAssets().open("app.properties"));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
		return prop.getProperty(property, null);
	}

	/**
	 * Add to train favorites
	 *
	 * @param stationId  the station id
	 * @param preference the preference
	 */
	public static void addToTrainFavorites(final Integer stationId, final String preference) {
		final List<Integer> favorites = Preferences.getTrainFavorites(preference);
		if (!favorites.contains(stationId)) {
			favorites.add(stationId);
			Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		}
		Toast.makeText(ChicagoTracker.getContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove train from favorites
	 *
	 * @param stationId  the station id
	 * @param preference the preference
	 */
	public static void removeFromTrainFavorites(final Integer stationId, final String preference) {
		final List<Integer> favorites = Preferences.getTrainFavorites(preference);
		favorites.remove(stationId);
		Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		Toast.makeText(ChicagoTracker.getContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove from bus favorites
	 *
	 * @param busRouteId the bus route id
	 * @param busStopId  the bus stop id
	 * @param bound      the bus bound
	 * @param preference the preference
	 */
	public static void removeFromBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		final String id = busRouteId + "_" + busStopId + "_" + bound;
		final List<String> favorites = Preferences.getBusFavorites(preference);
		favorites.remove(id);
		Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		Toast.makeText(ChicagoTracker.getContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Add to bus favorites
	 *
	 * @param busRouteId the bus route id
	 * @param busStopId  the bus stop id
	 * @param bound      the bus bound
	 * @param preference the preference
	 */
	public static void addToBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		final String id = busRouteId + "_" + busStopId + "_" + bound;
		final List<String> favorites = Preferences.getBusFavorites(preference);
		if (!favorites.contains(id)) {
			favorites.add(id);
			Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		}
		Toast.makeText(ChicagoTracker.getContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static void addToBikeFavorites(final int stationId, final String preference) {
		final List<String> favorites = Preferences.getBikeFavorites(preference);
		if (!favorites.contains(String.valueOf(stationId))) {
			favorites.add(String.valueOf(stationId));
			Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		}
		Toast.makeText(ChicagoTracker.getContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static void removeFromBikeFavorites(final int stationId, final String preference) {
		final List<String> favorites = Preferences.getBikeFavorites(preference);
		favorites.remove(String.valueOf(stationId));
		Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		Toast.makeText(ChicagoTracker.getContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Decode bus favorites
	 *
	 * @param fav the favorites
	 * @return a tab containing the route id, the stop id and the bound
	 */
	public static String[] decodeBusFavorite(final String fav) {
		final int first = fav.indexOf('_');
		final String routeId = fav.substring(0, first);
		final int sec = fav.indexOf('_', first + 1);
		final String stopId = fav.substring(first + 1, sec);
		final String bound = fav.substring(sec + 1, fav.length());
		return new String[] { routeId, stopId, bound };
	}

	public static final Comparator<BikeStation> BIKE_COMPARATOR_NAME = new BikeStationComparator();

	private static final class BikeStationComparator implements Comparator<BikeStation> {
		@Override
		public int compare(final BikeStation station1, final BikeStation station2) {
			return station1.getName().compareTo(station2.getName());
		}
	}

	public static boolean isNetworkAvailable() {
		final ConnectivityManager connectivityManager = (ConnectivityManager) ChicagoTracker.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static int[] getScreenSize() {
		final WindowManager wm = (WindowManager) ChicagoTracker.getContext().getSystemService(Context.WINDOW_SERVICE);
		final Display display = wm.getDefaultDisplay();
		final Point size = new Point();
		display.getSize(size);
		return new int[] { size.x, size.y };
	}

	/**
	 * Google analytics track screen
	 *
	 * @param screen the screen name
	 */
	public static void trackScreen(final String screen) {
		final Tracker t = ChicagoTracker.getTracker();
		t.setScreenName(screen);
		t.send(new HitBuilders.ScreenViewBuilder().build());
	}

	public static void trackAction(final Activity activity, final int category, final int action, final int label, final int value) {
		final Tracker tracker = ChicagoTracker.getTracker();
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory(activity.getString(category))
				.setAction(activity.getString(action))
				.setLabel(activity.getString(label))
				.setValue(value).build());
	}

	public static void setToolbarColor(final Activity activity, final Toolbar toolbar, final TrainLine trainLine) {
		int backgroundColor = 0;
		int statusBarColor = 0;
		int textTitleColor = R.color.white;
		switch (trainLine) {
		case BLUE:
			backgroundColor = R.color.blueLine;
			statusBarColor = R.color.blueLineDark;
			break;
		case BROWN:
			backgroundColor = R.color.brownLine;
			statusBarColor = R.color.brownLineDark;
			break;
		case GREEN:
			backgroundColor = R.color.greenLine;
			statusBarColor = R.color.greenLineDark;
			break;
		case ORANGE:
			backgroundColor = R.color.orangeLine;
			statusBarColor = R.color.orangeLineDark;
			break;
		case PINK:
			backgroundColor = R.color.pinkLine;
			statusBarColor = R.color.pinkLineDark;
			break;
		case PURPLE:
			backgroundColor = R.color.purpleLine;
			statusBarColor = R.color.purpleLineDark;
			break;
		case RED:
			backgroundColor = R.color.redLine;
			statusBarColor = R.color.redLineDark;
			break;
		case YELLOW:
			backgroundColor = R.color.yellowLine;
			statusBarColor = R.color.yellowLineDark;
			break;
		case NA:
			backgroundColor = R.color.primaryColor;
			statusBarColor = R.color.primaryColorDark;
			break;
		}
		toolbar.setBackgroundColor(ContextCompat.getColor(ChicagoTracker.getContext(), backgroundColor));
		toolbar.setTitleTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), textTitleColor));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, statusBarColor));
		}
	}

	public static int getRandomColor() {
		final Random random = new Random();
		final List<TrainLine> keys = Collections.unmodifiableList(Arrays.asList(TrainLine.values()));
		return keys.get(random.nextInt(keys.size())).getColor();
	}

	public static void centerMap(final GoogleMapAbility googleFragment, final SupportMapFragment mapFragment, final Activity activity, final Position position) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				googleFragment.setGoogleMap(googleMap);
				if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(activity,
							new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
					return;
				}
				googleMap.setMyLocationEnabled(true);
				if (position != null) {
					final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
				} else {
					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CHICAGO, 10));
				}
			}
		});
	}

	public static boolean textNumberToBoolean(@NonNull final String number) {
		return Boolean.parseBoolean(number);
	}

	public static void loadFavorites(final Object instance, final Class<?> classe, final Activity activity){
		final MultiValuedMap<String, String> paramTrain = Util.getFavoritesTrainParams(activity);
		final MultiValuedMap<String, String> paramBus = Util.getFavoritesBusParams(activity);
		final GlobalConnectTask task = new GlobalConnectTask(instance, classe, TRAIN_ARRIVALS, paramTrain, BUS_ARRIVALS, paramBus);
		task.execute((Void) null);
	}

	private static MultiValuedMap<String, String> getFavoritesTrainParams(final Activity activity) {
		final MultiValuedMap<String, String> paramsTrain = new ArrayListValuedHashMap<>();
		final List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		for (final Integer favorite : favorites) {
			paramsTrain.put(activity.getString(R.string.request_map_id), favorite.toString());
		}
		return paramsTrain;
	}

	private static MultiValuedMap<String, String> getFavoritesBusParams(final Activity activity) {
		final MultiValuedMap<String, String> paramsBus = new ArrayListValuedHashMap<>();
		final List<String> busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		for (final String busFavorite : busFavorites) {
			final String[] fav = Util.decodeBusFavorite(busFavorite);
			paramsBus.put(activity.getString(R.string.request_rt), fav[0]);
			paramsBus.put(activity.getString(R.string.request_stop_id), fav[1]);
		}
		return paramsBus;
	}

}
