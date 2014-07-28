package fr.cph.chicago.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainMapSnippetAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

public class TrainMapActivity extends Activity {
	/** Tag **/
	private static final String TAG = "TrainMapActivity";
	/** The map fragment from google api **/
	private MapFragment mapFragment;
	/** The map **/
	private GoogleMap map;
	/** Line **/
	private String line;
	/** Bus Markers **/
	private List<Marker> markers;
	/** Markers **/
//	private List<Marker> stationMarkers;
	/** Menu **/
	private Menu menu;
	/** A refresh task **/
	private RefreshTask refreshTimingTask;
	/** Train data **/
	private TrainData data;

	private boolean mRefreshingInfoWindow = false;
	private Marker mSelectedMarker = null;
	private Map<Marker, View> views;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			if (savedInstanceState != null) {
				line = savedInstanceState.getString("line");
			} else {
				line = getIntent().getExtras().getString("line");
			}
			
			Log.i(TAG,"Set line: " + line);

			// Load data
			DataHolder dataHolder = DataHolder.getInstance();
			this.data = dataHolder.getTrainData();

			markers = new ArrayList<Marker>();
			
			//stationMarkers = new ArrayList<Marker>();

			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public final void onStart() {
		super.onStart();
		FragmentManager fm = getFragmentManager();
		mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 10, 0, 0);
		options.camera(camera);
		mapFragment = MapFragment.newInstance(options);
		mapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onPause() {
		super.onPause();
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
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

			map.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					return null;
				}

