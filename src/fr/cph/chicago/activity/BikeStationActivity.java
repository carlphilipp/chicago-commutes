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
import android.content.Context;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.util.Util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeStationActivity extends Activity {

	private static final String TAG = BikeStationActivity.class.getSimpleName();

	private ImageView streetViewImage;
	private TextView streetViewText;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ImageView favoritesImage;

	private BikeStation bikeStation;
	private boolean isFavorite;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_bike_station);
			bikeStation = getIntent().getExtras().getParcelable("station");
			if (bikeStation != null) {
				final double latitude = bikeStation.getLatitude();
				final double longitude = bikeStation.getLongitude();

				// Call google street api to load image
				new DisplayGoogleStreetPicture().execute(latitude, longitude);

				swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
				swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						new DivvyAsyncTask().execute();
					}
				});

				isFavorite = isFavorite();

				streetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);
				streetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);
				final ImageView mapImage = (ImageView) findViewById(R.id.activity_map_image);
				mapImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
				final ImageView directionImage = (ImageView) findViewById(R.id.activity_map_direction);
				directionImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
				favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
				final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.map_container);
				final LinearLayout walkContainer = (LinearLayout) findViewById(R.id.walk_container);
				if (isFavorite) {
					favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
				} else {
					favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
				}
				final LinearLayout favoritesImageContainer = (LinearLayout) findViewById(R.id.favorites_container);
				favoritesImageContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						BikeStationActivity.this.switchFavorite();
					}
				});

				final TextView bikeStationValue = (TextView) findViewById(R.id.activity_bike_station_value);
				bikeStationValue.setText(bikeStation.getStAddress1());

				streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(this, latitude, longitude));
				mapContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + latitude + "+" + longitude;
						final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						startActivity(i);
					}
				});
				walkContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String uri = "http://maps.google.com/?f=d&daddr=" + latitude + "," + longitude + "&dirflg=w";
						final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						startActivity(i);
					}
				});

				drawData();
			}
			setToolBar();
		}
	}

	private void setToolBar() {
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.main);
		toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				swipeRefreshLayout.setRefreshing(true);
				new DivvyAsyncTask().execute();
				return false;
			}
		}));
		Util.setToolbarColor(this, toolbar, TrainLine.NA);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			toolbar.setElevation(4);
		}
		toolbar.setTitle(bikeStation.getName());
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void drawData() {
		final Context context = ChicagoTracker.getContext();

		final LinearLayout container = (LinearLayout) findViewById(R.id.favorites_bikes_list);
		final LinearLayout availableLayout = new LinearLayout(context);
		final LinearLayout availableBikes = new LinearLayout(context);
		final LinearLayout availableDocks = new LinearLayout(context);

		final TextView availableBike = new TextView(context);
		final TextView availableDock = new TextView(context);
		final TextView amountBike = new TextView(context);
		final TextView amountDock = new TextView(context);

		container.removeAllViews();
		container.setOrientation(LinearLayout.HORIZONTAL);
		availableLayout.setOrientation(LinearLayout.VERTICAL);
		availableBikes.setOrientation(LinearLayout.HORIZONTAL);
		availableBike.setText(getResources().getText(R.string.bike_available_bikes));
		availableBike.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
		availableBikes.addView(availableBike);
		amountBike.setText(String.valueOf(bikeStation.getAvailableBikes()));
		if (bikeStation.getAvailableBikes() == 0) {
			amountBike.setTextColor(ContextCompat.getColor(context, R.color.red));
		} else {
			amountBike.setTextColor(ContextCompat.getColor(context, R.color.green));
		}
		availableBikes.addView(amountBike);
		availableLayout.addView(availableBikes);
		availableDocks.setOrientation(LinearLayout.HORIZONTAL);
		availableDock.setText(getResources().getText(R.string.bike_available_docks));
		availableDock.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
		availableDocks.addView(availableDock);
		amountDock.setText(String.valueOf(bikeStation.getAvailableDocks()));
		if (bikeStation.getAvailableDocks() == 0) {
			amountDock.setTextColor(ContextCompat.getColor(context, R.color.red));
		} else {
			amountDock.setTextColor(ContextCompat.getColor(context, R.color.green));
		}
		availableDocks.addView(amountDock);
		availableLayout.addView(availableDocks);
		container.addView(availableLayout);
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bikeStation = savedInstanceState.getParcelable(getString(R.string.bundle_bike_station));
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState) {
		savedInstanceState.putParcelable(getString(R.string.bundle_bike_station), bikeStation);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Is favorite or not ?
	 *
	 * @return if the station is favorite
	 */
	private boolean isFavorite() {
		final List<String> favorites = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		for (final String favorite : favorites) {
			if (Integer.valueOf(favorite) == bikeStation.getId()) {
				return true;
			}
		}
		return false;
	}

	private void refreshStation(final BikeStation station) {
		this.bikeStation = station;
		drawData();
	}

	private class DivvyAsyncTask extends AsyncTask<Void, Void, List<BikeStation>> {

		@Override
		protected List<BikeStation> doInBackground(final Void... params) {
			List<BikeStation> bikeStations = null;
			try {
				final Json json = new Json();
				final DivvyConnect divvyConnect = DivvyConnect.getInstance();
				final String bikeContent = divvyConnect.connect();
				bikeStations = json.parseStations(bikeContent);
				Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				Util.trackAction(BikeStationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
						R.string.analytics_action_get_divvy_all, 0);
			} catch (final ConnectException | ParserException e) {
				BikeStationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getContext(), "A surprising error has occurred. Try again!", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e(TAG, "Error while connecting or parsing divvy data", e);
			}
			return bikeStations;
		}

		@Override
		protected final void onPostExecute(final List<BikeStation> result) {
			for (final BikeStation station : result) {
				if (BikeStationActivity.this.bikeStation.getId() == station.getId()) {
					BikeStationActivity.this.refreshStation(station);
					final Bundle bundle = getIntent().getExtras();
					bundle.putParcelable(getString(R.string.bundle_bike_station), station);
					break;
				}
			}
			if (swipeRefreshLayout != null) {
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	/**
	 * Display google street view image
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private final class DisplayGoogleStreetPicture extends AsyncTask<Double, Void, Drawable> {

		private Double latitude;
		private Double longitude;

		@Override
		protected final Drawable doInBackground(final Double... params) {
			try {
				final GStreetViewConnect connect = GStreetViewConnect.getInstance();
				latitude = params[0];
				longitude = params[1];
				Util.trackAction(BikeStationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(latitude, longitude);
			} catch (IOException e) {
				Log.e(TAG, "Error while connecting to google street view API", e);
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			final int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			final LayoutParams layoutParams = (LayoutParams) BikeStationActivity.this.streetViewImage.getLayoutParams();
			final ViewGroup.LayoutParams params = BikeStationActivity.this.streetViewImage.getLayoutParams();
			params.height = height;
			params.width = layoutParams.width;

			BikeStationActivity.this.streetViewImage.setLayoutParams(params);
			BikeStationActivity.this.streetViewImage.setImageDrawable(result);
			BikeStationActivity.this.streetViewText.setText(ChicagoTracker.getContext().getResources().getString(R.string.station_activity_street_view));
		}
	}

	/**
	 * Add/remove favorites
	 */
	private void switchFavorite() {
		if (isFavorite) {
			Util.removeFromBikeFavorites(bikeStation.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			isFavorite = false;
		} else {
			Util.addToBikeFavorites(bikeStation.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			Preferences.addBikeRouteNameMapping(String.valueOf(bikeStation.getId()), bikeStation.getName());
			isFavorite = true;
		}
		if (isFavorite) {
			favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
		} else {
			favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
		}
	}
}
