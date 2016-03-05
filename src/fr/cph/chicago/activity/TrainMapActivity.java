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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.listener.TrainMapOnCameraChangeListener;
import fr.cph.chicago.task.LoadCurrentPosition;
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

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_FOLLOW;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_LOCATION;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapActivity extends Activity {

	private static final String TAG = TrainMapActivity.class.getSimpleName();

	private ViewGroup viewGroup;
	private MapFragment mapFragment;
	private GoogleMap googleMap;
	private Marker selectedMarker;

	private String line;
	private Map<Marker, View> views;
	private Map<Marker, Boolean> status;
	private List<Marker> markers;
	private TrainData trainData;
	private TrainMapOnCameraChangeListener trainListener;

	private boolean centerMap = true;
	private boolean refreshingInfoWindow = false;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_map);
			viewGroup = (ViewGroup) findViewById(android.R.id.content);
			if (savedInstanceState != null) {
				line = savedInstanceState.getString(getString(R.string.bundle_train_line));
			} else {
				line = getIntent().getExtras().getString(getString(R.string.bundle_train_line));
			}

			initData();
			setToolbar();

			Util.trackScreen(getResources().getString(R.string.analytics_train_map));
		}
	}

	private void initData() {
		// Load data
		final DataHolder dataHolder = DataHolder.getInstance();
		trainData = dataHolder.getTrainData();
		markers = new ArrayList<>();
		status = new HashMap<>();
		trainListener = new TrainMapOnCameraChangeListener();
	}

	private void setToolbar() {
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.main);
		toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final MenuItem item) {
				new LoadCurrentPosition(TrainMapActivity.this, mapFragment).execute();
				new LoadTrainPosition().execute(false, true);
				return false;
			}
		}));
		final TrainLine trainLine = TrainLine.fromXmlString(line);
		Util.setToolbarColor(this, toolbar, trainLine);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			toolbar.setElevation(4);
		}
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		toolbar.setTitle(trainLine.toString() + " Line");
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
			final CameraPosition camera = new CameraPosition(Util.CHICAGO, 10, 0, 0);
			options.camera(camera);
			mapFragment = MapFragment.newInstance(options);
			mapFragment.setRetainInstance(true);
			fm.beginTransaction().replace(R.id.map, mapFragment).commit();
		}
	}

	@Override
	public final void onStop() {
		super.onStop();
		centerMap = false;
		googleMap = null;
	}

	@Override
	public final void onResume() {
		super.onResume();
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				TrainMapActivity.this.googleMap = googleMap;
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
				if (Util.isNetworkAvailable()) {
					new LoadCurrentPosition(TrainMapActivity.this, mapFragment).execute();
					new LoadTrainPosition().execute(centerMap, true);
				} else {
					Toast.makeText(TrainMapActivity.this, "No network connection detected!", Toast.LENGTH_SHORT).show();
				}
			}
		});
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

	private void refreshInfoWindow() {
		if (selectedMarker == null) {
			return;
		}
		refreshingInfoWindow = true;
		selectedMarker.showInfoWindow();
		refreshingInfoWindow = false;
	}

	private void centerMapOnBus(final List<Train> result) {
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final GoogleMap googleMap) {
				TrainMapActivity.this.googleMap = googleMap;
				final Position position;
				final int zoom;
				if (result.size() == 1) {
					position = result.get(0).getPosition();
					zoom = 15;
				} else {
					position = Train.getBestPosition(result);
					zoom = 11;
				}
				final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
			}
		});
	}

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

				final Marker marker = googleMap.addMarker(
						new MarkerOptions().position(point).title(title).snippet(snippet).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
				markers.add(marker);

				final View view = this.getLayoutInflater().inflate(R.layout.marker_train, viewGroup, false);
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
				final String content = connect.connect(TRAIN_FOLLOW, connectParam);
				final Xml xml = new Xml();
				etas = xml.parseTrainsFollow(content, trainData);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_follow, 0);
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
		 * Positions list
		 **/
		private List<Position> positions;

		@Override
		protected List<Train> doInBackground(Boolean... params) {
			centerMap = params[0];
			List<Train> trains = null;
			final CtaConnect connect = CtaConnect.getInstance();
			final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
			connectParam.put("rt", line);
			try {
				final String content = connect.connect(TRAIN_LOCATION, connectParam);
				final Xml xml = new Xml();
				trains = xml.parseTrainsLocation(content);
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_location, 0);
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
}
