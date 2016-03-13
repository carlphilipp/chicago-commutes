package fr.cph.chicago.task;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_PATTERN;

public class LoadBusPatternTask extends AsyncTask<Void, Void, BusPattern> {

    private static final String TAG = LoadBusPatternTask.class.getSimpleName();

    private BusBoundActivity activity;
    private MapFragment mapFragment;
    private BusPattern busPattern;
    private String busRouteId;
    private String bound;

    public LoadBusPatternTask(final BusBoundActivity activity, final MapFragment mapFragment, final String busRouteId, final String bound) {
        this.activity = activity;
        this.mapFragment = mapFragment;
        this.busRouteId = busRouteId;
        this.bound = bound;
    }

    @Override
    protected final BusPattern doInBackground(final Void... params) {
        final CtaConnect connect = CtaConnect.getInstance();
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        connectParam.put(activity.getString(R.string.request_rt), busRouteId);
        final String boundIgnoreCase = bound.toLowerCase(Locale.US);
        try {
            final InputStream content = connect.connect(BUS_PATTERN, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            final List<BusPattern> patterns = xml.parsePatterns(content);
            for (final BusPattern pattern : patterns) {
                final String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
                if (pattern.getDirection().equals(bound) || boundIgnoreCase.contains(directionIgnoreCase)) {
                    this.busPattern = pattern;
                    break;
                }
            }
        } catch (final ConnectException | ParserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_pattern, 0);
        return this.busPattern;
    }

    @Override
    protected final void onPostExecute(final BusPattern result) {
        if (result != null) {
            final int center = result.getPoints().size() / 2;
            final Position position = result.getPoints().get(center).getPosition();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                    if (position != null) {
                        final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(9), 500, null);
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Util.CHICAGO, 10));
                    }
                }
            });
            activity.drawPattern(result);
        } else {
            Toast.makeText(activity, "Sorry, could not load the path!", Toast.LENGTH_SHORT).show();
        }
    }
}
