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

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
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
import fr.cph.chicago.util.LayoutUtil;

/**
 * Adapter that will handle nearby
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// FIXME use one view holder for trains/buses and bikes. Tried already but did not work
public final class NearbyAdapter extends BaseAdapter {

    private static final int LINE_1_PADDING_COLOR = (int) App.getContext().getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
    private static final int STOPS_PADDING_TOP = (int) App.getContext().getResources().getDimension(R.dimen.activity_station_stops_padding_top);

    private GoogleMap googleMap;
    private final Context context;

    private final BusData busData;
    private SparseArray<Map<String, List<BusArrival>>> busArrivals;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BusStop> busStops;
    private List<Station> stations;
    private List<Marker> markers;
    private List<BikeStation> bikeStations;

    public NearbyAdapter() {
        this.context = App.getContext();
        this.busStops = new ArrayList<>();
        this.busArrivals = new SparseArray<>();
        this.stations = new ArrayList<>();
        this.bikeStations = new ArrayList<>();
        this.trainArrivals = new SparseArray<>();
        this.busData = DataHolder.getInstance().getBusData();
    }

    @Override
    public final int getCount() {
        return busStops.size() + stations.size() + bikeStations.size();
    }

    @Override
    public final Object getItem(final int position) {
        final Object object;
        if (position < stations.size()) {
            object = stations.get(position);
        } else if (position < stations.size() + busStops.size()) {
            int index = position - stations.size();
            object = busStops.get(index);
        } else {
            int index = position - (stations.size() + busStops.size());
            object = bikeStations.get(index);
        }
        return object;
    }

    @Override
    public final long getItemId(final int position) {
        final int id;
        if (position < stations.size()) {
            id = stations.get(position).getId();
        } else if (position < stations.size() + busStops.size()) {
            int index = position - stations.size();
            id = busStops.get(index).getId();
        } else {
            int index = position - (stations.size() + busStops.size());
            id = bikeStations.get(index).getId();
        }
        return id;
    }

    @Override
    public final View getView(final int position, View convertView, final ViewGroup parent) {
        final LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        if (position < stations.size()) {
            return handleTrains(position, convertView, parent, paramsLayout);
        } else if (position < stations.size() + busStops.size()) {
            return handleBuses(position, parent, paramsLayout, paramsTextView);
        } else {
            return handleBikes(position, parent, paramsLayout, paramsTextView);
        }
    }

    private View handleTrains(final int position, View convertView, final ViewGroup parent, LinearLayout.LayoutParams paramsLayout) {
        // Train
        final Station station = stations.get(position);
        final LinearLayout.LayoutParams paramsArrival = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final Set<TrainLine> trainLines = station.getLines();

        TrainViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            final LayoutInflater vi = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_nearby, parent, false);

            viewHolder = new TrainViewHolder();
            viewHolder.resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);
            viewHolder.stationNameView = (TextView) convertView.findViewById(R.id.station_name);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.details = new HashMap<>();
            viewHolder.arrivalTime = new HashMap<>();

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TrainViewHolder) convertView.getTag();
        }

        viewHolder.stationNameView.setText(station.getName());
        viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(App.getContext(), R.drawable.ic_train_white_24dp));

        for (final TrainLine trainLine : trainLines) {
            if (trainArrivals.indexOfKey(station.getId()) != -1) {
                final List<Eta> etas = trainArrivals.get(station.getId()).getEtas(trainLine);
                if (etas.size() != 0) {
                    final LinearLayout llv;
                    boolean cleanBeforeAdd = false;
                    if (viewHolder.details.containsKey(trainLine)) {
                        llv = viewHolder.details.get(trainLine);
                        cleanBeforeAdd = true;
                    } else {
                        final LinearLayout llh = new LinearLayout(context);
                        llh.setLayoutParams(paramsLayout);
                        llh.setOrientation(LinearLayout.HORIZONTAL);
                        llh.setPadding(LINE_1_PADDING_COLOR, STOPS_PADDING_TOP, 0, 0);

                        llv = new LinearLayout(context);
                        llv.setLayoutParams(paramsLayout);
                        llv.setOrientation(LinearLayout.VERTICAL);
                        llv.setPadding(LINE_1_PADDING_COLOR, 0, 0, 0);

                        llh.addView(llv);
                        viewHolder.resultLayout.addView(llh);
                        viewHolder.details.put(trainLine, llv);
                    }

                    List<String> keysCleaned = new ArrayList<>();

                    for (final Eta eta : etas) {
                        final Stop stop = eta.getStop();
                        final String key = station.getName() + "_" + trainLine.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName();
                        if (viewHolder.arrivalTime.containsKey(key)) {
                            final LinearLayout insideLayout = viewHolder.arrivalTime.get(key);
                            final TextView timing = (TextView) insideLayout.getChildAt(2);
                            if (cleanBeforeAdd && !keysCleaned.contains(key)) {
                                timing.setText("");
                                keysCleaned.add(key);
                            }
                            final String timingText = timing.getText() + eta.getTimeLeftDueDelay() + " ";
                            timing.setText(timingText);
                        } else {
                            final LinearLayout insideLayout = new LinearLayout(context);
                            insideLayout.setOrientation(LinearLayout.HORIZONTAL);
                            insideLayout.setLayoutParams(paramsArrival);

                            final RelativeLayout coloredRound = LayoutUtil.createColoredRoundForFavorites(trainLine);
                            insideLayout.addView(coloredRound);

                            final TextView stopName = new TextView(context);
                            final String destName = eta.getDestName() + ": ";
                            stopName.setText(destName);
                            stopName.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey_5));
                            insideLayout.addView(stopName);

                            final TextView timing = new TextView(context);
                            final String timeLeftDueDelay = eta.getTimeLeftDueDelay() + " ";
                            timing.setText(timeLeftDueDelay);
                            timing.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
                            timing.setLines(1);
                            timing.setEllipsize(TruncateAt.END);
                            insideLayout.addView(timing);

                            llv.addView(insideLayout);
                            viewHolder.arrivalTime.put(key, insideLayout);
                        }
                    }
                }
            }
        }

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final LatLng latLng = new LatLng(station.getStopsPosition().get(0).getLatitude(), station.getStopsPosition().get(0).getLongitude());
                final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
                for (final Marker marker : markers) {
                    if (marker.getSnippet().equals(Integer.toString(station.getId()))) {
                        marker.showInfoWindow();
                        break;
                    }
                }
            }
        });
        return convertView;
    }

    private View handleBuses(final int position, @NonNull final ViewGroup parent, @NonNull final LinearLayout.LayoutParams paramsLayout, @NonNull final LinearLayout.LayoutParams paramsTextView) {
        final LayoutInflater vi = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = vi.inflate(R.layout.list_nearby, parent, false);

        // Bus
        final int index = position - stations.size();
        final BusStop busStop = busStops.get(index);

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        imageView.setImageDrawable(ContextCompat.getDrawable(App.getContext(), R.drawable.ic_directions_bus_white_24dp));

        final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
        routeView.setText(busStop.getName());

        final LinearLayout resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);

        final Map<String, List<BusArrival>> arrivalsForStop = busArrivals.get(busStop.getId(), new HashMap<String, List<BusArrival>>());

        for (final Entry<String, List<BusArrival>> entry : arrivalsForStop.entrySet()) {
            final LinearLayout llh = new LinearLayout(context);
            llh.setLayoutParams(paramsLayout);
            llh.setOrientation(LinearLayout.HORIZONTAL);
            llh.setPadding(LINE_1_PADDING_COLOR, STOPS_PADDING_TOP, 0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llh.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.any_selector));
            }

            final RelativeLayout coloredRound = LayoutUtil.createColoredRoundForFavorites(TrainLine.NA);
            llh.addView(coloredRound);

            final String direction = entry.getKey();
            final List<BusArrival> busArrivals = entry.getValue();

            final LinearLayout stopLayout = new LinearLayout(context);
            stopLayout.setOrientation(LinearLayout.VERTICAL);
            stopLayout.setPadding(LINE_1_PADDING_COLOR, 0, 0, 0);

            final LinearLayout boundLayout = new LinearLayout(context);
            boundLayout.setOrientation(LinearLayout.HORIZONTAL);

            final TextView bound = new TextView(context);
            final String routeId = busData.getRoute(busArrivals.get(0).getRouteId()).getId();
            final String routeIdText = routeId + " (" + direction + "): ";
            bound.setText(routeIdText);
            bound.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
            boundLayout.addView(bound);

            for (final BusArrival busArrival : busArrivals) {
                final TextView timeView = new TextView(context);
                final String timeLeftDueDelay = busArrival.getTimeLeftDueDelay() + " ";
                timeView.setText(timeLeftDueDelay);
                timeView.setTextColor(ContextCompat.getColor(context, R.color.grey));
                timeView.setLines(1);
                timeView.setEllipsize(TruncateAt.END);
                boundLayout.addView(timeView);
            }
            stopLayout.addView(boundLayout);
            llh.addView(stopLayout);
            resultLayout.addView(llh);
        }

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final LatLng latLng = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
                final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
                for (final Marker marker : markers) {
                    if (marker.getSnippet().equals(Integer.toString(busStop.getId()))) {
                        marker.showInfoWindow();
                        break;
                    }
                }
            }
        });
        return convertView;
    }

    private View handleBikes(final int position, @NonNull final ViewGroup parent, @NonNull final LinearLayout.LayoutParams paramsLayout, @NonNull final LinearLayout.LayoutParams paramsTextView) {
        final LayoutInflater vi = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = vi.inflate(R.layout.list_nearby, parent, false);

        final int index = position - (stations.size() + busStops.size());
        final BikeStation bikeStation = bikeStations.get(index);

        final LinearLayout favoritesData = (LinearLayout) convertView.findViewById(R.id.nearby_results);

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        imageView.setImageDrawable(ContextCompat.getDrawable(App.getContext(), R.drawable.ic_directions_bike_white_24dp));

        final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
        routeView.setText(bikeStation.getName());

        final LinearLayout llh = new LinearLayout(context);
        llh.setLayoutParams(paramsLayout);
        llh.setOrientation(LinearLayout.HORIZONTAL);
        llh.setPadding(LINE_1_PADDING_COLOR, STOPS_PADDING_TOP, 0, 0);

        final LinearLayout availableLayout = new LinearLayout(context);
        availableLayout.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout availableBikes = new LinearLayout(context);
        availableBikes.setOrientation(LinearLayout.HORIZONTAL);
        availableBikes.setPadding(LINE_1_PADDING_COLOR, 0, 0, 0);

        final RelativeLayout coloredRound1 = LayoutUtil.createColoredRoundForFavorites(TrainLine.NA);
        availableBikes.addView(coloredRound1);

        final TextView availableBike = new TextView(context);
        availableBike.setText(context.getString(R.string.bike_available_bikes));
        availableBike.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableBikes.addView(availableBike);

        final TextView amountBike = new TextView(context);
        final String amountBikeText = String.valueOf(bikeStation.getAvailableBikes());
        amountBike.setText(amountBikeText);
        if (bikeStation.getAvailableBikes() == 0) {
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        availableBikes.addView(amountBike);

        availableLayout.addView(availableBikes);

        final LinearLayout availableDocks = new LinearLayout(context);
        availableDocks.setOrientation(LinearLayout.HORIZONTAL);
        availableDocks.setPadding(LINE_1_PADDING_COLOR, 0, 0, 0);

        final RelativeLayout coloredRound2 = LayoutUtil.createColoredRoundForFavorites(TrainLine.NA);
        availableDocks.addView(coloredRound2);

        final TextView availableDock = new TextView(context);
        availableDock.setText(context.getString(R.string.bike_available_docks));
        availableDock.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableDocks.addView(availableDock);

        final TextView amountDock = new TextView(context);
        final String amountDockText = String.valueOf(bikeStation.getAvailableDocks());
        amountDock.setText(amountDockText);
        if (bikeStation.getAvailableDocks() == 0) {
            amountDock.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            amountDock.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        availableDocks.addView(amountDock);
        availableLayout.addView(availableDocks);

        llh.addView(availableLayout);

        favoritesData.addView(llh);

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final LatLng latLng = new LatLng(bikeStation.getLatitude(), bikeStation.getLongitude());
                final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
                for (final Marker marker : markers) {
                    if (marker.getSnippet().equals(Integer.toString(bikeStation.getId()))) {
                        marker.showInfoWindow();
                        break;
                    }
                }
            }
        });
        return convertView;
    }

    public final void updateData(@NonNull final List<BusStop> busStops,
                                 @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivals,
                                 @NonNull final List<Station> stations,
                                 @NonNull final SparseArray<TrainArrival> trainArrivals,
                                 @NonNull final List<BikeStation> bikeStations,
                                 @NonNull final GoogleMap map,
                                 @NonNull final List<Marker> markers) {
        this.busStops = busStops;
        this.busArrivals = busArrivals;
        this.stations = stations;
        this.trainArrivals = trainArrivals;
        this.bikeStations = bikeStations;
        this.googleMap = map;
        this.markers = markers;
    }

    static class TrainViewHolder {
        TextView stationNameView;
        ImageView imageView;
        LinearLayout resultLayout;
        Map<TrainLine, LinearLayout> details;
        Map<String, LinearLayout> arrivalTime;
    }
}
