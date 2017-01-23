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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.util.GPSUtil;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import lombok.Data;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
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
    private static final double DEFAULT_RANGE = 0.008;

    @BindView(R.id.activity_bar)
    ProgressBar progressBar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.loading_layout_container)
    LinearLayout layoutContainer;

    @BindString(R.string.request_stop_id)
    String requestStopId;
    @BindString(R.string.request_map_id)
    String requestMapId;
    @BindString(R.string.bundle_bike_stations)
    String bundleBikeStations;

    private Unbinder unbinder;

    private SupportMapFragment mapFragment;

    MainActivity activity;
    private GoogleApiClient googleApiClient;
    private MarkerDataHolder markerDataHolder;

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
            markerDataHolder = new MarkerDataHolder();
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

    private void loadAllArrivals(@NonNull final List<AStation> stations) {
        final List<Station> trainStations = Stream.of(stations).filter(station -> station instanceof Station).map(station -> (Station) station).collect(Collectors.toList());
        final List<BusStop> busStops = Stream.of(stations).filter(station -> station instanceof BusStop).map(station -> (BusStop) station).collect(Collectors.toList());
        final List<BikeStation> bikeStations = Stream.of(stations).filter(station -> station instanceof BikeStation).map(station -> (BikeStation) station).collect(Collectors.toList());

        // Train handling
        final SparseArray<TrainArrival> resultTrainStation = new SparseArray<>();
        final Observable<Object> trainObservable = Observable.fromIterable(trainStations)
            .map(station -> {
                loadAroundTrainArrival(station, resultTrainStation);
                return new Object();
            });

        // Bus handling
        final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap = new SparseArray<>();
        final Observable<Object> busObservable = Observable.fromIterable(busStops)
            .flatMap(busStop -> Observable.just(busStop).subscribeOn(Schedulers.computation())
                .map(currentBusStop -> {
                    loadAroundBusArrivals(currentBusStop, busArrivalsMap);
                    return new Object();
                })
            );


        Observable.fromArray(trainObservable, busObservable)
            .doOnError(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                activity.runOnUiThread(() -> showProgress(false));
            })
            .subscribeOn(Schedulers.io())
            .subscribe(val -> {
                final List<BikeStation> bikeStationsRes = loadAroundBikeArrivals(bikeStations);
                Log.i(TAG, "busArrivalsMap: " + busArrivalsMap);
                Log.i(TAG, "resultTrainStation: " + resultTrainStation);
                Log.i(TAG, "bikeStationsRes: " + bikeStationsRes);
                activity.runOnUiThread(() -> updateMarkersAndModel(busStops, trainStations, bikeStationsRes));
            });
    }

    Map<String, List<BusArrival>> loadAroundBusArrivals(@NonNull final BusStop busStop, @NonNull final SparseArray<Map<String, List<BusArrival>>> busArrivalsMap) {
        final Map<String, List<BusArrival>> result = new HashMap<>();
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
        return result;
    }

    SparseArray<TrainArrival> loadAroundTrainArrivals(@NonNull final Station station) {
        final SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        if (isAdded()) {
            try {
                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(requestMapId, Integer.toString(station.getId()));
                final InputStream xmlRes = CtaConnect.INSTANCE.connect(TRAIN_ARRIVALS, reqParams, getContext());
                final SparseArray<TrainArrival> temp = XmlParser.INSTANCE.parseArrivals(xmlRes, DataHolder.INSTANCE.getTrainData());
                for (int j = 0; j < temp.size(); j++) {
                    trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                }
                trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
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
        if (isAdded()) {
            try {
                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
                reqParams.put(requestMapId, Integer.toString(station.getId()));
                final InputStream xmlRes = CtaConnect.INSTANCE.connect(TRAIN_ARRIVALS, reqParams, getContext());
                final SparseArray<TrainArrival> temp = XmlParser.INSTANCE.parseArrivals(xmlRes, DataHolder.INSTANCE.getTrainData());
                for (int j = 0; j < temp.size(); j++) {
                    resultTrainStation.put(temp.keyAt(j), temp.valueAt(j));
                }
                trackWithGoogleAnalytics(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
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

    class MarkerDataHolder {
        final Map<LatLng, List<MarkerHolder>> data;

        private MarkerDataHolder() {
            data = new HashMap<>();
        }

        void addData(final Marker marker, final AStation station) {
            marker.setVisible(true);
            final MarkerHolder markerHolder = new MarkerHolder();
            markerHolder.setMarker(marker);
            markerHolder.setStation(station);
            final LatLng latLng = marker.getPosition();
            if (data.containsKey(latLng)) {
                final List<MarkerHolder> markerHolderList = data.get(latLng);
                final Optional<MarkerHolder> optional = Stream.of(markerHolderList)
                    .filter(m -> m.getStation() instanceof Station)
                    .findFirst();
                if (!optional.isPresent()) {
                    Stream.of(markerHolderList).forEach(m -> m.getMarker().setVisible(false));
                }
                markerHolderList.add(markerHolder);
            } else {
                final List<MarkerHolder> markerHolderList = new ArrayList<>();
                markerHolderList.add(markerHolder);
                data.put(latLng, markerHolderList);
            }
        }

        void clear() {
            data.clear();
        }

        List<AStation> getData(final Marker marker) {
            return Stream.of(data.get(marker.getPosition())).map(MarkerHolder::getStation).collect(Collectors.toList());
        }

        @Data
        private class MarkerHolder {
            private Marker marker;
            private AStation station;
        }
    }

    private void updateMarkersAndModel(
        @NonNull final List<BusStop> busStops,
        @NonNull final List<Station> trainStation,
        @NonNull final List<BikeStation> bikeStations) {
        if (isAdded()) {
            mapFragment.getMapAsync(googleMap -> {
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                markerDataHolder.clear();

                final BitmapDescriptor bitmapDescriptorBus = createStop(getContext(), R.drawable.bus_stop_icon);
                final BitmapDescriptor bitmapDescriptorTrain = createStop(getContext(), R.drawable.train_station_icon);
                final BitmapDescriptor bitmapDescriptorBike = createStop(getContext(), R.drawable.bike_station_icon);

                Stream.of(busStops)
                    .forEach(busStop -> {
                        final LatLng point = new LatLng(busStop.getPosition().getLatitude(), busStop.getPosition().getLongitude());
                        final MarkerOptions markerOptions = new MarkerOptions()
                            .position(point)
                            .title(busStop.getName())
                            .snippet(busStop.getDescription())
                            .icon(bitmapDescriptorBus);
                        final Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(busStop.getId() + "_" + busStop.getName());
                        marker.setVisible(false);
                        markerDataHolder.addData(marker, busStop);
                        Log.i(TAG, "Add bus stop: " + busStop.getId() + "_" + busStop.getName() + " " + busStop.getPosition().getLatitude() + " " + busStop.getPosition().getLongitude());
                    });

                Stream.of(trainStation)
                    .forEach(station ->
                        Stream.of(station.getStopsPosition())
                            .forEach(position -> {
                                final String key = station.getId() + "_" + station.getName() + "_train";
                                //if (!markerHolder.containsStation(station)) {
                                final LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
                                final MarkerOptions markerOptions = new MarkerOptions()
                                    .position(point)
                                    .title(station.getName())
                                    .icon(bitmapDescriptorTrain);
                                final Marker marker = googleMap.addMarker(markerOptions);
                                marker.setTag(key);
                                marker.setVisible(false);
                                markerDataHolder.addData(marker, station);
                                Log.i(TAG, "Add train station: " + key + " " + position.getLatitude() + " " + position.getLongitude());
                                //}
                            })
                    );

                Stream.of(bikeStations)
                    .forEach(station -> {
                        final LatLng point = new LatLng(station.getLatitude(), station.getLongitude());
                        final MarkerOptions markerOptions = new MarkerOptions()
                            .position(point)
                            .title(station.getName())
                            .icon(bitmapDescriptorBike);
                        final Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(station.getId() + "_" + station.getName());
                        marker.setVisible(false);

                        markerDataHolder.addData(marker, station);
                        Log.i(TAG, "Add bike stop: " + station.getId() + "_" + station.getName() + " " + station.getLatitude() + " " + station.getLongitude());
                    });

                showProgress(false);
                googleMap.setOnMarkerClickListener(new OnMarkerClickListener(markerDataHolder, NearbyFragment.this));
            });
        }
    }

    private static BitmapDescriptor createStop(@Nullable final Context context, @DrawableRes final int icon) {
        if (context != null) {
            final int px = context.getResources().getDimensionPixelSize(R.dimen.icon_shadow_2);
            final Bitmap bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitMapBusStation);
            final Drawable shape = ContextCompat.getDrawable(context, icon);
            shape.setBounds(0, 0, px, bitMapBusStation.getHeight());
            shape.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitMapBusStation);
        } else {
            return BitmapDescriptorFactory.defaultMarker();
        }
    }

    private void showProgress(final boolean show) {
        if (isAdded()) {
            if (show) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(50);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public final void reloadData() {
        loadNearbyIfAllowed();
    }

    private class LoadNearbyTask extends AsyncTask<Void, Void, Optional<Position>> {

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

            final GPSUtil gpsUtil = new GPSUtil(googleApiClient);
            final Optional<Position> position = gpsUtil.getLocation();
            if (position.isPresent()) {
                final Realm realm = Realm.getDefaultInstance();
                busStops = busData.readNearbyStops(realm, position.get(), DEFAULT_RANGE);
                realm.close();
                trainStations = trainData.readNearbyStation(position.get(), DEFAULT_RANGE);
                bikeStations = BikeStation.readNearbyStation(bikeStations, position.get(), DEFAULT_RANGE);
            }
            return position;
        }

        @Override
        protected final void onPostExecute(final Optional<Position> result) {
            Util.centerMap(mapFragment, result);
            //final List<BikeStation> bikeStationsRes = loadAroundBikeArrivals(bikeStations);
            //activity.runOnUiThread(() -> updateMarkersAndModel(busStops, trainStations, bikeStations));
            updateMarkersAndModel(busStops, trainStations, bikeStations);
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
            //nearbyContainer.setVisibility(View.GONE);
            showProgress(true);
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            new LoadNearbyTask().execute();
        } else {
            Util.showNetworkErrorMessage(activity);
            showProgress(false);
        }
    }
}
