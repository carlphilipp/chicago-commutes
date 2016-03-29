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

package fr.cph.chicago;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Date;

import fr.cph.chicago.activity.BaseActivity;
import fr.cph.chicago.activity.ErrorActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;

/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class ChicagoTracker extends Application {
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
     * Application context
     **/
    private static Context context;
    /**
     * Last update of favorites
     **/
    private static Date lastUpdate;
    /**
     * Container that is used to get a faded black background
     **/
    public static FrameLayout container;
    /**
     * Analytics stuff
     **/
    private static Tracker tracker;

    @Override
    public final void onCreate() {
        super.onCreate();
        ChicagoTracker.context = getApplicationContext();
    }

    /**
     * Get Application context
     *
     * @return the context
     */
    public static Context getContext() {
        return ChicagoTracker.context;
    }

    /**
     * Modify last update date.
     *
     * @param date the last update of favorites
     */
    public static void modifyLastUpdate(@NonNull final Date date) {
        lastUpdate = date;
    }

    /**
     * Get last update
     *
     * @return the last update
     */
    @NonNull
    public static Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Function that is used all over the application. Display error message and provide a way to retry
     *
     * @param activity that is needed to lunch a new one
     * @param ex       the kind of exception. Used to display the error message
     */
    public static void displayError(@NonNull final Activity activity, @NonNull final TrackerException ex) {
        final Intent intent = new Intent(activity, ErrorActivity.class);
        Bundle extras = new Bundle();
        extras.putString(activity.getString(R.string.bundle_error), ex.getMessage());
        intent.putExtras(extras);
        activity.finish();
        activity.startActivity(intent);
    }

    public static void checkData(@NonNull final Activity mActivity) {
        if (DataHolder.getInstance().getBusData() == null || DataHolder.getInstance().getTrainData() == null) {
            startErrorActivity(mActivity);
        }
    }

    public static boolean checkTrainData(@NonNull final Activity activity) {
        if (DataHolder.getInstance().getTrainData() == null) {
            startErrorActivity(activity);
            return false;
        }
        return true;
    }

    public static boolean checkBusData(@NonNull final Activity mActivity) {
        if (DataHolder.getInstance().getBusData() == null) {
            startErrorActivity(mActivity);
            return false;
        }
        return true;
    }

    public static void reloadData() {
        final DataHolder dataHolder = DataHolder.getInstance();
        final BusData busData = BusData.getInstance();
        if (busData.readAllBusStops() == null || busData.readAllBusStops().size() == 0) {
            busData.readBusStops();
            dataHolder.setBusData(busData);
        }
        final TrainData trainData = TrainData.getInstance();
        if (trainData.isStationNull() || trainData.isStopsNull()) {
            trainData.read();
            dataHolder.setTrainData(trainData);
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
    public static Tracker getTracker() {
        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(ChicagoTracker.getContext());
        if (tracker == null) {
            final String key = Util.getProperty("google.analytics");
            tracker = analytics.newTracker(key);
            tracker.enableAutoActivityTracking(true);
        }
        return tracker;
    }
}
