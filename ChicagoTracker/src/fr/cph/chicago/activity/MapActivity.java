package fr.cph.chicago.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.Pattern;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

public class MapActivity extends Activity {
	/** Tag **/
	private static final String TAG = "MapActivity";
	/** The map fragment from google api **/
	private MapFragment mapFragment;
	/** The map **/
	private GoogleMap map;
	/** Bus id **/
	private Integer busId;
	/** Bus route id **/
	private String busRouteId;
	/** Bound **/
	private String bound;
	/** Markers **/
	private List<Marker> markers;

	private Menu menu;
	/** A refresh task **/
	private RefreshTask refreshTimingTask;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			busId = getIntent().getExtras().getInt("busId");
			busRouteId = getIntent().getExtras().getString("busRouteId");
			bound = getIntent().getExtras().getString("bound");

			markers = new ArrayList<Marker>();

			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public final void onStart() {
		super.onStart();
		FragmentManager fm = getFragmentManager();
		mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 7, 0, 0);
		options.camera(camera);
		mapFragment = MapFragment.newInstance(options);
		mapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onPause() {
		super.onPause();
		refreshTimingTask.cancel(true);
	}

	@Override
	public final void onStop() {
		super.onStop();
		map = null;
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (map == null) {
			map = mapFragment.getMap();
		}
		if (refreshTimingTask != null && refreshTimingTask.getStatus() == Status.FINISHED) {
			startRefreshTask();
		}
		if (Util.isNetworkAvailable()) {
			if (busId == 0) {
				new LoadCurrentPosition().execute(new Boolean[] { true });
				new LoadBusPosition().execute(new Boolean[] { false, false });
			} else {
				new LoadCurrentPosition().execute(new Boolean[] { false });
				new LoadBusPosition().execute(new Boolean[] { true, false });
			}
			new LoadPattern().execute();
		} else {
			Toast.makeText(this, "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busId = savedInstanceState.getInt("busId");
		busRouteId = savedInstanceState.getString("busRouteId");
		bound = savedInstanceState.getString("bound");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("busId", busId);
		savedInstanceState.putString("busRouteId", busRouteId);
		savedInstanceState.putString("bound", bound);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.main_no_search, menu);
		startRefreshAnimation();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			startRefreshAnimation();
			new LoadCurrentPosition().execute(new Boolean[] { false });
			new LoadBusPosition().execute(new Boolean[] { false, true });
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Load animation in menu
	 */
	private final void startRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			if (refreshMenuItem.getActionView() == null) {
				refreshMenuItem.setActionView(R.layout.progressbar);
				refreshMenuItem.expandActionView();
			}
		}
	}

