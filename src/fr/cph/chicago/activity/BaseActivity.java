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

package fr.cph.chicago.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.task.LoadLocalDataTask;
import fr.cph.chicago.util.Util;

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        new LoadLocalDataTask(this).execute();
    }


    /**
     * Display error. Set train and bus data to null before running the error activity
     *
     * @param exceptionToBeThrown the exception that has been thrown
     */
    public void displayError(@NonNull final TrackerException exceptionToBeThrown) {
        DataHolder.getInstance().setTrainData(null);
        DataHolder.getInstance().setBusData(null);
        ChicagoTracker.displayError(this, exceptionToBeThrown);
    }

    /**
     * Connect to CTA API and get arrivals trains and buses from favorites
     *
     * @throws ParserException the exception
     */
    public void loadFavorites() throws ParserException {
        Util.loadFavorites(this, BaseActivity.class, this);
        trackWithGoogleAnalytics();
    }

    /**
     * Called via reflection from CtaConnectTask. It load arrivals data into ChicagoTracker object. Update
     * last update time. Start main activity
     *
     * @param trainArrivals list of train arrivals
     * @param busArrivals   list of bus arrivals
     */
    public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals, final List<BikeStation> bikeStations, final Boolean trainBoolean,
                                 final Boolean busBoolean, final Boolean bikeBoolean, final Boolean networkAvailable) {
        if (!networkAvailable) {
            Util.showNetworkErrorMessage(this);
        }
        ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
        startMainActivity(trainArrivals, busArrivals);
    }

    /**
     * Finish current activity and start main activity with custom transition
     *
     * @param trainArrivals the train arrivals
     * @param busArrivals   the bus arrivals
     */
    private void startMainActivity(@NonNull final SparseArray<TrainArrival> trainArrivals, @NonNull final List<BusArrival> busArrivals) {
        if (!isFinishing()) {
            final Intent intent = new Intent(this, MainActivity.class);
            final Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), (ArrayList<BusArrival>) busArrivals);
            bundle.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), trainArrivals);
            intent.putExtras(bundle);

            finish();
            startActivity(intent);
        }
    }

    private void trackWithGoogleAnalytics() {
        Util.trackAction(BaseActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
        Util.trackAction(BaseActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
    }
}
