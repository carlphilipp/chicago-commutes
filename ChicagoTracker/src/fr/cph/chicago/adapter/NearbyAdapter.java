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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Adapter that will handle nearby
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class NearbyAdapter extends BaseAdapter {
	/**
	 * The context
	 **/
	private Context context;
	/**
	 * The bus data
	 **/
	private BusData busData;
	/**
	 * List of bus stop
	 **/
	private List<BusStop> busStops;
	/**
	 * Bus arrivals
	 **/
	private SparseArray<Map<String, List<BusArrival>>> busArrivals;
	/**
	 * Trian arrivals
	 **/
	private SparseArray<TrainArrival> trainArrivals;
	/**
	 * List of stations
	 **/
	private List<Station> stations;
	/**
	 * Google map
	 **/
	private GoogleMap googleMap;
	/**
	 * Markers on the map
	 **/
	private List<Marker> markers;
	/**
	 * Layout ids
	 **/
	private Map<String, Integer> ids;
	/**
	 * Layouts
	 **/
	private Map<Integer, LinearLayout> layouts;
	/**
	 * View
	 **/
	private Map<Integer, View> views;
	/**
	 * List of bike stations
	 **/
	private List<BikeStation> bikeStations;

	@SuppressLint("UseSparseArrays")
	public NearbyAdapter(final MainActivity activity) {
		this.context = ChicagoTracker.getAppContext();
		this.busStops = new ArrayList<>();
		this.busArrivals = new SparseArray<>();
		this.stations = new ArrayList<>();
		this.bikeStations = new ArrayList<>();
		this.trainArrivals = new SparseArray<>();
		this.busData = DataHolder.getInstance().getBusData();

		this.ids = new HashMap<>();
		this.layouts = new HashMap<>();
		this.views = new HashMap<>();
	}

	@Override
	public final int getCount() {
		return busStops.size() + stations.size() + bikeStations.size();
	}

	@Override
	public final Object getItem(int position) {
		Object res;
		if (position < stations.size()) {
			res = stations.get(position);
		} else if (position < stations.size() + busStops.size()) {
			int indice = position - stations.size();
			res = busStops.get(indice);
		} else {
			int indice = position - (stations.size() + busStops.size());
			res = bikeStations.get(indice);
		}
		return res;
	}

	@Override
	public final long getItemId(int position) {
		int id;
		if (position < stations.size()) {
			id = stations.get(position).getId();
		} else if (position < stations.size() + busStops.size()) {
			int indice = position - stations.size();
			id = busStops.get(indice).getId();
		} else {
			int indice = position - (stations.size() + busStops.size());
			id = bikeStations.get(indice).getId();
		}
		return id;
	}

	@SuppressLint("NewApi")
	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		final LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		final int line1PaddingColor = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
		final int stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);

		final LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_nearby, null);

		if (position < stations.size()) {
			final Station station = stations.get(position);

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (googleMap != null) {
						final LatLng latLng = new LatLng(station.getStopsPosition().get(0).getLatitude(), station.getStopsPosition().get(0).getLongitude());
						final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
						googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
						for (final Marker marker : markers) {
							if (marker.getSnippet().equals(station.getId().toString())) {
								marker.showInfoWindow();
								break;
							}
						}
					}
				}
			});

			final LinearLayout resultLayout;

			if (layouts.containsKey(station.getId())) {
				resultLayout = layouts.get(station.getId());
				convertView = views.get(station.getId());
			} else {
				resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);
				layouts.put(station.getId(), resultLayout);
				views.put(station.getId(), convertView);

				final TrainViewHolder holder = new TrainViewHolder();

				final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
				routeView.setText(station.getName());
				holder.stationNameView = routeView;

				final TextView typeView = (TextView) convertView.findViewById(R.id.train_bus_type);
				typeView.setText("T");
				holder.type = typeView;

				convertView.setTag(holder);
			}

			final LinearLayout.LayoutParams paramsArrival = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			final Set<TrainLine> setTL = station.getLines();

			// Reset ETAs
			for (int i = 0; i < resultLayout.getChildCount(); i++) {
				final LinearLayout layout = (LinearLayout) resultLayout.getChildAt(i);
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

			for (final TrainLine tl : setTL) {
				if (trainArrivals.get(station.getId()) != null) {
					final List<Eta> etas = trainArrivals.get(station.getId()).getEtas(tl);
					if (etas.size() != 0) {
						final String key = station.getName() + "_" + tl.toString() + "_h";
						final String key2 = station.getName() + "_" + tl.toString() + "_v";
						final Integer idLayout = ids.get(key);
						final Integer idLayout2 = ids.get(key2);

						final LinearLayout llh, llv;
						if (idLayout == null) {
							llh = new LinearLayout(context);
							// llh.setBackgroundResource(R.drawable.border);
							llh.setLayoutParams(paramsLayout);
							llh.setOrientation(LinearLayout.HORIZONTAL);
							llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);
							final int id = Util.generateViewId();
							llh.setId(id);
							ids.put(key, id);

							final TextView tlView = new TextView(context);
							tlView.setBackgroundColor(tl.getColor());
							tlView.setText("   ");
							tlView.setLayoutParams(paramsTextView);
							llh.addView(tlView);

							llv = new LinearLayout(context);
							llv.setLayoutParams(paramsLayout);
							llv.setOrientation(LinearLayout.VERTICAL);
							llv.setPadding(line1PaddingColor, 0, 0, 0);
							final int id2 = Util.generateViewId();
							llv.setId(id2);
							ids.put(key2, id2);

							llh.addView(llv);
							resultLayout.addView(llh);

						} else {
							llh = (LinearLayout) resultLayout.findViewById(idLayout);
							llv = (LinearLayout) resultLayout.findViewById(idLayout2);
						}
						for (final Eta eta : etas) {
							final Stop stop = eta.getStop();
							final String key3 = (station.getName() + "_" + tl.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName());
							final Integer idLayout3 = ids.get(key3);
							if (idLayout3 == null) {
								final LinearLayout insideLayout = new LinearLayout(context);
								insideLayout.setOrientation(LinearLayout.HORIZONTAL);
								insideLayout.setLayoutParams(paramsArrival);
								final int newId = Util.generateViewId();
								insideLayout.setId(newId);
								ids.put(key3, newId);

								final TextView stopName = new TextView(context);
								stopName.setText(eta.getDestName() + ": ");
								stopName.setTextColor(context.getResources().getColor(R.color.grey_5));
								insideLayout.addView(stopName);

								final TextView timing = new TextView(context);
								timing.setText(eta.getTimeLeftDueDelay() + " ");
								timing.setTextColor(context.getResources().getColor(R.color.grey));
								timing.setLines(1);
								timing.setEllipsize(TruncateAt.END);
								insideLayout.addView(timing);

								llv.addView(insideLayout);
							} else {
								// llv can be null sometimes (after a remove from favorites for example)
								if (llv != null) {
									final LinearLayout insideLayout = (LinearLayout) llv.findViewById(idLayout3);
									// InsideLayout can be null too if removed before
									final TextView timing = (TextView) insideLayout.getChildAt(1);
									timing.setText(timing.getText() + eta.getTimeLeftDueDelay() + " ");
								}
							}
						}
					}
				}
			}
		} else if (position < stations.size() + busStops.size()) {
			final int indice = position - stations.size();
			final BusStop busStop = busStops.get(indice);

			final TextView typeView = (TextView) convertView.findViewById(R.id.train_bus_type);
			typeView.setText("B");

			final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
			routeView.setText(busStop.getName());

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (googleMap != null) {
						final LatLng latLng = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
						final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
						googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
						for (final Marker marker : markers) {
							if (marker.getSnippet().equals(busStop.getId().toString())) {
								marker.showInfoWindow();
								break;
							}
						}
					}
				}
			});

			final LinearLayout resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);

			if (busArrivals.size() > 0) {
				for (final Entry<String, List<BusArrival>> entry : busArrivals.get(busStop.getId()).entrySet()) {
					final LinearLayout llh = new LinearLayout(context);
					llh.setLayoutParams(paramsLayout);
					llh.setOrientation(LinearLayout.HORIZONTAL);
					llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						llh.setBackground(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.any_selector));
					}

					final TextView tlView = new TextView(context);
					tlView.setBackgroundColor(context.getResources().getColor(R.color.black));
					tlView.setText("   ");
					tlView.setLayoutParams(paramsTextView);
					llh.addView(tlView);

					final String key2 = entry.getKey();
					final List<BusArrival> buses = entry.getValue();

					final LinearLayout stopLayout = new LinearLayout(context);
					stopLayout.setOrientation(LinearLayout.VERTICAL);
					stopLayout.setPadding(line1PaddingColor, 0, 0, 0);

					final LinearLayout boundLayout = new LinearLayout(context);
					boundLayout.setOrientation(LinearLayout.HORIZONTAL);

					final TextView bound = new TextView(context);
					final String routeId = busData.getRoute(buses.get(0).getRouteId()).getId();
					bound.setText(routeId + " (" + key2 + "): ");
					bound.setTextColor(context.getResources().getColor(R.color.grey_5));
					boundLayout.addView(bound);

					for (final BusArrival arri : buses) {
						final TextView timeView = new TextView(context);
						timeView.setText(arri.getTimeLeftDueDelay() + " ");
						timeView.setTextColor(context.getResources().getColor(R.color.grey));
						timeView.setLines(1);
						timeView.setEllipsize(TruncateAt.END);
						boundLayout.addView(timeView);
					}
					stopLayout.addView(boundLayout);
					llh.addView(stopLayout);
					resultLayout.addView(llh);
				}
			}
		} else {
			final int index = position - (stations.size() + busStops.size());
			final BikeStation bikeStation = bikeStations.get(index);

			final LinearLayout favoritesData = (LinearLayout) convertView.findViewById(R.id.nearby_results);

			final TextView typeView = (TextView) convertView.findViewById(R.id.train_bus_type);
			typeView.setText("D");

			final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
			routeView.setText(bikeStation.getName());

			final LinearLayout llh = new LinearLayout(context);
			llh.setLayoutParams(paramsLayout);
			llh.setOrientation(LinearLayout.HORIZONTAL);
			llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

			final TextView tlView = new TextView(context);
			tlView.setBackgroundColor(context.getResources().getColor(R.color.black));
			tlView.setText("   ");
			tlView.setLayoutParams(paramsTextView);
			llh.addView(tlView);

			final LinearLayout availableLayout = new LinearLayout(context);
			availableLayout.setOrientation(LinearLayout.VERTICAL);

			final LinearLayout availableBikes = new LinearLayout(context);
			availableBikes.setOrientation(LinearLayout.HORIZONTAL);
			availableBikes.setPadding(line1PaddingColor, 0, 0, 0);

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
			availableDocks.setPadding(line1PaddingColor, 0, 0, 0);

			final TextView availableDock = new TextView(context);
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

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (googleMap != null) {
						final LatLng latLng = new LatLng(bikeStation.getPosition().getLatitude(), bikeStation.getPosition().getLongitude());
						final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
						googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
						for (final Marker marker : markers) {
							if (marker.getSnippet().equals(bikeStation.getId() + "")) {
								marker.showInfoWindow();
								break;
							}
						}
					}
				}
			});
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
		TextView type;
	}

	/**
	 * Update data
	 *
	 * @param busStops      the bus stops
	 * @param busArrivals   the bus arrivals
	 * @param stations      the stations
	 * @param trainArrivals the train arrivals
	 * @param map           the map
	 * @param markers       the markers
	 */
	public final void updateData(final List<BusStop> busStops, final SparseArray<Map<String, List<BusArrival>>> busArrivals,
			final List<Station> stations, final SparseArray<TrainArrival> trainArrivals, final List<BikeStation> bikeStations, final GoogleMap map,
			final List<Marker> markers) {
		this.busStops = busStops;
		this.busArrivals = busArrivals;
		this.stations = stations;
		this.trainArrivals = trainArrivals;
		this.bikeStations = bikeStations;
		this.googleMap = map;
		this.markers = markers;
	}
}
