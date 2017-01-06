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
import lombok.Getter;
import lombok.Setter;

/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class App extends Application {
    /**
     * Preference string that is used to in shared preference of the phone
     **/
    public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavorites";
    /**
     * Train preference string
     **/
    public static final String PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain";
    /**
     * Bus preference string
     **/
    public static final String PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus";
    /**
     * Bus mapping name string
     **/
    public static final String PREFERENCE_FAVORITES_BUS_ROUTE_NAME_MAPPING = "ChicagoTrackerFavoritesBusNameMapping";
    /**
     * Bus mapping name string
     **/
    public static final String PREFERENCE_FAVORITES_BUS_STOP_NAME_MAPPING = "ChicagoTrackerFavoritesBusStopNameMapping";
    /**
     * Bike preference string
     **/
    public static final String PREFERENCE_FAVORITES_BIKE = "ChicagoTrackerFavoritesBike";
    /**
     * Bike mapping name string
     **/
    public static final String PREFERENCE_FAVORITES_BIKE_NAME_MAPPING = "ChicagoTrackerFavoritesBikeNameMapping";
    /**
     * Last update of favorites
     **/
    @Setter
    @Getter
    private static Date lastUpdate;
    /**
     * Analytics stuff
     **/
    private static Tracker tracker;
    @Getter
    private static int screenWidth;
    @Getter
    private static float lineWidth;

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
        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        if (tracker == null) {
            final String key = context.getString(R.string.google_analytics_key);
            tracker = analytics.newTracker(key);
            tracker.enableAutoActivityTracking(true);
        }
        return tracker;
    }

    public static void setupScreenWidth(@NonNull final Context context) {
        final int[] screenSize = Util.getScreenSize(context);
        screenWidth = screenSize[0];
        lineWidth = screenWidth > 1080 ? 7f : (screenWidth > 480 ? 4f : 2f);
    }
}
