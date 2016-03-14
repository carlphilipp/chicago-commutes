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
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.util.Util;

/**
 * Created by carl on 3/13/2016.
 */
public class DivvyAsyncTask extends AsyncTask<Void, Void, List<BikeStation>> {

    private static final String TAG = DivvyAsyncTask.class.getSimpleName();

    private BikeStationActivity activity;
    private int bikeStationId;
    private SwipeRefreshLayout swipeRefreshLayout;

    public DivvyAsyncTask(final BikeStationActivity activity, final int bikeStationId, final SwipeRefreshLayout swipeRefreshLayout) {
        this.activity = activity;
        this.bikeStationId = bikeStationId;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected List<BikeStation> doInBackground(final Void... params) {
        List<BikeStation> bikeStations = null;
        try {
            final JsonParser json = JsonParser.getInstance();
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            bikeStations = json.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
        } catch (final ConnectException | ParserException e) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ChicagoTracker.getContext(), "A surprising error has occurred. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(TAG, "Error while connecting or parsing divvy data", e);
        }
        return bikeStations;
    }

    @Override
    protected final void onPostExecute(final List<BikeStation> result) {
        for (final BikeStation station : result) {
            if (bikeStationId == station.getId()) {
                activity.refreshStation(station);
                final Bundle bundle = activity.getIntent().getExtras();
                bundle.putParcelable(activity.getString(R.string.bundle_bike_station), station);
                break;
            }
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}