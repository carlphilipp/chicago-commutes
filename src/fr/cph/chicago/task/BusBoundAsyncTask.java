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
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.List;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;

/**
 * Task that connect to API to get the bound of the selected stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusBoundAsyncTask extends AsyncTask<Void, Void, List<BusStop>> {

    private BusBoundActivity activity;
    private String busRouteId;
    private String bound;
    private BusBoundAdapter busBoundAdapter;
    private TrackerException trackerException;

    public BusBoundAsyncTask(@NonNull final BusBoundActivity activity,
                             @NonNull final String busRouteId,
                             @NonNull final String bound,
                             @NonNull final BusBoundAdapter busBoundAdapter) {
        this.activity = activity;
        this.busRouteId = busRouteId;
        this.bound = bound;
        this.busBoundAdapter = busBoundAdapter;
    }

    @Override
    protected final List<BusStop> doInBackground(final Void... params) {
        List<BusStop> busStops = null;
        try {
            busStops = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, bound);
        } catch (final ParserException | ConnectException e) {
            this.trackerException = e;
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_stop, 0);
        return busStops;
    }

    @Override
    protected final void onPostExecute(final List<BusStop> result) {
        if (trackerException == null) {
            activity.setBusStops(result);
            busBoundAdapter.update(result);
            busBoundAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(ChicagoTracker.getContext(), trackerException.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}