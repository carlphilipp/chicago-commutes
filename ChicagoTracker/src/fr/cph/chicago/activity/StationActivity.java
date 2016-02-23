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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
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
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationActivity extends Activity {

	private static final String TAG = StationActivity.class.getSimpleName();

	private TrainData trainData;

	private TrainArrival trainArrival;

	private Integer stationId;

	private Station station;

	private ImageView streetViewImage;

	private TextView streetViewText;

	private ImageView mapImage;

	private ImageView directionImage;

	private ImageView favoritesImage;

	private boolean isFavorite;

	private Map<String, Integer> ids;

	private LinearLayout.LayoutParams paramsStop;

	private SwipeRefreshLayout swipeRefreshLayout;

	@SuppressWarnings("unchecked")
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		if (!this.isFinishing()) {
			// Load data
			final DataHolder dataHolder = DataHolder.getInstance();
			trainData = dataHolder.getTrainData();

			ids = new HashMap<>();

			// Load right xml
			setContentView(R.layout.activity_station);

			// Get station id from bundle extra
			if (stationId == null) {
				stationId = getIntent().getExtras().getInt("stationId");
			}

			// Get station from station id
			station = trainData.getStation(stationId);

			final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
			reqParams.put("mapid", String.valueOf(station.getId()));

			swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
			swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					new LoadData().execute(reqParams);
				}
			});

			new LoadData().execute(reqParams);

			// Call google street api to load image
			new DisplayGoogleStreetPicture().execute(station.getStops().get(0).getPosition());

			isFavorite = isFavorite();

			final TextView textView = (TextView) findViewById(R.id.activity_bike_station_station_name);
			textView.setText(station.getName());

			streetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);
			streetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);
			mapImage = (ImageView) findViewById(R.id.activity_bike_station_map_image);
			directionImage = (ImageView) findViewById(R.id.activity_bike_station_map_direction);
			favoritesImage = (ImageView) findViewById(R.id.activity_bike_station_favorite_star);
			if (isFavorite) {
				favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.ic_save_active));
			}
			favoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					StationActivity.this.switchFavorite();
				}
			});
			final LinearLayout stopsView = (LinearLayout) findViewById(R.id.activity_bike_station_details);
			paramsStop = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			final Map<TrainLine, List<Stop>> stops = station.getStopByLines();
			CheckBox checkBox;
			for (final Entry<TrainLine, List<Stop>> e : stops.entrySet()) {
				final TrainLine line = e.getKey();
				final List<Stop> stopss = e.getValue();
				Collections.sort(stopss);
				final LayoutInflater layoutInflater = (LayoutInflater) ChicagoTracker.getAppContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View view = layoutInflater.inflate(R.layout.activity_station_line_title, null);

				final TextView lineTextView = (TextView) view.findViewById(R.id.activity_bus_station_value);
				lineTextView.setText(line.toStringWithLine());

				final TextView lineColorTextView = (TextView) view.findViewById(R.id.activity_bus_color);
				lineColorTextView.setBackgroundColor(line.getColor());
				stopsView.addView(view);

				for (final Stop stop : stopss) {
					final LinearLayout line2 = new LinearLayout(this);
					line2.setOrientation(LinearLayout.HORIZONTAL);
					line2.setLayoutParams(paramsStop);

					checkBox = new CheckBox(this);
					checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							Preferences.saveTrainFilter(stationId, line, stop.getDirection(), isChecked);
						}
					});
					checkBox.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// Update timing
							final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
							reqParams.put("mapid", String.valueOf(station.getId()));
							new LoadData().execute(reqParams);
						}
					});
					checkBox.setChecked(Preferences.getTrainFilter(stationId, line, stop.getDirection()));
					checkBox.setText(stop.getDirection().toString());
					checkBox.setTextColor(getResources().getColor(R.color.grey));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						checkBox.setBackgroundTintList(ColorStateList.valueOf(line.getColor()));
						checkBox.setButtonTintList(ColorStateList.valueOf(line.getColor()));
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						checkBox.setForegroundTintList(ColorStateList.valueOf(line.getColor()));
					}

					line2.addView(checkBox);
					stopsView.addView(line2);

					final LinearLayout line3 = new LinearLayout(this);
					line3.setOrientation(LinearLayout.VERTICAL);
					line3.setLayoutParams(paramsStop);
					int id3 = Util.generateViewId();
					line3.setId(id3);
					ids.put(line.toString() + "_" + stop.getDirection().toString(), id3);

					stopsView.addView(line3);
				}

			}

			final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			toolbar.inflateMenu(R.menu.main);
			toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(final MenuItem item) {
					swipeRefreshLayout.setRefreshing(true);
					final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
					reqParams.put("mapid", String.valueOf(station.getId()));
					new LoadData().execute(reqParams);
					return false;
				}
			}));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}

			final TrainLine randomTrainLine = getRandomLine(stops);

			Util.setToolbarColor(this, toolbar, randomTrainLine);

			toolbar.setTitle("Train station");
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			Util.trackScreen(getResources().getString(R.string.analytics_train_details));
		}
	}

	private TrainLine getRandomLine(final Map<TrainLine, List<Stop>> stops) {
		final Random random = new Random();
		final List<TrainLine> keys = new ArrayList<>(stops.keySet());
		return keys.get(random.nextInt(keys.size()));
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		stationId = savedInstanceState.getInt("stationId");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("stationId", stationId);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			item.setActionView(R.layout.progressbar);
			item.expandActionView();

			//			MultiMap<String, String> params = new MultiValueMap<>();
			//			List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			//			for (Integer fav : favorites) {
			//				params.put("mapid", String.valueOf(fav));
			//			}
			final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
			reqParams.put("mapid", String.valueOf(station.getId()));
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
		final List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		for (final Integer favorite : favorites) {
			if (favorite.intValue() == stationId.intValue()) {
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
	private class DisplayGoogleStreetPicture extends AsyncTask<Position, Void, Drawable> {
		/** **/
		private Position position;

		@Override
		protected final Drawable doInBackground(final Position... params) {
			final GStreetViewConnect connect = GStreetViewConnect.getInstance();
			try {
				position = params[0];
				Util.trackAction(StationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(params[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			final int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) StationActivity.this.streetViewImage
					.getLayoutParams();
			final ViewGroup.LayoutParams params2 = StationActivity.this.streetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			StationActivity.this.streetViewImage.setLayoutParams(params2);
			StationActivity.this.streetViewImage.setImageDrawable(result);
			StationActivity.this.streetViewImage.setOnClickListener(new View.OnClickListener() {
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
						Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			StationActivity.this.mapImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.da_turn_arrive));
			StationActivity.this.mapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			StationActivity.this.directionImage
					.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.ic_directions_walking));
			StationActivity.this.directionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			StationActivity.this.streetViewText.setText(ChicagoTracker.getAppContext().getResources()
					.getString(R.string.station_activity_street_view));
		}
	}

	/**
	 * Load train arrivals
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class LoadData extends AsyncTask<MultiValuedMap<String, String>, Void, TrainArrival> {

		/**
		 * The exception that might be thrown
		 **/
		private TrackerException trackerException;

		@SafeVarargs
		@Override
		protected final TrainArrival doInBackground(final MultiValuedMap<String, String>... params) {
			// Get menu item and put it to loading mod
			publishProgress((Void[]) null);
			SparseArray<TrainArrival> arrivals = new SparseArray<>();
			final CtaConnect connect = CtaConnect.getInstance();
			try {
				final Xml xml = new Xml();
				final String xmlResult = connect.connect(CtaRequestType.TRAIN_ARRIVALS, params[0]);
				// String xmlResult = connectTest();
				arrivals = xml.parseArrivals(xmlResult, StationActivity.this.trainData);
				// Apply filters
				int index = 0;
				while (index < arrivals.size()) {
					final TrainArrival arri = arrivals.valueAt(index++);
					final List<Eta> etas = arri.getEtas();
					// Sort Eta by arriving time
					Collections.sort(etas);
					// Copy data into new list to be able to avoid looping on a list that we want to
					// modify
					final List<Eta> etas2 = new ArrayList<>();
					etas2.addAll(etas);
					int j = 0;
					for (int i = 0; i < etas2.size(); i++) {
						final Eta eta = etas2.get(i);
						final Station station = eta.getStation();
						final TrainLine line = eta.getRouteName();
						final TrainDirection direction = eta.getStop().getDirection();
						final boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
						if (!toRemove) {
							etas.remove(i - j++);
						}
					}
				}
			} catch (final ParserException | ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(StationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
					R.string.analytics_action_get_train_arrivals, 0);
			if (arrivals.size() == 1) {
				@SuppressWarnings("unchecked")
				final String id = ((List<String>) params[0].get("mapid")).get(0);
				return arrivals.get(Integer.valueOf(id));
			} else {
				return null;
			}
		}

		@Override
		protected final void onProgressUpdate(final Void... values) {
		}

		@Override
		protected final void onPostExecute(final TrainArrival result) {
			if (this.trackerException == null) {
				trainArrival = result;
				List<Eta> etas;
				if (trainArrival != null) {
					etas = trainArrival.getEtas();
				} else {
					etas = Collections.emptyList();
				}
				reset(StationActivity.this.station);
				for (final Eta eta : etas) {
					drawLine3(eta);
				}
			} else {
				ChicagoTracker.displayError(StationActivity.this, trackerException);
			}
			if (swipeRefreshLayout != null) {
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	/**
	 * Reset arrival layouts
	 *
	 * @param station the station
	 */
	private void reset(final Station station) {
		final Set<TrainLine> setTL = station.getLines();
		if (setTL != null) {
			for (TrainLine tl : setTL) {
				for (TrainDirection d : TrainDirection.values()) {
					final Integer id = ids.get(tl.toString() + "_" + d.toString());
					if (id != null) {
						final LinearLayout line3View = (LinearLayout) findViewById(id);
						if (line3View != null) {
							line3View.setVisibility(View.GONE);
							if (line3View.getChildCount() > 0) {
								for (int i = 0; i < line3View.getChildCount(); i++) {
									final LinearLayout view = (LinearLayout) line3View.getChildAt(i);
									final TextView timing = (TextView) view.getChildAt(1);
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
	}

	/**
	 * Draw line
	 *
	 * @param eta the eta
	 */
	private void drawLine3(final Eta eta) {
		final TrainLine line = eta.getRouteName();
		final Stop stop = eta.getStop();
		final int line3Padding = (int) getResources().getDimension(R.dimen.activity_station_stops_line3);
		final Integer viewId = ids.get(line.toString() + "_" + stop.getDirection().toString());
		// viewId might be not there if CTA API provide wrong data
		if (viewId != null) {
			final LinearLayout line3View = (LinearLayout) findViewById(viewId);
			final Integer id = ids.get(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName());
			if (id == null) {
				final LinearLayout insideLayout = new LinearLayout(this);
				insideLayout.setOrientation(LinearLayout.HORIZONTAL);
				insideLayout.setLayoutParams(paramsStop);
				final int newId = Util.generateViewId();
				insideLayout.setId(newId);
				ids.put(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName(), newId);

				final TextView stopName = new TextView(this);
				stopName.setText(eta.getDestName() + ": ");
				stopName.setTextColor(getResources().getColor(R.color.grey));
				stopName.setPadding(line3Padding, 0, 0, 0);
				insideLayout.addView(stopName);

				final TextView timing = new TextView(this);
				timing.setText(eta.getTimeLeftDueDelay() + " ");
				timing.setTextColor(getResources().getColor(R.color.grey));
				timing.setLines(1);
				timing.setEllipsize(TruncateAt.END);
				insideLayout.addView(timing);

				line3View.addView(insideLayout);
			} else {
				final LinearLayout insideLayout = (LinearLayout) findViewById(id);
				final TextView timing = (TextView) insideLayout.getChildAt(1);
				timing.setText(timing.getText() + eta.getTimeLeftDueDelay() + " ");
			}
			line3View.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Add/remove favorites
	 */
	private void switchFavorite() {
		if (isFavorite) {
			Util.removeFromTrainFavorites(stationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			isFavorite = false;
		} else {
			Util.addToTrainFavorites(stationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
			isFavorite = true;
		}
		if (isFavorite) {
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.ic_save_active));
		} else {
			favoritesImage.setImageDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.ic_save_disabled));
		}
	}
}
