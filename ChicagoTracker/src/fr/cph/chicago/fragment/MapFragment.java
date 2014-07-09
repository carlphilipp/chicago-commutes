/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Mao Fragment
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class MapFragment extends Fragment {
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** The main activity **/
	private MainActivity mActivity;
	/** The map fragment from google api **/
	private com.google.android.gms.maps.MapFragment mapFragment;
	/** The map **/
	private GoogleMap map;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static final MapFragment newInstance(final int sectionNumber) {
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);
		if (!mActivity.isFinishing()) {
			CheckBox checkBoxTrain = (CheckBox) rootView.findViewById(R.id.trains_check_box);
			CheckBox checkBoxBus = (CheckBox) rootView.findViewById(R.id.buses_check_box);
			CheckBox checkBoxBike = (CheckBox) rootView.findViewById(R.id.bikes_check_box);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
			boolean loadTrain = sharedPref.getBoolean("cta_train", true);
			boolean loadBus = sharedPref.getBoolean("cta_bus", true);
			boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
			if (!loadTrain) {
				checkBoxTrain.setEnabled(false);
			} else {
				checkBoxTrain.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						mActivity.startRefreshAnimation();
						loadTrains();
					}

				});
			}
			if (!loadBus) {
				checkBoxBus.setEnabled(false);
			} else {

			}
			if (!loadBike) {
				checkBoxBike.setEnabled(false);
			} else {

			}
		}
		return rootView;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onStart() {
		super.onStart();
		FragmentManager fm = getFragmentManager();
		mapFragment = (com.google.android.gms.maps.MapFragment) fm.findFragmentById(R.id.map);
		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 10, 0, 0);
		options.camera(camera);
		mapFragment = com.google.android.gms.maps.MapFragment.newInstance(options);
		mapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (map == null) {
			map = mapFragment.getMap();
		}
		new LoadCurrentPosition().execute();
	}

	private final void loadTrains() {

		new Thread() {
			public void run() {
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DataHolder dataHolder = DataHolder.getInstance();
						TrainData trainData = dataHolder.getTrainData();
						Map<TrainLine, List<Station>> trainMap = trainData.getAllStations();
						List<Marker> markers = new ArrayList<Marker>();
						for (Entry<TrainLine, List<Station>> entry : trainMap.entrySet()) {
							TrainLine line = entry.getKey();
							List<Station> stations = entry.getValue();
							for (Station station : stations) {
								Position position = station.getStopsPosition().get(0);
								LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
								MarkerOptions options = new MarkerOptions().position(point).title(station.getName())
										.snippet(station.getId().toString());
								switch (line) {
								case BLUE:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
									break;
								case BROWN:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
									break;
								case GREEN:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
									break;
								case NA:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
									break;
								case ORANGE:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
									break;
								case PINK:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
									break;
								case PURPLE:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
									break;
								case RED:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
									break;
								case YELLOW:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
									break;
								default:
									options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
									break;
								}

								Marker marker = map.addMarker(options);
								markers.add(marker);
							}
						}
						mActivity.stopRefreshAnimation();
					}
				});
			}
		}.start();
	}

	private final class LoadCurrentPosition extends AsyncTask<Void, Void, Void> implements LocationListener {

		// The minimum distance to change Updates in meters
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
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
		// flag for GPS status
		private boolean isGPSEnabled = false;
		// flag for network status
		private boolean isNetworkEnabled = false;

		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		protected Void doInBackground(Void... params) {
			locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

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
			centerMap(position);
			locationManager.removeUpdates(LoadCurrentPosition.this);
		}

		/**
		 * Function to show settings alert dialog
		 */
		private void showSettingsAlert() {
			new Thread() {
				public void run() {
					MapFragment.this.mActivity.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapFragment.this.getActivity());
							alertDialogBuilder.setTitle("GPS settings");
							alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
							alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									MapFragment.this.getActivity().startActivity(intent);
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
	 * @param positon
	 *            the position we want to center on
	 */
	private void centerMap(final Position positon) {
		// Because the fragment can possibly not be ready
		while (mapFragment.getMap() == null) {
		}
		map = mapFragment.getMap();
		map.setMyLocationEnabled(true);
	}
}
