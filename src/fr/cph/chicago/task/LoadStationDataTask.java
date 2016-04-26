package fr.cph.chicago.task;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;

/**
 * Load data class. Contact Bus CTA api to get arrival buses
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class LoadStationDataTask extends AsyncTask<Void, Void, List<BusArrival>> {

    /**
     * The exception that could potentially been thrown during request
     **/
    private final BusActivity activity;
    private final SwipeRefreshLayout swipeRefreshLayout;
    private final String busRouteId;
    private final int busStopId;
    private TrackerException trackerException;

    public LoadStationDataTask(@NonNull final BusActivity activity, @NonNull final SwipeRefreshLayout swipeRefreshLayout, @NonNull final String busRouteId, final int busStopId) {
        this.activity = activity;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.busRouteId = busRouteId;
        this.busStopId = busStopId;
    }

    @Override
    protected List<BusArrival> doInBackground(final Void... params) {
        final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
        reqParams.put(activity.getString(R.string.request_rt), busRouteId);
        reqParams.put(activity.getString(R.string.request_stop_id), Integer.toString(busStopId));
        final CtaConnect connect = CtaConnect.getInstance();
        try {
            final XmlParser xml = XmlParser.getInstance();
            // Connect to CTA API bus to get XML result of inc buses
            final InputStream xmlResult = connect.connect(BUS_ARRIVALS, reqParams);
            // Parse and return arrival buses
            return xml.parseBusArrivals(xmlResult);
        } catch (final ParserException | ConnectException e) {
            this.trackerException = e;
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_arrival, 0);
        return null;
    }

    @Override
    protected final void onProgressUpdate(final Void... values) {
    }

    @Override
    protected final void onPostExecute(final List<BusArrival> result) {
        if (trackerException == null) {
            activity.setBusArrivals(result);
            activity.drawArrivals();
        } else {
            Util.showNetworkErrorMessage(swipeRefreshLayout);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