	/**
	 * Stop animation in menu
	 */
	private final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	private final class LoadBusPosition extends AsyncTask<Boolean, Void, List<Bus>> {

		private boolean centerMap;

		private boolean stopRefresh;

		@Override
		protected List<Bus> doInBackground(Boolean... params) {
			centerMap = params[0];
			stopRefresh = params[1];
			List<Bus> buses = null;
			CtaConnect connect = CtaConnect.getInstance();
			MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
			if (busId != 0) {
				connectParam.put("vid", String.valueOf(busId));
			} else {
				connectParam.put("rt", busRouteId);
			}
			try {
				String content = connect.connect(CtaRequestType.BUS_VEHICLES, connectParam);
				Xml xml = new Xml();
				buses = xml.parseVehicles(content);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return buses;
		}

		@Override
		protected final void onPostExecute(final List<Bus> result) {
			if (result != null) {
				drawBuses(result);
				if (centerMap) {
					centerMapOnBus(result);
				}
			}
			if (stopRefresh) {
				stopRefreshAnimation();
			}
		}
	}

	private final class LoadCurrentPosition extends AsyncTask<Boolean, Void, Void> implements LocationListener {

		// The minimum distance to change Updates in meters
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
		// flag for GPS status
		private boolean isGPSEnabled = false;
		// flag for network status
		private boolean isNetworkEnabled = false;
		/** The location **/
		private Location location;
		/** The position **/
		private Position position;
		/** The latitude **/
		private double latitude;
		/** THe longitude **/
		private double longitude;
		/** The location manager **/
		private LocationManager locationManager;

		private boolean userCenter;

		@Override
		protected final Void doInBackground(final Boolean... params) {

			userCenter = params[0];

			locationManager = (LocationManager) MapActivity.this.getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				showSettingsAlert();
			} else {
				if (isNetworkEnabled) {
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
			}
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			int i = 0;
			while (map == null && i < 20) {
				map = mapFragment.getMap();
				i++;
			}
			if (map != null) {
				map = mapFragment.getMap();
				map.setMyLocationEnabled(true);
				locationManager.removeUpdates(LoadCurrentPosition.this);
				if (userCenter) {
					centerMapOnUser(position);
				}
			}
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
					MapActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
							alertDialogBuilder.setTitle("GPS settings");
							alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
							alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									MapActivity.this.startActivity(intent);
								}
							}).setNegativeButton("No", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
						}
					});
				}
			}.start();
		}
	}

	/**
	 * Load nearby data
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadPattern extends AsyncTask<Void, Void, Pattern> implements LocationListener {

		private Pattern pattern;

		@Override
		protected final Pattern doInBackground(final Void... params) {

			CtaConnect connect = CtaConnect.getInstance();
			MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
			connectParam.put("rt", busRouteId);
			String boundIgnoreCase = bound.toLowerCase(Locale.US);
			try {
				String content = connect.connect(CtaRequestType.BUS_PATTERN, connectParam);
				Xml xml = new Xml();
				List<Pattern> patterns = xml.parsePatterns(content);
				for (Pattern pattern : patterns) {
					String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
					if (pattern.getDirection().equals(bound) || boundIgnoreCase.indexOf(directionIgnoreCase) != -1) {
						this.pattern = pattern;
						break;
					}
				}
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return this.pattern;
		}

		@Override
		protected final void onPostExecute(final Pattern result) {
			if (result != null) {
				drawPattern(result);
			} else {
				Toast.makeText(MapActivity.this, "Sorry, could not load the path!", Toast.LENGTH_SHORT).show();
			}
			stopRefreshAnimation();
			startRefreshTask();
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

	private void centerMapOnUser(Position position) {
		int i = 0;
		while (map == null && i < 20) {
			map = mapFragment.getMap();
			i++;
		}
		if (map != null) {
			LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
		}
	}

	private void centerMapOnBus(List<Bus> result) {
		Bus bus = result.get(0);
		int i = 0;
		while (map == null && i < 20) {
			map = mapFragment.getMap();
			i++;
		}
		if (map != null) {
			LatLng latLng = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
		}
	}

	/**
	 * Start refresh task
	 */
	private void startRefreshTask() {
		refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void drawBuses(final List<Bus> buses) {
		if (map != null) {
			for (Marker marker : markers) {
				marker.remove();
			}
			markers.clear();
			for (Bus bus : buses) {
				Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.bus_gta);
				Bitmap bhalfsize = Bitmap.createScaledBitmap(icon, icon.getWidth() / 4, icon.getHeight() / 4, false);
				LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
				Marker marker = map.addMarker(new MarkerOptions().position(point).title(bus.getId() + "").snippet(bus.getId() + "")
						.icon(BitmapDescriptorFactory.fromBitmap(bhalfsize)).anchor(0.5f, 0.5f).rotation(bus.getHeading()).flat(true));
				markers.add(marker);
			}
		}
	}

	private void drawPattern(final Pattern pattern) {
		int i = 0;
		while (map == null && i < 20) {
			map = mapFragment.getMap();
			i++;
		}
		if (map != null) {
			final List<Marker> markers = new ArrayList<Marker>();
			PolylineOptions poly = new PolylineOptions();
			poly.geodesic(true).color(Color.BLUE);
			for (PatternPoint patternPoint : pattern.getPoints()) {
				LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
				poly.add(point);
				if (patternPoint.getStopId() != null) {
					Marker marker = map.addMarker(new MarkerOptions().position(point).title(patternPoint.getStopName())
							.snippet(patternPoint.getSequence() + "").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					markers.add(marker);
					marker.setVisible(false);
				}
			}
			map.addPolyline(poly);

			map.setOnCameraChangeListener(new OnCameraChangeListener() {
				private float currentZoom = -1;

				@Override
				public void onCameraChange(CameraPosition pos) {
					if (pos.zoom != currentZoom) {
						currentZoom = pos.zoom;
						if (currentZoom >= 16) {
							for (Marker marker : markers) {
								marker.setVisible(true);
							}
						} else {
							for (Marker marker : markers) {
								marker.setVisible(false);
							}
						}
					}
				}
			});
		}
	}

	/**
	 * RefreshTask
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class RefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected final void onProgressUpdate(Void... values) {
			super.onProgressUpdate();
			startRefreshAnimation();
			new LoadCurrentPosition().execute(new Boolean[] { false });
			new LoadBusPosition().execute(new Boolean[] { false, true });
		}

		@Override
		protected final Void doInBackground(final Void... params) {
			while (!this.isCancelled()) {
				Log.i(TAG, "Updated of time " + Thread.currentThread().getId());
				try {
					publishProgress();
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Log.i(TAG, "Stopping thread. Normal Behavior");
				}
			}
			return null;
		}
	}
}
