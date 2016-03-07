package fr.cph.chicago.task;

import android.Manifest;
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
import com.google.android.gms.maps.SupportMapFragment;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carl on 3/6/2016.
 */
public class LoadNearbyTask extends AsyncTask<Void, Void, Void> implements LocationListener {

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
	// flag for GPS status
	private boolean isGPSEnabled = false;
	// flag for network status
	private boolean isNetworkEnabled = false;
	/**
	 * The location
	 **/
	private Location location;
	/**
	 * The position
	 **/
	private Position position;
	/**
	 * The latitude
	 **/
	private double latitude;
	/**
	 * THe longitude
	 **/
	private double longitude;
	/**
	 * The list of bus stops
	 **/
	private List<BusStop> busStops;
	/**
	 * The list of train stations
	 **/
	private List<Station> trainStations;
	/**
	 * List of bike stations
	 **/
	private List<BikeStation> bikeStations;
	/**
	 * The location manager
	 **/
	private LocationManager locationManager;

	private MainActivity activity;

	private NearbyFragment fragment;

	private SupportMapFragment mapFragment;

	public LoadNearbyTask(final NearbyFragment fragment, final MainActivity activity, final SupportMapFragment mapFragment) {
		this.fragment = fragment;
		this.activity = activity;
		this.mapFragment = mapFragment;
	}

	@Override
	protected final Void doInBackground(final Void... params) {
		busStops = new ArrayList<>();
		trainStations = new ArrayList<>();
		bikeStations = activity.getIntent().getExtras().getParcelableArrayList(activity.getString(R.string.bundle_bike_stations));

		final DataHolder dataHolder = DataHolder.getInstance();
		final BusData busData = dataHolder.getBusData();
		final TrainData trainData = dataHolder.getTrainData();

		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
			// no network provider is enabled
			showSettingsAlert();
		} else {
			if (isNetworkEnabled) {
				if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(activity,
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
					if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED
							&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(activity,
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
			position = new Position();
			position.setLatitude(latitude);
			position.setLongitude(longitude);

			busStops = busData.readNearbyStops(position);
			trainStations = trainData.readNearbyStation(position);
			// TODO: wait bikeStations is loaded
			if (bikeStations != null) {
				bikeStations = BikeStation.readNearbyStation(bikeStations, position);
			}
		}
		return null;
	}

	@Override
	protected final void onPostExecute(final Void result) {
		fragment.loadArrivals(busStops, trainStations, bikeStations);

		Util.centerMap(fragment, mapFragment, activity, position);

		if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity,
					new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
			return;
		}
		locationManager.removeUpdates(this);
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
				activity.runOnUiThread(new Runnable() {
					public void run() {
						final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
						alertDialogBuilder.setTitle("GPS settings");
						alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
						alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								activity.startActivity(intent);
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