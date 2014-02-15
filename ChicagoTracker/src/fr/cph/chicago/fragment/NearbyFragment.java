package fr.cph.chicago.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.NearbyAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.xml.Xml;

public class NearbyFragment extends Fragment {

	private static final String TAG = "NearbyFragment";

	private static MainActivity mActivity;

	private MapFragment mapFragment;
	private View loadLayout;
	private GoogleMap map;
	private NearbyAdapter ada;

	private static final LatLng CHICAGO = new LatLng(41.8819, -87.6278);
	private static final LatLng SKOKIE = new LatLng(41.883273, -88.309474);

	private Menu menu;

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static NearbyFragment newInstance(final int sectionNumber) {
		NearbyFragment fragment = new NearbyFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
		ada = new NearbyAdapter(mActivity);
		ListView listView = (ListView) rootView.findViewById(R.id.fragment_nearby_list);
		listView.setAdapter(ada);

		// map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		// map = ((MapView)rootView.findViewById(R.id.map)).getMap();
		// Marker chicago = map.addMarker(new MarkerOptions().position(CHICAGO).title("Chicago"));
		// Marker skokie = map.addMarker(new MarkerOptions().position(SKOKIE).title("Geneva").snippet("Amber's place"));
		// .icon(BitmapDescriptorFactory.fromResource(R.drawable.left_arrow)));

		// Move the camera instantly to hamburg with a zoom of 15.
		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(CHICAGO, 15));

		// Zoom in, animating the camera.
		// map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		// View rootView = inflater.inflate(R.layout.loading, container, false);
		// loadLayout = rootView.findViewById(R.id.loading_layout);
		//
		// showProgress(true, null);
		// new LoadNearby().execute();

		//
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
		if (mapFragment == null) {
			mapFragment = MapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map, mapFragment).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (map == null) {
			map = mapFragment.getMap();
		}
		new LoadNearby().execute();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MapFragment f = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		if (f != null) {
			getFragmentManager().beginTransaction().remove(f).commit();
		}
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		this.menu = menu;
		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			// MenuItem menuItem = item;
			// menuItem.setActionView(R.layout.progressbar);
			// menuItem.expandActionView();
			Toast.makeText(this.getActivity(), "Refresh...!", Toast.LENGTH_SHORT).show();
			return true;
		}
		return true;
	}

	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	private final class LoadArrivals extends AsyncTask<List<BusStop>, Void, SparseArray<Map<String, List<BusArrival>>>> {

		private SparseArray<Map<String, List<BusArrival>>> busArrivalsMap;
		private List<BusStop> busStops;

		@Override
		protected SparseArray<Map<String, List<BusArrival>>> doInBackground(List<BusStop>... params) {
			busStops = params[0];
			busArrivalsMap = new SparseArray<Map<String, List<BusArrival>>>();
			
			// Get info bus stop from CSV, to be able to add missing data to current stop get from web
//			DataHolder dataHolder = DataHolder.getInstance();
//			BusData busData = dataHolder.getBusData();
			
			// Loop over bus stops around user
			for (BusStop busStop : busStops) {
				
				Map<String, List<BusArrival>> tempMap;
				
				// Get current bus info from CSV
//				BusStop currentBusStop = busData.readOneBus(busStop.getId());
				// Get its name
//				String name = currentBusStop.getName();
				
				// Create 
				tempMap = busArrivalsMap.get(busStop.getId(), null);
				if (tempMap == null) {
					tempMap = new HashMap<String, List<BusArrival>>();
					busArrivalsMap.put(busStop.getId(), tempMap);
				}

				try {
					CtaConnect cta = CtaConnect.getInstance();
					MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
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
							List<BusArrival> temp = new ArrayList<BusArrival>();
							temp.add(busArrival);
							tempMap.put(direction, temp);
						}
					}
				} catch (ConnectException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (ParserException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return busArrivalsMap;
		}

		@Override
		protected final void onPostExecute(SparseArray<Map<String, List<BusArrival>>> result) {
			load(busStops, busArrivalsMap);
		}
	}

	/**
	 * Load nearby data
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadNearby extends AsyncTask<Void, Void, List<BusStop>> implements LocationListener {

		private static final String TAG = "LoadNearby";

		// The minimum distance to change Updates in meters
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

		// flag for GPS status
		boolean isGPSEnabled = false;
		// flag for network status
		boolean isNetworkEnabled = false;

		boolean canGetLocation = false;

		private Location location;
		private Position positon;
		private double latitude; // latitude
		private double longitude; // longitude

		@Override
		protected final List<BusStop> doInBackground(final Void... params) {
			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();

			LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			List<BusStop> busStops = new ArrayList<BusStop>();
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				showSettingsAlert();
			} else {
				this.canGetLocation = true;
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
				positon = new Position();
				positon.setLatitude(latitude);
				positon.setLongitude(longitude);
				busStops = busData.readNearbyStops(positon);
			}

			return busStops;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected final void onPostExecute(final List<BusStop> result) {
			new LoadArrivals().execute(result);
			centerMap(positon);
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
		 * Function to show settings alert dialog On pressing Settings button will lauch Settings Options
		 * */
		public void showSettingsAlert() {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChicagoTracker.getAppContext());

			// Setting Dialog Title
			alertDialog.setTitle("GPS is settings");

			// Setting Dialog Message
			alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

			// On pressing Settings button
			alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					ChicagoTracker.getAppContext().startActivity(intent);
				}
			});

			// on pressing cancel button
			alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			// Showing Alert Message
			alertDialog.show();
		}
	}

	public final void centerMap(final Position positon) {
		while (mapFragment.getMap() == null) {
		}
		map = mapFragment.getMap();
		map.setMyLocationEnabled(true);
		LatLng latLng = new LatLng(positon.getLatitude(), positon.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
	}

	private final void load(final List<BusStop> buses, final SparseArray<Map<String, List<BusArrival>>> busArrivals) {
		for (BusStop busStop : buses) {
			LatLng point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
			map.addMarker(new MarkerOptions().position(point).title(busStop.getName()));
		}
		ada.setBusStops(buses, busArrivals);
		ada.notifyDataSetChanged();
	}

	/**
	 * Load error
	 * 
	 */
	public final void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		ChicagoTracker.displayError(mActivity, exceptionToBeThrown);
		mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

}
