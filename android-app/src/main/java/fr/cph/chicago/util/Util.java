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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.dto.BusFavoriteDTO;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import lombok.SneakyThrows;

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum Util {
    ;

    public static final Comparator<BikeStation> BIKE_COMPARATOR_NAME = new BikeStationComparator();
    public static final Comparator<BusRoute> BUS_STOP_COMPARATOR_NAME = new BusStopComparator();

    public static final LatLng CHICAGO = new LatLng(41.8819, -87.6278);
    private static final Pattern PATTERN = Pattern.compile("(\\d{1,3})");
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

    public static boolean isAtLeastTwoErrors(final boolean isTrainError, final boolean isBusError, final boolean isBikeError) {
        return (isTrainError && (isBusError || isBikeError)) || (isBusError && isBikeError);
    }

    /**
     * Add to train favorites
     *
     * @param stationId the station id
     * @param view      the view
     */
    public static void addToTrainFavorites(@NonNull final Integer stationId, @NonNull final View view) {
        final List<Integer> favorites = PreferencesImpl.INSTANCE.getTrainFavorites(view.getContext(), App.PREFERENCE_FAVORITES_TRAIN);
        if (!favorites.contains(stationId)) {
            favorites.add(stationId);
            PreferencesImpl.INSTANCE.saveTrainFavorites(view.getContext(), App.PREFERENCE_FAVORITES_TRAIN, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    /**
     * Remove train from favorites
     *
     * @param stationId the station id
     * @param view      the view
     */
    public static void removeFromTrainFavorites(@NonNull final Integer stationId, @NonNull final View view) {
        final List<Integer> favorites = PreferencesImpl.INSTANCE.getTrainFavorites(view.getContext(), App.PREFERENCE_FAVORITES_TRAIN);
        favorites.remove(stationId);
        PreferencesImpl.INSTANCE.saveTrainFavorites(view.getContext(), App.PREFERENCE_FAVORITES_TRAIN, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Remove from bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param view       the view
     */
    public static void removeFromBusFavorites(@NonNull final String busRouteId, @NonNull final String busStopId, @NonNull final String bound,
                                              @NonNull final View view) {
        final String id = busRouteId + "_" + busStopId + "_" + bound;
        final List<String> favorites = PreferencesImpl.INSTANCE.getBusFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BUS);
        favorites.remove(id);
        PreferencesImpl.INSTANCE.saveBusFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BUS, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Add to bus favorites
     *
     * @param busRouteId the bus route id
     * @param busStopId  the bus stop id
     * @param bound      the bus bound
     * @param view       the view
     */
    public static void addToBusFavorites(@NonNull final String busRouteId, @NonNull final String busStopId, @NonNull final String bound, @NonNull final View view) {
        final String id = busRouteId + "_" + busStopId + "_" + bound;
        final List<String> favorites = PreferencesImpl.INSTANCE.getBusFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BUS);
        if (!favorites.contains(id)) {
            favorites.add(id);
            PreferencesImpl.INSTANCE.saveBusFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BUS, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    public static void addToBikeFavorites(final int stationId, @NonNull final View view) {
        final List<String> favorites = PreferencesImpl.INSTANCE.getBikeFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BIKE);
        if (!favorites.contains(Integer.toString(stationId))) {
            favorites.add(Integer.toString(stationId));
            PreferencesImpl.INSTANCE.saveBikeFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BIKE, favorites);
            showSnackBar(view, R.string.message_add_fav);
        }
    }

    public static void removeFromBikeFavorites(final int stationId, @NonNull final View view) {
        final List<String> favorites = PreferencesImpl.INSTANCE.getBikeFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BIKE);
        favorites.remove(Integer.toString(stationId));
        PreferencesImpl.INSTANCE.saveBikeFavorites(view.getContext(), App.PREFERENCE_FAVORITES_BIKE, favorites);
        showSnackBar(view, R.string.message_remove_fav);
    }

    /**
     * Decode bus favorites
     *
     * @param favorite the favorites
     * @return a tab containing the route id, the stop id and the bound
     */
    @NonNull
    public static BusFavoriteDTO decodeBusFavorite(@NonNull final String favorite) {
        final int first = favorite.indexOf('_');
        final String routeId = favorite.substring(0, first);
        final int sec = favorite.indexOf('_', first + 1);
        final String stopId = favorite.substring(first + 1, sec);
        final String bound = favorite.substring(sec + 1, favorite.length());
        return BusFavoriteDTO.builder().routeId(routeId).stopId(stopId).bound(bound).build();
    }

    private static final class BikeStationComparator implements Comparator<BikeStation> {
        @Override
        public int compare(final BikeStation station1, final BikeStation station2) {
            return station1.getName().compareTo(station2.getName());
        }
    }

    private static final class BusStopComparator implements Comparator<BusRoute> {

        @Override
        public int compare(final BusRoute route1, final BusRoute route2) {
            final Matcher matcher1 = PATTERN.matcher(route1.getId());
            final Matcher matcher2 = PATTERN.matcher(route2.getId());
            if (matcher1.find() && matcher2.find()) {
                final int one = Integer.parseInt(matcher1.group(1));
                final int two = Integer.parseInt(matcher2.group(1));
                return one < two ? -1 : (one == two ? 0 : 1);
            } else {
                return route1.getId().compareTo(route2.getId());
            }
        }
    }

    public static boolean isNetworkAvailable(@NonNull final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int[] getScreenSize(@NonNull final Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
    public static void trackScreen(final Context context, final String screen) {
        final Tracker t = App.getTracker(context);
        t.setScreenName(screen);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void trackAction(@NonNull final Context context, final int category, final int action, final String label, final int value) {
        new Thread(() -> {
            final Tracker tracker = App.getTracker(context.getApplicationContext());
            tracker.send(new HitBuilders.EventBuilder()
                .setCategory(context.getString(category))
                .setAction(context.getString(action))
                .setLabel(label)
                .setValue(value).build());
        }).start();
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
        toolbar.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), backgroundColor));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity.getApplicationContext(), textTitleColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, statusBarColor));
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.primaryColorDarker));
        }
    }

    public static int getRandomColor() {
        final Random random = new Random();
        final List<TrainLine> keys = Arrays.asList(TrainLine.values());
        return keys.get(random.nextInt(keys.size())).getColor();
    }

    public static void centerMap(@NonNull final SupportMapFragment mapFragment, @NonNull final Optional<Position> position) throws SecurityException {
        mapFragment.getMapAsync(googleMap -> {
            googleMap.setMyLocationEnabled(true);
            if (position.isPresent()) {
                final LatLng latLng = new LatLng(position.get().getLatitude(), position.get().getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CHICAGO, 10));
            }
        });
    }

    @NonNull
    public static MultiValuedMap<String, String> getFavoritesTrainParams(@NonNull final Context context) {
        final MultiValuedMap<String, String> paramsTrain = new ArrayListValuedHashMap<>();
        final List<Integer> favorites = PreferencesImpl.INSTANCE.getTrainFavorites(context, App.PREFERENCE_FAVORITES_TRAIN);
        Stream.of(favorites).forEach(favorite -> paramsTrain.put(context.getString(R.string.request_map_id), favorite.toString()));
        return paramsTrain;
    }

    @NonNull
    public static MultiValuedMap<String, String> getFavoritesBusParams(@NonNull final Context context) {
        final MultiValuedMap<String, String> paramsBus = new ArrayListValuedHashMap<>();
        final List<String> busFavorites = PreferencesImpl.INSTANCE.getBusFavorites(context, App.PREFERENCE_FAVORITES_BUS);
        Stream.of(busFavorites)
            .map(Util::decodeBusFavorite)
            .forEach(busFavoriteDTO -> {
                paramsBus.put(context.getString(R.string.request_rt), busFavoriteDTO.getRouteId());
                paramsBus.put(context.getString(R.string.request_stop_id), busFavoriteDTO.getStopId());
            });
        return paramsBus;
    }

    /**
     * Function to show settings alert dialog
     */
    static void showSettingsAlert(@NonNull final Activity activity) {
        new Thread() {
            public void run() {
                activity.runOnUiThread(() -> {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle("GPS settings");
                    alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings main.java.fr.cph.chicago.res.menu?");
                    alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(intent);
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                });
            }
        }.start();
    }

    public static int convertDpToPixel(@NonNull final Context context, final int dp) {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return (int) pixels;
    }

    public static void showNetworkErrorMessage(@NonNull final Activity activity) {
        showSnackBar(activity, R.string.message_network_error);
    }

    public static void showNetworkErrorMessage(@NonNull final View view) {
        showSnackBar(view, R.string.message_network_error);
    }

    public static void showMessage(@NonNull final Activity activity, final int message) {
        showSnackBar(activity, message);
    }

    public static void showMessage(@NonNull final View view, final int message) {
        showSnackBar(view, message);
    }

    public static void showSnackBar(@NonNull final Activity activity, final int message) {
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

    private static void showRateSnackBar(@NonNull final View view, @NonNull final Activity activity) {
        final int textColor = ContextCompat.getColor(view.getContext(), R.color.greenLineDark);
        final Snackbar snackBar1 = Snackbar.make(view, "Do you like this app?", Snackbar.LENGTH_LONG)
            .setAction("YES", view1 -> {
                final Snackbar snackBar2 = Snackbar.make(view1, "Rate this app on the market", Snackbar.LENGTH_LONG)
                    .setAction("OK", view2 -> rateThisApp(activity))
                    .setActionTextColor(textColor)
                    .setDuration(10000);
                snackBar2.show();
            })
            .setActionTextColor(textColor)
            .setDuration(10000);
        snackBar1.show();
    }

    public static void displayRateSnackBarIfNeeded(@NonNull final View view, @NonNull final Activity activity) {
        Handler handler = new Handler();
        Runnable r = () -> {
            final Date now = new Date();
            final Date lastSeen = PreferencesImpl.INSTANCE.getRateLastSeen(view.getContext());
            // if it has been more than 30 days or if it's the first time
            if (now.getTime() - lastSeen.getTime() > 2592000000L || now.getTime() - lastSeen.getTime() < 1000L) {
                showRateSnackBar(view, activity);
                PreferencesImpl.INSTANCE.setRateLastSeen(view.getContext());
            }
        };
        handler.postDelayed(r, 2500L);
    }

    public static void rateThisApp(final Activity activity) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=fr.cph.chicago"));
        activity.startActivity(intent);
    }

    public static void handleConnectOrParserException(@NonNull final Throwable throwable, @Nullable final Activity activity, @Nullable final View connectView, @NonNull final View parserView) {
        if (throwable.getCause() instanceof ConnectException) {
            if (activity != null) {
                showNetworkErrorMessage(activity);
            } else if (connectView != null) {
                showNetworkErrorMessage(connectView);
            }
        } else if (throwable.getCause() instanceof ParserException) {
            showOopsSomethingWentWrong(parserView);
        }
    }

    @NonNull
    public static String trimBusStopNameIfNeeded(@NonNull final String name) {
        return name.length() > 25
            ? name.substring(0, 24).trim() + "..."
            : name;
    }

    @SneakyThrows(PackageManager.NameNotFoundException.class)
    public static String getCurrentVersion(final Context context){
        final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }
}
