/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import static android.content.Context.MODE_PRIVATE;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_BIKE;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_BIKE_NAME_MAPPING;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_BUS;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING;
import static fr.cph.chicago.core.App.PREFERENCE_FAVORITES_TRAIN;

/**
 * Class that store user preferences into phoneO
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum PreferencesImpl implements Preferences {
    INSTANCE;
    // FIXME change all bikeId (and other ids?) that should be int. Be sure whenever a user has a favorite already saved on the phone that it will work correctly

    private static final String TAG = PreferencesImpl.class.getSimpleName();

    private static final Pattern PATTERN = Pattern.compile("(\\d{1,3})");
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    /**
     * Save train favorites
     *
     * @param context   the context
     * @param favorites the favorites
     */
    @Override
    public void saveTrainFavorites(@NonNull final Context context, @NonNull final List<Integer> favorites) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final Set<String> set = Stream.of(favorites).map(Object::toString).collect(Collectors.toSet());
        Log.v(TAG, "Put train favorites: " + favorites.toString());
        editor.putStringSet(PREFERENCE_FAVORITES_TRAIN, set);
        editor.apply();
    }

    /**
     * Check if the user has favorites already
     *
     * @param context the context
     * @return a boolean
     */
    @Override
    public boolean hasFavorites(@NonNull final Context context) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final Set<String> setPref1 = sharedPref.getStringSet(PREFERENCE_FAVORITES_TRAIN, null);
        final Set<String> setPref2 = sharedPref.getStringSet(PREFERENCE_FAVORITES_BUS, null);
        final Set<String> setPref3 = sharedPref.getStringSet(PREFERENCE_FAVORITES_BIKE, null);
        return !((setPref1 == null || setPref1.size() == 0) && (setPref2 == null || setPref2.size() == 0) && (setPref3 == null || setPref3.size() == 0));
    }

    @Override
    public void saveBikeFavorites(@NonNull final Context context, @NonNull final List<String> favorites) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final Set<String> set = Stream.of(favorites).collect(Collectors.toSet());
        Log.v(TAG, "Put bike favorites: " + set.toString());
        editor.putStringSet(PREFERENCE_FAVORITES_BIKE, set);
        editor.apply();
    }

    @Override
    @NonNull
    public List<String> getBikeFavorites(@NonNull final Context context) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final Set<String> setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_BIKE, new LinkedHashSet<>());
        Log.v(TAG, "Read bike favorites : " + setPref.toString());
        return Stream.of(setPref).sorted().collect(Collectors.toList());
    }

    @Override
    public void addBikeRouteNameMapping(@NonNull final Context context, @NonNull final String bikeId, @NonNull final String bikeName) {
        final SharedPreferences sharedPref = getPrivatePreferencesBikeMapping(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(bikeId, bikeName);
        Log.v(TAG, "Add bike name mapping : " + bikeId + " => " + bikeName);
        editor.apply();
    }

    @Override
    @Nullable
    public String getBikeRouteNameMapping(@NonNull final Context context, @NonNull final String bikeId) {
        final SharedPreferences sharedPref = getPrivatePreferencesBikeMapping(context);
        final String bikeName = sharedPref.getString(bikeId, null);
        Log.v(TAG, "Get bike name mapping : " + bikeId + " => " + bikeName);
        return bikeName;
    }

    /**
     * Save bus into favorites
     *
     * @param context   the context
     * @param favorites the list of favorites to save
     */
    @Override
    public void saveBusFavorites(@NonNull final Context context, @NonNull final List<String> favorites) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final Set<String> set = new LinkedHashSet<>();
        set.addAll(favorites);
        Log.v(TAG, "Put bus favorites: " + favorites.toString());
        editor.putStringSet(PREFERENCE_FAVORITES_BUS, set);
        editor.apply();
    }

    /**
     * Get favorites bus
     *
     * @param context the context
     * @return a list of favorites bus
     */
    @Override
    @NonNull
    public List<String> getBusFavorites(@NonNull final Context context) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final Set<String> setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_BUS, new LinkedHashSet<>());
        Log.v(TAG, "Read bus favorites : " + setPref.toString());
        return Stream.of(setPref).sorted(
            (str1, str2) -> {
                final String str1Decoded = Util.decodeBusFavorite(str1).getRouteId();
                final String str2Decoded = Util.decodeBusFavorite(str2).getRouteId();

                final Matcher matcher1 = PATTERN.matcher(str1Decoded);
                final Matcher matcher2 = PATTERN.matcher(str2Decoded);
                if (matcher1.find() && matcher2.find()) {
                    final int one = Integer.parseInt(matcher1.group(1));
                    final int two = Integer.parseInt(matcher2.group(1));
                    return one < two ? -1 : (one == two ? 0 : 1);
                } else {
                    return str1Decoded.compareTo(str2Decoded);
                }
            }).collect(Collectors.toList());
    }

    @Override
    public void addBusRouteNameMapping(@NonNull final Context context, @NonNull final String busStopId, @NonNull final String routeName) {
        final SharedPreferences sharedPref = getPrivatePreferencesBusRouteMapping(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(busStopId, routeName);
        Log.v(TAG, "Add bus route name mapping : " + busStopId + " => " + routeName);
        editor.apply();
    }

    @Override
    @Nullable
    public String getBusRouteNameMapping(@NonNull final Context context, @NonNull final String busStopId) {
        final SharedPreferences sharedPref = getPrivatePreferencesBusRouteMapping(context);
        final String routeName = sharedPref.getString(busStopId, null);
        Log.v(TAG, "Get bus route name mapping : " + busStopId + " => " + routeName);
        return routeName;
    }

    @Override
    public void addBusStopNameMapping(@NonNull final Context context, @NonNull final String busStopId, @NonNull final String stopName) {
        final SharedPreferences sharedPref = getPrivatePreferencesBusStopMapping(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(busStopId, stopName);
        Log.v(TAG, "Add bus stop name mapping : " + busStopId + " => " + stopName);
        editor.apply();
    }

    @Override
    @Nullable
    public String getBusStopNameMapping(@NonNull final Context context, @NonNull final String busStopId) {
        final SharedPreferences sharedPref = getPrivatePreferencesBusStopMapping(context);
        final String stopName = sharedPref.getString(busStopId, null);
        Log.v(TAG, "Get bus stop name mapping : " + busStopId + " => " + stopName);
        return stopName;
    }

    /**
     * Get train favorites
     *
     * @param context the context
     * @return the favorites
     */
    @Override
    @NonNull
    public List<Integer> getTrainFavorites(@NonNull final Context context) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final Set<String> setPref = sharedPref.getStringSet(PREFERENCE_FAVORITES_TRAIN, new LinkedHashSet<>());
        Log.v(TAG, "Read train favorites : " + setPref);
        return Stream.of(setPref)
            .map(Integer::valueOf)
            .map(favorite -> DataHolder.INSTANCE.getTrainData().getStation(favorite))
            .map(optional -> optional.orElse(new Station()))
            .sorted()
            .map(station -> station.getId())
            .collect(Collectors.toList());
    }

    /**
     * Save train filter
     *
     * @param stationId the station id
     * @param line      the line
     * @param direction the direction
     * @param value     the value
     */
    @Override
    public void saveTrainFilter(@NonNull final Context context, @NonNull final Integer stationId, @NonNull final TrainLine line, @NonNull final TrainDirection direction, final boolean value) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
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
    @Override
    public boolean getTrainFilter(@NonNull final Context context, @NonNull final Integer stationId, @NonNull final TrainLine line, @NonNull final TrainDirection direction) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        return sharedPref.getBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, true);
    }

    @Override
    public Date getRateLastSeen(@NonNull final Context context) {
        try {
            final SharedPreferences sharedPref = getPrivatePreferences(context);
            final String defaultDate = FORMAT.format(new Date());
            return FORMAT.parse(sharedPref.getString("rateLastSeen", defaultDate));
        } catch (final ParseException e) {
            return new Date();
        }
    }

    @Override
    public void setRateLastSeen(@NonNull final Context context) {
        final SharedPreferences sharedPref = getPrivatePreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("rateLastSeen", FORMAT.format(new Date()));
        editor.apply();
    }

    @Override
    public void clearPreferences(@NonNull final Context context) {
        getPrivatePreferences(context).edit().clear().apply();
    }

    private SharedPreferences getPrivatePreferences(@NonNull final Context context) {
        return context.getSharedPreferences(PREFERENCE_FAVORITES, MODE_PRIVATE);
    }

    private SharedPreferences getPrivatePreferencesBusStopMapping(@NonNull final Context context) {
        return context.getSharedPreferences(PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING, MODE_PRIVATE);
    }

    private SharedPreferences getPrivatePreferencesBusRouteMapping(@NonNull final Context context) {
        return context.getSharedPreferences(PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING, MODE_PRIVATE);
    }

    private SharedPreferences getPrivatePreferencesBikeMapping(@NonNull final Context context) {
        return context.getSharedPreferences(PREFERENCE_FAVORITES_BIKE_NAME_MAPPING, MODE_PRIVATE);
    }
}
