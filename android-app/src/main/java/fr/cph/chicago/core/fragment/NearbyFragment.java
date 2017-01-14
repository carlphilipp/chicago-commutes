/**
 * Copyright 2017 Carl-Philipp Harmant
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
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
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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
    private GoogleApiClient googleApiClient;

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
        googleApiClient = new GoogleApiClient.Builder(activity)
            .addApi(LocationServices.API)
            .build();
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
        new Thread(() -> fm.beginTransaction().replace(R.id.map, mapFragment).commit()).start();
    }

    @Override
    public final void onResume() {
        super.onResume();
        loadNearbyIfAllowed();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void loadAllArrivals(@NonNull final List<BusStop> busStops, @NonNull final List<Station> trainStations, @NonNull final List<BikeStation> bikeStations) {
        final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap = new SparseArray<>();
        // Execute in parallel all requests to bus arrivals
        // To be able to wait that all the threads ended we transform to list (it enforces it)
        // And then process train and bikes
        Observable.fromIterable(busStops)
            .flatMap(busStop -> Observable.just(busStop).subscribeOn(Schedulers.computation())
                .map(currentBusStop -> {
                    loadAroundBusArrivals(currentBusStop, busArrivalsMap);
                    return new Object();
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
                trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, activity.getString(R.string.analytics_action_get_divvy_all));
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
        Util.trackAction(context, category, action, label);
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
                final BitmapDescriptor bitmapDescriptorBus = createStop(getContext(), R.drawable.bus_stop_icon);
                final BitmapDescriptor bitmapDescriptorTrain = createStop(getContext(), R.drawable.train_station_icon);
                final BitmapDescriptor bitmapDescriptorBike = createStop(getContext(), R.drawable.bike_station_icon);

                Stream.of(busStops)
                    .map(busStop -> {
                        final LatLng point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
                        final MarkerOptions markerOptions = new MarkerOptions()
                            .position(point)
                            .title(busStop.getName())
                            .snippet(busStop.getDescription())
                            .icon(bitmapDescriptorBus);
                        final Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(busStop.getId() + "_" + busStop.getName());
                        return marker;
                    })
                    .forEach(markers::add);

                Stream.of(trainStation)
                    .forEach(station ->
                        Stream.of(station.getStopsPosition())
                            .map(position -> {
                                final LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
                                final MarkerOptions markerOptions = new MarkerOptions()
                                    .position(point)
                                    .title(station.getName())
                                    .icon(bitmapDescriptorTrain);
                                final Marker marker = googleMap.addMarker(markerOptions);
                                marker.setTag(station.getId() + "_" + station.getName());
                                return marker;
                            })
                            .forEach(markers::add)
                    );

                Stream.of(bikeStations)
                    .map(station -> {
                        final LatLng point = new LatLng(station.getLatitude(), station.getLongitude());
                        final MarkerOptions markerOptions = new MarkerOptions()
                            .position(point)
                            .title(station.getName())
                            .icon(bitmapDescriptorBike);
                        final Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(station.getId() + "_" + station.getName());
                        return marker;
                    })
                    .forEach(markers::add);

                addClickEventsToMarkers(busStops, trainStation, bikeStations);
                nearbyAdapter.updateData(busStops, busArrivals, trainStation, trainArrivals, bikeStations, googleMap, markers);
                nearbyAdapter.notifyDataSetChanged();
                showProgress(false);
                nearbyContainer.setVisibility(View.VISIBLE);
            });
        }
    }

    private static BitmapDescriptor createStop(@NonNull final Context context, @DrawableRes final int icon) {
        final int px = context.getResources().getDimensionPixelSize(R.dimen.icon_shadow_2);
        final Bitmap bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitMapBusStation);
        final Drawable shape = ContextCompat.getDrawable(context, icon);
        shape.setBounds(0, 0, px, bitMapBusStation.getHeight());
        shape.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitMapBusStation);
    }

    private void addClickEventsToMarkers(
        @NonNull final List<BusStop> busStops,
        @NonNull final List<Station> stations,
        @NonNull final List<BikeStation> bikeStations) {
        mapFragment.getMapAsync(googleMap ->
            googleMap.setOnMarkerClickListener(marker -> {
                for (int i = 0; i < stations.size(); i++) {
                    final Station station = stations.get(i);
                    if (marker.getTag() != null && marker.getTag().equals(station.getId() + "_" + station.getName())) {
                        listView.smoothScrollToPosition(i);
                        return false;
                    }
                }
                for (int i = 0; i < busStops.size(); i++) {
                    int index = i + stations.size();
                    final BusStop busStop = busStops.get(i);
                    if (marker.getTag() != null && marker.getTag().equals(busStop.getId() + "_" + busStop.getName())) {
                        listView.smoothScrollToPosition(index);
                        return false;
                    }
                }
                for (int i = 0; i < bikeStations.size(); i++) {
                    int index = i + stations.size() + busStops.size();
                    final BikeStation bikeStation = bikeStations.get(i);
                    if (marker.getTag() != null && marker.getTag().equals(bikeStation.getId() + "_" + bikeStation.getName())) {
                        listView.smoothScrollToPosition(index);
                        return false;
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
            busStops = new ArrayList<>();
            trainStations = new ArrayList<>();
        }

        @Override
        protected final Optional<Position> doInBackground(final Void... params) {
            bikeStations = activity.getIntent().getExtras().getParcelableArrayList(bundleBikeStations);

            if (!googleApiClient.isConnected()) {
                googleApiClient.blockingConnect();
            }

            final BusData busData = DataHolder.INSTANCE.getBusData();
            final TrainData trainData = DataHolder.INSTANCE.getTrainData();

            final GPSUtil gpsUtil = new GPSUtil(googleApiClient, this);
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
            Log.v(TAG, "Location changed: " + location.getLatitude() + " " + location.getLongitude());
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
