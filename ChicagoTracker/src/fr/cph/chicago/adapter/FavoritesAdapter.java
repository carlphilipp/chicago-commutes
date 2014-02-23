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

package fr.cph.chicago.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.VehiculeArrival;
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
import fr.cph.chicago.util.Util;

/**
 * Adapter that will handle favorites
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class FavoritesAdapter extends BaseAdapter {

	/** Main activity **/
	private MainActivity activity;
	/** The context **/
	private Context context;
	/** The layout that is used to display a fade black background **/
	private FrameLayout firstLayout;
	/** The model **/
	private VehiculeArrival arrival;
	/** Ids of layouts **/
	private Map<String, Integer> ids;
	/** Layouts **/
	private Map<Integer, LinearLayout> layouts;
	/** Views **/
	private Map<Integer, View> views;
	/** Map of textview that holds updates **/
	private Map<String, TextView> mUpdated;
	/** List update **/
	private String lastUpdate;
	/** Params layout **/
	private LinearLayout.LayoutParams paramsLayout;
	/** Params text view **/
	private LinearLayout.LayoutParams paramsTextView;
	/** Padding color **/
	private int line1PaddingColor;
	/** Stops padding top **/
	private int stopsPaddingTop;

	@SuppressLint("UseSparseArrays")
	public FavoritesAdapter(final MainActivity activity) {
		this.context = ChicagoTracker.getAppContext();

		this.activity = activity;
		this.firstLayout = ChicagoTracker.container;
		this.arrival = new VehiculeArrival();

		this.ids = new HashMap<String, Integer>();
		this.layouts = new HashMap<Integer, LinearLayout>();
		this.views = new HashMap<Integer, View>();
		this.mUpdated = new HashMap<String, TextView>();

		this.paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		this.line1PaddingColor = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
		this.stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
	}

	@Override
	public final int getCount() {
		return arrival.size();
	}

	@Override
	public final Object getItem(final int position) {
		return arrival.getObject(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		Date lastUpdate = ChicagoTracker.getLastUpdate();
		Object object = arrival.getObject(position);
		if (object != null) {
			if (object instanceof Station) {
				Station station = (Station) object;
				final Integer stationId = station.getId();
				final LinearLayout favoritesLayout;

				TextView updatedView = null;

				if (layouts.containsKey(stationId)) {
					favoritesLayout = layouts.get(stationId);
					convertView = views.get(stationId);

					TrainViewHolder viewHolder = (TrainViewHolder) convertView.getTag();
					updatedView = viewHolder.updatedView;
					updatedView.setText(this.lastUpdate);

				} else {
					LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.list_favorites_train, null);
					favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);
					layouts.put(stationId, favoritesLayout);
					views.put(stationId, convertView);

					TrainViewHolder holder = new TrainViewHolder();

					TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
					stationNameView.setText(station.getName());
					holder.stationNameView = stationNameView;

					updatedView = (TextView) convertView.findViewById(R.id.station_updated);
					mUpdated.put(station.getId().toString(), updatedView);
					if (lastUpdate != null) {
						updatedView.setText(this.lastUpdate);
					}
					holder.updatedView = updatedView;

					convertView.setTag(holder);
				}

				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
						Bundle extras = new Bundle();
						extras.putInt("stationId", stationId);
						intent.putExtras(extras);
						activity.startActivity(intent);
						activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
					}
				});

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
					if (arrival.getTrainArrival(stationId) != null) {
						List<Eta> etas = arrival.getTrainArrival(stationId).getEtas(tl);
						if (etas.size() != 0) {
							String key = station.getName() + "_" + tl.toString() + "_h";
							String key2 = station.getName() + "_" + tl.toString() + "_v";
							Integer idLayout = ids.get(key);
							Integer idLayout2 = ids.get(key2);

							LinearLayout llh, llv;
							if (idLayout == null) {
								llh = new LinearLayout(context);
								// llh.setBackgroundResource(R.drawable.border);
								llh.setLayoutParams(paramsLayout);
								llh.setOrientation(LinearLayout.HORIZONTAL);
								llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);
								int id = Util.generateViewId();
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
								llv.setPadding(line1PaddingColor, 0, 0, 0);
								int id2 = Util.generateViewId();
								llv.setId(id2);
								ids.put(key2, id2);

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
								Integer idLayout3 = ids.get(key3);
								if (idLayout3 == null) {
									LinearLayout insideLayout = new LinearLayout(context);
									insideLayout.setOrientation(LinearLayout.HORIZONTAL);
									insideLayout.setLayoutParams(paramsArrival);
									int newId = Util.generateViewId();
									insideLayout.setId(newId);
									ids.put(key3, newId);

									TextView stopName = new TextView(context);
									stopName.setText(eta.getDestName() + ": ");
									stopName.setTextColor(context.getResources().getColor(R.color.grey_5));
									insideLayout.addView(stopName);

									TextView timing = new TextView(context);
									timing.setText(eta.getTimeLeftDueDelay() + " ");
									timing.setTextColor(context.getResources().getColor(R.color.grey));
									timing.setLines(1);
									timing.setEllipsize(TruncateAt.END);
									insideLayout.addView(timing);

									llv.addView(insideLayout);
								} else {
									// llv can be null sometimes (after a remove from favorites for
									// example)
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
								for (Entry<String, Integer> e : this.ids.entrySet()) {
									if (e.getValue().intValue() == layoutChildH.getId()) {
										toRemove.add(e.getKey());
									}
								}
								for (String d : toRemove) {
									this.ids.remove(d);
								}
							}
						}
					}
				}
			} else {
				final BusRoute busRoute = (BusRoute) object;
				LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_bus, null);
				TextView routeIdView = (TextView) convertView.findViewById(R.id.route_id);
				routeIdView.setText(busRoute.getId());

				TextView routeNameView = (TextView) convertView.findViewById(R.id.route_name_value);
				routeNameView.setText(" " + busRoute.getName());

				final LinearLayout favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);

				TextView updated = (TextView) convertView.findViewById(R.id.station_updated);
				if (!mUpdated.containsKey(busRoute.getId())) {
					mUpdated.put(busRoute.getId(), updated);
				}
				if (lastUpdate != null) {
					updated.setText(this.lastUpdate);
				}

				Map<String, Map<String, List<BusArrival>>> busArrivals = arrival.getBusArrivalsMapped(busRoute.getId());
				if (busArrivals.size() > 0) {
					// arrow.setImageDrawable(activity.getResources().getDrawable(R.drawable.down_arrow));
					for (Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
						LinearLayout llh = new LinearLayout(context);
						llh.setLayoutParams(paramsLayout);
						llh.setOrientation(LinearLayout.HORIZONTAL);
						llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							llh.setBackground(context.getResources().getDrawable(R.drawable.any_selector));
						}

						TextView tlView = new TextView(context);
						tlView.setBackgroundColor(context.getResources().getColor(R.color.black));
						tlView.setText("   ");
						tlView.setLayoutParams(paramsTextView);
						llh.addView(tlView);

						final String key = entry.getKey();
						final Map<String, List<BusArrival>> value = entry.getValue();

						llh.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (value.entrySet().size() == 1) {
									BusArrival busArrival = value.entrySet().iterator().next().getValue().get(0);
									activity.startRefreshAnimation();
									new BusBoundAsyncTask().execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
											String.valueOf(busArrival.getStopId()), key);
								} else {
									List<String> menuTitles = new ArrayList<String>();
									for (Entry<String, List<BusArrival>> entry : value.entrySet()) {
										menuTitles.add(entry.getKey());
									}
									PopupMenu popupMenu = new PopupMenu(context, v);
									for (int i = 0; i < menuTitles.size(); i++) {
										popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuTitles.get(i));
									}
									popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
										@Override
										public boolean onMenuItemClick(MenuItem item) {
											Iterator<Entry<String, List<BusArrival>>> iterator = value.entrySet().iterator();
											if (item.getItemId() == 1) {
												iterator.next();
											} else if (item.getItemId() == 2) {
												iterator.next();
												iterator.next();
											} else if (item.getItemId() == 3) {
												iterator.next();
												iterator.next();
												iterator.next();
											}
											BusArrival busArrival = iterator.next().getValue().get(0);
											new BusBoundAsyncTask().execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
													String.valueOf(busArrival.getStopId()), key);
											activity.startRefreshAnimation();
											return false;
										}
									});
									popupMenu.setOnDismissListener(new OnDismissListener() {
										@Override
										public void onDismiss(PopupMenu menu) {
											firstLayout.getForeground().setAlpha(0);
										}
									});
									firstLayout.getForeground().setAlpha(210);
									popupMenu.show();
								}
							}
						});

						LinearLayout stopLayout = new LinearLayout(context);
						stopLayout.setOrientation(LinearLayout.VERTICAL);
						stopLayout.setPadding(line1PaddingColor, 0, 0, 0);

						TextView stopName = new TextView(context);
						stopName.setText(String.valueOf(key));
						stopName.setTextColor(context.getResources().getColor(R.color.grey_5));
						stopName.setTypeface(Typeface.DEFAULT_BOLD);

						stopLayout.addView(stopName);

						for (Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
							String key2 = entry2.getKey();
							List<BusArrival> buses = entry2.getValue();

							LinearLayout boundLayout = new LinearLayout(context);
							boundLayout.setOrientation(LinearLayout.HORIZONTAL);

							TextView bound = new TextView(context);
							bound.setText(key2 + ": ");
							bound.setTextColor(context.getResources().getColor(R.color.grey_5));
							boundLayout.addView(bound);

							for (BusArrival arri : buses) {
								TextView timeView = new TextView(context);
								timeView.setText(arri.getTimeLeftDueDelay() + " ");
								timeView.setTextColor(context.getResources().getColor(R.color.grey));
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
	 * St arrivals
	 * 
	 * @param arrivals
	 *            the trains arrivals
	 * @param busArrivals
	 *            the buses arrivals
	 */
	public final void setArrivals(final SparseArray<TrainArrival> arrivals, final List<BusArrival> busArrivals) {
		arrival.setArrivals(arrivals, busArrivals);
	}

	/**
	 * Set favorites
	 */
	public final void setFavorites() {
		arrival.setFavorites();
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
	private class BusBoundAsyncTask extends AsyncTask<String, Void, BusStop> {

		private String busRouteId;
		private String bound;
		private String stopId;
		private String busRouteName;
		private TrackerException trackerException;

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
