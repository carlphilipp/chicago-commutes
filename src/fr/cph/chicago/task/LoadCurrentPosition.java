package fr.cph.chicago.task;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import fr.cph.chicago.entity.Position;

public class LoadCurrentPosition extends AsyncTask<Boolean, Void, Void> implements LocationListener {
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

	private LocationManager locationManager;
	private Activity currentActivity;
	private MapFragment mapFragment;
	private Location location;
	private double latitude;
	private double longitude;

	public LoadCurrentPosition(final Activity currentActivity, final MapFragment mapFragment) {
		this.currentActivity = currentActivity;
		this.mapFragment = mapFragment;
	}

	@Override
	protected final Void doInBackground(final Boolean... params) {
		locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);

		// getting GPS status
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
			// no network provider is enabled
			showSettingsAlert();
		} else {
			if (isNetworkEnabled) {
				if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(currentActivity,
							new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
					return null;
				}
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
						this, Looper.getMainLooper());
				if (locationManager != null) {
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				if (location == null) {
					if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED
							&& ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(currentActivity,
								new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
						return null;
					}
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
							this, Looper.getMainLooper());
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}
			final Position position = new Position();
			position.setLatitude(latitude);
			position.setLongitude(longitude);
		}
		return null;
	}

	@Override
	protected final void onPostExecute(final Void result) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(currentActivity,
							new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
					return;
				}
				googleMap.setMyLocationEnabled(true);
				locationManager.removeUpdates(LoadCurrentPosition.this);
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

	/**
	 * Function to show settings alert dialog
	 */
	private void showSettingsAlert() {
		new Thread() {
			public void run() {
				currentActivity.runOnUiThread(new Runnable() {
					public void run() {
						final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentActivity);
						alertDialogBuilder.setTitle("GPS settings");
						alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
						alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								currentActivity.startActivity(intent);
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						final AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.show();
					}
				});
			}
		}.start();
	}
}