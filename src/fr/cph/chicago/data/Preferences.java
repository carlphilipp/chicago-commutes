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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cph.chicago.App;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Class that store user preferences into phoneO
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Preferences {
    // FIXME change all bikeId (and other ids?) that should be int. Be sure whenever a user as a favorite already saved on the phone that it will work correctly

    /**
     * Tag
     **/
    private static final String TAG = Preferences.class.getSimpleName();

    private static final Pattern pattern = Pattern.compile("(\\d{1,3})");

    /**
     * Check if the user has favorites already
     *
     * @param trains the trains preference string
     * @param bus    the bus preference string
     * @return a boolean
     */
    public static boolean hasFavorites(@NonNull final String trains, @NonNull final String bus, @NonNull final String bike) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        final Set<String> setPref1 = sharedPref.getStringSet(trains, null);
        final Set<String> setPref2 = sharedPref.getStringSet(bus, null);
        final Set<String> setPref3 = sharedPref.getStringSet(bike, null);
        boolean res = true;
        if ((setPref1 == null || setPref1.size() == 0) && (setPref2 == null || setPref2.size() == 0) && (setPref3 == null || setPref3.size() == 0)) {
            res = false;
        }
        return res;
    }

    public static void saveBikeFavorites(@NonNull final String name, @NonNull final List<String> favorites) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final Set<String> set = new LinkedHashSet<>();
        for (final String fav : favorites) {
            set.add(fav);
        }
        Log.v(TAG, "Put bike favorites: " + set.toString());
        editor.putStringSet(name, set);
        editor.apply();
    }

    @NonNull
    public static List<String> getBikeFavorites(@NonNull final String name) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
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

    public static void addBikeRouteNameMapping(@NonNull final String bikeId, @NonNull final String bikeName) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(bikeId, bikeName);
        Log.v(TAG, "Add bike name mapping : " + bikeId + " => " + bikeName);
        editor.apply();
    }

    @NonNull
    public static String getBikeRouteNameMapping(@NonNull final String bikeId) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context
            .getSharedPreferences(App.PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, Context.MODE_PRIVATE);
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
    public static void saveBusFavorites(@NonNull final String name, @NonNull final List<String> favorites) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
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
    @NonNull
    public static List<String> getBusFavorites(@NonNull final String name) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        final Set<String> setPref = sharedPref.getStringSet(name, null);
        final List<String> favorites = new ArrayList<>();
        if (setPref != null) {
            for (final String value : setPref) {
                favorites.add(value);
            }
        }
        Collections.sort(favorites, new Comparator<String>() {
            @Override
            public int compare(final String str1, final String str2) {
                final String str1Decoded = Util.decodeBusFavorite(str1)[0];
                final String str2Decoded = Util.decodeBusFavorite(str2)[0];

                final Matcher matcher1 = pattern.matcher(str1Decoded);
                final Matcher matcher2 = pattern.matcher(str2Decoded);
                if (matcher1.find() && matcher2.find()) {
                    final Integer one = Integer.valueOf(matcher1.group(1));
                    final Integer two = Integer.valueOf(matcher2.group(1));
                    return one.compareTo(two);
                } else {
                    return str1Decoded.compareTo(str2Decoded);
                }
            }
        });
        Log.v(TAG, "Read bus favorites : " + favorites.toString());
        return favorites;
    }

    public static void addBusRouteNameMapping(@NonNull final String busStopId, @NonNull final String routeName) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(busStopId, routeName);
        Log.v(TAG, "Add bus route name mapping : " + busStopId + " => " + routeName);
        editor.apply();
    }

    @Nullable
    public static String getBusRouteNameMapping(@NonNull final String busStopId) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, Context.MODE_PRIVATE);
        final String routeName = sharedPref.getString(busStopId, null);
        Log.v(TAG, "Get bus route name mapping : " + busStopId + " => " + routeName);
        return routeName;
    }

    public static void addBusStopNameMapping(@NonNull final String busStopId, @NonNull final String stopName) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(busStopId, stopName);
        Log.v(TAG, "Add bus stop name mapping : " + busStopId + " => " + stopName);
        editor.apply();
    }

    @Nullable
    public static String getBusStopNameMapping(@NonNull final String busStopId) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context
            .getSharedPreferences(App.PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, Context.MODE_PRIVATE);
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
    public static void saveTrainFavorites(@NonNull final String name, @NonNull final List<Integer> favorites) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
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
    @NonNull
    public static List<Integer> getTrainFavorites(@NonNull final String name) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
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
    public static void saveTrainFilter(@NonNull final Integer stationId, @NonNull final TrainLine line, @NonNull final TrainDirection direction, final boolean value) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
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
    public static boolean getTrainFilter(@NonNull final Integer stationId, @NonNull final TrainLine line, @NonNull final TrainDirection direction) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, true);
    }

    public static void saveHideShowNearby(boolean hide) {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("hideNearby", hide);
        editor.apply();
    }

    public static boolean getHideShowNearby() {
        final Context context = App.getContext();
        final SharedPreferences sharedPref = context.getSharedPreferences(App.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        return sharedPref.getBoolean("hideNearby", true);
    }
}
