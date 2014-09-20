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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

/**
 * Activity that represents the train station
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationActivity extends Activity {
	/** Train data **/
	private TrainData mTrainData;
	/** Train arrival **/
	private TrainArrival mArrival;
	/** The station id **/
	private Integer mStationId;
	/** The station **/
	private Station mStation;
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
	/** Map of ids **/
	private Map<String, Integer> mIds;
	/** Params stops **/
	private LinearLayout.LayoutParams mParamsStop;
	/** The menu **/
	private Menu mMenu;
	/** The first load **/
	private boolean mFirstLoad = true;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		if (!this.isFinishing()) {
			// Load data
			DataHolder dataHolder = DataHolder.getInstance();
			this.mTrainData = dataHolder.getTrainData();

			mIds = new HashMap<String, Integer>();

			// Load right xml
			setContentView(R.layout.activity_station);

			// Get station id from bundle extra
			if (mStationId == null) {
				mStationId = getIntent().getExtras().getInt("stationId");
			}

			// Get station from station id
			mStation = mTrainData.getStation(mStationId);

			MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
			reqParams.put("mapid", String.valueOf(mStation.getId()));
			new LoadData().execute(reqParams);

			// Call google street api to load image
			new DisplayGoogleStreetPicture().execute(mStation.getStops().get(0).getPosition());

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
					StationActivity.this.switchFavorite();
				}
			});

			LinearLayout stopsView = (LinearLayout) findViewById(R.id.activity_bike_station_details);

			this.mParamsStop = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			Map<TrainLine, List<Stop>> stops = mStation.getStopByLines();
			CheckBox checkBox = null;
			for (Entry<TrainLine, List<Stop>> e : stops.entrySet()) {
				final TrainLine line = e.getKey();
				List<Stop> stopss = e.getValue();
				Collections.sort(stopss);
				LayoutInflater layoutInflater = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = layoutInflater.inflate(R.layout.activity_station_line_title, null);

				TextView lineTextView = (TextView) view.findViewById(R.id.activity_bus_station_value);
				lineTextView.setText(line.toStringWithLine());

				TextView lineColorTextView = (TextView) view.findViewById(R.id.activity_bus_color);
				lineColorTextView.setBackgroundColor(line.getColor());
				stopsView.addView(view);

				for (final Stop stop : stopss) {
					LinearLayout line2 = new LinearLayout(this);
					line2.setOrientation(LinearLayout.HORIZONTAL);
					line2.setLayoutParams(mParamsStop);

					checkBox = new CheckBox(this);
					checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							Preferences.saveTrainFilter(mStationId, line, stop.getDirection(), isChecked);
						}
					});
					checkBox.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// Update timing
							MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
							reqParams.put("mapid", String.valueOf(mStation.getId()));
							new LoadData().execute(reqParams);
						}
					});
					checkBox.setChecked(Preferences.getTrainFilter(mStationId, line, stop.getDirection()));
					checkBox.setText(stop.getDirection().toString());
					checkBox.setTextColor(getResources().getColor(R.color.grey));

					line2.addView(checkBox);
					stopsView.addView(line2);

					LinearLayout line3 = new LinearLayout(this);
					line3.setOrientation(LinearLayout.VERTICAL);
					line3.setLayoutParams(mParamsStop);
					int id3 = Util.generateViewId();
					line3.setId(id3);
					mIds.put(line.toString() + "_" + stop.getDirection().toString(), id3);

					stopsView.addView(line3);
				}

			}
			getActionBar().setDisplayHomeAsUpEnabled(true);

			Util.trackScreen(this, R.string.analytics_train_details);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mStationId = savedInstanceState.getInt("stationId");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("stationId", mStationId);
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

	@SuppressWarnings("unchecked")
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

			MultiMap<String, String> params = new MultiValueMap<String, String>();
			List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			for (Integer fav : favorites) {
				params.put("mapid", String.valueOf(fav));
			}
			MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
			reqParams.put("mapid", String.valueOf(mStation.getId()));
			new LoadData().execute(reqParams);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Is favorite or not ?
	 * 
	 * @return if the station is favorite
	 */
	private boolean isFavorite() {
		boolean isFavorite = false;
		List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		for (Integer fav : favorites) {
			if (fav.intValue() == mStationId.intValue()) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
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
				Util.trackAction(StationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
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
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) StationActivity.this.mStreetViewImage
					.getLayoutParams();
			ViewGroup.LayoutParams params2 = StationActivity.this.mStreetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			StationActivity.this.mStreetViewImage.setLayoutParams(params2);
			StationActivity.this.mStreetViewImage.setImageDrawable(result);
			StationActivity.this.mStreetViewImage.setOnClickListener(new View.OnClickListener() {
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
			StationActivity.this.mMapImage.setImageDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.da_turn_arrive));
			StationActivity.this.mMapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			StationActivity.this.mDirectionImage.setImageDrawable(ChicagoTracker.getAppContext().getResources()
					.getDrawable(R.drawable.ic_directions_walking));
			StationActivity.this.mDirectionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			StationActivity.this.mStreetViewText.setText(ChicagoTracker.getAppContext().getResources()
					.getString(R.string.station_activity_street_view));

			if (mMenu != null) {
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.collapseActionView();
				refreshMenuItem.setActionView(null);
			}
			mFirstLoad = false;
		}
	}

	/**
	 * Load train arrivals
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class LoadData extends AsyncTask<MultiMap<String, String>, Void, TrainArrival> {

		/** The exception that might be thrown **/
		private TrackerException trackerException;

		@SafeVarargs
		@Override
		protected final TrainArrival doInBackground(final MultiMap<String, String>... params) {
			// Get menu item and put it to loading mod
			publishProgress((Void[]) null);
			SparseArray<TrainArrival> arrivals = new SparseArray<TrainArrival>();
			CtaConnect connect = CtaConnect.getInstance();
			try {
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.TRAIN_ARRIVALS, params[0]);
				// String xmlResult = connectTest();
				arrivals = xml.parseArrivals(xmlResult, StationActivity.this.mTrainData);
				// Apply filters
				int index = 0;
				while (index < arrivals.size()) {
					TrainArrival arri = arrivals.valueAt(index++);
					List<Eta> etas = arri.getEtas();
					// Sort Eta by arriving time
					Collections.sort(etas);
					// Copy data into new list to be able to avoid looping on a list that we want to
					// modify
					List<Eta> etas2 = new ArrayList<Eta>();
					etas2.addAll(etas);
					int j = 0;
					Eta eta = null;
					Station station = null;
					TrainLine line = null;
					TrainDirection direction = null;
					for (int i = 0; i < etas2.size(); i++) {
						eta = etas2.get(i);
						station = eta.getStation();
						line = eta.getRouteName();
						direction = eta.getStop().getDirection();
						boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
						if (!toRemove) {
							etas.remove(i - j++);
						}
					}
				}
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(StationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
					R.string.analytics_action_get_train_arrivals, 0);
			if (arrivals.size() == 1) {
				@SuppressWarnings("unchecked")
				String id = ((List<String>) params[0].get("mapid")).get(0);
				return arrivals.get(Integer.valueOf(id));
			} else {
				return null;
			}
		}

		@Override
		protected final void onProgressUpdate(final Void... values) {
			// Get menu item and put it to loading mod
			if (mMenu != null) {
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.setActionView(R.layout.progressbar);
				refreshMenuItem.expandActionView();
			}
		}

		@Override
		protected final void onPostExecute(final TrainArrival result) {
			if (this.trackerException == null) {
				mArrival = result;
				List<Eta> etas;
				if (mArrival != null) {
					etas = mArrival.getEtas();
				} else {
					etas = new ArrayList<Eta>();
				}
				reset(StationActivity.this.mStation);
				for (Eta eta : etas) {
					drawLine3(eta);
				}
				if (!mFirstLoad) {
					MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
					refreshMenuItem.collapseActionView();
					refreshMenuItem.setActionView(null);
				}
			} else {
				ChicagoTracker.displayError(StationActivity.this, trackerException);
			}
		}
	}

	/**
	 * Reset arrival layouts
	 * 
	 * @param station
	 *            the station
	 */
	private final void reset(final Station station) {
		Set<TrainLine> setTL = station.getLines();
		for (TrainLine tl : setTL) {
			for (TrainDirection d : TrainDirection.values()) {
				Integer id = mIds.get(tl.toString() + "_" + d.toString());
				if (id != null) {
					LinearLayout line3View = (LinearLayout) findViewById(id);
					if (line3View != null) {
						line3View.setVisibility(View.GONE);
						if (line3View.getChildCount() > 0) {
							for (int i = 0; i < line3View.getChildCount(); i++) {
								LinearLayout view = (LinearLayout) line3View.getChildAt(i);
								TextView timing = (TextView) view.getChildAt(1);
								if (timing != null) {
									timing.setText("");
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Draw line
	 * 
	 * @param eta
	 *            the eta
	 */
	private final void drawLine3(final Eta eta) {
		TrainLine line = eta.getRouteName();
		Stop stop = eta.getStop();
		int line3Padding = (int) getResources().getDimension(R.dimen.activity_station_stops_line3);
		Integer viewId = mIds.get(line.toString() + "_" + stop.getDirection().toString());
		// viewId might be not there if CTA API provide wrong data
		if (viewId != null) {
			LinearLayout line3View = (LinearLayout) findViewById(viewId);
			Integer id = mIds.get(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName());
			if (id == null) {
				LinearLayout insideLayout = new LinearLayout(this);
				insideLayout.setOrientation(LinearLayout.HORIZONTAL);
				insideLayout.setLayoutParams(mParamsStop);
				int newId = Util.generateViewId();
				insideLayout.setId(newId);
				mIds.put(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName(), newId);

				TextView stopName = new TextView(this);
				stopName.setText(eta.getDestName() + ": ");
				stopName.setTextColor(getResources().getColor(R.color.grey));
				stopName.setPadding(line3Padding, 0, 0, 0);
				insideLayout.addView(stopName);

				TextView timing = new TextView(this);
				timing.setText(eta.getTimeLeftDueDelay() + " ");
				timing.setTextColor(getResources().getColor(R.color.grey));
				timing.setLines(1);
				timing.setEllipsize(TruncateAt.END);
				insideLayout.addView(timing);

				line3View.addView(insideLayout);
			} else {
				LinearLayout insideLayout = (LinearLayout) findViewById(id);
				TextView timing = (TextView) insideLayout.getChildAt(1);
				timing.setText(timing.getText() + eta.getTimeLeftDueDelay() + " ");
			}
			line3View.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Add/remove favorites
	 */
	private final void switchFavorite() {
		if (mIsFavorite) {
			Util.removeFromTrainFavorites(mStationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			mIsFavorite = false;
		} else {
			Util.addToTrainFavorites(mStationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			mIsFavorite = true;
		}
		if (mIsFavorite) {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
		} else {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_disabled));
		}
	}
}
