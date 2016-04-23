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

package fr.cph.chicago.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.util.Util;

public class LoadBusAndBikeDataTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LoadBusAndBikeDataTask.class.getSimpleName();

    private MainActivity activity;
    private BusData busData;
    private List<BikeStation> bikeStations;

    public LoadBusAndBikeDataTask(final MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        final DataHolder dataHolder = DataHolder.getInstance();
        busData = dataHolder.getBusData();

        // Load buses data
        try {
            busData.loadBusRoutes();
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_routes, 0);
            publishProgress();
        } catch (final ParserException | ConnectException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Util.showMessage(activity, R.string.message_error);
                }
            });
            Log.e(TAG, e.getMessage(), e);
        }

        // Load divvy data
        try {
            final JsonParser json = JsonParser.getInstance();
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            bikeStations = json.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
            publishProgress();
        } catch (final ConnectException | ParserException e) {
            Log.e(TAG, e.getMessage(), e);
            bikeStations = new ArrayList<>();
        }
        return null;
    }

    @Override
    protected final void onProgressUpdate(final Void... progress) {
    }

    @Override
    protected final void onPostExecute(final Void result) {
        activity.refresh(busData, bikeStations);
    }
}
