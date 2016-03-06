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

package fr.cph.chicago.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.NearbyAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Map Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class NearbyFragment extends Fragment implements GoogleMapAbility {

	private static final String TAG = NearbyFragment.class.getSimpleName();
	private static final String ARG_SECTION_NUMBER = "section_number";

	private SupportMapFragment mapFragment;
	private View loadLayout;
	private RelativeLayout nearbyContainer;
	private ListView listView;

	private MainActivity mainActivity;
	private GoogleMap googleMap;
	private NearbyAdapter nearbyAdapter;
	private boolean hideStationsStops;
	
	public static NearbyFragment newInstance(final int sectionNumber) {
		final NearbyFragment fragment = new NearbyFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Context context) {
		super.onAttach(context);
		mainActivity = context instanceof Activity ? (MainActivity) context : null;
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(mainActivity);
		ChicagoTracker.checkBusData(mainActivity);
		Util.trackScreen(getResources().getString(R.string.analytics_nearby_fragment));
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
		if (!mainActivity.isFinishing()) {
			nearbyAdapter = new NearbyAdapter(mainActivity);
			listView = (ListView) rootView.findViewById(R.id.fragment_nearby_list);
			listView.setAdapter(nearbyAdapter);
			setHasOptionsMenu(true);
			loadLayout = rootView.findViewById(R.id.loading_layout);
			nearbyContainer = (RelativeLayout) rootView.findViewById(R.id.nerby_list_container);

			hideStationsStops = Preferences.getHideShowNearby();
			final CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.hideEmptyStops);
			checkBox.setChecked(hideStationsStops);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
					Preferences.saveHideShowNearby(isChecked);
					hideStationsStops = isChecked;
					if (Util.isNetworkAvailable()) {
						reloadData();
					}
				}
			});
			showProgress(true);
		}
		return rootView;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public final void onStart() {
		super.onStart();
		final GoogleMapOptions options = new GoogleMapOptions();
		final CameraPosition camera = new CameraPosition(Util.CHICAGO, 7, 0, 0);
		final FragmentManager fm = mainActivity.getSupportFragmentManager();
		options.camera(camera);
		mapFragment = SupportMapFragment.newInstance(options);
		mapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onResume() {
		super.onResume();
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				NearbyFragment.this.googleMap = googleMap;
				if (Util.isNetworkAvailable()) {
					new LoadNearby(mainActivity).execute();
					nearbyContainer.setVisibility(View.GONE);
					showProgress(true);
				} else {
					Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
					showProgress(false);
				}
			}
		});
	}

	public final void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		ChicagoTracker.displayError(mainActivity, exceptionToBeThrown);
	}

	public void setGoogleMap(GoogleMap googleMap) {
		this.googleMap = googleMap;
	}

	private class LoadArrivals extends AsyncTask<List<?>, Void, Void> {

		private SparseArray<Map<String, List<BusArrival>>> busArrivalsMap;
		private SparseArray<TrainArrival> trainArrivals;
		private List<BusStop> busStops;
		private List<Station> stations;
		private List<BikeStation> bikeStationsRes;
		private List<BikeStation> bikeStationsTemp;

		@SuppressWarnings("unchecked")
		@Override
		protected final Void doInBackground(final List<?>... params) {
			busStops = (List<BusStop>) params[0];
			stations = (List<Station>) params[1];
			bikeStationsRes = new ArrayList<>();
			bikeStationsTemp = (List<BikeStation>) params[2];

			busArrivalsMap = new SparseArray<>();
			trainArrivals = new SparseArray<>();

			final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			final boolean loadTrain = sharedPref.getBoolean("cta_train", true);
			final boolean loadBus = sharedPref.getBoolean("cta_bus", true);
			final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

			final CtaConnect cta = CtaConnect.getInstance();

			// Loop over bus stops around user
			if (loadBus) {
				for (final BusStop busStop : busStops) {
					// Create
					Map<String, List<BusArrival>> tempMap = busArrivalsMap.get(busStop.getId(), null);
					if (tempMap == null) {
						tempMap = new HashMap<>();
						busArrivalsMap.put(busStop.getId(), tempMap);
					}

					// Buses
					try {
						final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
						reqParams.put(getResources().getString(R.string.request_stop_id), Integer.toString(busStop.getId()));

						final String xmlRes = cta.connect(BUS_ARRIVALS, reqParams);
						final Xml xml = new Xml();
						final List<BusArrival> busArrivals = xml.parseBusArrivals(xmlRes);
						for (final BusArrival busArrival : busArrivals) {
							final String direction = busArrival.getRouteDirection();
							if (tempMap.containsKey(direction)) {
								final List<BusArrival> temp = tempMap.get(direction);
								temp.add(busArrival);
							} else {
								final List<BusArrival> temp = new ArrayList<>();
								temp.add(busArrival);
								tempMap.put(direction, temp);
							}
						}
						Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
					} catch (final ConnectException | ParserException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			if (loadTrain) {
				// Train
				for (final Station station : stations) {
					try {
						final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
						reqParams.put(getResources().getString(R.string.request_map_id), String.valueOf(station.getId()));
						final String xmlRes = cta.connect(TRAIN_ARRIVALS, reqParams);
						final Xml xml = new Xml();
						final SparseArray<TrainArrival> temp = xml.parseArrivals(xmlRes, DataHolder.getInstance().getTrainData());
						for (int j = 0; j < temp.size(); j++) {
							trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
						}
						Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
					} catch (final ConnectException | ParserException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			// TODO: modify the second check
			if (loadBike && bikeStationsTemp != null) {
				// Bike
				final DivvyConnect connect = DivvyConnect.getInstance();
				try {
					final Json json = Json.getInstance();
					final String content = connect.connect();
					final List<BikeStation> bikeStationUpdated = json.parseStations(content);
					for (final BikeStation station : bikeStationUpdated) {
						if (bikeStationsTemp.contains(station)) {
							bikeStationsRes.add(station);
						}
					}
					Collections.sort(bikeStationsRes, Util.BIKE_COMPARATOR_NAME);
					Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
				} catch (final ConnectException | ParserException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			if (hideStationsStops) {
				final List<BusStop> busStopTmp = new ArrayList<>();
				for (final BusStop busStop : busStops) {
					if (busArrivalsMap.get(busStop.getId()).size() == 0) {
						busArrivalsMap.remove(busStop.getId());
					} else {
						busStopTmp.add(busStop);
					}
				}
				busStops.clear();
				busStops = busStopTmp;

				final List<Station> trainStationTmp = new ArrayList<>();
				for (final Station station : stations) {
					if (trainArrivals.get(station.getId()) == null || trainArrivals.get(station.getId()).getEtas().size() == 0) {
						trainArrivals.remove(station.getId());
					} else {
						trainStationTmp.add(station);
					}
				}
				stations.clear();
				stations = trainStationTmp;
			}
			mainActivity.runOnUiThread(new Runnable() {
				public void run() {
					load(busStops, busArrivalsMap, stations, trainArrivals, bikeStationsRes);
				}
			});
		}
	}

	private class LoadNearby extends AsyncTask<Void, Void, Void> implements LocationListener {

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

		public LoadNearby(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		protected final Void doInBackground(final Void... params) {
			busStops = new ArrayList<>();
			trainStations = new ArrayList<>();
			bikeStations = NearbyFragment.this.mainActivity.getIntent().getExtras().getParcelableArrayList("bikeStations");

			final DataHolder dataHolder = DataHolder.getInstance();
			final BusData busData = dataHolder.getBusData();
			final TrainData trainData = dataHolder.getTrainData();

			locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

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

				final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
				final boolean loadTrain = sharedPref.getBoolean("cta_train", true);
				final boolean loadBus = sharedPref.getBoolean("cta_bus", true);
				final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

				if (loadBus) {
					busStops = busData.readNearbyStops(position);
				}
				if (loadTrain) {
					trainStations = trainData.readNearbyStation(position);
				}
				// TODO: wait bikeStations is loaded
				if (loadBike && bikeStations != null) {
					bikeStations = BikeStation.readNearbyStation(bikeStations, position);
				}
			}
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			new LoadArrivals().execute(busStops, trainStations, bikeStations);
			Util.centerMap(NearbyFragment.this, mapFragment, mainActivity, position);

			if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED
					&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity,
						new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
				return;
			}
			locationManager.removeUpdates(LoadNearby.this);
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
					NearbyFragment.this.mainActivity.runOnUiThread(new Runnable() {
						public void run() {
							final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NearbyFragment.this.getActivity());
							alertDialogBuilder.setTitle("GPS settings");
							alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
							alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									NearbyFragment.this.getActivity().startActivity(intent);
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

	private void load(final List<BusStop> buses, final SparseArray<Map<String, List<BusArrival>>> busArrivals, final List<Station> stations,
			final SparseArray<TrainArrival> trainArrivals, final List<BikeStation> bikeStations) {
		final List<Marker> markers = new ArrayList<>();
		final BitmapDescriptor azure = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		final BitmapDescriptor violet = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
		final BitmapDescriptor yellow = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
		MarkerOptions options;
		Marker marker;
		LatLng point;
		for (final BusStop busStop : buses) {
			point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
			options = new MarkerOptions().position(point).title(busStop.getName()).snippet(Integer.toString(busStop.getId()));
			options.icon(azure);
			marker = googleMap.addMarker(options);
			markers.add(marker);

		}
		for (final Station station : stations) {
			for (final Position position : station.getStopsPosition()) {
				point = new LatLng(position.getLatitude(), position.getLongitude());
				options = new MarkerOptions().position(point).title(station.getName()).snippet(String.valueOf(station.getId()));
				options.icon(violet);
				marker = googleMap.addMarker(options);
				markers.add(marker);
			}
		}
		for (final BikeStation station : bikeStations) {
			point = new LatLng(station.getLatitude(), station.getLongitude());
			options = new MarkerOptions().position(point).title(station.getName()).snippet(station.getId() + "");
			options.icon(yellow);
			marker = googleMap.addMarker(options);
			markers.add(marker);
		}
		addClickEventsToMarkers(buses, stations, bikeStations);
		nearbyAdapter.updateData(buses, busArrivals, stations, trainArrivals, bikeStations, googleMap, markers);
		nearbyAdapter.notifyDataSetChanged();
		showProgress(false);
		nearbyContainer.setVisibility(View.VISIBLE);

	}

	private void addClickEventsToMarkers(final List<BusStop> busStops, final List<Station> stations, final List<BikeStation> bikeStations) {
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				boolean found = false;
				for (int i = 0; i < stations.size(); i++) {
					if (marker.getSnippet().equals(String.valueOf(stations.get(i).getId()))) {
						listView.smoothScrollToPosition(i);
						found = true;
						break;
					}
				}
				if (!found) {
					for (int i = 0; i < busStops.size(); i++) {
						int index = i + stations.size();
						if (marker.getSnippet().equals(Integer.toString(busStops.get(i).getId()))) {
							listView.smoothScrollToPosition(index);
							break;
						}
					}
				}
				for (int i = 0; i < bikeStations.size(); i++) {
					int index = i + stations.size() + busStops.size();
					if (marker.getSnippet().equals(bikeStations.get(i).getId() + "")) {
						listView.smoothScrollToPosition(index);
						break;
					}
				}
				return false;
			}
		});
	}

	private void showProgress(final boolean show) {
		try {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			loadLayout.setVisibility(View.VISIBLE);
			loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} catch (final IllegalStateException e) {
			Log.w(TAG, e.getMessage(), e);
		}
	}

	public final void reloadData() {
		if (Util.isNetworkAvailable()) {
			googleMap.clear();
			showProgress(true);
			nearbyContainer.setVisibility(View.GONE);
			new LoadNearby(mainActivity).execute();
		} else {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
			showProgress(false);
		}
	}
}