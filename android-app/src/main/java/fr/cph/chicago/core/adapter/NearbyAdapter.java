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

package fr.cph.chicago.core.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.core.listener.NearbyOnClickListener;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Adapter that will handle nearby
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// FIXME use one view holder for trains/buses and bikes. Tried already but did not work
public final class NearbyAdapter extends BaseAdapter {

    private final Context context;

    private final int line1PaddingColor;
    private final int stopsPaddingTop;

    private GoogleMap googleMap;
    private SparseArray<Map<String, List<BusArrival>>> busArrivals;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BusStop> busStops;
    private List<Station> stations;
    private List<Marker> markers;
    private List<BikeStation> bikeStations;

    public NearbyAdapter(@NonNull final Context context) {
        this.context = context.getApplicationContext();
        this.line1PaddingColor = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
        this.stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
        this.busStops = new ArrayList<>();
        this.busArrivals = new SparseArray<>();
        this.stations = new ArrayList<>();
        this.bikeStations = new ArrayList<>();
        this.trainArrivals = new SparseArray<>();
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
        if (position < stations.size()) {
            return handleTrains(position, convertView, parent);
        } else if (position < stations.size() + busStops.size()) {
            return handleBuses(position, parent);
        } else {
            return handleBikes(position, parent);
        }
    }

    private View handleTrains(final int position, View convertView, @NonNull final ViewGroup parent) {
        // Train
        final Station station = stations.get(position);

        TrainViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            viewHolder.details = new HashMap<>();
            viewHolder.arrivalTime = new HashMap<>();
            viewHolder.resultLayout.removeAllViews();
        }

