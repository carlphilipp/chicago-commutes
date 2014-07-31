package fr.cph.chicago.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusMapSnippetAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.listener.BusMapOnCameraChangeListener;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapActivity extends Activity {
	/** Tag **/
	private static final String TAG = "BusMapActivity";
	/** The map fragment from google api **/
	private MapFragment mMapFragment;
	/** The map **/
	private GoogleMap mGooMap;
	/** Bus id **/
	private Integer mBusId;
	/** Bus route id **/
	private String mBusRouteId;
	/** Bounds **/
	private String[] mBounds;
	/** Bus Markers **/
	private List<Marker> mBusMarkers;
	/** Station Markers **/
	private List<Marker> mBusStationMarkers;
	/** Menu **/
	private Menu mMenu;
	/** Refreshing info window **/
	private boolean mRefreshingInfoWindow = false;
	/** Selected marker **/
	private Marker mSelectedMarker;
	/** Map views **/
	private Map<Marker, View> mViews;
	/** Map status **/
	private Map<Marker, Boolean> mStatus;
	/** On camera change zoom listener **/
	private BusMapOnCameraChangeListener mBusListener;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			if (savedInstanceState != null) {
				mBusId = savedInstanceState.getInt("busId");
				mBusRouteId = savedInstanceState.getString("busRouteId");
				mBounds = savedInstanceState.getStringArray("bounds");
			} else {
				mBusId = getIntent().getExtras().getInt("busId");
				mBusRouteId = getIntent().getExtras().getString("busRouteId");
				mBounds = getIntent().getExtras().getStringArray("bounds");
			}

			mBusMarkers = new ArrayList<Marker>();
			mBusStationMarkers = new ArrayList<Marker>();
			mViews = new HashMap<Marker, View>();
			mStatus = new HashMap<Marker, Boolean>();
			mBusListener = new BusMapOnCameraChangeListener();

			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public final void onStart() {
		super.onStart();
		FragmentManager fm = getFragmentManager();
		mMapFragment = (MapFragment) fm.findFragmentById(R.id.map);
		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 10, 0, 0);
		options.camera(camera);
		mMapFragment = MapFragment.newInstance(options);
		mMapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mMapFragment).commit();
	}

	@Override
	public final void onPause() {
		super.onPause();
	}

	@Override
	public final void onStop() {
		super.onStop();
		mGooMap = null;
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (mGooMap == null) {
			mGooMap = mMapFragment.getMap();
			mGooMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					return null;
				}

				@Override
				public View getInfoContents(Marker marker) {
					if (!marker.getSnippet().equals("")) {
						View view = mViews.get(marker);
						if (!mRefreshingInfoWindow) {
							mSelectedMarker = marker;
							String busId = marker.getSnippet();
							startRefreshAnimation();
							new LoadBusFollow(view, false).execute(busId);
							mStatus.put(marker, false);
						}
						return view;
					} else {
						return null;
					}
				}
			});

			mGooMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					if (!marker.getSnippet().equals("")) {
						final View view = mViews.get(marker);
						if (!mRefreshingInfoWindow) {
							mSelectedMarker = marker;
							final String runNumber = marker.getSnippet();
							startRefreshAnimation();
							boolean current = mStatus.get(marker);
							new LoadBusFollow(view, !current).execute(runNumber);
							mStatus.put(marker, !current);
						}
					}
				}
			});
		}
		if (Util.isNetworkAvailable()) {
			if (mBusId == 0) {
				new LoadCurrentPosition().execute();
				new LoadBusPosition().execute(new Boolean[] { true, false });
			} else {
				new LoadCurrentPosition().execute();
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
		mBusId = savedInstanceState.getInt("busId");
		mBusRouteId = savedInstanceState.getString("busRouteId");
		mBounds = savedInstanceState.getStringArray("bounds");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("busId", mBusId);
		savedInstanceState.putString("busRouteId", mBusRouteId);
		savedInstanceState.putStringArray("bounds", mBounds);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.mMenu = menu;
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
			new LoadCurrentPosition().execute();
			new LoadBusPosition().execute(new Boolean[] { false, true });
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 
	 */
	private void refreshInfoWindow() {
		if (mSelectedMarker == null) {
			return;
		}
		mRefreshingInfoWindow = true;
		mSelectedMarker.showInfoWindow();
		mRefreshingInfoWindow = false;
		stopRefreshAnimation();
	}

	/**
	 * Load animation in menu
	 */
	private final void startRefreshAnimation() {
		if (mMenu != null) {
			MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
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
		if (mMenu != null) {
			MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	/**
	 * @param result
	 */
	private void centerMapOnBus(final List<Bus> result) {
		int i = 0;
		while (mGooMap == null && i < 20) {
			mGooMap = mMapFragment.getMap();
			i++;
		}
		Position position;
		int zoom;
		if (result.size() == 1) {
			position = result.get(0).getPosition();
			zoom = 15;
		} else {
			position = Bus.getBestPosition(result);
			zoom = 11;
		}
		if (mGooMap != null) {
			LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			mGooMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
		}
	}

	/**
	 * @param buses
	 */
	private void drawBuses(final List<Bus> buses) {
		if (mGooMap != null) {
			for (Marker marker : mBusMarkers) {
				marker.remove();
			}
			mBusMarkers.clear();
			final Bitmap bitmap = mBusListener.getCurrentBitmap();
			for (Bus bus : buses) {
				LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
				Marker marker = mGooMap.addMarker(new MarkerOptions().position(point).title("To: " + bus.getDestination()).snippet(bus.getId() + "")
						.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f).rotation(bus.getHeading()).flat(true));
				mBusMarkers.add(marker);

				LayoutInflater layoutInflater = (LayoutInflater) BusMapActivity.this.getBaseContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View view = layoutInflater.inflate(R.layout.marker_train, null);
				TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(marker.getTitle());

				mViews.put(marker, view);
			}

			mBusListener.setBusMarkers(mBusMarkers);

			mGooMap.setOnCameraChangeListener(mBusListener);
		}
	}

	/**
	 * @param patterns
	 */
	private void drawPattern(final List<BusPattern> patterns) {
		int i = 0;
		while (mGooMap == null && i < 20) {
			mGooMap = mMapFragment.getMap();
			i++;
		}
		if (mGooMap != null) {
			int j = 0;
			for (BusPattern pattern : patterns) {
				PolylineOptions poly = new PolylineOptions();
				if (j == 0) {
					poly.geodesic(true).color(Color.RED);
				} else {
					poly.geodesic(true).color(Color.BLUE);
				}

				poly.width(7f);
				for (PatternPoint patternPoint : pattern.getPoints()) {
					LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
					poly.add(point);
					if (patternPoint.getStopId() != null) {
						MarkerOptions options = new MarkerOptions();
						options.position(point).title(patternPoint.getStopName() + " (" + pattern.getDirection() + ")").snippet("");
						if (j == 0) {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
						} else {
							options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
						}

						Marker marker = mGooMap.addMarker(options);
						mBusStationMarkers.add(marker);
						marker.setVisible(false);
					}
				}
				mGooMap.addPolyline(poly);
				j++;
			}

			mBusListener.setBusStationMarkers(mBusStationMarkers);

			mGooMap.setOnCameraChangeListener(mBusListener);
		}
	}

	private final class LoadBusPosition extends AsyncTask<Boolean, Void, List<Bus>> {
		/** Allow or not centering the map **/
		private boolean centerMap;
		/** Stop refresh animation or not **/
		private boolean stopRefresh;

		@Override
		protected List<Bus> doInBackground(Boolean... params) {
			centerMap = params[0];
			stopRefresh = params[1];
			List<Bus> buses = null;
			CtaConnect connect = CtaConnect.getInstance();
			MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
			if (mBusId != 0) {
				connectParam.put("vid", String.valueOf(mBusId));
			} else {
				connectParam.put("rt", mBusRouteId);
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
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_vehicles, 0);
			return buses;
		}

		@Override
		protected final void onPostExecute(final List<Bus> result) {
			if (result != null) {
				drawBuses(result);
				if (result.size() != 0) {
					if (centerMap) {
						centerMapOnBus(result);
					}
				} else {
					Toast.makeText(BusMapActivity.this, "No bus found!", Toast.LENGTH_LONG).show();
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

		@Override
		protected final Void doInBackground(final Boolean... params) {

			locationManager = (LocationManager) BusMapActivity.this.getSystemService(Context.LOCATION_SERVICE);

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
			while (mGooMap == null && i < 20) {
				mGooMap = mMapFragment.getMap();
				i++;
			}
			if (mGooMap != null) {
				mGooMap = mMapFragment.getMap();
				mGooMap.setMyLocationEnabled(true);
				locationManager.removeUpdates(LoadCurrentPosition.this);
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
					BusMapActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BusMapActivity.this);
							alertDialogBuilder.setTitle("GPS settings");
							alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
							alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									BusMapActivity.this.startActivity(intent);
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
	private final class LoadPattern extends AsyncTask<Void, Void, List<BusPattern>> implements LocationListener {
		/** **/
		private List<BusPattern> patterns;

		@Override
		protected final List<BusPattern> doInBackground(final Void... params) {
			this.patterns = new ArrayList<BusPattern>();
			CtaConnect connect = CtaConnect.getInstance();
			try {
				if (mBusId == 0) {
					MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
					reqParams.put("rt", mBusRouteId);
					Xml xml = new Xml();
					String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
					BusDirections busDirections = xml.parseBusDirections(xmlResult, mBusRouteId);
					mBounds = new String[busDirections.getlBusDirection().size()];
					for (int i = 0; i < busDirections.getlBusDirection().size(); i++) {
						mBounds[i] = busDirections.getlBusDirection().get(i).toString();
					}
				}

				MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
				connectParam.put("rt", mBusRouteId);
				String content = connect.connect(CtaRequestType.BUS_PATTERN, connectParam);
				Xml xml = new Xml();
				List<BusPattern> patterns = xml.parsePatterns(content);
				for (BusPattern pattern : patterns) {
					String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
					for (String bound : mBounds) {
						if (pattern.getDirection().equals(bound) || bound.toLowerCase(Locale.US).indexOf(directionIgnoreCase) != -1) {
							this.patterns.add(pattern);
						}
					}
				}
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_direction, 0);
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_pattern, 0);
			return this.patterns;
		}

		@Override
		protected final void onPostExecute(final List<BusPattern> result) {
			if (result != null) {
				drawPattern(result);
			} else {
				Toast.makeText(BusMapActivity.this, "Sorry, could not load the path!", Toast.LENGTH_SHORT).show();
			}
			stopRefreshAnimation();
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

	private final class LoadBusFollow extends AsyncTask<String, Void, List<BusArrival>> {
		/** **/
		private View view;
		/** **/
		private boolean loadAll;

		/**
		 * @param view
		 * @param loadAll
		 */
		public LoadBusFollow(final View view, final boolean loadAll) {
			this.view = view;
			this.loadAll = loadAll;
		}

		@Override
		protected List<BusArrival> doInBackground(final String... params) {
			final String busId = params[0];
			List<BusArrival> arrivals = new ArrayList<BusArrival>();
			try {
				CtaConnect connect = CtaConnect.getInstance();
				MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
				connectParam.put("vid", busId);
				String content = connect.connect(CtaRequestType.BUS_ARRIVALS, connectParam);
				Xml xml = new Xml();
				arrivals = xml.parseBusArrivals(content);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_arrival, 0);
			if (!loadAll && arrivals.size() > 7) {
				arrivals = arrivals.subList(0, 6);
				BusArrival arrival = new BusArrival();
				arrival.setStopName("Display all results");
				arrival.setIsDly(false);
				arrivals.add(arrival);
			}
			return arrivals;
		}

		@Override
		protected final void onPostExecute(final List<BusArrival> result) {
			if (result.size() != 0) {
				ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
				BusMapSnippetAdapter ada = new BusMapSnippetAdapter(result);
				arrivals.setAdapter(ada);
			} else {
				TextView error = (TextView) view.findViewById(R.id.error);
				error.setVisibility(TextView.VISIBLE);
			}
			refreshInfoWindow();
		}
	}
}
