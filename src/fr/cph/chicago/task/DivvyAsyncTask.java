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

    public BikeStationActivity activity;
    public int bikeStationId;
    private SwipeRefreshLayout swipeRefreshLayout;

    public DivvyAsyncTask(final BikeStationActivity activity, final int bikeStationId, final SwipeRefreshLayout swipeRefreshLayout) {
        this.activity = activity;
        this.bikeStationId = bikeStationId;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected List<BikeStation> doInBackground(final Void... params) {
        final List<BikeStation> bikeStations;
        try {
            final JsonParser json = JsonParser.getInstance();
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            bikeStations = json.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
        } catch (final ConnectException | ParserException e) {
            bikeStations = null;
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