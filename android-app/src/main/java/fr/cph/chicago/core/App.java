/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Date;

import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.BaseActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.util.Util;

/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class App extends Application {

    public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavorites";
    public static final String PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain";
    public static final String PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus";
    public static final String PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING = "ChicagoTrackerFavoritesBusNameMapping";
    public static final String PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING = "ChicagoTrackerFavoritesBusStopNameMapping";
    public static final String PREFERENCE_FAVORITES_BIKE = "ChicagoTrackerFavoritesBike";
    public static final String PREFERENCE_FAVORITES_BIKE_NAME_MAPPING = "ChicagoTrackerFavoritesBikeNameMapping";

    /**
     * Last update of favorites
     **/
    private static Date lastUpdate;
    private static Tracker tracker;


    private static int screenWidth;
    private static float lineWidth;
    private static String ctaTrainKey;
    private static String ctaBusKey;
    private static String googleStreetKey;

    public static Date getLastUpdate() {
        return lastUpdate;
    }

    public static void setLastUpdate(Date lastUpdate) {
        App.lastUpdate = lastUpdate;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static void setScreenWidth(int screenWidth) {
        App.screenWidth = screenWidth;
    }

    public static float getLineWidth() {
        return lineWidth;
    }

    public static void setLineWidth(float lineWidth) {
        App.lineWidth = lineWidth;
    }

    public static String getCtaTrainKey() {
        return ctaTrainKey;
    }

    public static void setCtaTrainKey(String ctaTrainKey) {
        App.ctaTrainKey = ctaTrainKey;
    }

    public static String getCtaBusKey() {
        return ctaBusKey;
    }

    public static void setCtaBusKey(String ctaBusKey) {
        App.ctaBusKey = ctaBusKey;
    }

    public static String getGoogleStreetKey() {
        return googleStreetKey;
    }

    public static void setGoogleStreetKey(String googleStreetKey) {
        App.googleStreetKey = googleStreetKey;
    }

    public static boolean checkTrainData(@NonNull final Activity activity) {
        if (DataHolder.INSTANCE.getTrainData() == null) {
            startErrorActivity(activity);
            return false;
        }
        return true;
    }

    public static void checkBusData(@NonNull final Activity activity) {
        if (DataHolder.INSTANCE.getBusData() == null) {
            startErrorActivity(activity);
        }
    }

    private static void startErrorActivity(@NonNull final Activity activity) {
        final Intent intent = new Intent(activity, BaseActivity.class);
        intent.putExtra(activity.getString(R.string.bundle_error), true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @NonNull
    public static Tracker getTracker(final Context context) {
        if (tracker == null) {
            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            final String key = context.getString(R.string.google_analytics_key);
            tracker = analytics.newTracker(key);
            tracker.enableAutoActivityTracking(true);
        }
        return tracker;
    }

    public static void setupContextData(@NonNull final Context context) {
        final int[] screenSize = Util.INSTANCE.getScreenSize(context);
        screenWidth = screenSize[0];
        lineWidth = screenWidth > 1080 ? 7f : (screenWidth > 480 ? 4f : 2f);
        ctaTrainKey = context.getString(R.string.cta_train_key);
        ctaBusKey = context.getString(R.string.cta_bus_key);
        googleStreetKey = context.getString(R.string.google_maps_api_key);
    }
}
