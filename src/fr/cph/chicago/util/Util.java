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

package fr.cph.chicago.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Util {

    public static final LatLng CHICAGO = new LatLng(41.8819, -87.6278);

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private Util() {
    }

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
     * Add to train favorites
     *
     * @param stationId  the station id
     * @param preference the preference
     */
    public static void addToTrainFavorites(@NonNull final Integer stationId, @NonNull final View view) {
        final List<Integer> favorites = Preferences.getTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN);
        if (!favorites.contains(stationId)) {
            favorites.add(stationId);
            Preferences.saveTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    /**
     * Remove train from favorites
     *
     * @param stationId  the station id
     * @param preference the preference
     */
    public static void removeFromTrainFavorites(@NonNull final Integer stationId, @NonNull final View view) {
        final List<Integer> favorites = Preferences.getTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN);
        favorites.remove(stationId);
        Preferences.saveTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Remove from bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param preference the preference
     */
    public static void removeFromBusFavorites(@NonNull final String busRouteId, @NonNull final String busStopId, @NonNull final String bound,
                                              @NonNull final View view) {
        final String id = busRouteId + "_" + busStopId + "_" + bound;
        final List<String> favorites = Preferences.getBusFavorites(App.PREFERENCE_FAVORITES_BUS);
        favorites.remove(id);
        Preferences.saveBusFavorites(App.PREFERENCE_FAVORITES_BUS, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Add to bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param preference the preference
     */
    public static void addToBusFavorites(@NonNull final String busRouteId, @NonNull final String busStopId, @NonNull final String bound, @NonNull final View view) {
        final String id = busRouteId + "_" + busStopId + "_" + bound;
        final List<String> favorites = Preferences.getBusFavorites(App.PREFERENCE_FAVORITES_BUS);
        if (!favorites.contains(id)) {
            favorites.add(id);
            Preferences.saveBusFavorites(App.PREFERENCE_FAVORITES_BUS, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    public static void addToBikeFavorites(final int stationId, @NonNull final View view) {
        final List<String> favorites = Preferences.getBikeFavorites(App.PREFERENCE_FAVORITES_BIKE);
        if (!favorites.contains(Integer.toString(stationId))) {
            favorites.add(Integer.toString(stationId));
            Preferences.saveBikeFavorites(App.PREFERENCE_FAVORITES_BIKE, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    public static void removeFromBikeFavorites(final int stationId, @NonNull final View view) {
        final List<String> favorites = Preferences.getBikeFavorites(App.PREFERENCE_FAVORITES_BIKE);
        favorites.remove(Integer.toString(stationId));
        Preferences.saveBikeFavorites(App.PREFERENCE_FAVORITES_BIKE, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Decode bus favorites
     *
     * @param favorite the favorites
     * @return a tab containing the route id, the stop id and the bound
     */
    @NonNull
    public static String[] decodeBusFavorite(@NonNull final String favorite) {
        final int first = favorite.indexOf('_');
        final String routeId = favorite.substring(0, first);
        final int sec = favorite.indexOf('_', first + 1);
        final String stopId = favorite.substring(first + 1, sec);
        final String bound = favorite.substring(sec + 1, favorite.length());
        return new String[]{routeId, stopId, bound};
    }

    public static final Comparator<BikeStation> BIKE_COMPARATOR_NAME = new BikeStationComparator();

    private static final class BikeStationComparator implements Comparator<BikeStation> {
        @Override
        public int compare(final BikeStation station1, final BikeStation station2) {
            return station1.getName().compareTo(station2.getName());
        }
    }

    public static boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int[] getScreenSize() {
        final WindowManager wm = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return new int[]{size.x, size.y};
    }

    /**
     * Google analytics track screen
     *
     * @param screen the screen name
     */
    public static void trackScreen(final String screen) {
        final Tracker t = App.getTracker();
        t.setScreenName(screen);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void trackAction(@NonNull final Activity activity, final int category, final int action, final int label, final int value) {
        final Tracker tracker = App.getTracker();
        tracker.send(new HitBuilders.EventBuilder()
            .setCategory(activity.getString(category))
            .setAction(activity.getString(action))
            .setLabel(activity.getString(label))
            .setValue(value).build());
    }

    public static void setWindowsColor(@NonNull final Activity activity, @NonNull final Toolbar toolbar, @NonNull final TrainLine trainLine) {
        int backgroundColor = 0;
        int statusBarColor = 0;
        //int navigationBarColor = 0;
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
                statusBarColor = R.color.orangeLineDarker;
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
        toolbar.setBackgroundColor(ContextCompat.getColor(App.getContext(), backgroundColor));
        toolbar.setTitleTextColor(ContextCompat.getColor(App.getContext(), textTitleColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, statusBarColor));
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.primaryColorDarker));
        }
    }

    public static int getRandomColor() {
        final Random random = new Random();
        final List<TrainLine> keys = Collections.unmodifiableList(Arrays.asList(TrainLine.values()));
        return keys.get(random.nextInt(keys.size())).getColor();
    }

    public static void centerMap(@NonNull final SupportMapFragment mapFragment, @NonNull final Activity activity, @Nullable final Position position) {
        mapFragment.getMapAsync(googleMap -> {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }
            googleMap.setMyLocationEnabled(true);
            if (position != null) {
                final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CHICAGO, 10));
            }
        });
    }

    public static boolean textNumberToBoolean(@NonNull final String number) {
        return Boolean.parseBoolean(number);
    }

    @NonNull
    public static MultiValuedMap<String, String> getFavoritesTrainParams() {
        final MultiValuedMap<String, String> paramsTrain = new ArrayListValuedHashMap<>();
        final List<Integer> favorites = Preferences.getTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN);
        Stream.of(favorites).forEach(favorite -> paramsTrain.put(App.getContext().getString(R.string.request_map_id), favorite.toString()));
        return paramsTrain;
    }

    @NonNull
    public static MultiValuedMap<String, String> getFavoritesBusParams() {
        final MultiValuedMap<String, String> paramsBus = new ArrayListValuedHashMap<>();
        final List<String> busFavorites = Preferences.getBusFavorites(App.PREFERENCE_FAVORITES_BUS);
        Stream.of(busFavorites)
            .map(Util::decodeBusFavorite)
            .forEach(fav -> {
                paramsBus.put(App.getContext().getString(R.string.request_rt), fav[0]);
                paramsBus.put(App.getContext().getString(R.string.request_stop_id), fav[1]);
            });
        return paramsBus;
    }

    /**
     * Function to show settings alert dialog
     */
    public static void showSettingsAlert(@NonNull final Activity activity) {
        new Thread() {
            public void run() {
                activity.runOnUiThread(() -> {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle("GPS settings");
                    alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                    alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", (dialog, id) -> {
                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(intent);
                    }).setNegativeButton("No", (dialog, id) -> dialog.cancel());
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                });
            }
        }.start();
    }

    public static int convertDpToPixel(final Activity activity, final int dp) {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
        return (int) pixels;
    }

    public static void showNetworkErrorMessage(@NonNull final Activity activity) {
        showSnackBar(activity, R.string.message_network_error);
    }

    public static void showNetworkErrorMessage(@NonNull final View view) {
        showSnackBar(view, R.string.message_network_error);
    }

    public static void showMessage(final Activity activity, final int message) {
        showSnackBar(activity, message);
    }

    private static void showSnackBar(@NonNull final Activity activity, final int message) {
        if (activity.getCurrentFocus() != null) {
            Snackbar.make(activity.getCurrentFocus(), activity.getString(message), Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, activity.getString(message), Toast.LENGTH_LONG).show();
        }
    }

    private static void showSnackBar(@NonNull final View view, final int message) {
        Snackbar.make(view, view.getContext().getString(message), Snackbar.LENGTH_SHORT).show();
    }

    public static void showOopsSomethingWentWrong(@NonNull final View view) {
        Snackbar.make(view, view.getContext().getString(R.string.message_something_went_wrong), Snackbar.LENGTH_SHORT).show();
    }

    @NonNull
    public static String trimBusStopNameIfNeeded(@NonNull final String name) {
        if (name.length() > 25) {
            return name.substring(0, 24).trim() + "...";
        } else {
            return name;
        }
    }

    public static void setLocationOnMap(@NonNull final Activity activity, @NonNull final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }
}
