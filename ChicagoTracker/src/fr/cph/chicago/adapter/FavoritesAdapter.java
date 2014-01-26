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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.VehiculeArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

public final class FavoritesAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "FavoritesAdapter";

	private Activity activity;
	private Context context;

	// private SparseArray<TrainArrival> trainArrivals;
	// private List<Integer> trainFavorites;
	//
	// private TrainData trainData;

	private VehiculeArrival arrival;

	private Map<String, Integer> ids;
	private Map<Integer, LinearLayout> layouts;
	private Map<Integer, View> views;
	private List<TextView> lupdated;

	@SuppressLint("UseSparseArrays")
	public FavoritesAdapter(Activity activity) {
		this.context = ChicagoTracker.getAppContext();

		this.activity = activity;
		this.arrival = new VehiculeArrival();
		// this.trainArrivals = new SparseArray<TrainArrival>();
		// this.trainFavorites = new ArrayList<Integer>();
		// this.trainData = DataHolder.getInstance().getTrainData();

		this.ids = new HashMap<String, Integer>();
		this.layouts = new HashMap<Integer, LinearLayout>();
		this.views = new HashMap<Integer, View>();
		this.lupdated = new ArrayList<TextView>();
	}

	@Override
	public final int getCount() {
		return arrival.size();
	}

	@Override
	public final Object getItem(int position) {
		return arrival.getObject(position);
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, ViewGroup parent) {
		
		Date lastUpdate = ChicagoTracker.getLastTrainUpdate();
		
		Object object = arrival.getObject(position);
		if (object instanceof Station) {
			Station station = (Station) object;
			final Integer stationId = station.getId();
			final LinearLayout favoritesLayout;

			if (layouts.containsKey(stationId)) {
				favoritesLayout = layouts.get(stationId);
				convertView = views.get(stationId);
			} else {
				LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_favorites_train, null);
				favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);
				layouts.put(stationId, favoritesLayout);
				views.put(stationId, convertView);
			}

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
					Bundle extras = new Bundle();
					extras.putInt("stationId", stationId);
					intent.putExtras(extras);
					activity.startActivity(intent);
				}
			});

			TextView textView = (TextView) convertView.findViewById(R.id.station_name_value);
			textView.setText(station.getName());
			textView.setTypeface(Typeface.DEFAULT_BOLD);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			textView.setTextColor(activity.getResources().getColor(R.color.black));

			final ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);

			TextView updated = (TextView) convertView.findViewById(R.id.station_updated);
			lupdated.add(updated);
			if (lastUpdate != null) {
				updated.setText(String.valueOf(getLastUpdateInMinutes(lastUpdate)));
			}

			LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
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

			int line1PaddingColor = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
			int stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
			for (TrainLine tl : setTL) {
				if (arrival.getTrainArrival(stationId) != null) {
					arrow.setImageDrawable(activity.getResources().getDrawable(R.drawable.down_arrow));
					// TODO chedk if mod ok
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
							String key3 = (station.getName() + "_" + tl.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName());
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
								stopName.setTextColor(context.getResources().getColor(R.color.grey));
								// stopName.setPadding(line3Padding, 0, 0, 0);
								insideLayout.addView(stopName);

								TextView timing = new TextView(context);
								timing.setText(eta.getTimeLeftDueDelay() + " ");
								timing.setTextColor(context.getResources().getColor(R.color.grey));
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
			BusRoute busRoute = (BusRoute) object;
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_favorites_bus, null);
			TextView textView = (TextView) convertView.findViewById(R.id.route_name_value);
			textView.setText(busRoute.getId() + " " + busRoute.getName());
			textView.setTypeface(Typeface.DEFAULT_BOLD);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			textView.setTextColor(activity.getResources().getColor(R.color.black));

			final ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);

			final LinearLayout favoritesLayout = (LinearLayout) convertView.findViewById(R.id.favorites_list);
			
			TextView updated = (TextView) convertView.findViewById(R.id.station_updated);
			lupdated.add(updated);
			if (lastUpdate != null) {
				updated.setText(String.valueOf(getLastUpdateInMinutes(lastUpdate)));
			}

			Map<String, Map<String, List<BusArrival>>> busArrivals = arrival.getBusArrivalsMapped(busRoute.getId());
			if (busArrivals.size() > 0) {
				arrow.setImageDrawable(activity.getResources().getDrawable(R.drawable.down_arrow));
				for(Entry<String, Map<String, List<BusArrival>>> entry: busArrivals.entrySet()){
					String key = entry.getKey();
					Map<String, List<BusArrival>> value = entry.getValue();
					
					LinearLayout stopLayout = new LinearLayout(context);
					stopLayout.setOrientation(LinearLayout.VERTICAL);
					
					TextView stopName = new TextView(context);
					stopName.setText(String.valueOf(key));
					stopName.setTextColor(context.getResources().getColor(R.color.grey));
					
					stopLayout.addView(stopName);
					
					for(Entry<String, List<BusArrival>> entry2: value.entrySet()){
						String key2 = entry2.getKey();
						List<BusArrival> buses = entry2.getValue();
						
						LinearLayout boundLayout = new LinearLayout(context);
						boundLayout.setOrientation(LinearLayout.HORIZONTAL);
						
						TextView bound = new TextView(context);
						bound.setText(key2 + ": ");
						bound.setTextColor(context.getResources().getColor(R.color.grey));
						
						boundLayout.addView(bound);
						
						for(BusArrival arri :  buses){
							TextView timeView = new TextView(context);
							timeView.setText(arri.getTimeLeftDueDelay()+ " ");
							timeView.setTextColor(context.getResources().getColor(R.color.grey));
							boundLayout.addView(timeView);
						}
						stopLayout.addView(boundLayout);
					}
					favoritesLayout.addView(stopLayout);
				}
			}
		}
		return convertView;
	}

	private String getLastUpdateInMinutes(Date lastUpdate) {
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
					res = String.valueOf(diff[0]) + " h" + String.valueOf(diff[1]) + " min";
				}

			}
		} else {
			res = "";
		}
		return res;
	}

	public static long[] getTimeDifference(Date d1, Date d2) {
		long[] result = new long[2];
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);

		long t1 = cal.getTimeInMillis();
		cal.setTime(d2);

		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		// final int ONE_SECOND = ONE_MINUTE / 60;

		// long d = diff / ONE_DAY;
		diff %= ONE_DAY;

		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;

		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;

		// long s = diff / ONE_SECOND;
		// long ms = diff % ONE_SECOND;
		// result[0] = d;
		// result[1] = h;
		// result[2] = m;
		// result[3] = s;
		// result[4] = ms;
		result[0] = h;
		result[1] = m;

		return result;
	}

	public void setArrivals(SparseArray<TrainArrival> arrivals, List<BusArrival> busArrivals) {
		arrival.setArrivals(arrivals, busArrivals);
	}

	public void setFavorites() {
		arrival.setFavorites();
	}

	public void refreshUpdated() {
		ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
	}

	public void refreshUpdatedView() {
		Date lastUpdate = ChicagoTracker.getLastTrainUpdate();
		for (TextView updated : lupdated) {
			updated.setText(String.valueOf(getLastUpdateInMinutes(lastUpdate)));
		}
	}
}
