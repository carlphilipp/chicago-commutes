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

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.listener.BusMapOnCameraChangeListener;
import fr.cph.chicago.task.LoadCurrentPosition;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.connection.CtaRequestType.BUS_PATTERN;
import static fr.cph.chicago.connection.CtaRequestType.BUS_VEHICLES;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapActivity extends Activity {

	private static final String TAG = BusMapActivity.class.getSimpleName();
	private ViewGroup viewGroup;
	private MapFragment mapFragment;
	private GoogleMap googleMap;
	private Marker selectedMarker;

	private List<Marker> busMarkers;
	private List<Marker> busStationMarkers;
	private Map<Marker, View> views;
	private Map<Marker, Boolean> status;

	private Integer busId;
	private String busRouteId;
	private String[] bounds;
	private BusMapOnCameraChangeListener busListener;

	private boolean refreshingInfoWindow = false;
	private boolean centerMap = true;
	private boolean loadPattern = true;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			viewGroup = (ViewGroup) findViewById(android.R.id.content);
			if (savedInstanceState != null) {
				busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id));
				busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
				bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds));
			} else {
				busId = getIntent().getExtras().getInt(getString(R.string.bundle_bus_id));
				busRouteId = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_id));
				bounds = getIntent().getExtras().getStringArray(getString(R.string.bundle_bus_bounds));
			}

			busMarkers = new ArrayList<>();
			busStationMarkers = new ArrayList<>();
			views = new HashMap<>();
			status = new HashMap<>();
			busListener = new BusMapOnCameraChangeListener();

			setToolbar();

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
			final CameraPosition camera = new CameraPosition(Util.CHICAGO, 10, 0, 0);
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
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				BusMapActivity.this.googleMap = googleMap;
				googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
					@Override
					public View getInfoWindow(Marker marker) {
						return null;
					}

					@Override
					public View getInfoContents(final Marker marker) {
						if (!marker.getSnippet().equals("")) {
							final View view = views.get(marker);
							if (!refreshingInfoWindow) {
								selectedMarker = marker;
								final String busId = marker.getSnippet();
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
								final boolean current = status.get(marker);
								new LoadBusFollow(view, !current).execute(runNumber);
								status.put(marker, !current);
							}
						}
					}
				});
				if (Util.isNetworkAvailable()) {
					new LoadCurrentPosition(BusMapActivity.this, mapFragment).execute();
					new LoadBusPosition().execute(centerMap, !loadPattern);
					if (loadPattern) {
						new LoadPattern().execute();
					}
				} else {
					Toast.makeText(BusMapActivity.this, "No network connection detected!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id));
		busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
		bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds));
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState) {
		savedInstanceState.putInt(getString(R.string.bundle_bus_id), busId);
		savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId);
		savedInstanceState.putStringArray(getString(R.string.bundle_bus_bounds), bounds);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void setToolbar() {
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		toolbar.inflateMenu(R.menu.main);
		toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				new LoadCurrentPosition(BusMapActivity.this, mapFragment).execute();
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
			public void onClick(final View v) {
				finish();
			}
		});
	}

	private void refreshInfoWindow() {
		if (selectedMarker == null) {
			return;
		}
		refreshingInfoWindow = true;
		selectedMarker.showInfoWindow();
		refreshingInfoWindow = false;
	}

	private void centerMapOnBus(final List<Bus> result) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				BusMapActivity.this.googleMap = googleMap;
				final Position position;
				final int zoom;
				if (result.size() == 1) {
					position = result.get(0).getPosition();
					zoom = 15;
				} else {
					position = Bus.getBestPosition(result);
					zoom = 11;
				}
				final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
			}
		});
	}

	private void drawBuses(final List<Bus> buses) {
		if (googleMap != null) {
			for (Marker marker : busMarkers) {
				marker.remove();
			}
			busMarkers.clear();
			final Bitmap bitmap = busListener.getCurrentBitmap();
			for (final Bus bus : buses) {
				final LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
				final Marker marker = googleMap.addMarker(
						new MarkerOptions().position(point).title("To " + bus.getDestination()).snippet(bus.getId() + "").icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f)
								.rotation(bus.getHeading()).flat(true));
				busMarkers.add(marker);

				final LayoutInflater layoutInflater = (LayoutInflater) BusMapActivity.this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View view = layoutInflater.inflate(R.layout.marker_train, viewGroup, false);
				final TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(marker.getTitle());

				views.put(marker, view);
			}

			busListener.setBusMarkers(busMarkers);

			googleMap.setOnCameraChangeListener(busListener);
		}
	}

	private void drawPattern(final List<BusPattern> patterns) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				BusMapActivity.this.googleMap = googleMap;
				int j = 0;
				final BitmapDescriptor red = BitmapDescriptorFactory.defaultMarker();
				final BitmapDescriptor blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
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
						//if (patternPoint.getStopId() != null) {
							options = new MarkerOptions();
							options.position(point).title(patternPoint.getStopName() + " (" + pattern.getDirection() + ")").snippet("");
							if (j == 0) {
								options.icon(red);
							} else {
								options.icon(blue);
							}

							final Marker marker = googleMap.addMarker(options);
							busStationMarkers.add(marker);
							marker.setVisible(false);
						//}
					}
					googleMap.addPolyline(poly);
					j++;
				}
				busListener.setBusStationMarkers(busStationMarkers);
				googleMap.setOnCameraChangeListener(busListener);
			}
		});
	}

	private class LoadBusPosition extends AsyncTask<Boolean, Void, List<Bus>> {
		/**
		 * Allow or not centering the map
		 **/
		private boolean centerMap;

		@Override
		protected List<Bus> doInBackground(final Boolean... params) {
			centerMap = params[0];
			List<Bus> buses = null;
			final CtaConnect connect = CtaConnect.getInstance();
			final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
			if (busId != 0) {
				connectParam.put("vid", String.valueOf(busId));
			} else {
				connectParam.put("rt", busRouteId);
			}
			try {
				final String content = connect.connect(BUS_VEHICLES, connectParam);
				final Xml xml = new Xml();
				buses = xml.parseVehicles(content);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_vehicles, 0);
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
		}
	}

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
					final String xmlResult = connect.connect(BUS_DIRECTION, reqParams);
					final BusDirections busDirections = xml.parseBusDirections(xmlResult, busRouteId);
					bounds = new String[busDirections.getlBusDirection().size()];
					for (int i = 0; i < busDirections.getlBusDirection().size(); i++) {
						bounds[i] = busDirections.getlBusDirection().get(i).getBusDirectionEnum().toString();
					}
					Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_direction, 0);
				}

				final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
				connectParam.put("rt", busRouteId);
				final String content = connect.connect(BUS_PATTERN, connectParam);
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
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_pattern, 0);
			return this.patterns;
		}

		@Override
		protected final void onPostExecute(final List<BusPattern> result) {
			if (result != null) {
				drawPattern(result);
			} else {
				Toast.makeText(BusMapActivity.this, "Sorry, could not load the path!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class LoadBusFollow extends AsyncTask<String, Void, List<BusArrival>> {

		private View view;
		private boolean loadAll;

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
				String content = connect.connect(BUS_ARRIVALS, connectParam);
				Xml xml = new Xml();
				arrivals = xml.parseBusArrivals(content);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
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
				final BusMapSnippetAdapter ada = new BusMapSnippetAdapter(BusMapActivity.this, result);
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
