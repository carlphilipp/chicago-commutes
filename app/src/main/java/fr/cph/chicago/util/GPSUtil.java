package fr.cph.chicago.util;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import fr.cph.chicago.entity.Position;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class GPSUtil {
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    private final LocationListener locationListener;
    private final LocationManager locationManager;
    private final Activity activity;

    public GPSUtil(@NonNull final LocationListener locationListener, @NonNull final Activity activity) {
        this.locationListener = locationListener;
        this.activity = activity;
        this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    @NonNull
    public Optional<Position> getLocation() throws SecurityException {
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            Util.showSettingsAlert(activity);
            return Optional.empty();
        } else {
            Position position = null;
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener, Looper.getMainLooper());
                Location location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
                if (location != null) {
                    position = new Position();
                    position.setLatitude(location.getLatitude());
                    position.setLongitude(location.getLongitude());
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener, Looper.getMainLooper());
                Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (location != null) {
                    position = new Position();
                    position.setLatitude(location.getLatitude());
                    position.setLongitude(location.getLongitude());
                }
            }
            locationManager.removeUpdates(locationListener);
            return Optional.ofNullable(position);
        }
    }
}
