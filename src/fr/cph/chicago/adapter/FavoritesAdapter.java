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

package fr.cph.chicago.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.BusActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Favorites;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.listener.FavoritesBusOnClickListener;
import fr.cph.chicago.listener.FavoritesTrainOnClickListener;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Adapter that will handle favorites
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public final class FavoritesAdapter extends BaseAdapter {

	private Context context;
	private LinearLayout.LayoutParams paramsLayout;
	private LinearLayout.LayoutParams paramsTextView;

	private MainActivity mainActivity;
	private Favorites favorites;
	private Map<String, Integer> ids;
	private Map<Integer, LinearLayout> layouts;
	private Map<Integer, View> views;
	private Map<String, TextView> updated;
	private String lastUpdate;
	private int line1Padding;
	private int stopsPaddingTop;

	public FavoritesAdapter(final MainActivity activity) {
		this.context = ChicagoTracker.getContext();

		this.mainActivity = activity;
		this.favorites = new Favorites();

		this.ids = new HashMap<>();
		this.layouts = new HashMap<>();
		this.views = new HashMap<>();
		this.updated = new HashMap<>();

		this.paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		this.line1Padding = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
		this.stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
	}

	@Override
	public final int getCount() {
		return favorites.size();
	}

	@Override
	public final Object getItem(final int position) {
		return favorites.getObject(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		final Date lastUpdate = ChicagoTracker.getLastUpdate();
		final Object object = favorites.getObject(position);
		if (object != null) {
			/*********************************************************/
			/********************** TRAINS ***************************/
			/*********************************************************/
			if (object instanceof Station) {
				final Station station = (Station) object;
				final Integer stationId = station.getId();
				final LinearLayout favoritesLayout;

				TextView updatedView;

				if (layouts.containsKey(stationId)) {
					favoritesLayout = layouts.get(stationId);
					convertView = views.get(stationId);

					final TrainViewHolder viewHolder = (TrainViewHolder) convertView.getTag();
					updatedView = viewHolder.updatedView;
					updatedView.setText(this.lastUpdate);

				} else {
					final LayoutInflater vi = (LayoutInflater) ChicagoTracker.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.list_favorites_train, parent, false);
					favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list_main);
					layouts.put(stationId, favoritesLayout);
					views.put(stationId, convertView);

					final TrainViewHolder holder = new TrainViewHolder();

					final TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
					stationNameView.setText(station.getName());
					holder.stationNameView = stationNameView;

					updatedView = (TextView) convertView.findViewById(R.id.station_updated);
					updated.put(station.getId().toString(), updatedView);
					if (lastUpdate != null) {
						updatedView.setText(this.lastUpdate);
					}
					holder.updatedView = updatedView;

					convertView.setTag(holder);
				}

				final LinearLayout.LayoutParams paramsArrival = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

				final Set<TrainLine> setTL = station.getLines();

				// Reset ETAs
				for (int i = 0; i < favoritesLayout.getChildCount(); i++) {
					final LinearLayout layout = (LinearLayout) favoritesLayout.getChildAt(i);
					final LinearLayout layoutChild = (LinearLayout) layout.getChildAt(1);
					for (int j = 0; j < layoutChild.getChildCount(); j++) {
						final LinearLayout layoutChildV = (LinearLayout) layoutChild.getChildAt(j);
						final TextView timing = (TextView) layoutChildV.getChildAt(1);
						// to delete ?
						if (timing != null) {
							timing.setText("");
						}
					}
				}

				if (setTL != null) {
					for (final TrainLine tl : setTL) {
						if (favorites.getTrainArrival(stationId) != null) {
							final List<Eta> etas = favorites.getTrainArrival(stationId).getEtas(tl);
							if (etas.size() != 0) {
								final String key = station.getName() + "_" + tl.toString() + "_h";
								final String key2 = station.getName() + "_" + tl.toString() + "_v";
								final Integer idLayout = ids.get(key);
								final Integer idLayout2 = ids.get(key2);

								LinearLayout llh, llv;
								if (idLayout == null) {
									llh = new LinearLayout(context);
									llh.setLayoutParams(paramsLayout);
									llh.setOrientation(LinearLayout.HORIZONTAL);
									llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);
									final int id = Util.generateViewId();
									llh.setId(id);
									ids.put(key, id);

									TextView tlView = new TextView(context);
									tlView.setBackgroundColor(tl.getColor());
									tlView.setText("   ");
									tlView.setLayoutParams(paramsTextView);
									llh.addView(tlView);

									llv = new LinearLayout(context);
									llv.setLayoutParams(paramsLayout);
									llv.setOrientation(LinearLayout.VERTICAL);
									llv.setPadding(line1Padding, 0, 0, 0);
									final int id2 = Util.generateViewId();
									llv.setId(id2);
									ids.put(key2, id2);

									llh.addView(llv);
									favoritesLayout.addView(llh);

								} else {
									llh = (LinearLayout) favoritesLayout.findViewById(idLayout);
									llv = (LinearLayout) favoritesLayout.findViewById(idLayout2);
								}
								for (final Eta eta : etas) {
									final Stop stop = eta.getStop();
									final String key3 = (station.getName() + "_" + tl.toString() + "_" + stop.getDirection().toString() + "_" + eta
											.getDestName());
									final Integer idLayout3 = ids.get(key3);
									if (idLayout3 == null) {
										final LinearLayout insideLayout = new LinearLayout(context);
										insideLayout.setOrientation(LinearLayout.HORIZONTAL);
										insideLayout.setLayoutParams(paramsArrival);
										final int newId = Util.generateViewId();
										insideLayout.setId(newId);
										ids.put(key3, newId);

										final TextView stopName = new TextView(context);
										final String stopNameData = eta.getDestName() + ": ";
										stopName.setText(stopNameData);
										stopName.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
										insideLayout.addView(stopName);

										final TextView timing = new TextView(context);
										final String timingData = eta.getTimeLeftDueDelay() + " ";
										timing.setText(timingData);
										timing.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
										timing.setLines(1);
										timing.setEllipsize(TruncateAt.END);
										insideLayout.addView(timing);

										llv.addView(insideLayout);
									} else {
										// llv can be null sometimes (after a remove from favorites for example)
										if (llv != null) {
											final LinearLayout insideLayout = (LinearLayout) llv.findViewById(idLayout3);
											final TextView timing = (TextView) insideLayout.getChildAt(1);
											final String timingData = timing.getText() + eta.getTimeLeftDueDelay() + " ";
											timing.setText(timingData);
										}
									}
								}
							}
						}
					}
				}

				convertView.setOnClickListener(new FavoritesTrainOnClickListener(mainActivity, stationId, setTL));

				// Remove empty bloc
				for (int i = 0; i < favoritesLayout.getChildCount(); i++) {
					final LinearLayout llh = (LinearLayout) favoritesLayout.getChildAt(i);
					final LinearLayout layoutChild = (LinearLayout) llh.getChildAt(1);
					for (int j = 0; j < layoutChild.getChildCount(); j++) {
						final LinearLayout layoutChildH = (LinearLayout) layoutChild.getChildAt(j);
						final TextView timing = (TextView) layoutChildH.getChildAt(1);
						if (timing != null) {
							if (timing.getText().toString().equals("")) {
								layoutChildH.removeAllViews();
								final List<String> toRemove = new ArrayList<>();
								for (final Entry<String, Integer> e : this.ids.entrySet()) {
									if (e.getValue() == layoutChildH.getId()) {
										toRemove.add(e.getKey());
									}
								}
								for (final String d : toRemove) {
									this.ids.remove(d);
								}
							}
						}
					}
				}
				/*********************************************************/
				/*********************** BUSES ***************************/
				/*********************************************************/
			} else if (object instanceof BusRoute) {
				final BusRoute busRoute = (BusRoute) object;
				final LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_bus, parent, false);
				TextView routeIdView = (TextView) convertView.findViewById(R.id.route_id);
				routeIdView.setText(busRoute.getId());

				final TextView routeNameView = (TextView) convertView.findViewById(R.id.station_name);
				final String routeName = " " + busRoute.getName();
				routeNameView.setText(routeName);

				final LinearLayout favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);

				final TextView updated = (TextView) convertView.findViewById(R.id.station_updated);
				if (!this.updated.containsKey(busRoute.getId())) {
					this.updated.put(busRoute.getId(), updated);
				}
				if (lastUpdate != null) {
					updated.setText(this.lastUpdate);
				}

				final Map<String, Map<String, List<BusArrival>>> busArrivals = favorites.getBusArrivalsMapped(busRoute.getId());
				if (busArrivals.size() > 0) {
					for (Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
						final LinearLayout llh = new LinearLayout(context);
						llh.setLayoutParams(paramsLayout);
						llh.setOrientation(LinearLayout.HORIZONTAL);
						llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							llh.setBackground(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.any_selector));
						}

						final TextView tlView = new TextView(context);
						tlView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
						tlView.setText("   ");
						tlView.setLayoutParams(paramsTextView);
						llh.addView(tlView);

						final String key = entry.getKey();
						final Map<String, List<BusArrival>> value = entry.getValue();

						llh.setOnClickListener(new FavoritesBusOnClickListener(mainActivity, parent, busRoute, value));

						final LinearLayout stopLayout = new LinearLayout(context);
						stopLayout.setOrientation(LinearLayout.VERTICAL);
						stopLayout.setPadding(line1Padding, 0, 0, 0);

						final TextView stopName = new TextView(context);
						stopName.setText(String.valueOf(key));
						stopName.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
						stopName.setTypeface(Typeface.DEFAULT_BOLD);

						stopLayout.addView(stopName);

						for (final Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
							final String key2 = entry2.getKey();
							final List<BusArrival> buses = entry2.getValue();

							final LinearLayout boundLayout = new LinearLayout(context);
							boundLayout.setOrientation(LinearLayout.HORIZONTAL);

							final TextView bound = new TextView(context);
							final String boundText = key2 + ": ";
							bound.setText(boundText);
							bound.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
							boundLayout.addView(bound);

							for (final BusArrival arri : buses) {
								final TextView timeView = new TextView(context);
								final String timeText = arri.getTimeLeftDueDelay() + " ";
								timeView.setText(timeText);
								timeView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
								timeView.setLines(1);
								timeView.setEllipsize(TruncateAt.END);
								boundLayout.addView(timeView);
							}
							stopLayout.addView(boundLayout);
						}
						llh.addView(stopLayout);
						favoritesLayout.addView(llh);
					}
				}
				/*********************************************************/
				/*********************** BIKES ***************************/
				/*********************************************************/
			} else {
				final BikeStation bikeStation = (BikeStation) object;
				final LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_bike, parent, false);
				final TextView bikeStationName = (TextView) convertView.findViewById(R.id.bike_station_name);
				bikeStationName.setText(bikeStation.getName());

				final TextView updatedView = (TextView) convertView.findViewById(R.id.station_updated);
				if (lastUpdate != null) {
					updatedView.setText(this.lastUpdate);
				}

				final LinearLayout favoritesData = (LinearLayout) convertView.findViewById(R.id.favorites_bikes_list);

				final LinearLayout llh = new LinearLayout(context);
				llh.setLayoutParams(paramsLayout);
				llh.setOrientation(LinearLayout.HORIZONTAL);
				llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);

				final TextView tlView = new TextView(context);
				tlView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
				tlView.setText("   ");
				tlView.setLayoutParams(paramsTextView);
				llh.addView(tlView);

				final LinearLayout availableLayout = new LinearLayout(context);
				availableLayout.setOrientation(LinearLayout.VERTICAL);

				final LinearLayout availableBikes = new LinearLayout(context);
				availableBikes.setOrientation(LinearLayout.HORIZONTAL);
				availableBikes.setPadding(line1Padding, 0, 0, 0);

				final TextView availableBike = new TextView(context);
				availableBike.setText(mainActivity.getString(R.string.bike_available_bikes));
				availableBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
				availableBikes.addView(availableBike);

				final TextView amountBike = new TextView(context);
				if (bikeStation.getAvailableBikes() == null) {
					amountBike.setText("?");
					amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.orange));
				} else {
					final String availableBikesText = bikeStation.getAvailableBikes().toString();
					amountBike.setText(availableBikesText);
					if (bikeStation.getAvailableBikes() == 0) {
						amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.red));
					} else {
						amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.green));
					}
				}
				availableBikes.addView(amountBike);

				availableLayout.addView(availableBikes);

				final LinearLayout availableDocks = new LinearLayout(context);
				availableDocks.setOrientation(LinearLayout.HORIZONTAL);
				availableDocks.setPadding(line1Padding, 0, 0, 0);

				final TextView availableDock = new TextView(context);
				availableDock.setText(mainActivity.getString(R.string.bike_available_docks));
				availableDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
				availableDocks.addView(availableDock);

				final TextView amountDock = new TextView(context);
				if (bikeStation.getAvailableDocks() == null) {
					amountDock.setText("?");
					amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.orange));
				} else {
					final String availableDocksText = bikeStation.getAvailableDocks().toString();
					amountDock.setText(availableDocksText);
					if (bikeStation.getAvailableDocks() == 0) {
						amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.red));
					} else {
						amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.green));
					}
				}
				availableDocks.addView(amountDock);

				availableLayout.addView(availableDocks);

				llh.addView(availableLayout);

				favoritesData.addView(llh);

				final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
				final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

				final boolean isNetworkAvailable = Util.isNetworkAvailable();

				if (bikeStation.getLatitude() != null && bikeStation.getLongitude() != null) {

					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
							} else {
								final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
								final Bundle extras = new Bundle();
								extras.putParcelable("station", bikeStation);
								intent.putExtras(extras);
								mainActivity.startActivity(intent);
							}
						}
					});
				} else if (loadBike) {
					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(mainActivity, "Not ready yet. Please try again in few seconds!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(mainActivity, "You must activate divvy bikes data", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}
		return convertView;
	}

	/**
	 * DP view holder
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	static class TrainViewHolder {
		TextView stationNameView;
		TextView updatedView;
	}

	/**
	 * Set favorites
	 *
	 * @param arrivals    the trains arrivals
	 * @param busArrivals the buses arrivals
	 */
	public final void setArrivalsAndBikeStations(final SparseArray<TrainArrival> arrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations) {
		favorites.setArrivalsAndBikeStations(arrivals, busArrivals, bikeStations);
	}

	public final void setBikeStations(final List<BikeStation> bikeStations) {
		favorites.setBikeStations(bikeStations);
	}

	/**
	 * Set favorites
	 */
	public final void setFavorites() {
		favorites.setFavorites();
	}

	/**
	 * Refresh date update
	 */
	public final void refreshUpdated() {
		ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
	}

	/**
	 * Refresh udpdated view
	 */
	public final void refreshUpdatedView() {
		final Date lastUpdate = ChicagoTracker.getLastUpdate();
		if (!String.valueOf(getLastUpdateInMinutes(lastUpdate)).equals(this.lastUpdate)) {
			this.lastUpdate = String.valueOf(getLastUpdateInMinutes(lastUpdate));
			this.notifyDataSetChanged();
		}
	}

	/**
	 * Bus bound task. Start bus activity
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	public static class BusBoundAsyncTask extends AsyncTask<String, Void, BusStop> {

		private String busRouteId;
		private String bound;
		private String boundTitle;
		private String stopId;
		private String busRouteName;
		private TrackerException trackerException;
		private MainActivity activity;

		public BusBoundAsyncTask(final MainActivity activity) {
			this.activity = activity;
		}

		@Override
		protected final BusStop doInBackground(final String... params) {
			BusStop res = null;
			try {
				busRouteId = params[0];
				bound = params[1];
				boundTitle = params[2];
				stopId = params[3];
				busRouteName = params[4];
				final List<BusStop> busStops = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, boundTitle);

				for (final BusStop bus : busStops) {
					if (String.valueOf(bus.getId()).equals(stopId)) {
						res = bus;
						break;
					}
				}
			} catch (final ConnectException | ParserException e) {
				this.trackerException = e;
			}
			Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_stop, 0);
			return res;
		}

		@Override
		protected final void onPostExecute(final BusStop busStop) {
			if (trackerException == null) {
				final Intent intent = new Intent(ChicagoTracker.getContext(), BusActivity.class);
				final Bundle extras = new Bundle();
				extras.putInt("busStopId", busStop.getId());
				extras.putString("busStopName", busStop.getName());
				extras.putString("busRouteId", busRouteId);
				extras.putString("busRouteName", busRouteName);
				extras.putString("bound", bound);
				extras.putString("boundTitle", boundTitle);
				extras.putDouble("latitude", busStop.getPosition().getLatitude());
				extras.putDouble("longitude", busStop.getPosition().getLongitude());

				intent.putExtras(extras);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivity(intent);
			} else {
				ChicagoTracker.displayError(activity, trackerException);
			}
		}
	}

	/**
	 * Get time difference between 2 dates
	 *
	 * @param d1 the date one
	 * @param d2 the date two
	 * @return a tab containing in 0 the hour and in 1 the minutes
	 */
	private long[] getTimeDifference(final Date d1, final Date d2) {
		final long[] result = new long[2];
		final Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		final long t1 = cal.getTimeInMillis();
		cal.setTime(d2);
		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		diff %= ONE_DAY;
		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;
		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;
		result[0] = h;
		result[1] = m;
		return result;
	}

	/**
	 * Get last update in minutes
	 *
	 * @param lastUpdate the last update
	 * @return a string
	 */
	private String getLastUpdateInMinutes(final Date lastUpdate) {
		String res;
		if (lastUpdate != null) {
			final Date currentCDate = Calendar.getInstance().getTime();
			final long[] diff = getTimeDifference(lastUpdate, currentCDate);
			if (diff[0] == 0 && diff[1] == 0) {
				res = "now";
			} else {
				if (diff[0] == 0) {
					res = String.valueOf(diff[1]) + " min";
				} else {
					res = String.valueOf(diff[0]) + " h " + String.valueOf(diff[1]) + " min";
				}
			}
		} else {
			res = "";
		}
		return res;
	}
}