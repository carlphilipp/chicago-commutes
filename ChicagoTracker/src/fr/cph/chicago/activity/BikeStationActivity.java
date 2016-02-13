/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.util.Util;

/**
 * Activity the list of train stations
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeStationActivity extends Activity {
	/** Tag **/
	private static final String TAG = "BikeStationActivity";
	/** The station **/
	private BikeStation mStation;
	/** Street view image **/
	private ImageView mStreetViewImage;
	/** Street view text **/
	private TextView mStreetViewText;
	/** Map image **/
	private ImageView mMapImage;
	/** Direction image **/
	private ImageView mDirectionImage;
	/** Favorite image **/
	private ImageView mFavoritesImage;
	/** Is favorite **/
	private boolean mIsFavorite;
	/** The menu **/
	private Menu mMenu;
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {

			mStation = getIntent().getExtras().getParcelable("station");

			// Load right xml
			setContentView(R.layout.activity_bike_station);

			// Call google street api to load image
			new DisplayGoogleStreetPicture().execute(mStation.getPosition());

			this.mIsFavorite = isFavorite();

			TextView textView = (TextView) findViewById(R.id.activity_bike_station_station_name);
			textView.setText(mStation.getName().toString());

			mStreetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);

			mStreetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);

			mMapImage = (ImageView) findViewById(R.id.activity_bike_station_map_image);

			mDirectionImage = (ImageView) findViewById(R.id.activity_bike_station_map_direction);

			mFavoritesImage = (ImageView) findViewById(R.id.activity_bike_station_favorite_star);
			if (mIsFavorite) {
				mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
			}
			mFavoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BikeStationActivity.this.switchFavorite();
				}
			});

			TextView bikeStationValue = (TextView) findViewById(R.id.activity_bike_station_value);
			bikeStationValue.setText(mStation.getStAddress1());

			setValue();

			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setValue() {
		Context context = ChicagoTracker.getAppContext();
		LinearLayout favoritesData = (LinearLayout) findViewById(R.id.favorites_bikes_list);
		favoritesData.removeAllViews();
		LinearLayout llh = new LinearLayout(context);
		llh.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout availableLayout = new LinearLayout(context);
		availableLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout availableBikes = new LinearLayout(context);
		availableBikes.setOrientation(LinearLayout.HORIZONTAL);
		TextView availableBike = new TextView(context);
		availableBike.setText("Available bikes: ");
		availableBike.setTextColor(context.getResources().getColor(R.color.grey_5));
		availableBikes.addView(availableBike);
		TextView amountBike = new TextView(context);
		amountBike.setText("" + mStation.getAvailableBikes());
		if (mStation.getAvailableBikes() == 0) {
			amountBike.setTextColor(context.getResources().getColor(R.color.red));
		} else {
			amountBike.setTextColor(context.getResources().getColor(R.color.green));
		}
		availableBikes.addView(amountBike);
		availableLayout.addView(availableBikes);
		LinearLayout availableDocks = new LinearLayout(context);
		availableDocks.setOrientation(LinearLayout.HORIZONTAL);
		TextView availableDock = new TextView(context);
		availableDock.setText("Available docks: ");
		availableDock.setTextColor(context.getResources().getColor(R.color.grey_5));
		availableDocks.addView(availableDock);
		TextView amountDock = new TextView(context);
		amountDock.setText("" + mStation.getAvailableDocks());
		if (mStation.getAvailableDocks() == 0) {
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
		mStation = savedInstanceState.getParcelable("station");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("station", mStation);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.mMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_no_search, menu);

		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();

		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			MenuItem menuItem = item;
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();

			new DivvyAsyncTask().execute();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Is favorite or not ?
	 * 
	 * @return if the station is favorite
	 */
	private final boolean isFavorite() {
		boolean isFavorite = false;
		List<String> favorites = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		for (String fav : favorites) {
			if (Integer.valueOf(fav) == mStation.getId()) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
	}

	public final void refreshStation(BikeStation station) {
		this.mStation = station;
		// setValue(bikeAvail);
		setValue();
		MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
		refreshMenuItem.collapseActionView();
		refreshMenuItem.setActionView(null);
	}

	private final class DivvyAsyncTask extends AsyncTask<Void, Void, List<BikeStation>> {

		@Override
		protected List<BikeStation> doInBackground(Void... params) {
			List<BikeStation> bikeStations = new ArrayList<BikeStation>();
			try {
				Json json = new Json();
				DivvyConnect divvyConnect = DivvyConnect.getInstance();
				String bikeContent = divvyConnect.connect();
				bikeStations = json.parseStations(bikeContent);
				Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				Util.trackAction(BikeStationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
						R.string.analytics_action_get_divvy_all, 0);
			} catch (ConnectException e) {
				BikeStationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "A surprising error has occured. Try again!", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e(TAG, "Connect error", e);
			} catch (ParserException e) {
				BikeStationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "A surprising error has occured. Try again!", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e(TAG, "Parser error", e);
			}
			return bikeStations;
		}

		@Override
		protected final void onPostExecute(final List<BikeStation> result) {
			for (BikeStation station : result) {
				if (BikeStationActivity.this.mStation.getId() == station.getId()) {
					BikeStationActivity.this.refreshStation(station);
					Bundle bundle = getIntent().getExtras();
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
			GStreetViewConnect connect = GStreetViewConnect.getInstance();
			try {
				this.position = params[0];
				Util.trackAction(BikeStationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) BikeStationActivity.this.mStreetViewImage
					.getLayoutParams();
			ViewGroup.LayoutParams params2 = BikeStationActivity.this.mStreetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BikeStationActivity.this.mStreetViewImage.setLayoutParams(params2);
			BikeStationActivity.this.mStreetViewImage.setImageDrawable(result);
			BikeStationActivity.this.mStreetViewImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", position.getLatitude(),
							position.getLongitude());
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0",
								position.getLatitude(), position.getLongitude());
						Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			BikeStationActivity.this.mMapImage.setImageDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.da_turn_arrive));
			BikeStationActivity.this.mMapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.mDirectionImage.setImageDrawable(ChicagoTracker.getAppContext().getResources()
					.getDrawable(R.drawable.ic_directions_walking));
			BikeStationActivity.this.mDirectionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.mStreetViewText.setText(ChicagoTracker.getAppContext().getResources()
					.getString(R.string.station_activity_street_view));

			if (mMenu != null) {
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.collapseActionView();
				refreshMenuItem.setActionView(null);
			}
		}
	}

	/**
	 * Add/remove favorites
	 */
	private final void switchFavorite() {
		if (mIsFavorite) {
			Util.removeFromBikeFavorites(mStation.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			mIsFavorite = false;
		} else {
			Util.addToBikeFavorites(mStation.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			Preferences.addBikeRouteNameMapping(String.valueOf(mStation.getId()), mStation.getName());
			mIsFavorite = true;
		}
		if (mIsFavorite) {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
		} else {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_disabled));
		}
	}
}
