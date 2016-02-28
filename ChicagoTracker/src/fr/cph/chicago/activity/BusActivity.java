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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusActivity extends Activity {
	/**
	 * Tag
	 **/
	private static final String TAG = BusActivity.class.getSimpleName();

	private boolean isFirstLoad = true;
	private List<BusArrival> busArrivals;
	private String busRouteId, bound;
	private Integer busStopId;
	private String busStopName, busRouteName;
	private Double latitude, longitude;
	private int firstLoadCount;
	private boolean isFavorite;

	private ImageView streetViewImage, mapImage, directionImage, favoritesImage;
	private TextView streetViewText;
	private LinearLayout stopsView;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_bus);

			if (busStopId == null && busRouteId == null && bound == null && busStopName == null && busRouteName == null && latitude == null
					&& longitude == null) {
				busStopId = getIntent().getExtras().getInt(getString(R.string.bundle_bus_stop_id));
				busRouteId = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_id));
				bound = getIntent().getExtras().getString(getString(R.string.bundle_bus_bound));
				busStopName = getIntent().getExtras().getString(getString(R.string.bundle_bus_stop_name));
				busRouteName = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_name));
				latitude = getIntent().getExtras().getDouble(getString(R.string.bundle_bus_latitude));
				longitude = getIntent().getExtras().getDouble(getString(R.string.bundle_bus_longitude));
			}

			final Position position = new Position();
			position.setLatitude(latitude);
			position.setLongitude(longitude);

			isFavorite = isFavorite();

			stopsView = (LinearLayout) findViewById(R.id.activity_bus_stops);
			streetViewImage = (ImageView) findViewById(R.id.activity_bus_streetview_image);
			streetViewText = (TextView) findViewById(R.id.activity_bus_steetview_text);
			mapImage = (ImageView) findViewById(R.id.activity_bus_map_image);
			directionImage = (ImageView) findViewById(R.id.activity_bus_map_direction);
			favoritesImage = (ImageView) findViewById(R.id.activity_bus_favorite_star);
			if (isFavorite) {
				favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_active));
			}
			favoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BusActivity.this.switchFavorite();
				}
			});
			swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_bus_stop_swipe_refresh_layout);
			swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					new LoadData().execute();
				}
			});

			final TextView busRouteNameView = (TextView) findViewById(R.id.activity_bus_station_name);
			busRouteNameView.setText(busStopName);
			final TextView busRouteNameView2 = (TextView) findViewById(R.id.activity_bus_station_value);
			final String title = busRouteName + " (" + bound + ")";
			busRouteNameView2.setText(title);

			// Load google street picture and data
			new DisplayGoogleStreetPicture().execute(position.getLatitude(), position.getLongitude());
			new LoadData().execute();

			// Toolbar
			final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			toolbar.inflateMenu(R.menu.main);
			toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					swipeRefreshLayout.setRefreshing(true);
					new LoadData().execute();
					return false;
				}
			}));
			Util.setToolbarColor(this, toolbar, TrainLine.NA);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}
			toolbar.setTitle("Bus stop");
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			// Google analytics
			Util.trackScreen(getResources().getString(R.string.analytics_bus_details));
		}
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busStopId = savedInstanceState.getInt(getString(R.string.bundle_bus_stop_id));
		busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
		bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound));
		busStopName = savedInstanceState.getString(getString(R.string.bundle_bus_stop_name));
		busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name));
		latitude = savedInstanceState.getDouble(getString(R.string.bundle_bus_latitude));
		longitude = savedInstanceState.getDouble(getString(R.string.bundle_bus_longitude));
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState) {
		savedInstanceState.putInt(getString(R.string.bundle_bus_stop_id), busStopId);
		savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId);
		savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound);
		savedInstanceState.putString(getString(R.string.bundle_bus_stop_name), busStopName);
		savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName);
		savedInstanceState.putDouble(getString(R.string.bundle_bus_latitude), latitude);
		savedInstanceState.putDouble(getString(R.string.bundle_bus_longitude), longitude);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Draw arrivals in current layout
	 */
	private void drawArrivals() {
		if (busArrivals != null) {
			final Map<String, TextView> mapRes = new HashMap<>();
			if (busArrivals.size() != 0) {
				for (final BusArrival arrival : busArrivals) {
					if (arrival.getRouteDirection().equals(bound)) {
						final String destination = arrival.getBusDestination();
						if (mapRes.containsKey(destination)) {
							final TextView arrivalView = mapRes.get(destination);
							String arrivalText;
							if (arrival.getIsDly()) {
								arrivalText = arrivalView.getText() + " Delay";
							} else {
								arrivalText = arrivalView.getText() + " " + arrival.getTimeLeft();
							}
							arrivalView.setText(arrivalText);
						} else {
							final TextView arrivalView = new TextView(ChicagoTracker.getContext());
							String arrivalText;
							if (arrival.getIsDly()) {
								arrivalText = arrival.getBusDestination() + ": Delay";
							} else {
								arrivalText = arrival.getBusDestination() + ": " + arrival.getTimeLeft();
							}
							arrivalView.setText(arrivalText);
							arrivalView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
							mapRes.put(destination, arrivalView);
						}
					}
				}
			} else {
				final TextView arrivalView = new TextView(ChicagoTracker.getContext());
				arrivalView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
				arrivalView.setText(getString(R.string.bus_activity_no_service));
				mapRes.put("", arrivalView);
			}
			stopsView.removeAllViews();
			for (final Entry<String, TextView> entry : mapRes.entrySet()) {
				stopsView.addView(entry.getValue());
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isFavorite() {
		boolean isFavorite = false;
		final List<String> favorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		for (final String favorite : favorites) {
			if (favorite.equals(busRouteId + "_" + busStopId + "_" + bound)) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
	}

	/**
	 * Load data class. Contact Bus CTA api to get arrival buses
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class LoadData extends AsyncTask<Void, Void, List<BusArrival>> {

		/**
		 * The exception that could potentially been thrown during request
		 **/
		private TrackerException trackerException;

		@Override
		protected List<BusArrival> doInBackground(final Void... params) {
			final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
			reqParams.put("rt", busRouteId);
			reqParams.put("stpid", String.valueOf(busStopId));
			final CtaConnect connect = CtaConnect.getInstance();
			try {
				final Xml xml = new Xml();
				// Connect to CTA API bus to get XML result of inc buses
				final String xmlResult = connect.connect(CtaRequestType.BUS_ARRIVALS, reqParams);
				// Parse and return arrival buses
				return xml.parseBusArrivals(xmlResult);
			} catch (ParserException | ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(BusActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_arrival, 0);
			return null;
		}

		@Override
		protected final void onProgressUpdate(final Void... values) {
		}

		@Override
		protected final void onPostExecute(final List<BusArrival> result) {
			if (trackerException == null) {
				BusActivity.this.busArrivals = result;
				BusActivity.this.drawArrivals();
			} else {
				ChicagoTracker.displayError(BusActivity.this, trackerException);
			}
			if (isFirstLoad) {
				setFirstLoad();
			}
			if (swipeRefreshLayout != null) {
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	/**
	 * Load image from google street
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class DisplayGoogleStreetPicture extends AsyncTask<Double, Void, Drawable> {

		private Double latitude;
		private Double longitude;

		@Override
		protected final Drawable doInBackground(final Double... params) {
			final GStreetViewConnect connect = GStreetViewConnect.getInstance();
			try {
				latitude = params[0];
				longitude = params[1];
				Util.trackAction(BusActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(latitude, longitude);
			} catch (final IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			final LayoutParams params = (LayoutParams) BusActivity.this.streetViewImage.getLayoutParams();
			final ViewGroup.LayoutParams params2 = BusActivity.this.streetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BusActivity.this.streetViewText.setText(getString(R.string.bus_activity_street_view_text));
			BusActivity.this.streetViewImage.setLayoutParams(params2);
			BusActivity.this.streetViewImage.setImageDrawable(result);
			BusActivity.this.streetViewImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", latitude, longitude);
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					try {
						startActivity(intent);
					} catch (final ActivityNotFoundException ex) {
						// Redirect to browser if the user does not have google map installed
						uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0", latitude, longitude);
						final Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			BusActivity.this.mapImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.da_turn_arrive));
			BusActivity.this.mapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + latitude + "+" + longitude;
					final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});
			BusActivity.this.directionImage
					.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_directions_walking));
			BusActivity.this.directionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String uri = "http://maps.google.com/?f=d&daddr=" + latitude + "," + longitude + "&dirflg=w";
					final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});
			setFirstLoad();
		}
	}

	private void setFirstLoad() {
		if (isFirstLoad && firstLoadCount == 1) {
			isFirstLoad = false;
		}
		firstLoadCount++;
	}

	/**
	 * Add or remove from favorites
	 */
	private void switchFavorite() {
		if (isFavorite) {
			Util.removeFromBusFavorites(busRouteId, String.valueOf(busStopId), bound, ChicagoTracker.PREFERENCE_FAVORITES_BUS);
			isFavorite = false;
		} else {
			Util.addToBusFavorites(busRouteId, String.valueOf(busStopId), bound, ChicagoTracker.PREFERENCE_FAVORITES_BUS);
			Log.i(TAG, "busRouteName: " + busRouteName);
			Preferences.addBusRouteNameMapping(String.valueOf(busStopId), busRouteName);
			Preferences.addBusStopNameMapping(String.valueOf(busStopId), busStopName);
			isFavorite = true;
		}
		if (isFavorite) {
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_active));
		} else {
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_disabled));
		}
	}
}