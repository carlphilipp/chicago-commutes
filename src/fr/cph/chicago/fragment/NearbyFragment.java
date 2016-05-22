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

package fr.cph.chicago.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.NearbyAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.task.LoadNearbyTask;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Map Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class NearbyFragment extends Fragment {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private SupportMapFragment mapFragment;
    private View loadLayout;
    private RelativeLayout nearbyContainer;
    private ListView listView;

    private MainActivity activity;
    private GoogleMap googleMap;
    private NearbyAdapter nearbyAdapter;
    private boolean hideStationsStops;

    @NonNull
    public static NearbyFragment newInstance(final int sectionNumber) {
        final NearbyFragment fragment = new NearbyFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (MainActivity) context : null;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkTrainData(activity);
        App.checkBusData(activity);
        Util.trackScreen(getString(R.string.analytics_nearby_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        if (!activity.isFinishing()) {
            nearbyAdapter = new NearbyAdapter(activity);
            listView = (ListView) rootView.findViewById(R.id.fragment_nearby_list);
            listView.setAdapter(nearbyAdapter);
            loadLayout = rootView.findViewById(R.id.loading_layout);
            nearbyContainer = (RelativeLayout) rootView.findViewById(R.id.nearby_list_container);

            hideStationsStops = Preferences.getHideShowNearby();
            final CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.hideEmptyStops);
            checkBox.setChecked(hideStationsStops);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Preferences.saveHideShowNearby(isChecked);
                hideStationsStops = isChecked;
                if (Util.isNetworkAvailable()) {
                    reloadData();
                }
            });
            showProgress(true);
        }
        return rootView;
    }

    @Override
    public final void onStart() {
        super.onStart();
        final GoogleMapOptions options = new GoogleMapOptions();
        final CameraPosition camera = new CameraPosition(Util.CHICAGO, 7, 0, 0);
        final FragmentManager fm = activity.getSupportFragmentManager();
        options.camera(camera);
        mapFragment = SupportMapFragment.newInstance(options);
        mapFragment.setRetainInstance(true);
        fm.beginTransaction().replace(R.id.map, mapFragment).commit();
    }

    @Override
    public final void onResume() {
        super.onResume();
        mapFragment.getMapAsync(googleMap1 -> {
            NearbyFragment.this.googleMap = googleMap1;
            if (Util.isNetworkAvailable()) {
                new LoadNearbyTask(NearbyFragment.this, activity, mapFragment).execute();
                nearbyContainer.setVisibility(View.GONE);
                showProgress(true);
            } else {
                Util.showNetworkErrorMessage(activity);
                showProgress(false);
            }
        });
    }

    public void loadArrivals(@NonNull final List<BusStop> busStops, @NonNull final List<Station> trainStations, @NonNull final List<BikeStation> bikeStations) {
        new LoadArrivals().execute(busStops, trainStations, bikeStations);
    }

    private class LoadArrivals extends AsyncTask<List<?>, Void, Void> {

        private final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap;
        private final SparseArray<TrainArrival> trainArrivals;
        private List<BusStop> busStops;
        private List<Station> stations;
        private final List<BikeStation> bikeStationsRes;
        private List<BikeStation> bikeStationsTemp;

        private LoadArrivals() {
            bikeStationsRes = new ArrayList<>();
            busArrivalsMap = new SparseArray<>();
            trainArrivals = new SparseArray<>();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final Void doInBackground(final List<?>... params) {
            busStops = (List<BusStop>) params[0];
            stations = (List<Station>) params[1];
            bikeStationsTemp = (List<BikeStation>) params[2];
            loadAroundBusArrivals();
            loadAroundTrainArrivals();
            loadAroundBikeData();
            return null;
        }

        private void loadAroundBusArrivals() {
            final CtaConnect cta = CtaConnect.getInstance();
            for (final BusStop busStop : busStops) {
                int busId = busStop.getId();
                // Create
                final Map<String, List<BusArrival>> tempMap = busArrivalsMap.get(busId, new HashMap<>());
                if (!tempMap.containsKey(Integer.toString(busId))) {
                    busArrivalsMap.put(busId, tempMap);
                }

                try {
                    final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
                    if (NearbyFragment.this.isAdded()) {
                        reqParams.put(getString(R.string.request_stop_id), Integer.toString(busId));
                        final InputStream is = cta.connect(BUS_ARRIVALS, reqParams);
                        final XmlParser xml = XmlParser.getInstance();
                        final List<BusArrival> busArrivals = xml.parseBusArrivals(is);
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
                        Util.trackAction(NearbyFragment.this.activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_arrival, 0);
                    }
                } catch (final ConnectException | ParserException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        private void loadAroundTrainArrivals() {
            final CtaConnect cta = CtaConnect.getInstance();
            for (final Station station : stations) {
                try {
                    final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
                    if (NearbyFragment.this.isAdded()) {
                        reqParams.put(getString(R.string.request_map_id), Integer.toString(station.getId()));
                        final InputStream xmlRes = cta.connect(TRAIN_ARRIVALS, reqParams);
                        final XmlParser xml = XmlParser.getInstance();
                        final SparseArray<TrainArrival> temp = xml.parseArrivals(xmlRes, DataHolder.getInstance().getTrainData());
                        for (int j = 0; j < temp.size(); j++) {
                            trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                        }
                        Util.trackAction(NearbyFragment.this.activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.url_train_arrivals, 0);
                    }
                } catch (final ConnectException | ParserException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        private void loadAroundBikeData() {
            // TODO: modify the check
            if (bikeStationsTemp != null) {
                // Bike
                final DivvyConnect connect = DivvyConnect.getInstance();
                try {
                    final JsonParser json = JsonParser.getInstance();
                    final InputStream content = connect.connect();
                    final List<BikeStation> bikeStationUpdated = json.parseStations(content);
                    bikeStationsRes.addAll(
                        Stream.of(bikeStationUpdated)
                            .filter(station -> bikeStationsTemp.contains(station))
                            .collect(Collectors.toList())
                    );
                    Collections.sort(bikeStationsRes, Util.BIKE_COMPARATOR_NAME);
                    Util.trackAction(NearbyFragment.this.activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
                } catch (final ConnectException | ParserException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        @Override
        protected final void onPostExecute(final Void result) {
            // TODO recode that
            if (hideStationsStops) {
                final List<BusStop> busStopTmp = new ArrayList<>();
                for (final BusStop busStop : busStops) {
                    if (busArrivalsMap.get(busStop.getId()).size() == 0) {
                        busArrivalsMap.remove(busStop.getId());
                    } else {
                        busStopTmp.add(busStop);
                    }
                }
                busStops.clear();
                busStops = busStopTmp;

                final List<Station> trainStationTmp = new ArrayList<>();
                for (final Station station : stations) {
                    if (trainArrivals.get(station.getId()) == null || trainArrivals.get(station.getId()).getEtas().size() == 0) {
                        trainArrivals.remove(station.getId());
                    } else {
                        trainStationTmp.add(station);
                    }
                }
                stations.clear();
                stations = trainStationTmp;
            }
            activity.runOnUiThread(() -> load(busStops, busArrivalsMap, stations, trainArrivals, bikeStationsRes));
        }
    }

    private void load(@NonNull final List<BusStop> buses,
                      @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivals,
                      @NonNull final List<Station> stations,
                      @NonNull final SparseArray<TrainArrival> trainArrivals,
                      @NonNull final List<BikeStation> bikeStations) {
        final List<Marker> markers = new ArrayList<>();
        final BitmapDescriptor azure = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        final BitmapDescriptor violet = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
        final BitmapDescriptor yellow = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        MarkerOptions options;
        Marker marker;
        LatLng point;
        for (final BusStop busStop : buses) {
            point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
            options = new MarkerOptions().position(point).title(busStop.getName()).snippet(Integer.toString(busStop.getId()));
            options.icon(azure);
            marker = googleMap.addMarker(options);
            markers.add(marker);

        }
        for (final Station station : stations) {
            for (final Position position : station.getStopsPosition()) {
                point = new LatLng(position.getLatitude(), position.getLongitude());
                options = new MarkerOptions().position(point).title(station.getName()).snippet(Integer.toString(station.getId()));
                options.icon(violet);
                marker = googleMap.addMarker(options);
                markers.add(marker);
            }
        }
        for (final BikeStation station : bikeStations) {
            point = new LatLng(station.getLatitude(), station.getLongitude());
            options = new MarkerOptions().position(point).title(station.getName()).snippet(station.getId() + "");
            options.icon(yellow);
            marker = googleMap.addMarker(options);
            markers.add(marker);
        }
        addClickEventsToMarkers(buses, stations, bikeStations);
        nearbyAdapter.updateData(buses, busArrivals, stations, trainArrivals, bikeStations, googleMap, markers);
        nearbyAdapter.notifyDataSetChanged();
        showProgress(false);
        nearbyContainer.setVisibility(View.VISIBLE);

    }

    private void addClickEventsToMarkers(@NonNull final List<BusStop> busStops, @NonNull final List<Station> stations, @NonNull final List<BikeStation> bikeStations) {
        googleMap.setOnMarkerClickListener(marker -> {
            boolean found = false;
            for (int i = 0; i < stations.size(); i++) {
                if (marker.getSnippet().equals(Integer.toString(stations.get(i).getId()))) {
                    listView.smoothScrollToPosition(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (int i = 0; i < busStops.size(); i++) {
                    int index = i + stations.size();
                    if (marker.getSnippet().equals(Integer.toString(busStops.get(i).getId()))) {
                        listView.smoothScrollToPosition(index);
                        break;
                    }
                }
            }
            for (int i = 0; i < bikeStations.size(); i++) {
                int index = i + stations.size() + busStops.size();
                if (marker.getSnippet().equals(bikeStations.get(i).getId() + "")) {
                    listView.smoothScrollToPosition(index);
                    break;
                }
            }
            return false;
        });
    }

    private void showProgress(final boolean show) {
        try {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            loadLayout.setVisibility(View.VISIBLE);
            loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } catch (final IllegalStateException e) {
            Log.w(TAG, e.getMessage(), e);
        }
    }

    public final void reloadData() {
        if (Util.isNetworkAvailable()) {
            googleMap.clear();
            showProgress(true);
            nearbyContainer.setVisibility(View.GONE);
            new LoadNearbyTask(this, activity, mapFragment).execute();
        } else {
            Util.showNetworkErrorMessage(activity);
            showProgress(false);
        }
    }
}
