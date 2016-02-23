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
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainMapSnippetAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.listener.TrainMapOnCameraChangeListener;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapActivity extends Activity {

	private static final String TAG = TrainMapActivity.class.getSimpleName();

	private MapFragment mapFragment;

	private GoogleMap googleMap;

	private String line;

	private List<Marker> markers;

	private TrainData trainData;

	private boolean refreshingInfoWindow = false;

	private Marker selectedMarker;

	private Map<Marker, View> views;

	private Map<Marker, Boolean> status;

	private TrainMapOnCameraChangeListener trainListener;

	private boolean centerMap = true;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			if (savedInstanceState != null) {
				line = savedInstanceState.getString("line");
			} else {
				line = getIntent().getExtras().getString("line");
			}

			// Load data
			final DataHolder dataHolder = DataHolder.getInstance();
			trainData = dataHolder.getTrainData();

			markers = new ArrayList<>();
			status = new HashMap<>();
			trainListener = new TrainMapOnCameraChangeListener();

			final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

			toolbar.inflateMenu(R.menu.main);
			toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					new LoadCurrentPosition().execute();
					new LoadTrainPosition().execute(false, true);
					return false;
				}
			}));

			final TrainLine trainLine = TrainLine.fromXmlString(line);

			Util.setToolbarColor(this, toolbar, trainLine);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}

			toolbar.setTitle(trainLine.toString() + " Line");

			Util.trackScreen(getResources().getString(R.string.analytics_train_map));
		}
	}

	@Override
	public final void onRestart() {
		super.onRestart();
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
							final String runNumber = marker.getSnippet();
							new LoadTrainFollow(view, false).execute(runNumber);
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
							final boolean current = status.get(marker);
							new LoadTrainFollow(view, !current).execute(runNumber);
							status.put(marker, !current);
						}
					}
				}
			});
		}
		if (Util.isNetworkAvailable()) {
			new LoadCurrentPosition().execute();
			new LoadTrainPosition().execute(centerMap, true);
		} else {
			Toast.makeText(this, "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		line = savedInstanceState.getString("line");
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState) {
		savedInstanceState.putString("line", line);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Refresh windows
	 */
	private void refreshInfoWindow() {
		if (selectedMarker == null) {
			return;
		}
		refreshingInfoWindow = true;
		selectedMarker.showInfoWindow();
		refreshingInfoWindow = false;
	}

	/**
	 * @param result
	 */
	private void centerMapOnBus(final List<Train> result) {
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
			position = Train.getBestPosition(result);
			zoom = 11;
		}
		if (googleMap != null) {
			final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
		}
	}

	/**
	 * @param trains    the list of trains
	 * @param positions the list of positions
	 */
	private void drawTrains(final List<Train> trains, final List<Position> positions) {
		if (googleMap != null) {
			if (views != null) {
				views.clear();
			}
			views = new HashMap<>();
			for (final Marker marker : markers) {
				marker.remove();
			}
			markers.clear();
			final Bitmap bitmap = trainListener.getCurrentBitmap();
			for (final Train train : trains) {
				final LatLng point = new LatLng(train.getPosition().getLatitude(), train.getPosition().getLongitude());
				final String title = "To " + train.getDestName();
				final String snippet = String.valueOf(train.getRouteNumber());

				final Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet)
						.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
				markers.add(marker);

				final LayoutInflater layoutInflater = (LayoutInflater) TrainMapActivity.this.getBaseContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				final View view = layoutInflater.inflate(R.layout.marker_train, null);
				final TextView title2 = (TextView) view.findViewById(R.id.title);
				title2.setText(title);

				final TextView color = (TextView) view.findViewById(R.id.route_color_value);
				color.setBackgroundColor(TrainLine.fromXmlString(TrainMapActivity.this.line).getColor());

				views.put(marker, view);
			}

			trainListener.setTrainMarkers(markers);

			googleMap.setOnCameraChangeListener(trainListener);

			final PolylineOptions poly = new PolylineOptions();
			poly.width(7f);
			poly.geodesic(true).color(TrainLine.fromXmlString(this.line).getColor());
			for (final Position position : positions) {
				final LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
				poly.add(point);
			}
			googleMap.addPolyline(poly);
		}
	}

	private class LoadTrainFollow extends AsyncTask<String, Void, List<Eta>> {
		/**
		 * Current view
		 **/
		private View view;
		/**
		 * Load all
		 **/
		private boolean loadAll;

		/**
		 * Constructor
		 *
		 * @param view    the view
		 * @param loadAll a boolean to load everything
		 */
		public LoadTrainFollow(final View view, final boolean loadAll) {
			this.view = view;
			this.loadAll = loadAll;
		}

		@Override
		protected final List<Eta> doInBackground(final String... params) {
			final String runNumber = params[0];
			List<Eta> etas = new ArrayList<>();
			try {
				final CtaConnect connect = CtaConnect.getInstance();
				final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
				connectParam.put("runnumber", runNumber);
				final String content = connect.connect(CtaRequestType.TRAIN_FOLLOW, connectParam);
				final Xml xml = new Xml();
				etas = xml.parseTrainsFollow(content, trainData);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
					R.string.analytics_action_get_train_follow, 0);
			if (!loadAll && etas.size() > 7) {
				etas = etas.subList(0, 6);

				// Add a fake Eta cell to alert the user about the fact that only a part of the result is
				// displayed
				final Eta eta = new Eta();
				eta.setIsDly(false);
				eta.setIsApp(false);
				final Date currentDate = Calendar.getInstance().getTime();
				eta.setArrivalDepartureDate(currentDate);
				eta.setPredictionDate(currentDate);
				final Station fakeStation = new Station();
				fakeStation.setName("Display all results");
				eta.setStation(fakeStation);
				etas.add(eta);
			}
			return etas;
		}

		@Override
		protected final void onPostExecute(final List<Eta> result) {
			final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
			final TextView error = (TextView) view.findViewById(R.id.error);
			if (result.size() != 0) {
				final TrainMapSnippetAdapter ada = new TrainMapSnippetAdapter(result);
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

	private class LoadTrainPosition extends AsyncTask<Boolean, Void, List<Train>> {
		/**
		 * Center map
		 **/
		private boolean centerMap;
		/**
		 * Stop refresh
		 **/
		private boolean stopRefresh;
		/**
		 * Positions list
		 **/
		private List<Position> positions;

		@Override
		protected List<Train> doInBackground(Boolean... params) {
			centerMap = params[0];
			stopRefresh = params[1];
			List<Train> trains = null;
			final CtaConnect connect = CtaConnect.getInstance();
			final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
			connectParam.put("rt", line);
			try {
				final String content = connect.connect(CtaRequestType.TRAIN_LOCATION, connectParam);
				final Xml xml = new Xml();
				trains = xml.parseTrainsLocation(content);
			} catch (ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
					R.string.analytics_action_get_train_location, 0);
			TrainData data = TrainMapActivity.this.trainData;
			if (data == null) {
				final DataHolder dataHolder = DataHolder.getInstance();
				data = dataHolder.getTrainData();
			}
			positions = data.readPattern(TrainLine.fromXmlString(TrainMapActivity.this.line));
			return trains;
		}

		@Override
		protected final void onPostExecute(final List<Train> result) {
			if (result != null) {
				drawTrains(result, positions);
				if (result.size() != 0) {
					if (centerMap) {
						centerMapOnBus(result);
					}
				} else {
					Toast.makeText(TrainMapActivity.this, "No trains found!", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(TrainMapActivity.this, "Error while loading data!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class LoadCurrentPosition extends AsyncTask<Boolean, Void, Void> implements LocationListener {
		// The minimum distance to change Updates in meters
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
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
					if (ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED
							&& ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(TrainMapActivity.this,
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
						if (ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED
								&& ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(TrainMapActivity.this,
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
				if (ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(TrainMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(TrainMapActivity.this,
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
					TrainMapActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrainMapActivity.this);
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
							final AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
						}
					});
				}
			}.start();
		}
	}
}
