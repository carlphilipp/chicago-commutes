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
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.adapter.BusMapSnippetAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;

public class LoadBusFollowTask extends AsyncTask<String, Void, List<BusArrival>> {

    private static final String TAG = LoadBusFollowTask.class.getSimpleName();

    private BusMapActivity activity;
    private View view;
    private boolean loadAll;

    public LoadBusFollowTask(@NonNull final BusMapActivity activity, @NonNull final View view, final boolean loadAll) {
        this.activity = activity;
        this.view = view;
        this.loadAll = loadAll;
    }

    @Override
    protected List<BusArrival> doInBackground(final String... params) {
        final String busId = params[0];
        List<BusArrival> arrivals = new ArrayList<>();
        try {
            CtaConnect connect = CtaConnect.getInstance();
            MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
            connectParam.put(activity.getString(R.string.request_vid), busId);
            InputStream content = connect.connect(BUS_ARRIVALS, connectParam);
            XmlParser xml = XmlParser.getInstance();
            arrivals = xml.parseBusArrivals(content);
        } catch (final ConnectException | ParserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
        if (!loadAll && arrivals.size() > 7) {
            arrivals = arrivals.subList(0, 6);
            final BusArrival arrival = new BusArrival();
            arrival.setStopName(activity.getString(R.string.bus_all_results));
            arrival.setDly(false);
            arrivals.add(arrival);
        }
        return arrivals;
    }

    @Override
    protected final void onPostExecute(final List<BusArrival> result) {
        final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
        final TextView error = (TextView) view.findViewById(R.id.error);
        if (result.size() != 0) {
            final BusMapSnippetAdapter ada = new BusMapSnippetAdapter(activity, result);
            arrivals.setAdapter(ada);
            arrivals.setVisibility(ListView.VISIBLE);
            error.setVisibility(TextView.GONE);
        } else {
            arrivals.setVisibility(ListView.GONE);
            error.setVisibility(TextView.VISIBLE);
        }
        activity.refreshInfoWindow();
    }
}
