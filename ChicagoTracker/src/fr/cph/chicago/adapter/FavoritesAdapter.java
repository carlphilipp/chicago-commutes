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

package fr.cph.chicago.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
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

/**
 * Adapter that will handle favorites
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class FavoritesAdapter extends BaseAdapter {
	/** Main activity **/
	private MainActivity mActivity;
	/** The context **/
	private Context mContext;
	/** The layout that is used to display a fade black background **/
	private FrameLayout mFirstLayout;
	/** The model **/
	private Favorites mFavorites;
	/** Ids of layouts **/
	private Map<String, Integer> mIds;
	/** Layouts **/
	private Map<Integer, LinearLayout> mLayouts;
	/** Views **/
	private Map<Integer, View> mViews;
	/** Map of textview that holds updates **/
	private Map<String, TextView> mUpdated;
	/** List update **/
	private String mLastUpdate;
	/** Params layout **/
	private LinearLayout.LayoutParams mParamsLayout;
	/** Params text view **/
	private LinearLayout.LayoutParams mParamsTextView;
	/** Padding color **/
	private int mLine1Padding;
	/** Stops padding top **/
	private int mStopsPaddingTop;

	@SuppressLint("UseSparseArrays")
	public FavoritesAdapter(final MainActivity activity) {
		this.mContext = ChicagoTracker.getAppContext();

		this.mActivity = activity;
		this.mFirstLayout = ChicagoTracker.container;
		this.mFavorites = new Favorites();

		this.mIds = new HashMap<String, Integer>();
		this.mLayouts = new HashMap<Integer, LinearLayout>();
		this.mViews = new HashMap<Integer, View>();
		this.mUpdated = new HashMap<String, TextView>();

		this.mParamsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.mParamsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		this.mLine1Padding = (int) mContext.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
		this.mStopsPaddingTop = (int) mContext.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
	}

	@Override
	public final int getCount() {
		return mFavorites.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mFavorites.getObject(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		Date lastUpdate = ChicagoTracker.getLastUpdate();
		Object object = mFavorites.getObject(position);
		if (object != null) {
			/*********************************************************/
			/********************** TRAINS ***************************/
			/*********************************************************/
			if (object instanceof Station) {
				Station station = (Station) object;
				final Integer stationId = station.getId();
				final LinearLayout favoritesLayout;

				TextView updatedView = null;

				if (mLayouts.containsKey(stationId)) {
					favoritesLayout = mLayouts.get(stationId);
					convertView = mViews.get(stationId);

					TrainViewHolder viewHolder = (TrainViewHolder) convertView.getTag();
					updatedView = viewHolder.updatedView;
					updatedView.setText(this.mLastUpdate);

				} else {
					LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.list_favorites_train, null);
					favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);
					mLayouts.put(stationId, favoritesLayout);
					mViews.put(stationId, convertView);

					TrainViewHolder holder = new TrainViewHolder();

					TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
					stationNameView.setText(station.getName());
					holder.stationNameView = stationNameView;

					updatedView = (TextView) convertView.findViewById(R.id.station_updated);
					mUpdated.put(station.getId().toString(), updatedView);
					if (lastUpdate != null) {
						updatedView.setText(this.mLastUpdate);
					}
					holder.updatedView = updatedView;

					convertView.setTag(holder);
				}

				LinearLayout.LayoutParams paramsArrival = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

				Set<TrainLine> setTL = station.getLines();

				// Reset ETAs
				for (int i = 0; i < favoritesLayout.getChildCount(); i++) {
					LinearLayout layout = (LinearLayout) favoritesLayout.getChildAt(i);
					LinearLayout layoutChild = (LinearLayout) layout.getChildAt(1);
					for (int j = 0; j < layoutChild.getChildCount(); j++) {
						LinearLayout layoutChildV = (LinearLayout) layoutChild.getChildAt(j);
						TextView timing = (TextView) layoutChildV.getChildAt(1);
						// to delete ?
						if (timing != null) {
							timing.setText("");
						}
					}
				}

				for (TrainLine tl : setTL) {
					if (mFavorites.getTrainArrival(stationId) != null) {
						List<Eta> etas = mFavorites.getTrainArrival(stationId).getEtas(tl);
						if (etas.size() != 0) {
							String key = station.getName() + "_" + tl.toString() + "_h";
							String key2 = station.getName() + "_" + tl.toString() + "_v";
							Integer idLayout = mIds.get(key);
							Integer idLayout2 = mIds.get(key2);

							LinearLayout llh, llv;
							if (idLayout == null) {
								llh = new LinearLayout(mContext);
								llh.setLayoutParams(mParamsLayout);
								llh.setOrientation(LinearLayout.HORIZONTAL);
								llh.setPadding(mLine1Padding, mStopsPaddingTop, 0, 0);
								int id = Util.generateViewId();
								llh.setId(id);
								mIds.put(key, id);

								TextView tlView = new TextView(mContext);
								tlView.setBackgroundColor(tl.getColor());
								tlView.setText("   ");
								tlView.setLayoutParams(mParamsTextView);
								llh.addView(tlView);

								llv = new LinearLayout(mContext);
								llv.setLayoutParams(mParamsLayout);
								llv.setOrientation(LinearLayout.VERTICAL);
								llv.setPadding(mLine1Padding, 0, 0, 0);
								int id2 = Util.generateViewId();
								llv.setId(id2);
								mIds.put(key2, id2);

								llh.addView(llv);
								favoritesLayout.addView(llh);

							} else {
								llh = (LinearLayout) favoritesLayout.findViewById(idLayout);
								llv = (LinearLayout) favoritesLayout.findViewById(idLayout2);
							}
							for (Eta eta : etas) {
								Stop stop = eta.getStop();
								String key3 = (station.getName() + "_" + tl.toString() + "_" + stop.getDirection().toString() + "_" + eta
										.getDestName());
								Integer idLayout3 = mIds.get(key3);
								if (idLayout3 == null) {
									LinearLayout insideLayout = new LinearLayout(mContext);
									insideLayout.setOrientation(LinearLayout.HORIZONTAL);
									insideLayout.setLayoutParams(paramsArrival);
									int newId = Util.generateViewId();
									insideLayout.setId(newId);
									mIds.put(key3, newId);

									TextView stopName = new TextView(mContext);
									stopName.setText(eta.getDestName() + ": ");
									stopName.setTextColor(mContext.getResources().getColor(R.color.grey_5));
									insideLayout.addView(stopName);

									TextView timing = new TextView(mContext);
									timing.setText(eta.getTimeLeftDueDelay() + " ");
									timing.setTextColor(mContext.getResources().getColor(R.color.grey));
									timing.setLines(1);
									timing.setEllipsize(TruncateAt.END);
									insideLayout.addView(timing);

									llv.addView(insideLayout);
								} else {
									// llv can be null sometimes (after a remove from favorites for example)
									if (llv != null) {
										LinearLayout insideLayout = (LinearLayout) llv.findViewById(idLayout3);
										// InsideLayout can be null too if removed before
										TextView timing = (TextView) insideLayout.getChildAt(1);
										timing.setText(timing.getText() + eta.getTimeLeftDueDelay() + " ");
									}
								}
							}
						}
					}
				}

				convertView.setOnClickListener(new FavoritesTrainOnClickListener(mActivity, mFirstLayout, stationId, setTL));

				// Remove empty bloc
				for (int i = 0; i < favoritesLayout.getChildCount(); i++) {
					LinearLayout llh = (LinearLayout) favoritesLayout.getChildAt(i);
					LinearLayout layoutChild = (LinearLayout) llh.getChildAt(1);
					for (int j = 0; j < layoutChild.getChildCount(); j++) {
						LinearLayout layoutChildH = (LinearLayout) layoutChild.getChildAt(j);
						TextView timing = (TextView) layoutChildH.getChildAt(1);
						if (timing != null) {
							if (timing.getText().toString().equals("")) {
								layoutChildH.removeAllViews();
								List<String> toRemove = new ArrayList<String>();
								for (Entry<String, Integer> e : this.mIds.entrySet()) {
									if (e.getValue().intValue() == layoutChildH.getId()) {
										toRemove.add(e.getKey());
									}
								}
								for (String d : toRemove) {
									this.mIds.remove(d);
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
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_bus, null);
				TextView routeIdView = (TextView) convertView.findViewById(R.id.route_id);
				routeIdView.setText(busRoute.getId());

				TextView routeNameView = (TextView) convertView.findViewById(R.id.station_name);
				routeNameView.setText(" " + busRoute.getName());

				final LinearLayout favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);

				TextView updated = (TextView) convertView.findViewById(R.id.station_updated);
				if (!mUpdated.containsKey(busRoute.getId())) {
					mUpdated.put(busRoute.getId(), updated);
				}
				if (lastUpdate != null) {
					updated.setText(this.mLastUpdate);
				}

				Map<String, Map<String, List<BusArrival>>> busArrivals = mFavorites.getBusArrivalsMapped(busRoute.getId());
				if (busArrivals.size() > 0) {
					for (Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
						LinearLayout llh = new LinearLayout(mContext);
						llh.setLayoutParams(mParamsLayout);
						llh.setOrientation(LinearLayout.HORIZONTAL);
						llh.setPadding(mLine1Padding, mStopsPaddingTop, 0, 0);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							llh.setBackground(mContext.getResources().getDrawable(R.drawable.any_selector));
						}

						TextView tlView = new TextView(mContext);
						tlView.setBackgroundColor(mContext.getResources().getColor(R.color.black));
						tlView.setText("   ");
						tlView.setLayoutParams(mParamsTextView);
						llh.addView(tlView);

						final String key = entry.getKey();
						final Map<String, List<BusArrival>> value = entry.getValue();

						llh.setOnClickListener(new FavoritesBusOnClickListener(mActivity, mFirstLayout, busRoute, value));

						LinearLayout stopLayout = new LinearLayout(mContext);
						stopLayout.setOrientation(LinearLayout.VERTICAL);
						stopLayout.setPadding(mLine1Padding, 0, 0, 0);

						TextView stopName = new TextView(mContext);
						stopName.setText(String.valueOf(key));
						stopName.setTextColor(mContext.getResources().getColor(R.color.grey_5));
						stopName.setTypeface(Typeface.DEFAULT_BOLD);

						stopLayout.addView(stopName);

						for (Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
							String key2 = entry2.getKey();
							List<BusArrival> buses = entry2.getValue();

							LinearLayout boundLayout = new LinearLayout(mContext);
							boundLayout.setOrientation(LinearLayout.HORIZONTAL);

							TextView bound = new TextView(mContext);
							bound.setText(key2 + ": ");
							bound.setTextColor(mContext.getResources().getColor(R.color.grey_5));
							boundLayout.addView(bound);

							for (BusArrival arri : buses) {
								TextView timeView = new TextView(mContext);
								timeView.setText(arri.getTimeLeftDueDelay() + " ");
								timeView.setTextColor(mContext.getResources().getColor(R.color.grey));
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
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_bike, null);
				TextView bikeStationName = (TextView) convertView.findViewById(R.id.bike_station_name);
				bikeStationName.setText(bikeStation.getName());

				TextView updatedView = (TextView) convertView.findViewById(R.id.station_updated);
				if (lastUpdate != null) {
					updatedView.setText(this.mLastUpdate);
				}

				LinearLayout favoritesData = (LinearLayout) convertView.findViewById(R.id.favorites_bikes_list);

				LinearLayout llh = new LinearLayout(mContext);
				llh.setLayoutParams(mParamsLayout);
				llh.setOrientation(LinearLayout.HORIZONTAL);
				llh.setPadding(mLine1Padding, mStopsPaddingTop, 0, 0);

				TextView tlView = new TextView(mContext);
				tlView.setBackgroundColor(mContext.getResources().getColor(R.color.black));
				tlView.setText("   ");
				tlView.setLayoutParams(mParamsTextView);
				llh.addView(tlView);

				LinearLayout availableLayout = new LinearLayout(mContext);
				availableLayout.setOrientation(LinearLayout.VERTICAL);

				LinearLayout availableBikes = new LinearLayout(mContext);
				availableBikes.setOrientation(LinearLayout.HORIZONTAL);
				availableBikes.setPadding(mLine1Padding, 0, 0, 0);

				TextView availableBike = new TextView(mContext);
				availableBike.setText("Available bikes: ");
				availableBike.setTextColor(mContext.getResources().getColor(R.color.grey_5));
				availableBikes.addView(availableBike);

				TextView amountBike = new TextView(mContext);
				if (bikeStation.getAvailableBikes() == null) {
					amountBike.setText("?");
					amountBike.setTextColor(mContext.getResources().getColor(R.color.orange));
				} else {
					amountBike.setText("" + bikeStation.getAvailableBikes());
					if (bikeStation.getAvailableBikes() == 0) {
						amountBike.setTextColor(mContext.getResources().getColor(R.color.red));
					} else {
						amountBike.setTextColor(mContext.getResources().getColor(R.color.green));
					}
				}
				availableBikes.addView(amountBike);

				availableLayout.addView(availableBikes);

				LinearLayout availableDocks = new LinearLayout(mContext);
				availableDocks.setOrientation(LinearLayout.HORIZONTAL);
				availableDocks.setPadding(mLine1Padding, 0, 0, 0);

				TextView availableDock = new TextView(mContext);
				availableDock.setText("Available docks: ");
				availableDock.setTextColor(mContext.getResources().getColor(R.color.grey_5));
				availableDocks.addView(availableDock);

				TextView amountDock = new TextView(mContext);
				if (bikeStation.getAvailableDocks() == null) {
					amountDock.setText("?");
					amountDock.setTextColor(mContext.getResources().getColor(R.color.orange));
				} else {
					amountDock.setText("" + bikeStation.getAvailableDocks());
					if (bikeStation.getAvailableDocks() == 0) {
						amountDock.setTextColor(mContext.getResources().getColor(R.color.red));
					} else {
						amountDock.setTextColor(mContext.getResources().getColor(R.color.green));
					}
				}
				availableDocks.addView(amountDock);

				availableLayout.addView(availableDocks);

				llh.addView(availableLayout);

				favoritesData.addView(llh);

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
				boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

				final boolean isNetworkAvailable = Util.isNetworkAvailable();
				if (bikeStation.getPosition() != null) {

					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
							} else {
								Intent intent = new Intent(ChicagoTracker.getAppContext(), BikeStationActivity.class);
								Bundle extras = new Bundle();
								extras.putParcelable("station", bikeStation);
								intent.putExtras(extras);
								mActivity.startActivity(intent);
								mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
							}
						}
					});
				} else if (loadBike) {
					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(mActivity, "Not ready yet. Please try again in few seconds!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					convertView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!isNetworkAvailable) {
								Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(mActivity, "You must activate divvy bikes data", Toast.LENGTH_SHORT).show();
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
	 * @param arrivals
	 *            the trains arrivals
	 * @param busArrivals
	 *            the buses arrivals
	 */
	public final void setArrivalsAndBikeStations(final SparseArray<TrainArrival> arrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations) {
		mFavorites.setArrivalsAndBikeStations(arrivals, busArrivals, bikeStations);
	}

	public final void setBikeStations(List<BikeStation> bikeStations) {
		mFavorites.setBikeStations(bikeStations);
	}

	/**
	 * Set favorites
	 */
	public final void setFavorites() {
		mFavorites.setFavorites();
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
		Date lastUpdate = ChicagoTracker.getLastUpdate();
		if (!String.valueOf(getLastUpdateInMinutes(lastUpdate)).equals(this.mLastUpdate)) {
			this.mLastUpdate = String.valueOf(getLastUpdateInMinutes(lastUpdate));
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
				stopId = params[2];
				busRouteName = params[3];
				List<BusStop> busStops;
				busStops = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, bound);

				for (BusStop bus : busStops) {
					if (String.valueOf(bus.getId()).equals(stopId)) {
						res = bus;
						break;
					}
				}
			} catch (ConnectException e) {
				this.trackerException = e;
			} catch (ParserException e) {
				this.trackerException = e;
			}
			Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_stop, 0);
			return res;
		}

		@Override
		protected final void onPostExecute(final BusStop result) {
			if (trackerException == null) {
				BusStop busStop = result;

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
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
				activity.stopRefreshAnimation();
			} else {
				ChicagoTracker.displayError(activity, trackerException);
			}
		}
	}

	/**
	 * Get time difference between 2 dates
	 * 
	 * @param d1
	 *            the date one
	 * @param d2
	 *            the date two
	 * @return a tab containing in 0 the hour and in 1 the minutes
	 */
	private final long[] getTimeDifference(final Date d1, final Date d2) {
		long[] result = new long[2];
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		long t1 = cal.getTimeInMillis();
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
	 * @param lastUpdate
	 *            the last update
	 * @return a string
	 */
	private final String getLastUpdateInMinutes(final Date lastUpdate) {
		String res = null;
		if (lastUpdate != null) {
			Date currentCDate = Calendar.getInstance().getTime();
			long[] diff = getTimeDifference(lastUpdate, currentCDate);
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