				@Override
				public View getInfoContents(Marker marker) {
					if (!marker.getSnippet().equals("")) {
						View view = views.get(marker);
						if (!mRefreshingInfoWindow) {
							mSelectedMarker = marker;
							String runNumber = marker.getSnippet();
							startRefreshAnimation();
							new LoadTrainFollow(view).execute(runNumber);
						}
						return view;
					} else {
						return null;
					}
				}
			});
		}
		if (refreshTimingTask != null && refreshTimingTask.getStatus() == Status.FINISHED) {
			startRefreshTask();
		}
		if (Util.isNetworkAvailable()) {
			new LoadCurrentPosition().execute();
			new LoadTrainPosition().execute(new Boolean[] { true, true });
		} else {
			Toast.makeText(this, "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
	}

	private void refreshInfoWindow() {
		if (mSelectedMarker == null) {
			return;
		}
		mRefreshingInfoWindow = true;
		mSelectedMarker.showInfoWindow();
		mRefreshingInfoWindow = false;
		stopRefreshAnimation();
	}

	private final class LoadTrainFollow extends AsyncTask<String, Void, List<Eta>> {

		private View view;

		public LoadTrainFollow(View view) {
			this.view = view;
		}

		@Override
		protected List<Eta> doInBackground(String... params) {
			final String runNumber = params[0];
			List<Eta> etas = new ArrayList<Eta>();
			try {
				CtaConnect connect = CtaConnect.getInstance();
				MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
				connectParam.put("runnumber", runNumber);
				String content = connect.connect(CtaRequestType.TRAIN_FOLLOW, connectParam);
				Xml xml = new Xml();
				etas = xml.parseTrainsFollow(content, data);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return etas;
		}

		@Override
		protected final void onPostExecute(final List<Eta> result) {
			if (result.size() != 0) {
				ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
				TrainMapSnippetAdapter ada = new TrainMapSnippetAdapter(result);
				arrivals.setAdapter(ada);
			} else {
				TextView error = (TextView) view.findViewById(R.id.error);
				error.setVisibility(TextView.VISIBLE);
			}
			refreshInfoWindow();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		/* busId = savedInstanceState.getInt("busId"); */
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		/* savedInstanceState.putInt("busId", busId); */
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.main_no_search, menu);
		// startRefreshAnimation();
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
			new LoadCurrentPosition().execute();
			// new LoadTrainPosition().execute();
			new LoadTrainPosition().execute(new Boolean[] { false, true });
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

	private final class LoadTrainPosition extends AsyncTask<Boolean, Void, List<Train>> {

		private boolean centerMap;

		private boolean stopRefresh;

		private List<Position> patterns;

		@Override
		protected List<Train> doInBackground(Boolean... params) {
			centerMap = params[0];
			stopRefresh = params[1];
			List<Train> trains = null;
			CtaConnect connect = CtaConnect.getInstance();
			MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
			connectParam.put("rt", line);
			try {
				String content = connect.connect(CtaRequestType.TRAIN_LOCATION, connectParam);
				Xml xml = new Xml();
				trains = xml.parseTrainsLocation(content);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			patterns = TrainMapActivity.this.data.readPattern(TrainLine.fromXmlString(TrainMapActivity.this.line));
			return trains;
		}

		@Override
		protected final void onPostExecute(final List<Train> result) {
			if (result != null) {
				drawTrains(result, patterns);
				if (result.size() != 0) {
					if (centerMap) {
						centerMapOnBus(result);
					}
				} else {
					Toast.makeText(TrainMapActivity.this, "No trains found!", Toast.LENGTH_LONG).show();
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

		// private boolean userCenter;

		@Override
		protected final Void doInBackground(final Boolean... params) {

			// userCenter = params[0];

			locationManager = (LocationManager) TrainMapActivity.this.getSystemService(Context.LOCATION_SERVICE);

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
				/*
				 * if (userCenter) { centerMapOnUser(position); }
				 */
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
					TrainMapActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrainMapActivity.this);
							alertDialogBuilder.setTitle("GPS settings");
							alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
							alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									TrainMapActivity.this.startActivity(intent);
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

	private void centerMapOnBus(List<Train> result) {
		int i = 0;
		while (map == null && i < 20) {
			map = mapFragment.getMap();
			i++;
		}
		Position position;
		int zoom;
		if (result.size() == 1) {
			position = result.get(0).getPosition();
			zoom = 15;
		} else {
			position = Train.getBestPosition(result);
			zoom = 11;
		}
		if (map != null) {
			LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
		}
	}

	/**
	 * Start refresh task
	 */
	private void startRefreshTask() {
		if (!isFinishing()) {
			// refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void drawTrains(final List<Train> trains, final List<Position> patterns) {
		if (map != null) {
			if (views != null) {
				views.clear();
			}
			views = new HashMap<Marker, View>();
			for (Marker marker : markers) {
				marker.remove();
			}
			markers.clear();
			for (Train train : trains) {
				final Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.train_gta);
				final Bitmap bhalfsize = Bitmap.createScaledBitmap(icon, icon.getWidth() / 4, icon.getHeight() / 4, false);
				final LatLng point = new LatLng(train.getPosition().getLatitude(), train.getPosition().getLongitude());
				final String title = "To " + train.getDestName();
				final String snippet = String.valueOf(train.getRouteNumber());

				final Marker marker = map.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet)
						.icon(BitmapDescriptorFactory.fromBitmap(bhalfsize)).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
				markers.add(marker);

				LayoutInflater layoutInflater = (LayoutInflater) TrainMapActivity.this.getBaseContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View view = layoutInflater.inflate(R.layout.marker_train, null);
				TextView title2 = (TextView) view.findViewById(R.id.title);
				title2.setText(marker.getTitle());
				
				TextView color = (TextView) view.findViewById(R.id.route_color_value);
				color.setBackgroundColor(TrainLine.fromXmlString(TrainMapActivity.this.line).getColor());
				
				views.put(marker, view);
			}

			PolylineOptions poly = new PolylineOptions();
			poly.width(7f);
			poly.geodesic(true).color(TrainLine.fromXmlString(this.line).getColor());
			for (Position position : patterns) {
				LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
				poly.add(point);
/*				Marker marker = map.addMarker(new MarkerOptions().position(point).title("").snippet("")
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				marker.setVisible(false);
				stationMarkers.add(marker);*/
			}
			map.addPolyline(poly);
			/*map.setOnCameraChangeListener(new OnCameraChangeListener() {
				private float currentZoom = -1;

				@Override
				public void onCameraChange(CameraPosition pos) {
					if (pos.zoom != currentZoom) {
						currentZoom = pos.zoom;
						if (currentZoom >= 16) {
							for (Marker marker : stationMarkers) {
								marker.setVisible(true);
							}
						} else {
							for (Marker marker : stationMarkers) {
								marker.setVisible(false);
							}
						}
					}
				}
			});*/
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
			// new LoadCurrentPosition().execute();
			// new LoadBusPosition().execute(new Boolean[] { false, true });
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
