package fr.cph.chicago.task;

import android.os.AsyncTask;
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

    public LoadBusFollowTask(final BusMapActivity activity, final View view, final boolean loadAll) {
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
            connectParam.put("vid", busId);
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
            arrival.setStopName("Display all results");
            arrival.setIsDly(false);
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
