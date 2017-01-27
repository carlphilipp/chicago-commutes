package fr.cph.chicago.core.listener;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.core.adapter.NearbyAdapter;
import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class OnMarkerClickListener implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = OnMarkerClickListener.class.getSimpleName();

    private NearbyFragment nearbyFragment;
    private NearbyFragment.MarkerDataHolder markerDataHolder;

    public OnMarkerClickListener(final NearbyFragment.MarkerDataHolder markerDataHolder, final NearbyFragment nearbyFragment) {
        this.markerDataHolder = markerDataHolder;
        this.nearbyFragment = nearbyFragment;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i(TAG, "Marker selected: " + marker.getTag().toString());
        List<AStation> stations = markerDataHolder.getData(marker);
        nearbyFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        nearbyFragment.getLayoutContainer().removeAllViews();

        loadAllArrivals(stations);

        int line1PaddingColor = (int) nearbyFragment.getContext().getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
        int stopsPaddingTop = (int) nearbyFragment.getContext().getResources().getDimension(R.dimen.activity_station_stops_padding_top);

        //Log.i(TAG, "Object found: " + stations + " is a " + stations.getClass());
        //Log.i(TAG, "Size: " + stations.size());
        /*if (stations.size() != 0) {

            if (stations.get(0) instanceof Station) {
                final Station station = (Station) stations.get(0);
                final TextView textView = new TextView(nearbyFragment.getContext());
                textView.setText(station.getName());
                nearbyFragment.getLayoutContainer().addView(textView);

                new Thread(() -> {
                    final SparseArray<TrainArrival> trainArrivals = loadAroundTrainArrivals(station);
                    nearbyFragment.getActivity().runOnUiThread(() -> {

                        NearbyAdapter.TrainViewHolder viewHolder;
                        final LayoutInflater vi = (LayoutInflater) nearbyFragment.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View convertView = vi.inflate(R.layout.list_nearby, null, false);

                        viewHolder = new NearbyAdapter.TrainViewHolder();
                        viewHolder.resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_results);
                        viewHolder.stationNameView = (TextView) convertView.findViewById(R.id.station_name);
                        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.icon);
                        viewHolder.details = new HashMap<>();
                        viewHolder.arrivalTime = new HashMap<>();

                        convertView.setTag(viewHolder);

                        viewHolder.stationNameView.setText(station.getName());
                        viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(nearbyFragment.getContext(), R.drawable.ic_train_white_24dp));


                        for (final TrainLine trainLine : station.getLines()) {
                            final List<Eta> etas = trainArrivals.get(station.getId()).getEtas(trainLine);
                            if (!etas.isEmpty()) {
                                final LinearLayout mainLayout;
                                boolean cleanBeforeAdd = false;
                                if (viewHolder.details.containsKey(station.getName() + trainLine)) {
                                    mainLayout = viewHolder.details.get(station.getName() + trainLine);
                                    cleanBeforeAdd = true;
                                } else {
                                    mainLayout = new LinearLayout(nearbyFragment.getContext());
                                    mainLayout.setOrientation(LinearLayout.VERTICAL);
                                    mainLayout.setPadding(line1PaddingColor, 0, 0, 0);

                                    final LinearLayout linearLayout = new LinearLayout(nearbyFragment.getContext());
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
                                        final RelativeLayout insideLayout = new RelativeLayout(nearbyFragment.getContext());
                                        insideLayout.setLayoutParams(leftParam);

                                        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(nearbyFragment.getContext(), trainLine);
                                        int lineId = Util.generateViewId();
                                        lineIndication.setId(lineId);

                                        final RelativeLayout.LayoutParams availableParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId);
                                        availableParam.setMargins(Util.convertDpToPixel(nearbyFragment.getContext(), 10), 0, 0, 0);

                                        final TextView stopName = new TextView(nearbyFragment.getContext());
                                        final String destName = eta.getDestName() + ": ";
                                        stopName.setText(destName);
                                        stopName.setTextColor(ContextCompat.getColor(nearbyFragment.getContext(), R.color.grey_5));
                                        stopName.setLayoutParams(availableParam);
                                        int availableId = Util.generateViewId();
                                        stopName.setId(availableId);

                                        final RelativeLayout.LayoutParams availableValueParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                        availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId);
                                        availableValueParam.setMargins(0, 0, 0, 0);

                                        final TextView timing = new TextView(nearbyFragment.getContext());
                                        final String timeLeftDueDelay = eta.getTimeLeftDueDelay() + " ";
                                        timing.setText(timeLeftDueDelay);
                                        timing.setTextColor(ContextCompat.getColor(nearbyFragment.getContext(), R.color.grey));
                                        timing.setLines(1);
                                        timing.setEllipsize(TextUtils.TruncateAt.END);
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
                    });
                }).start();

            } else if (stations.get(0) instanceof BusStop) {
                final BusStop station = (BusStop) stations.get(0);
                final TextView textView = new TextView(nearbyFragment.getContext());
                textView.setText(station.getName());
                nearbyFragment.getLayoutContainer().addView(textView);

                // TODO refactor that code
                new Thread(() -> {
                    SparseArray<Map<String, List<BusArrival>>> busArrivalsMap2 = new SparseArray<>();
                    final Map<String, List<BusArrival>> busArrivalsMap = loadAroundBusArrivals(station, busArrivalsMap2);
                    nearbyFragment.getActivity().runOnUiThread(() -> {
                        if (busArrivalsMap.size() != 0) {
                            Stream.of(busArrivalsMap.entrySet()).forEach(entry -> {
                                final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                final RelativeLayout insideLayout = new RelativeLayout(nearbyFragment.getContext());
                                insideLayout.setLayoutParams(leftParam);
                                insideLayout.setPadding(line1PaddingColor * 2, stopsPaddingTop, 0, 0);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    insideLayout.setBackground(ContextCompat.getDrawable(nearbyFragment.getContext(), R.drawable.any_selector));
                                }

                                final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(nearbyFragment.getContext(), TrainLine.NA);
                                int lineId = Util.generateViewId();
                                lineIndication.setId(lineId);

                                final RelativeLayout.LayoutParams stopParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                stopParam.addRule(RelativeLayout.RIGHT_OF, lineId);
                                stopParam.setMargins(Util.convertDpToPixel(nearbyFragment.getContext(), 10), 0, 0, 0);

                                final LinearLayout stopLayout = new LinearLayout(nearbyFragment.getContext());
                                stopLayout.setOrientation(LinearLayout.VERTICAL);
                                stopLayout.setLayoutParams(stopParam);
                                int stopId = Util.generateViewId();
                                stopLayout.setId(stopId);

                                final RelativeLayout.LayoutParams boundParam = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                                boundParam.addRule(RelativeLayout.RIGHT_OF, stopId);

                                final LinearLayout boundLayout = new LinearLayout(nearbyFragment.getContext());
                                boundLayout.setOrientation(LinearLayout.HORIZONTAL);

                                final String direction = entry.getKey();
                                final List<BusArrival> busArrivals = entry.getValue();
                                final String routeId = busArrivals.get(0).getRouteId();

                                final TextView bound = new TextView(nearbyFragment.getContext());
                                final String routeIdText = routeId + " (" + direction + "): ";
                                bound.setText(routeIdText);
                                bound.setTextColor(ContextCompat.getColor(nearbyFragment.getContext(), R.color.grey_5));
                                boundLayout.addView(bound);

                                Stream.of(busArrivals).forEach(busArrival -> {
                                    final TextView timeView = new TextView(nearbyFragment.getContext());
                                    final String timeLeftDueDelay = busArrival.getTimeLeftDueDelay() + " ";
                                    timeView.setText(timeLeftDueDelay);
                                    timeView.setTextColor(ContextCompat.getColor(nearbyFragment.getContext(), R.color.grey));
                                    timeView.setLines(1);
                                    timeView.setEllipsize(TextUtils.TruncateAt.END);
                                    boundLayout.addView(timeView);
                                });

                                stopLayout.addView(boundLayout);

                                insideLayout.addView(lineIndication);
                                insideLayout.addView(stopLayout);
                                nearbyFragment.getLayoutContainer().addView(insideLayout);
                            });
                        } else {
                            final TextView noStopView = new TextView(nearbyFragment.getContext());
                            noStopView.setText("No result");
                            nearbyFragment.getLayoutContainer().addView(noStopView);
                        }
                    });
                }).start();
            } else if (stations.get(0) instanceof BikeStation) {
                final BikeStation station = (BikeStation) stations.get(0);
                final TextView textView = new TextView(nearbyFragment.getContext());
                textView.setText(station.getName());
                nearbyFragment.getLayoutContainer().addView(textView);
            }
        } else {
            final TextView noStopView = new TextView(nearbyFragment.getContext());
            noStopView.setText("No result");
            nearbyFragment.getLayoutContainer().addView(noStopView);
        }*/
        return false;
    }


    private void loadAllArrivals(@NonNull final List<AStation> stations) {
        // One train station can only be on one coordinate
        final Optional<Station> trainStation = Stream.of(stations).filter(station -> station instanceof Station).map(station -> (Station) station).findFirst();
        final List<BusStop> busStops = Stream.of(stations).filter(station -> station instanceof BusStop).map(station -> (BusStop) station).collect(Collectors.toList());
        // One bike station can only be on one coordinate
        final Optional<BikeStation> bikeStation = Stream.of(stations).filter(station -> station instanceof BikeStation).map(station -> (BikeStation) station).findFirst();
        ObservableUtil.createMarkerDataObservable(nearbyFragment.getContext(), trainStation, busStops, bikeStation)
            .subscribe(
                // TODO handle result => UI update
                result -> {
                    Log.i(TAG, "Done with " + result);
                    nearbyFragment.updateBottom(result);
                },
                onError -> Log.e(TAG, onError.getMessage(), onError)
            );
    }
}
