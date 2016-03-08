package fr.cph.chicago.task;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

// TODO useless class to refactor
public class LoadCurrentPositionTask extends AsyncTask<Boolean, Void, Void> implements LocationListener {

	private LocationManager locationManager;
	private Activity activity;
	private MapFragment mapFragment;

	public LoadCurrentPositionTask(final Activity currentActivity, final MapFragment mapFragment) {
		this.activity = currentActivity;
		this.mapFragment = mapFragment;
	}

	@Override
	protected final Void doInBackground(final Boolean... params) {
		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		return null;
	}

	@Override
	protected final void onPostExecute(final Void result) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
					return;
				}
				googleMap.setMyLocationEnabled(true);
				locationManager.removeUpdates(LoadCurrentPositionTask.this);
			}
		});
	}

	@Override
	public final void onLocationChanged(final Location location) {
	}

	@Override
	public final void onProviderDisabled(final String provider) {
	}

	@Override
	public final void onProviderEnabled(final String provider) {
	}

	@Override
	public final void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}
}