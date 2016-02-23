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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.Json;
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
	/**
	 * Tag
	 **/
	private static final String TAG = BikeStationActivity.class.getSimpleName();
	/**
	 * The station
	 **/
	private BikeStation bikeStation;
	/**
	 * Street view image
	 **/
	private ImageView streetViewImage;
	/**
	 * Street view text
	 **/
	private TextView streetViewText;
	/**
	 * Map image
	 **/
	private ImageView mapImage;
	/**
	 * Direction image
	 **/
	private ImageView directionImage;
	/**
	 * Favorite image
	 **/
	private ImageView favoritesImage;
	/**
	 * Is favorite
	 **/
	private boolean isFavorite;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {

			bikeStation = getIntent().getExtras().getParcelable("station");

			// Load right xml
			setContentView(R.layout.activity_bike_station);

			// Call google street api to load image
			new DisplayGoogleStreetPicture().execute(bikeStation.getPosition());

			isFavorite = isFavorite();

			final TextView textView = (TextView) findViewById(R.id.activity_bike_station_station_name);
			textView.setText(bikeStation.getName());

			streetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);

			streetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);

			mapImage = (ImageView) findViewById(R.id.activity_bike_station_map_image);

			directionImage = (ImageView) findViewById(R.id.activity_bike_station_map_direction);

			favoritesImage = (ImageView) findViewById(R.id.activity_bike_station_favorite_star);
			if (isFavorite) {
				favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_active));
			}
			favoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BikeStationActivity.this.switchFavorite();
				}
			});

			final TextView bikeStationValue = (TextView) findViewById(R.id.activity_bike_station_value);
			bikeStationValue.setText(bikeStation.getStAddress1());

			setValue();

			final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

			toolbar.inflateMenu(R.menu.main);
			toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					new DivvyAsyncTask().execute();
					return false;
				}
			}));

			Util.setToolbarColor(this, toolbar, TrainLine.NA);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}

			toolbar.setTitle("Divvy stop");
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
	}

	private void setValue() {
		final Context context = ChicagoTracker.getContext();
		final LinearLayout favoritesData = (LinearLayout) findViewById(R.id.favorites_bikes_list);
		favoritesData.removeAllViews();
		final LinearLayout llh = new LinearLayout(context);
		llh.setOrientation(LinearLayout.HORIZONTAL);
		final LinearLayout availableLayout = new LinearLayout(context);
		availableLayout.setOrientation(LinearLayout.VERTICAL);
		final LinearLayout availableBikes = new LinearLayout(context);
		availableBikes.setOrientation(LinearLayout.HORIZONTAL);
		final TextView availableBike = new TextView(context);
		availableBike.setText("Available bikes: ");
		availableBike.setTextColor(context.getResources().getColor(R.color.grey_5));
		availableBikes.addView(availableBike);
		final TextView amountBike = new TextView(context);
		amountBike.setText("" + bikeStation.getAvailableBikes());
		if (bikeStation.getAvailableBikes() == 0) {
			amountBike.setTextColor(context.getResources().getColor(R.color.red));
		} else {
			amountBike.setTextColor(context.getResources().getColor(R.color.green));
		}
		availableBikes.addView(amountBike);
		availableLayout.addView(availableBikes);
		final LinearLayout availableDocks = new LinearLayout(context);
		availableDocks.setOrientation(LinearLayout.HORIZONTAL);
		TextView availableDock = new TextView(context);
		availableDock.setText("Available docks: ");
		availableDock.setTextColor(context.getResources().getColor(R.color.grey_5));
		availableDocks.addView(availableDock);
		final TextView amountDock = new TextView(context);
		amountDock.setText("" + bikeStation.getAvailableDocks());
		if (bikeStation.getAvailableDocks() == 0) {
			amountDock.setTextColor(context.getResources().getColor(R.color.red));
		} else {
			amountDock.setTextColor(context.getResources().getColor(R.color.green));
		}
		availableDocks.addView(amountDock);
		availableLayout.addView(availableDocks);
		llh.addView(availableLayout);
		favoritesData.addView(llh);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bikeStation = savedInstanceState.getParcelable("station");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("station", bikeStation);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Is favorite or not ?
	 *
	 * @return if the station is favorite
	 */
	private boolean isFavorite() {
		boolean isFavorite = false;
		final List<String> favorites = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		for (final String favorite : favorites) {
			if (Integer.valueOf(favorite) == bikeStation.getId()) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
	}

	public final void refreshStation(final BikeStation station) {
		this.bikeStation = station;
		// setValue(bikeAvail);
		setValue();
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
				Log.e(TAG, "Connect error", e);
			}
			return bikeStations;
		}

		@Override
		protected final void onPostExecute(final List<BikeStation> result) {
			for (final BikeStation station : result) {
				if (BikeStationActivity.this.bikeStation.getId() == station.getId()) {
					BikeStationActivity.this.refreshStation(station);
					final Bundle bundle = getIntent().getExtras();
					bundle.putParcelable("station", station);
					break;
				}
			}
		}

	}

	/**
	 * Display google street view image
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private final class DisplayGoogleStreetPicture extends AsyncTask<Position, Void, Drawable> {
		/** **/
		private Position position;

		@Override
		protected final Drawable doInBackground(final Position... params) {
			try {
				final GStreetViewConnect connect = GStreetViewConnect.getInstance();
				this.position = params[0];
				Util.trackAction(BikeStationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(params[0]);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			final int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) BikeStationActivity.this.streetViewImage
					.getLayoutParams();
			final ViewGroup.LayoutParams params2 = BikeStationActivity.this.streetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BikeStationActivity.this.streetViewImage.setLayoutParams(params2);
			BikeStationActivity.this.streetViewImage.setImageDrawable(result);
			BikeStationActivity.this.streetViewImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", position.getLatitude(),
							position.getLongitude());
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0",
								position.getLatitude(), position.getLongitude());
						final Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			BikeStationActivity.this.mapImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.da_turn_arrive));
			BikeStationActivity.this.mapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.directionImage
					.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_directions_walking));
			BikeStationActivity.this.directionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.streetViewText.setText(ChicagoTracker.getContext().getResources()
					.getString(R.string.station_activity_street_view));
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
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_active));
		} else {
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.ic_save_disabled));
		}
	}
}
