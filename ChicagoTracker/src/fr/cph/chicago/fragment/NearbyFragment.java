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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
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
import fr.cph.chicago.connection.CtaRequestType;
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

/**
 * Map Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class NearbyFragment extends Fragment {
	/**
	 * Tag
	 **/
	private static final String TAG = "NearbyFragment";
	/**
	 * The fragment argument representing the section number for this fragment.
	 **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/**
	 * The main activity
	 **/
	private MainActivity mainActivity;
	/**
	 * The map fragment from google api
	 **/
	private MapFragment mapFragment;
	/**
	 * The load layout
	 **/
	private View loadLayout;
	/**
	 * The list layout
	 **/
	private RelativeLayout nearbyContainer;
	/**
	 * The map
	 **/
	private GoogleMap googleMap;
	/**
	 * The adapter
	 **/
	private NearbyAdapter nearbyAdapter;
	/**
	 * The list view
	 **/
	private ListView listView;
	/**
	 * The only check box
	 **/
	private CheckBox checkBox;
	/**
	 * Hide empty stations/stops
	 **/
	private boolean hideStationsStops;
	/**
	 * The Chicago position
	 **/
	public static final LatLng CHICAGO = new LatLng(41.8819, -87.6278);

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 * @return
	 */
	public static NearbyFragment newInstance(final int sectionNumber) {
		NearbyFragment fragment = new NearbyFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(mainActivity);
		ChicagoTracker.checkBusData(mainActivity);

		Util.trackScreen(mainActivity, R.string.analytics_nearby_fragment);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
		if (!mainActivity.isFinishing()) {
			nearbyAdapter = new NearbyAdapter(mainActivity);
			listView = (ListView) rootView.findViewById(R.id.fragment_nearby_list);
			listView.setAdapter(nearbyAdapter);
			setHasOptionsMenu(true);
			loadLayout = rootView.findViewById(R.id.loading_layout);
			nearbyContainer = (RelativeLayout) rootView.findViewById(R.id.nerby_list_container);
			checkBox = (CheckBox) rootView.findViewById(R.id.hideEmptyStops);
			hideStationsStops = Preferences.getHideShowNearby();
			checkBox.setChecked(hideStationsStops);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
//		FragmentManager fm = getFragmentManager();
//		mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
//		GoogleMapOptions options = new GoogleMapOptions();
//		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 7, 0, 0);
//		options.camera(camera);
//		mapFragment = MapFragment.newInstance(options);
//		mapFragment.setRetainInstance(true);
//		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (googleMap == null) {
			googleMap = mapFragment.getMap();
		}
		if (Util.isNetworkAvailable()) {
			new LoadNearby().execute();
		} else {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
			showProgress(false);
		}
	}

	/**
	 * Load error
	 */
	public final void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		DataHolder.getInstance().setAlertData(null);
		ChicagoTracker.displayError(mainActivity, exceptionToBeThrown);
		mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * Load arrivals
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class LoadArrivals extends AsyncTask<List<?>, Void, Void> {

		/**
		 * Bus arrival map
		 **/
		private SparseArray<Map<String, List<BusArrival>>> busArrivalsMap;
		/**
		 * Train arrivals
		 **/
		private SparseArray<TrainArrival> trainArrivals;
		/**
		 * Bus stops
		 **/
		private List<BusStop> busStops;
		/**
		 * Stations
		 **/
		private List<Station> stations;
		/**
		 * List of bike stations result
		 **/
		private List<BikeStation> bikeStationsRes;
		/**
		 * List of bike stations result
		 **/
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

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			boolean loadTrain = sharedPref.getBoolean("cta_train", true);
			boolean loadBus = sharedPref.getBoolean("cta_bus", true);
			boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

			CtaConnect cta = CtaConnect.getInstance();

			// Loop over bus stops around user
			if (loadBus) {
				for (BusStop busStop : busStops) {
					Map<String, List<BusArrival>> tempMap;

					// Create
					tempMap = busArrivalsMap.get(busStop.getId(), null);
					if (tempMap == null) {
						tempMap = new HashMap<>();
						busArrivalsMap.put(busStop.getId(), tempMap);
					}

					// Buses
					try {
						MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
						reqParams.put("stpid", busStop.getId().toString());

						String xmlRes = cta.connect(CtaRequestType.BUS_ARRIVALS, reqParams);
						Xml xml = new Xml();
						List<BusArrival> busArrivals = xml.parseBusArrivals(xmlRes);
						for (BusArrival busArrival : busArrivals) {
							String direction = busArrival.getRouteDirection();
							if (tempMap.containsKey(direction)) {
								List<BusArrival> temp = tempMap.get(direction);
								temp.add(busArrival);
							} else {
								List<BusArrival> temp = new ArrayList<>();
								temp.add(busArrival);
								tempMap.put(direction, temp);
							}
						}
						Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_bus,
								R.string.analytics_action_get_bus_arrival, 0);
					} catch (ConnectException | ParserException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			if (loadTrain) {
				// Train
				for (Station station : stations) {
					try {
						MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
						reqParams.put("mapid", String.valueOf(station.getId()));
						String xmlRes = cta.connect(CtaRequestType.TRAIN_ARRIVALS, reqParams);
						Xml xml = new Xml();
						SparseArray<TrainArrival> temp = xml.parseArrivals(xmlRes, DataHolder.getInstance().getTrainData());
						for (int j = 0; j < temp.size(); j++) {
							trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
						}
						Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_train,
								R.string.analytics_action_get_train_arrivals, 0);
					} catch (ConnectException | ParserException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			// TODO: modify the second check
			if (loadBike && bikeStationsTemp != null) {
				// Bike
				DivvyConnect connect = DivvyConnect.getInstance();
				try {
					Json json = new Json();
					String content = connect.connect();
					List<BikeStation> bikeStationUpdated = json.parseStations(content);
					for (BikeStation station : bikeStationUpdated) {
						if (bikeStationsTemp.contains(station)) {
							bikeStationsRes.add(station);
						}
					}
					Collections.sort(bikeStationsRes, Util.BIKE_COMPARATOR_NAME);
					Util.trackAction(NearbyFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
							R.string.analytics_action_get_divvy_all, 0);
				} catch (ConnectException | ParserException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			if (hideStationsStops) {
				List<BusStop> busStopTmp = new ArrayList<>();
				for (BusStop busStop : busStops) {
					if (busArrivalsMap.get(busStop.getId()).size() == 0) {
						busArrivalsMap.remove(busStop.getId());
					} else {
						busStopTmp.add(busStop);
					}
				}
				busStops.clear();
				busStops = busStopTmp;

				List<Station> trainStationTmp = new ArrayList<>();
				for (Station station : stations) {
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

	/**
	 * Load nearby data
	 *
	 * @author Carl-Philipp Harmant
	 */
	private class LoadNearby extends AsyncTask<Void, Void, Void> implements LocationListener {

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

		@Override
		protected final Void doInBackground(final Void... params) {
			busStops = new ArrayList<>();
			trainStations = new ArrayList<>();
			bikeStations = NearbyFragment.this.mainActivity.getIntent().getExtras().getParcelableArrayList("bikeStations");

			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();
			TrainData trainData = dataHolder.getTrainData();

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

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
				boolean loadTrain = sharedPref.getBoolean("cta_train", true);
				boolean loadBus = sharedPref.getBoolean("cta_bus", true);
				boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

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
			centerMap(position);
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
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NearbyFragment.this.getActivity());
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
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
						}
					});
				}
			}.start();
		}
	}

	/**
	 * Center map
	 *
	 * @param positon the position we want to center on
	 */
	private void centerMap(final Position positon) {
		// Because the fragment can possibly not be ready
		while (mapFragment.getMap() == null) {
		}
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		LatLng latLng = null;
		if (positon != null) {
			latLng = new LatLng(positon.getLatitude(), positon.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
		} else {
			latLng = CHICAGO;
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
		}
		// map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
	}

	/**
	 * Load data
	 *
	 * @param buses         the list of buses
	 * @param busArrivals   the list of bus arrivals
	 * @param stations      the list of station
	 * @param trainArrivals the list of train arrival
	 */
	private void load(final List<BusStop> buses, final SparseArray<Map<String, List<BusArrival>>> busArrivals, final List<Station> stations,
			final SparseArray<TrainArrival> trainArrivals, final List<BikeStation> bikeStations) {
		List<Marker> markers = new ArrayList<>();
		BitmapDescriptor azure = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		BitmapDescriptor violet = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
		BitmapDescriptor yellow = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
		MarkerOptions options;
		Marker marker;
		LatLng point;
		for (BusStop busStop : buses) {
			point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
			options = new MarkerOptions().position(point).title(busStop.getName()).snippet(busStop.getId().toString());
			options.icon(azure);
			marker = googleMap.addMarker(options);
			markers.add(marker);

		}
		for (Station station : stations) {
			for (Position position : station.getStopsPosition()) {
				point = new LatLng(position.getLatitude(), position.getLongitude());
				options = new MarkerOptions().position(point).title(station.getName()).snippet(station.getId().toString());
				options.icon(violet);
				marker = googleMap.addMarker(options);
				markers.add(marker);
			}
		}
		for (BikeStation station : bikeStations) {
			point = new LatLng(station.getPosition().getLatitude(), station.getPosition().getLongitude());
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

	/**
	 * Add click events to markers
	 *
	 * @param busStops the list of bus stops
	 * @param stations the list of stations
	 */
	private void addClickEventsToMarkers(final List<BusStop> busStops, final List<Station> stations, final List<BikeStation> bikeStations) {
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				boolean found = false;
				for (int i = 0; i < stations.size(); i++) {
					if (marker.getSnippet().equals(stations.get(i).getId().toString())) {
						listView.smoothScrollToPosition(i);
						found = true;
						break;
					}
				}
				if (!found) {
					for (int i = 0; i < busStops.size(); i++) {
						int indice = i + stations.size();
						if (marker.getSnippet().equals(busStops.get(i).getId().toString())) {
							listView.smoothScrollToPosition(indice);
							break;
						}
					}
				}
				for (int i = 0; i < bikeStations.size(); i++) {
					int indice = i + stations.size() + busStops.size();
					if (marker.getSnippet().equals(bikeStations.get(i).getId() + "")) {
						listView.smoothScrollToPosition(indice);
						break;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Show progress bar
	 *
	 * @param show true or falseO
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
				loadLayout.setVisibility(View.VISIBLE);
				loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
					}
				});
			} else {
				loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
			}
			mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		} catch (IllegalStateException e) {
			Log.w(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Reload data
	 */
	public final void reloadData() {
		if (Util.isNetworkAvailable()) {
			googleMap.clear();
			showProgress(true);
			nearbyContainer.setVisibility(View.GONE);
			new LoadNearby().execute();
		} else {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
			showProgress(false);
		}
	}
}
