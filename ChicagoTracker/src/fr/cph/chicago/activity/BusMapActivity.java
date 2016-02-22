/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import fr.cph.chicago.ChicagoTracker;
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
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.listener.BusMapOnCameraChangeListener;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapActivity extends Activity {
	/**
	 * Tag
	 **/
	private static final String TAG = "BusMapActivity";
	/**
	 * The map fragment from google api
	 **/
	private MapFragment mapFragment;
	/**
	 * The map
	 **/
	private GoogleMap googleMap;
	/**
	 * Bus id
	 **/
	private Integer busId;
	/**
	 * Bus route id
	 **/
	private String busRouteId;
	/**
	 * Bounds
	 **/
	private String[] bounds;
	/**
	 * Bus Markers
	 **/
	private List<Marker> busMarkers;
	/**
	 * Station Markers
	 **/
	private List<Marker> busStationMarkers;
	/**
	 * Menu
	 **/
	private Menu menu;
	/**
	 * Refreshing info window
	 **/
	private boolean refreshingInfoWindow = false;
	/**
	 * Selected marker
	 **/
	private Marker selectedMarker;
	/**
	 * Map views
	 **/
	private Map<Marker, View> views;
	/**
	 * Map status
	 **/
	private Map<Marker, Boolean> status;
	/**
	 * On camera change zoom listener
	 **/
	private BusMapOnCameraChangeListener busListener;

	private boolean centerMap = true;

	private boolean loadPattern = true;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			if (savedInstanceState != null) {
				busId = savedInstanceState.getInt("busId");
				busRouteId = savedInstanceState.getString("busRouteId");
				bounds = savedInstanceState.getStringArray("bounds");
			} else {
				busId = getIntent().getExtras().getInt("busId");
				busRouteId = getIntent().getExtras().getString("busRouteId");
				bounds = getIntent().getExtras().getStringArray("bounds");
			}

			busMarkers = new ArrayList<>();
			busStationMarkers = new ArrayList<>();
			views = new HashMap<>();
			status = new HashMap<>();
			busListener = new BusMapOnCameraChangeListener();

			final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

			toolbar.inflateMenu(R.menu.main);
			toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					startRefreshAnimation();
					new LoadCurrentPosition().execute();
					new LoadBusPosition().execute(false, true);
					return false;
				}
			}));

			Util.setToolbarColor(this, toolbar, TrainLine.NA);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}

			toolbar.setTitle(busRouteId);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			Util.trackScreen(getResources().getString(R.string.analytics_bus_map));
		}
	}

	@Override
	public final void onStart() {
		super.onStart();
		if (mapFragment == null) {
			final FragmentManager fm = getFragmentManager();
			mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
			final GoogleMapOptions options = new GoogleMapOptions();
			final CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 10, 0, 0);
			options.camera(camera);
			mapFragment = MapFragment.newInstance(options);
			mapFragment.setRetainInstance(true);
			fm.beginTransaction().replace(R.id.map, mapFragment).commit();
		}
	}

	@Override
	public final void onPause() {
		super.onPause();
	}

	@Override
	public final void onStop() {
		super.onStop();
		centerMap = false;
		loadPattern = false;
		googleMap = null;
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (googleMap == null) {
			googleMap = mapFragment.getMap();
			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					return null;
				}

				@Override
				public View getInfoContents(Marker marker) {
					if (!marker.getSnippet().equals("")) {
						final View view = views.get(marker);
						if (!refreshingInfoWindow) {
							selectedMarker = marker;
							final String busId = marker.getSnippet();
							startRefreshAnimation();
							new LoadBusFollow(view, false).execute(busId);
							status.put(marker, false);
						}
						return view;
					} else {
						return null;
					}
				}
			});

			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					if (!marker.getSnippet().equals("")) {
						final View view = views.get(marker);
						if (!refreshingInfoWindow) {
							selectedMarker = marker;
							final String runNumber = marker.getSnippet();
							startRefreshAnimation();
							boolean current = status.get(marker);
							new LoadBusFollow(view, !current).execute(runNumber);
							status.put(marker, !current);
						}
					}
				}
			});
		}
		if (Util.isNetworkAvailable()) {
			startRefreshAnimation();
			new LoadCurrentPosition().execute();
			new LoadBusPosition().execute(centerMap, !loadPattern);
			if (loadPattern) {
				new LoadPattern().execute();
			}
		} else {
			Toast.makeText(this, "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busId = savedInstanceState.getInt("busId");
		busRouteId = savedInstanceState.getString("busRouteId");
		bounds = savedInstanceState.getStringArray("bounds");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("busId", busId);
		savedInstanceState.putString("busRouteId", busRouteId);
		savedInstanceState.putStringArray("bounds", bounds);
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
			new LoadCurrentPosition().execute();
			new LoadBusPosition().execute(false, true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 *
	 */
	private void refreshInfoWindow() {
		if (selectedMarker == null) {
			return;
		}
		refreshingInfoWindow = true;
		selectedMarker.showInfoWindow();
		refreshingInfoWindow = false;
		stopRefreshAnimation();
	}

	/**
	 * Load animation in menu
	 */
	private void startRefreshAnimation() {
		if (menu != null) {
			final MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			if (refreshMenuItem.getActionView() == null) {
				refreshMenuItem.setActionView(R.layout.progressbar);
				refreshMenuItem.expandActionView();
			}
		}
	}

	/**
	 * Stop animation in menu
	 */
	private void stopRefreshAnimation() {
		if (menu != null) {
			final MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	/**
	 * @param result
	 */
	private void centerMapOnBus(final List<Bus> result) {
		int i = 0;
		while (googleMap == null && i < 20) {
			googleMap = mapFragment.getMap();
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
		if (googleMap != null) {
			final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
		}
	}

	/**
	 * @param buses
	 */
	private void drawBuses(final List<Bus> buses) {
		if (googleMap != null) {
			for (Marker marker : busMarkers) {
				marker.remove();
			}
			busMarkers.clear();
			final Bitmap bitmap = busListener.getCurrentBitmap();
			for (final Bus bus : buses) {
				final LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
				final Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title("To " + bus.getDestination()).snippet(bus.getId() + "")
						.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f).rotation(bus.getHeading()).flat(true));
				busMarkers.add(marker);

				final LayoutInflater layoutInflater = (LayoutInflater) BusMapActivity.this.getBaseContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				final View view = layoutInflater.inflate(R.layout.marker_train, null);
				final TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(marker.getTitle());

				views.put(marker, view);
			}

			busListener.setBusMarkers(busMarkers);

			googleMap.setOnCameraChangeListener(busListener);
		}
	}

	/**
	 * @param patterns
	 */
	private void drawPattern(final List<BusPattern> patterns) {
		int i = 0;
		while (googleMap == null && i < 20) {
			googleMap = mapFragment.getMap();
			i++;
		}
		if (googleMap != null) {
			int j = 0;
			final BitmapDescriptor red = BitmapDescriptorFactory.defaultMarker();
			BitmapDescriptor blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
			MarkerOptions options;
			for (final BusPattern pattern : patterns) {
				final PolylineOptions poly = new PolylineOptions();
				if (j == 0) {
					poly.geodesic(true).color(Color.RED);
				} else if (j == 1) {
					poly.geodesic(true).color(Color.BLUE);
				} else {
					poly.geodesic(true).color(Color.YELLOW);
				}
				poly.width(7f);
				for (final PatternPoint patternPoint : pattern.getPoints()) {
					final LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
					poly.add(point);
					if (patternPoint.getStopId() != null) {
						options = new MarkerOptions();
						options.position(point).title(patternPoint.getStopName() + " (" + pattern.getDirection() + ")").snippet("");
						if (j == 0) {
							options.icon(red);
						} else {
							// To modify to blue when no freeze issue
							options.icon(blue);
						}

						final Marker marker = googleMap.addMarker(options);
						busStationMarkers.add(marker);
						marker.setVisible(false);
					}
				}
				googleMap.addPolyline(poly);
				j++;
			}

			busListener.setBusStationMarkers(busStationMarkers);

			googleMap.setOnCameraChangeListener(busListener);
		}
	}

	private class LoadBusPosition extends AsyncTask<Boolean, Void, List<Bus>> {
		/**
		 * Allow or not centering the map
		 **/
		private boolean centerMap;
		/**
		 * Stop refresh animation or not
		 **/
		private boolean stopRefresh;

		@Override
		protected List<Bus> doInBackground(Boolean... params) {
			centerMap = params[0];
			stopRefresh = params[1];
			List<Bus> buses = null;
			final CtaConnect connect = CtaConnect.getInstance();
			final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
			if (busId != 0) {
				connectParam.put("vid", String.valueOf(busId));
			} else {
				connectParam.put("rt", busRouteId);
			}
			try {
				final String content = connect.connect(CtaRequestType.BUS_VEHICLES, connectParam);
				final Xml xml = new Xml();
				buses = xml.parseVehicles(content);
			} catch (final ConnectException | ParserException e) {
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
			} else {
				Toast.makeText(BusMapActivity.this, "Error while loading data!", Toast.LENGTH_SHORT).show();
			}
			if (stopRefresh) {
				stopRefreshAnimation();
			}
		}
	}

	private class LoadCurrentPosition extends AsyncTask<Boolean, Void, Void> implements LocationListener {
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
		 * The location manager
		 **/
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
					if (ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED
							&& ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(BusMapActivity.this,
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
						if (ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED
								&& ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(BusMapActivity.this,
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
			}
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			int i = 0;
			while (googleMap == null && i < 20) {
				googleMap = mapFragment.getMap();
				i++;
			}
			if (googleMap != null) {
				googleMap = mapFragment.getMap();
				if (ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(BusMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(BusMapActivity.this,
							new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
					return;
				}
				googleMap.setMyLocationEnabled(true);
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
							final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BusMapActivity.this);
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
							final AlertDialog alertDialog = alertDialogBuilder.create();
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
	 */
	private class LoadPattern extends AsyncTask<Void, Void, List<BusPattern>> {
		/**
		 * List of bus pattern
		 **/
		private List<BusPattern> patterns;

		@Override
		protected final List<BusPattern> doInBackground(final Void... params) {
			this.patterns = new ArrayList<>();
			final CtaConnect connect = CtaConnect.getInstance();
			try {
				if (busId == 0) {
					final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
					reqParams.put("rt", busRouteId);
					final Xml xml = new Xml();
					String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
					final BusDirections busDirections = xml.parseBusDirections(xmlResult, busRouteId);
					bounds = new String[busDirections.getlBusDirection().size()];
					for (int i = 0; i < busDirections.getlBusDirection().size(); i++) {
						bounds[i] = busDirections.getlBusDirection().get(i).toString();
					}
					Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
							R.string.analytics_action_get_bus_direction, 0);
				}

				final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
				connectParam.put("rt", busRouteId);
				final String content = connect.connect(CtaRequestType.BUS_PATTERN, connectParam);
				final Xml xml = new Xml();
				final List<BusPattern> patterns = xml.parsePatterns(content);
				for (final BusPattern pattern : patterns) {
					String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
					for (final String bound : bounds) {
						if (pattern.getDirection().equals(bound) || bound.toLowerCase(Locale.US).contains(directionIgnoreCase)) {
							this.patterns.add(pattern);
						}
					}
				}
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
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
	}

	private class LoadBusFollow extends AsyncTask<String, Void, List<BusArrival>> {
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
			List<BusArrival> arrivals = new ArrayList<>();
			try {
				CtaConnect connect = CtaConnect.getInstance();
				MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
				connectParam.put("vid", busId);
				String content = connect.connect(CtaRequestType.BUS_ARRIVALS, connectParam);
				Xml xml = new Xml();
				arrivals = xml.parseBusArrivals(content);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_arrival, 0);
			if (!loadAll && arrivals.size() > 7) {
				arrivals = arrivals.subList(0, 6);
				final BusArrival arrival = new BusArrival();
				arrival.setStopName("Display all results");
				arrival.setIsDly(false);
				arrivals.add(arrival);
			}
			return arrivals;
		}

		@Override
		protected final void onPostExecute(final List<BusArrival> result) {
			final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
			final TextView error = (TextView) view.findViewById(R.id.error);
			if (result.size() != 0) {
				final BusMapSnippetAdapter ada = new BusMapSnippetAdapter(result);
				arrivals.setAdapter(ada);
				arrivals.setVisibility(ListView.VISIBLE);
				error.setVisibility(TextView.GONE);
			} else {
				arrivals.setVisibility(ListView.GONE);
				error.setVisibility(TextView.VISIBLE);
			}
			refreshInfoWindow();
		}
	}
}
