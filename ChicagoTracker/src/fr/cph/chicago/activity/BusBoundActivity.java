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

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusBoundActivity extends ListActivity {
	/**
	 * Tag
	 **/
	private static final String TAG = "BusBoundActivity";
	/**
	 * Bus route id
	 **/
	private String busRouteId;
	/**
	 * Bus route name
	 **/
	private String busRouteName;
	/**
	 * Bound
	 **/
	private String bound;
	/**
	 * Adapter
	 **/
	private BusBoundAdapter busBoundAdapter;
	/**
	 * List of bus stop get via API
	 **/
	private List<BusStop> busStops;
	/**
	 * The map fragment from google api
	 **/
	private MapFragment mapFragment;
	/**
	 * The map
	 **/
	private GoogleMap googleMap;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_bus_bound);

			if (busRouteId == null && busRouteName == null && bound == null) {
				busRouteId = getIntent().getExtras().getString("busRouteId");
				busRouteName = getIntent().getExtras().getString("busRouteName");
				bound = getIntent().getExtras().getString("bound");
			}
			busBoundAdapter = new BusBoundAdapter(busRouteId);
			setListAdapter(busBoundAdapter);
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
					BusStop busStop = (BusStop) busBoundAdapter.getItem(position);
					Intent intent = new Intent(ChicagoTracker.getAppContext(), BusActivity.class);

					Bundle extras = new Bundle();
					extras.putInt("busStopId", busStop.getId());
					extras.putString("busStopName", busStop.getName());
					extras.putString("busRouteId", busRouteId);
					extras.putString("busRouteName", busRouteName);
					extras.putString("bound", bound);
					extras.putDouble("latitude", busStop.getPosition().getLatitude());
					extras.putDouble("longitude", busStop.getPosition().getLongitude());

					intent.putExtras(extras);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
				}
			});

			EditText filter = (EditText) findViewById(R.id.bus_filter);
			filter.addTextChangedListener(new TextWatcher() {
				List<BusStop> busStopsFiltered;

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					busStopsFiltered = new ArrayList<BusStop>();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					for (BusStop busStop : busStops) {
						if (StringUtils.containsIgnoreCase(busStop.getName(), s)) {
							this.busStopsFiltered.add(busStop);
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					busBoundAdapter.update(busStopsFiltered);
					busBoundAdapter.notifyDataSetChanged();
				}
			});

			getActionBar().setDisplayHomeAsUpEnabled(true);
			new BusBoundAsyncTask().execute();

			// Preventing keyboard from moving background when showing up
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
	}

	@Override
	public final void onStart() {
		super.onStart();
		FragmentManager fm = getFragmentManager();
		mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = new CameraPosition(NearbyFragment.CHICAGO, 7, 0, 0);
		options.camera(camera);
		mapFragment = MapFragment.newInstance(options);
		mapFragment.setRetainInstance(true);
		fm.beginTransaction().replace(R.id.map, mapFragment).commit();
	}

	@Override
	public final void onStop() {
		super.onStop();
		googleMap = null;
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (googleMap == null) {
			googleMap = mapFragment.getMap();
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
		}
		new LoadPattern().execute();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busRouteId = savedInstanceState.getString("busRouteId");
		busRouteName = savedInstanceState.getString("busRouteName");
		bound = savedInstanceState.getString("bound");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("busRouteId", busRouteId);
		savedInstanceState.putString("busRouteName", busRouteName);
		savedInstanceState.putString("bound", bound);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(this.busRouteName + " (" + this.bound + ")");
		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Task that connect to API to get the bound of the selected stop
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class BusBoundAsyncTask extends AsyncTask<Void, Void, List<BusStop>> {

		/**
		 * The exception that could potentially been thrown during request
		 **/
		private TrackerException trackerException;

		@Override
		protected final List<BusStop> doInBackground(final Void... params) {
			List<BusStop> lBuses = null;
			try {
				lBuses = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, bound);
			} catch (ParserException | ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(BusBoundActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_stop, 0);
			return lBuses;
		}

		@Override
		protected final void onPostExecute(final List<BusStop> result) {
			BusBoundActivity.this.busStops = result;
			if (trackerException == null) {
				busBoundAdapter.update(result);
				busBoundAdapter.notifyDataSetChanged();
			} else {
				ChicagoTracker.displayError(BusBoundActivity.this, trackerException);
			}
		}
	}

	/**
	 * Load nearby data
	 *
	 * @author Carl-Philipp Harmant
	 */
	private final class LoadPattern extends AsyncTask<Void, Void, BusPattern> implements LocationListener {

		private BusPattern busPattern;

		@Override
		protected final BusPattern doInBackground(final Void... params) {
			CtaConnect connect = CtaConnect.getInstance();
			MultiMap<String, String> connectParam = new MultiValueMap<String, String>();
			connectParam.put("rt", busRouteId);
			String boundIgnoreCase = bound.toLowerCase(Locale.US);
			try {
				String content = connect.connect(CtaRequestType.BUS_PATTERN, connectParam);
				Xml xml = new Xml();
				List<BusPattern> patterns = xml.parsePatterns(content);
				for (BusPattern pattern : patterns) {
					String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
					if (pattern.getDirection().equals(bound) || boundIgnoreCase.indexOf(directionIgnoreCase) != -1) {
						this.busPattern = pattern;
						break;
					}
				}
			} catch (ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Util.trackAction(BusBoundActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_pattern, 0);
			return this.busPattern;
		}

		@Override
		protected final void onPostExecute(final BusPattern result) {
			if (result != null) {
				int center = result.getPoints().size() / 2;
				centerMap(result.getPoints().get(center).getPosition());
				drawPattern(result);
			} else {
				Toast.makeText(BusBoundActivity.this, "Sorry, could not load the path!", Toast.LENGTH_SHORT).show();
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
	}

	/**
	 * Center map
	 *
	 * @param position the position we want to center on
	 */
	private void centerMap(final Position position) {
		// Because the fragment can possibly not be ready
		int i = 0;
		while (googleMap == null && i < 20) {
			googleMap = mapFragment.getMap();
			i++;
		}
		if (googleMap != null) {
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			googleMap.setMyLocationEnabled(true);
			LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7));
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(9), 500, null);
		}

	}

	private void drawPattern(final BusPattern pattern) {
		if (googleMap != null) {
			final List<Marker> markers = new ArrayList<>();
			PolylineOptions poly = new PolylineOptions();
			poly.geodesic(true).color(Color.BLACK);
			poly.width(7f);
			Marker marker;
			for (PatternPoint patternPoint : pattern.getPoints()) {
				LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
				poly.add(point);
				if (patternPoint.getStopId() != null) {
					marker = googleMap.addMarker(new MarkerOptions().position(point).title(patternPoint.getStopName())
							.snippet(String.valueOf(patternPoint.getSequence())));
					// .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					markers.add(marker);
					marker.setVisible(false);
				}
			}
			googleMap.addPolyline(poly);

			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
				private float currentZoom = -1;

				@Override
				public void onCameraChange(CameraPosition pos) {
					if (pos.zoom != currentZoom) {
						currentZoom = pos.zoom;
						if (currentZoom >= 14) {
							for (Marker marker : markers) {
								marker.setVisible(true);
							}
						} else {
							for (Marker marker : markers) {
								marker.setVisible(false);
							}
						}
					}
				}
			});
		}
	}
}