        viewHolder.stationNameView.setText(station.getName());
        viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_train_white_24dp));

        if (trainArrivals.indexOfKey(station.getId()) != -1) {
            for (final TrainLine trainLine : station.getLines()) {
                final List<Eta> etas = trainArrivals.get(station.getId()).getEtas(trainLine);
                if (!etas.isEmpty()) {
                    final LinearLayout mainLayout;
                    boolean cleanBeforeAdd = false;
                    if (viewHolder.details.containsKey(station.getName() + trainLine)) {
                        mainLayout = viewHolder.details.get(station.getName() + trainLine);
                        cleanBeforeAdd = true;
                    } else {
                        mainLayout = new LinearLayout(context);
                        mainLayout.setOrientation(LinearLayout.VERTICAL);
                        mainLayout.setPadding(line1PaddingColor, 0, 0, 0);

                        final LinearLayout linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

                        linearLayout.addView(mainLayout);
                        viewHolder.resultLayout.addView(linearLayout);
                        viewHolder.details.put(station.getName() + trainLine, mainLayout);
                    }

                    final List<String> keysCleaned = new ArrayList<>();

                    for (final Eta eta : etas) {
                        final Stop stop = eta.getStop();
                        final String key = station.getName() + "_" + trainLine.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName();
                        if (viewHolder.arrivalTime.containsKey(key)) {
                            final RelativeLayout insideLayout = viewHolder.arrivalTime.get(key);
                            final TextView timing = (TextView) insideLayout.getChildAt(2);
                            if (cleanBeforeAdd && !keysCleaned.contains(key)) {
                                timing.setText("");
                                keysCleaned.add(key);
                            }
                            final String timingText = timing.getText() + eta.getTimeLeftDueDelay() + " ";
                            timing.setText(timingText);
                        } else {
                            final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                            final RelativeLayout insideLayout = new RelativeLayout(context);
                            insideLayout.setLayoutParams(leftParam);

                            final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, trainLine);
                            int lineId = Util.generateViewId();
                            lineIndication.setId(lineId);

                            final RelativeLayout.LayoutParams availableParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                            availableParam.addRule(RelativeLayout.RIGHT_OF, lineId);
                            availableParam.setMargins(Util.convertDpToPixel(context, 10), 0, 0, 0);

                            final TextView stopName = new TextView(context);
                            final String destName = eta.getDestName() + ": ";
                            stopName.setText(destName);
                            stopName.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.grey_5));
                            stopName.setLayoutParams(availableParam);
                            int availableId = Util.generateViewId();
                            stopName.setId(availableId);

                            final RelativeLayout.LayoutParams availableValueParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                            availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId);
                            availableValueParam.setMargins(0, 0, 0, 0);

                            final TextView timing = new TextView(context);
                            final String timeLeftDueDelay = eta.getTimeLeftDueDelay() + " ";
                            timing.setText(timeLeftDueDelay);
                            timing.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.grey));
                            timing.setLines(1);
                            timing.setEllipsize(TruncateAt.END);
                            timing.setLayoutParams(availableValueParam);

                            insideLayout.addView(lineIndication);
                            insideLayout.addView(stopName);
                            insideLayout.addView(timing);

                            mainLayout.addView(insideLayout);
                            viewHolder.arrivalTime.put(key, insideLayout);
                        }

                    }
                }
            }
        }
        convertView.setOnClickListener(new NearbyOnClickListener(googleMap, markers, station.getId() + "_" + station.getName(), station.getStopsPosition().get(0).getLatitude(), station.getStopsPosition().get(0).getLongitude()));
        return convertView;
    }

    private View handleBuses(final int position, @NonNull final ViewGroup parent) {
        final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = vi.inflate(R.layout.list_nearby, parent, false);

        // Bus
        final int index = position - stations.size();
        final BusStop busStop = busStops.get(index);

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        imageView.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_directions_bus_white_24dp));

        final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
        routeView.setText(busStop.getName());

        final LinearLayout resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);

        final Map<String, List<BusArrival>> arrivalsForStop = busArrivals.get(busStop.getId(), new HashMap<>());

        Stream.of(arrivalsForStop.entrySet()).forEach(entry -> {
            final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            final RelativeLayout insideLayout = new RelativeLayout(context);
            insideLayout.setLayoutParams(leftParam);
            insideLayout.setPadding(line1PaddingColor * 2, stopsPaddingTop, 0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                insideLayout.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.any_selector));
            }

            final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA);
            int lineId = Util.generateViewId();
            lineIndication.setId(lineId);

            final RelativeLayout.LayoutParams stopParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            stopParam.addRule(RelativeLayout.RIGHT_OF, lineId);
            stopParam.setMargins(Util.convertDpToPixel(context, 10), 0, 0, 0);

            final LinearLayout stopLayout = new LinearLayout(context);
            stopLayout.setOrientation(LinearLayout.VERTICAL);
            stopLayout.setLayoutParams(stopParam);
            int stopId = Util.generateViewId();
            stopLayout.setId(stopId);

            final RelativeLayout.LayoutParams boundParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            boundParam.addRule(RelativeLayout.RIGHT_OF, stopId);

            final LinearLayout boundLayout = new LinearLayout(context);
            boundLayout.setOrientation(LinearLayout.HORIZONTAL);

            final String direction = entry.getKey();
            final List<BusArrival> busArrivals = entry.getValue();
            final String routeId = busArrivals.get(0).getRouteId();

            final TextView bound = new TextView(context);
            final String routeIdText = routeId + " (" + direction + "): ";
            bound.setText(routeIdText);
            bound.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
            boundLayout.addView(bound);

            Stream.of(busArrivals).forEach(busArrival -> {
                final TextView timeView = new TextView(context);
                final String timeLeftDueDelay = busArrival.getTimeLeftDueDelay() + " ";
                timeView.setText(timeLeftDueDelay);
                timeView.setTextColor(ContextCompat.getColor(context, R.color.grey));
                timeView.setLines(1);
                timeView.setEllipsize(TruncateAt.END);
                boundLayout.addView(timeView);
            });

            stopLayout.addView(boundLayout);

            insideLayout.addView(lineIndication);
            insideLayout.addView(stopLayout);
            resultLayout.addView(insideLayout);
        });

        convertView.setOnClickListener(new NearbyOnClickListener(googleMap, markers, busStop.getId() + "_" + busStop.getName(), busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude()));
        return convertView;
    }

    private View handleBikes(final int position, @NonNull final ViewGroup parent) {
        final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = vi.inflate(R.layout.list_nearby, parent, false);

        final int index = position - (stations.size() + busStops.size());
        final BikeStation bikeStation = bikeStations.get(index);

        final LinearLayout favoritesData = (LinearLayout) convertView.findViewById(R.id.nearby_results);

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        imageView.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_directions_bike_white_24dp));

        final TextView routeView = (TextView) convertView.findViewById(R.id.station_name);
        routeView.setText(bikeStation.getName());

        final LinearLayout llh = new LinearLayout(context);
        llh.setOrientation(LinearLayout.HORIZONTAL);
        llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

        final LinearLayout availableLayout = new LinearLayout(context);
        availableLayout.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        final RelativeLayout availableBikes = new RelativeLayout(context);
        availableBikes.setLayoutParams(leftParam);
        availableBikes.setPadding(line1PaddingColor, 0, 0, 0);

        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA);
        int lineId = Util.generateViewId();
        lineIndication.setId(lineId);

        final RelativeLayout.LayoutParams availableParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId);
        availableParam.setMargins(Util.convertDpToPixel(context, 10), 0, 0, 0);

        final TextView availableBike = new TextView(context);
        availableBike.setText(context.getString(R.string.bike_available_bikes));
        availableBike.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableBike.setLayoutParams(availableParam);
        int availableBikeId = Util.generateViewId();
        availableBike.setId(availableBikeId);

        final RelativeLayout.LayoutParams amountParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        amountParam.addRule(RelativeLayout.RIGHT_OF, availableBikeId);

        final TextView amountBike = new TextView(context);
        final String amountBikeText = String.valueOf(bikeStation.getAvailableBikes());
        amountBike.setText(amountBikeText);
        int color = bikeStation.getAvailableBikes() == 0 ? R.color.red : R.color.green;
        amountBike.setTextColor(ContextCompat.getColor(context, color));
        amountBike.setLayoutParams(amountParam);

        availableBikes.addView(lineIndication);
        availableBikes.addView(availableBike);
        availableBikes.addView(amountBike);

        final LinearLayout.LayoutParams leftParam2 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        final RelativeLayout availableDocks = new RelativeLayout(context);
        availableDocks.setLayoutParams(leftParam2);
        availableDocks.setPadding(line1PaddingColor, 0, 0, 0);

        final RelativeLayout lineIndication2 = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA);
        int lineId2 = Util.generateViewId();
        lineIndication2.setId(lineId2);

        final RelativeLayout.LayoutParams availableDockParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        availableDockParam.addRule(RelativeLayout.RIGHT_OF, lineId2);
        availableDockParam.setMargins(Util.convertDpToPixel(context, 10), 0, 0, 0);

        final TextView availableDock = new TextView(context);
        availableDock.setText(context.getString(R.string.bike_available_docks));
        availableDock.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableDock.setLayoutParams(availableDockParam);
        int availableDockBikeId = Util.generateViewId();
        availableDock.setId(availableDockBikeId);

        final RelativeLayout.LayoutParams amountParam2 = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        amountParam2.addRule(RelativeLayout.RIGHT_OF, availableDockBikeId);

        final TextView amountDock = new TextView(context);
        final String amountDockText = String.valueOf(bikeStation.getAvailableDocks());
        amountDock.setText(amountDockText);
        color = bikeStation.getAvailableDocks() == 0 ? R.color.red : R.color.green;
        amountDock.setTextColor(ContextCompat.getColor(context, color));

        amountDock.setLayoutParams(amountParam2);

        availableDocks.addView(lineIndication2);
        availableDocks.addView(availableDock);
        availableDocks.addView(amountDock);

        availableLayout.addView(availableBikes);
        availableLayout.addView(availableDocks);

        llh.addView(availableLayout);

        favoritesData.addView(llh);

        convertView.setOnClickListener(new NearbyOnClickListener(googleMap, markers, bikeStation.getId() + "_" + bikeStation.getName(), bikeStation.getLatitude(), bikeStation.getLongitude()));
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

    private static class TrainViewHolder {
        TextView stationNameView;
        ImageView imageView;
        LinearLayout resultLayout;
        Map<String, LinearLayout> details;
        Map<String, RelativeLayout> arrivalTime;
    }
}
