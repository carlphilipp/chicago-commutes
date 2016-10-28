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

package fr.cph.chicago.core.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
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
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.MainActivity;
import fr.cph.chicago.core.adapter.NearbyAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.util.GPSUtil;
import fr.cph.chicago.util.Util;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.GPS_ACCESS;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;
import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class NearbyFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";

    @BindView(R.id.fragment_nearby_list)
    ListView listView;
    @BindView(R.id.loading_layout)
    View loadLayout;
    @BindView(R.id.nearby_list_container)
    RelativeLayout nearbyContainer;
    @BindView(R.id.hideEmptyStops)
    CheckBox checkBox;

    @BindString(R.string.request_stop_id)
    String requestStopId;
    @BindString(R.string.request_map_id)
    String requestMapId;
    @BindString(R.string.bundle_bike_stations)
    String bundleBikeStations;

    private Unbinder unbinder;

    private SupportMapFragment mapFragment;

    private MainActivity activity;
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
        Util.trackScreen(getContext(), getString(R.string.analytics_nearby_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        if (!activity.isFinishing()) {
            unbinder = ButterKnife.bind(this, rootView);
            nearbyAdapter = new NearbyAdapter(getContext());
            listView.setAdapter(nearbyAdapter);

            hideStationsStops = PreferencesImpl.INSTANCE.getHideShowNearby(getContext());
            checkBox.setChecked(hideStationsStops);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferencesImpl.INSTANCE.saveHideShowNearby(getContext(), isChecked);
                hideStationsStops = isChecked;
                if (Util.isNetworkAvailable(getContext())) {
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
        options.camera(camera);
        mapFragment = SupportMapFragment.newInstance(options);
        mapFragment.setRetainInstance(true);
        final FragmentManager fm = activity.getSupportFragmentManager();
        new Thread(() -> {
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }).start();
    }

    @Override
    public final void onResume() {
        super.onResume();
        loadNearbyIfAllowed();
    }

    private void loadAllArrivals(@NonNull final List<BusStop> busStops, @NonNull final List<Station> trainStations, @NonNull final List<BikeStation> bikeStations) {
        final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap = new SparseArray<>();
        // Execute in parallel all requests to bus arrivals
        // To be able to wait that all the threads ended we transform to list (it enforces it)
        // And then process train and bikes
        Observable.from(busStops)
            .flatMap(busStop -> Observable.just(busStop).subscribeOn(Schedulers.computation())
                .map(currentBusStop -> {
                    loadAroundBusArrivals(currentBusStop, busArrivalsMap);
                    return null;
                })
            )
            .doOnError(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                Util.handleConnectOrParserException(throwable, null, listView, listView);
                activity.runOnUiThread(() -> showProgress(false));
            })
            .toList()
            .subscribeOn(Schedulers.io())
            .subscribe(
                val -> {
                    final SparseArray<TrainArrival> trainArrivals = loadAroundTrainArrivals(trainStations);
                    final List<BikeStation> bikeStationsRes = loadAroundBikeArrivals(bikeStations);
                    hideStationsAndStopsIfNeeded(busStops, busArrivalsMap, trainStations, trainArrivals);

                    activity.runOnUiThread(() -> updateMarkersAndModel(busStops, busArrivalsMap, trainStations, trainArrivals, bikeStationsRes));
                },
                throwable -> {
                    Log.e(TAG, throwable.getMessage(), throwable);
                    Util.handleConnectOrParserException(throwable, null, listView, listView);
                    activity.runOnUiThread(() -> showProgress(false));
                }
            );
    }

    private void loadAroundBusArrivals(@NonNull final BusStop busStop, @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap) {
        try {
            if (isAdded()) {
                int busStopId = busStop.getId();
                // Create
                final Map<String, List<BusArrival>> tempMap = busArrivalsMap.get(busStopId, new ConcurrentHashMap<>());
                if (!tempMap.containsKey(Integer.toString(busStopId))) {
                    busArrivalsMap.put(busStopId, tempMap);
                }

                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(requestStopId, Integer.toString(busStopId));
                final InputStream is = CtaConnect.INSTANCE.connect(BUS_ARRIVALS, reqParams, getContext());
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
                trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
            }
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
    }

    private SparseArray<TrainArrival> loadAroundTrainArrivals(@NonNull final List<Station> trainStations) {
        final SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        try {
            if (isAdded()) {
                for (final Station station : trainStations) {
                    final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                    reqParams.put(requestMapId, Integer.toString(station.getId()));
                    final InputStream xmlRes = CtaConnect.INSTANCE.connect(TRAIN_ARRIVALS, reqParams, getContext());
                    final SparseArray<TrainArrival> temp = XmlParser.INSTANCE.parseArrivals(xmlRes, DataHolder.INSTANCE.getTrainData());
                    for (int j = 0; j < temp.size(); j++) {
                        trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                    }
                    trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
                }
            }
            return trainArrivals;
        } catch (final ConnectException exception) {
            Log.e(TAG, exception.getMessage(), exception);
            return trainArrivals;
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
    }

    private List<BikeStation> loadAroundBikeArrivals(@NonNull final List<BikeStation> bikeStations) {
        List<BikeStation> bikeStationsRes = new ArrayList<>();
        try {
            if (isAdded()) {
                final InputStream content = DivvyConnect.INSTANCE.connect();
                final List<BikeStation> bikeStationUpdated = JsonParser.INSTANCE.parseStations(content);
                bikeStationsRes = Stream.of(bikeStationUpdated)
                    .filter(bikeStations::contains)
                    .sorted(Util.BIKE_COMPARATOR_NAME)
                    .collect(Collectors.toList());
                trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, getContext().getString(R.string.analytics_action_get_divvy_all));
            }
            return bikeStationsRes;
        } catch (final ConnectException exception) {
            Log.e(TAG, exception.getMessage(), exception);
            return bikeStationsRes;
        } catch (final Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            throw Exceptions.propagate(throwable);
        }
    }

    private void hideStationsAndStopsIfNeeded(
        @NonNull final List<BusStop> busStops,
        @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap,
        @NonNull final List<Station> trainStations,
        @NonNull final SparseArray<TrainArrival> trainArrivals) {
        if (hideStationsStops && isAdded()) {
            final List<BusStop> busStopTmp = new ArrayList<>();
            for (final BusStop busStop : busStops) {
                if (busArrivalsMap.get(busStop.getId(), new ConcurrentHashMap<>()).size() == 0) {
                    busArrivalsMap.remove(busStop.getId());
                } else {
                    busStopTmp.add(busStop);
                }
            }
            busStops.clear();
            busStops.addAll(busStopTmp);

            final List<Station> trainStationTmp = new ArrayList<>();
            for (final Station station : trainStations) {
                if (trainArrivals.get(station.getId()) == null || trainArrivals.get(station.getId()).getEtas().size() == 0) {
                    trainArrivals.remove(station.getId());
                } else {
                    trainStationTmp.add(station);
                }
            }
            trainStations.clear();
            trainStations.addAll(trainStationTmp);
        }
    }

    private void trackWithGoogleAnalytics(@NonNull final Context context, final int category, final int action, final String label) {
        Util.trackAction(context, category, action, label, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private void updateMarkersAndModel(
        @NonNull final List<BusStop> busStops,
        @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivals,
        @NonNull final List<Station> trainStation,
        @NonNull final SparseArray<TrainArrival> trainArrivals,
        @NonNull final List<BikeStation> bikeStations) {
        if (isAdded()) {
            mapFragment.getMapAsync(googleMap -> {
                final List<Marker> markers = new ArrayList<>();
                final BitmapDescriptor azure = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                final BitmapDescriptor violet = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                final BitmapDescriptor yellow = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                Stream.of(busStops)
                    .map(busStop -> {
                        final LatLng point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
                        return new MarkerOptions().position(point).title(busStop.getName()).snippet(Integer.toString(busStop.getId())).icon(azure);
                    })
                    .map(googleMap::addMarker)
                    .forEach(markers::add);


                Stream.of(trainStation)
                    .forEach(station ->
                        Stream.of(station.getStopsPosition())
                            .map(position -> {
                                final LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
                                return new MarkerOptions().position(point).title(station.getName()).snippet(Integer.toString(station.getId())).icon(violet);
                            })
                            .map(googleMap::addMarker)
                            .forEach(markers::add)
                    );

                Stream.of(bikeStations)
                    .map(station -> {
                        final LatLng point = new LatLng(station.getLatitude(), station.getLongitude());
                        return new MarkerOptions().position(point).title(station.getName()).snippet(station.getId() + "").icon(yellow);
                    })
                    .map(googleMap::addMarker)
                    .forEach(markers::add);

                addClickEventsToMarkers(busStops, trainStation, bikeStations);
                nearbyAdapter.updateData(busStops, busArrivals, trainStation, trainArrivals, bikeStations, googleMap, markers);
                nearbyAdapter.notifyDataSetChanged();
                showProgress(false);
                nearbyContainer.setVisibility(View.VISIBLE);
            });
        }
    }

    private void addClickEventsToMarkers(
        @NonNull final List<BusStop> busStops,
        @NonNull final List<Station> stations,
        @NonNull final List<BikeStation> bikeStations) {
        mapFragment.getMapAsync(googleMap ->
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
                    if (marker.getSnippet().equals(Integer.toString(bikeStations.get(i).getId()))) {
                        listView.smoothScrollToPosition(index);
                        break;
                    }
                }
                return false;
            })
        );
    }

    private void showProgress(final boolean show) {
        try {
            if (isAdded()) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                loadLayout.setVisibility(View.VISIBLE);
                loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        if (loadLayout != null) {
                            loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    }
                });
            }
        } catch (final IllegalStateException e) {
            Log.w(TAG, e.getMessage(), e);
        }
    }

    public final void reloadData() {
        loadNearbyIfAllowed();
    }

    private class LoadNearbyTask extends AsyncTask<Void, Void, Optional<Position>> implements LocationListener {

        private List<BusStop> busStops;
        private List<Station> trainStations;
        private List<BikeStation> bikeStations;

        private LoadNearbyTask() {
            this.busStops = new ArrayList<>();
            this.trainStations = new ArrayList<>();
        }

        @Override
        protected final Optional<Position> doInBackground(final Void... params) {
            bikeStations = activity.getIntent().getExtras().getParcelableArrayList(bundleBikeStations);

            final BusData busData = DataHolder.INSTANCE.getBusData();
            final TrainData trainData = DataHolder.INSTANCE.getTrainData();

            final GPSUtil gpsUtil = new GPSUtil(this, activity);
            final Optional<Position> position = gpsUtil.getLocation();
            if (position.isPresent()) {
                final Realm realm = Realm.getDefaultInstance();
                busStops = busData.readNearbyStops(realm, position.get());
                realm.close();
                trainStations = trainData.readNearbyStation(position.get());
                // TODO: wait bikeStations is loaded
                if (bikeStations != null) {
                    bikeStations = BikeStation.readNearbyStation(bikeStations, position.get());
                }
            }
            return position;
        }

        @Override
        protected final void onPostExecute(final Optional<Position> result) {
            Util.centerMap(mapFragment, result);
            loadAllArrivals(busStops, trainStations, bikeStations);
        }

        @Override
        public final void onLocationChanged(final Location location) {
        }

        @Override
        public final void onProviderDisabled(final String provider) {
        }

        @Override
        public final void onProviderEnabled(final String provider) {
        }

        @Override
        public final void onStatusChanged(final String provider, final int status, final Bundle extras) {
        }
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private void loadNearbyIfAllowed() {
        if (EasyPermissions.hasPermissions(getContext(), ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            startLoadingNearby();
        } else {
            EasyPermissions.requestPermissions(this, "To access that feature, we need to access your current location", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startLoadingNearby();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        showProgress(false);
    }

    private void startLoadingNearby() {
        if (Util.isNetworkAvailable(getContext())) {
            nearbyContainer.setVisibility(View.GONE);
            showProgress(true);
            new LoadNearbyTask().execute();
        } else {
            Util.showNetworkErrorMessage(activity);
            showProgress(false);
        }
    }
}
