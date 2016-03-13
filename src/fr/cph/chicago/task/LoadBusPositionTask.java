package fr.cph.chicago.task;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_VEHICLES;

public class LoadBusPositionTask extends AsyncTask<Boolean, Void, List<Bus>> {

    private static final String TAG = LoadBusPositionTask.class.getSimpleName();

    private boolean centerMap;
    private BusMapActivity activity;
    private int busId;
    private String busRouteId;

    public LoadBusPositionTask(final BusMapActivity activity, final int busId, final String busRouteId) {
        this.activity = activity;
        this.busId = busId;
        this.busRouteId = busRouteId;
    }

    @Override
    protected List<Bus> doInBackground(final Boolean... params) {
        centerMap = params[0];
        List<Bus> buses = null;
        final CtaConnect connect = CtaConnect.getInstance();
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        if (busId != 0) {
            connectParam.put("vid", String.valueOf(busId));
        } else {
            connectParam.put(activity.getString(R.string.request_rt), busRouteId);
        }
        try {
            final InputStream content = connect.connect(BUS_VEHICLES, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            buses = xml.parseVehicles(content);
        } catch (final ConnectException | ParserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_vehicles, 0);
        return buses;
    }

    @Override
    protected final void onPostExecute(final List<Bus> result) {
        if (result != null) {
            activity.drawBuses(result);
            if (result.size() != 0) {
                if (centerMap) {
                    activity.centerMapOnBus(result);
                }
            } else {
                Toast.makeText(activity, "No bus found!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activity, "Error while loading data!", Toast.LENGTH_SHORT).show();
        }
    }
}
