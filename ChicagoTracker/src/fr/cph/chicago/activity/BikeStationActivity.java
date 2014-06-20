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

package fr.cph.chicago.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
	private BikeStation station;
	/** Street view image **/
	private ImageView streetViewImage;
	/** Street view text **/
	private TextView streetViewText;
	/** Available bike **/
	private TextView bikeAvail;
	/** Map image **/
	private ImageView mapImage;
	/** Direction image **/
	private ImageView directionImage;
	/** Favorite image **/
	private ImageView favoritesImage;
	/** Is favorite **/
	private boolean isFavorite;
	/** The menu **/
	private Menu menu;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {

			station = getIntent().getExtras().getParcelable("station");

			// Load right xml
			setContentView(R.layout.activity_bike_station);

			// Call google street api to load image
			new DisplayGoogleStreetPicture().execute(station.getPosition());

			this.isFavorite = isFavorite();

			TextView textView = (TextView) findViewById(R.id.activity_bike_station_station_name);
			textView.setText(station.getName().toString());

			streetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);

			streetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);

			mapImage = (ImageView) findViewById(R.id.activity_bike_station_map_image);

			directionImage = (ImageView) findViewById(R.id.activity_bike_station_map_direction);

			favoritesImage = (ImageView) findViewById(R.id.activity_bike_station_favorite_star);
			if (isFavorite) {
				favoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
			}
			favoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BikeStationActivity.this.switchFavorite();
				}
			});

			TextView bikeStationValue = (TextView) findViewById(R.id.activity_bike_station_value);
			bikeStationValue.setText(station.getStAddress1());

			bikeAvail = (TextView) findViewById(R.id.activity_bike_station_availaible);
			setValue(bikeAvail);

			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setValue(TextView bikeAvail) {
		if (station.getAvailableBikes() == station.getTotalDocks()) {
			bikeAvail.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.red));
		} else {
			bikeAvail.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.green));
		}
		bikeAvail.setText(station.getAvailableBikes() + "/" + station.getTotalDocks());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		station = savedInstanceState.getParcelable("station");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("station", station);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
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
			Toast.makeText(this, "Refresh...!", Toast.LENGTH_SHORT).show();
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
		List<Integer> favorites = Preferences.getBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
		for (Integer fav : favorites) {
			if (fav.intValue() == station.getId()) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
	}

	public final void refreshStation(BikeStation station) {
		this.station = station;
		setValue(bikeAvail);
		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
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

			} catch (ConnectException e) {
				BikeStationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "Error, try again later!", Toast.LENGTH_LONG).show();
					}
				});
				Log.e(TAG, "Connect error", e);
			} catch (ParserException e) {
				BikeStationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "Error, try again later!", Toast.LENGTH_LONG).show();
					}
				});
				Log.e(TAG, "Parser error", e);
			}
			return bikeStations;
		}

		@Override
		protected final void onPostExecute(final List<BikeStation> result) {
			for (BikeStation station : result) {
				if (BikeStationActivity.this.station.getId() == station.getId()) {
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
				return connect.connect(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) BikeStationActivity.this.streetViewImage
					.getLayoutParams();
			ViewGroup.LayoutParams params2 = BikeStationActivity.this.streetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BikeStationActivity.this.streetViewImage.setLayoutParams(params2);
			BikeStationActivity.this.streetViewImage.setImageDrawable(result);
			BikeStationActivity.this.streetViewImage.setOnClickListener(new View.OnClickListener() {
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
			BikeStationActivity.this.mapImage.setImageDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.da_turn_arrive));
			BikeStationActivity.this.mapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.directionImage.setImageDrawable(ChicagoTracker.getAppContext().getResources()
					.getDrawable(R.drawable.ic_directions_walking));
			BikeStationActivity.this.directionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			BikeStationActivity.this.streetViewText.setText(ChicagoTracker.getAppContext().getResources()
					.getString(R.string.station_activity_street_view));

			if (menu != null) {
				MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
				refreshMenuItem.collapseActionView();
				refreshMenuItem.setActionView(null);
			}
		}
	}

	/**
	 * Add/remove favorites
	 */
	private final void switchFavorite() {
		if (isFavorite) {
			Util.removeFromBikeFavorites(station.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			isFavorite = false;
		} else {
			Log.i(TAG, "Add to bike fav " + station.getId());
			Util.addToBikeFavorites(station.getId(), ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			isFavorite = true;
		}
		if (isFavorite) {
			favoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
		} else {
			favoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_disabled));
		}
	}
}
