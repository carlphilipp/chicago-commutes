package fr.cph.chicago.core.listener;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;

import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;
import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

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
        final List<Station> trainStations = Stream.of(stations).filter(station -> station instanceof Station).map(station -> (Station) station).collect(Collectors.toList());
        final List<BusStop> busStops = Stream.of(stations).filter(station -> station instanceof BusStop).map(station -> (BusStop) station).collect(Collectors.toList());
        final List<BikeStation> bikeStations = Stream.of(stations).filter(station -> station instanceof BikeStation).map(station -> (BikeStation) station).collect(Collectors.toList());

        Log.i(TAG, "trainStations: " + trainStations.size());
        Log.i(TAG, "busStops: " + busStops.size());
        Log.i(TAG, "bikeStations: " + bikeStations.size());

        // Train handling
/*        final SparseArray<TrainArrival> resultTrainStation = new SparseArray<>();
        final Observable<Object> trainObservable = Observable.fromIterable(trainStations)
            .map(station -> {
                //loadAroundTrainArrival(station, resultTrainStation);
                return new Object();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        // Bus handling
        final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap = new SparseArray<>();
        final Observable<SparseArray<Map<String, List<BusArrival>>>> busObservable = Observable.fromIterable(busStops)
            .flatMap(busStop -> Observable.just(busStop).subscribeOn(Schedulers.computation())
                .map(currentBusStop -> {
                    loadAroundBusArrivals(currentBusStop, busArrivalsMap);
                    return busArrivalsMap;
                })
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());*/

        final Observable<Optional<TrainArrival>> trainArrivalObservable = ObservableUtil.createTrainArrivalsObservable(nearbyFragment.getContext(), trainStations);
        trainArrivalObservable.subscribe(
            result -> Log.e(TAG, "done with " + result),
            onError -> Log.e(TAG, onError.getMessage(), onError)
        );

/*        final Observable<NearbyDTO> zipped = ObservableUtil.createMarkerDataObservable(
            nearbyFragment.getRequestStopId(),
            busStops,
            nearbyFragment.getContext(),
            Stream.of(bikeStations).map(BikeStation::getId).collect(Collectors.toList())
        );
        zipped.subscribe(
            result -> Log.e(TAG, "done with " + result),
            onError -> {
                Log.e(TAG, onError.getMessage(), onError);
            }
        );*/
    }

    private Map<String, List<BusArrival>> loadAroundBusArrivals(@NonNull final BusStop busStop, @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap) {
        final Map<String, List<BusArrival>> result = new HashMap<>();
        try {
            if (nearbyFragment.isAdded()) {
                int busStopId = busStop.getId();
                // Create
                final Map<String, List<BusArrival>> tempMap = busArrivalsMap.get(busStopId, new ConcurrentHashMap<>());
                if (!tempMap.containsKey(Integer.toString(busStopId))) {
                    busArrivalsMap.put(busStopId, tempMap);
                }

                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(nearbyFragment.getRequestStopId(), Integer.toString(busStopId));
                final InputStream is = CtaConnect.INSTANCE.connect(BUS_ARRIVALS, reqParams, nearbyFragment.getContext());
                final List<BusArrival> busArrivals = XmlParser.INSTANCE.parseBusArrivals(is);
                for (final BusArrival busArrival : busArrivals) {
                    final String direction = busArrival.getRouteDirection();
                    if (tempMap.containsKey(direction)) {
                        final List<BusArrival> temp = tempMap.get(direction);
                        temp.add(busArrival);
                    } else {
                        final List<BusArrival> temp = new ArrayList<>();
                        temp.add(busArrival);
                        tempMap.put(direction, temp);
                    }
                }
                trackWithGoogleAnalytics(nearbyFragment.getActivity(), R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
            }
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
        return result;
    }

    private SparseArray<TrainArrival> loadAroundTrainArrivals(@NonNull final Station station) {
        final SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        if (nearbyFragment.isAdded()) {
            try {
                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(nearbyFragment.getRequestStopId(), Integer.toString(station.getId()));
                final InputStream xmlRes = CtaConnect.INSTANCE.connect(TRAIN_ARRIVALS, reqParams, nearbyFragment.getContext());
                final SparseArray<TrainArrival> temp = XmlParser.INSTANCE.parseArrivals(xmlRes, DataHolder.INSTANCE.getTrainData());
                for (int j = 0; j < temp.size(); j++) {
                    trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                }
                trackWithGoogleAnalytics(nearbyFragment.getActivity(), R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
            } catch (final ConnectException exception) {
                Log.e(TAG, exception.getMessage(), exception);
                return trainArrivals;
            } catch (final Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                throw Exceptions.propagate(throwable);
            }
        }
        return trainArrivals;
    }

    private void loadAroundTrainArrival(@NonNull final Station station, final SparseArray<TrainArrival> resultTrainStation) {
        if (nearbyFragment.isAdded()) {
            try {
                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(nearbyFragment.getRequestStopId(), Integer.toString(station.getId()));
                final InputStream xmlRes = CtaConnect.INSTANCE.connect(TRAIN_ARRIVALS, reqParams, nearbyFragment.getContext());
                final SparseArray<TrainArrival> temp = XmlParser.INSTANCE.parseArrivals(xmlRes, DataHolder.INSTANCE.getTrainData());
                for (int j = 0; j < temp.size(); j++) {
                    resultTrainStation.put(temp.keyAt(j), temp.valueAt(j));
                }
                trackWithGoogleAnalytics(nearbyFragment.getActivity(), R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
            } catch (final ConnectException exception) {
                Log.e(TAG, exception.getMessage(), exception);
            } catch (final Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                throw Exceptions.propagate(throwable);
            }
        }
    }

    private List<BikeStation> loadAroundBikeArrivals(@NonNull final List<BikeStation> bikeStations) {
        List<BikeStation> bikeStationsRes = new ArrayList<>();
        try {
            if (nearbyFragment.isAdded()) {
                final InputStream content = DivvyConnect.INSTANCE.connect();
                final List<BikeStation> bikeStationUpdated = JsonParser.INSTANCE.parseStations(content);
                bikeStationsRes = Stream.of(bikeStationUpdated)
                    .filter(bikeStations::contains)
                    .sorted(Util.BIKE_COMPARATOR_NAME)
                    .collect(Collectors.toList());
                trackWithGoogleAnalytics(nearbyFragment.getActivity(), R.string.analytics_category_req, R.string.analytics_action_get_divvy, nearbyFragment.getActivity().getString(R.string.analytics_action_get_divvy_all));
            }
/*        } catch (final ConnectException exception) {
            Log.e(TAG, exception.getMessage(), exception);
            return bikeStationsRes;*/
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
        return bikeStationsRes;
    }

    private void trackWithGoogleAnalytics(@NonNull final Context context, final int category, final int action, final String label) {
        Util.trackAction(context, category, action, label);
    }

}
