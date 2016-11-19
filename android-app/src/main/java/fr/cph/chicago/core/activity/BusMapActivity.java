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

package fr.cph.chicago.core.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.listener.RefreshMarkers;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.rx.observer.BusFollowObserver;
import fr.cph.chicago.rx.observer.BusObserver;
import fr.cph.chicago.util.Util;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.BUSES_DIRECTION_URL;
import static fr.cph.chicago.Constants.BUSES_PATTERN_URL;
import static fr.cph.chicago.Constants.BUSES_VEHICLES_URL;
import static fr.cph.chicago.Constants.GPS_ACCESS;
import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.connection.CtaRequestType.BUS_PATTERN;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks,
    GoogleMap.OnCameraIdleListener,
    OnMapReadyCallback {

    private static final String TAG = BusMapActivity.class.getSimpleName();

    @BindView(android.R.id.content)
    ViewGroup viewGroup;
    @BindView(R.id.map_container)
    LinearLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.bundle_bus_id)
    String bundleBusId;
    @BindString(R.string.bundle_bus_route_id)
    String bundleBusRouteId;
    @BindString(R.string.bundle_bus_bounds)
    String bundleBusBounds;
    @BindString(R.string.analytics_bus_map)
    String analyticsBusMap;
    @BindString(R.string.request_rt)
    String requestRt;

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    Drawable arrowBackWhite;

    private Marker selectedMarker;

    private List<Marker> busMarkers;
    private List<Marker> busStationMarkers;
    private Map<Marker, View> views;
    private Map<Marker, Boolean> status;

    private Integer busId;
    private String busRouteId;
    private String[] bounds;
    private int j;
    private RefreshMarkers refreshBusesBitmap;

    private boolean refreshingInfoWindow = false;
    private boolean loadPattern = true;

    private GoogleMap googleMap;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(this);
        if (!this.isFinishing()) {
            MapsInitializer.initialize(getApplicationContext());
            setContentView(R.layout.activity_map);
            ButterKnife.bind(this);

            if (savedInstanceState != null) {
                busId = savedInstanceState.getInt(bundleBusId);
                busRouteId = savedInstanceState.getString(bundleBusRouteId);
                bounds = savedInstanceState.getStringArray(bundleBusBounds);
            } else {
                final Bundle extras = getIntent().getExtras();
                busId = extras.getInt(bundleBusId);
                busRouteId = extras.getString(bundleBusRouteId);
                bounds = extras.getStringArray(bundleBusBounds);
            }

            busMarkers = new ArrayList<>();
            busStationMarkers = new ArrayList<>();
            views = new HashMap<>();
            status = new HashMap<>();
            refreshBusesBitmap = new RefreshMarkers(getApplicationContext());

            setToolbar();

            Util.trackScreen(getApplicationContext(), analyticsBusMap);

            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        loadPattern = false;
    }

    private void setToolbar() {
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_VEHICLES_URL, 0);
            ObservableUtil.createBusListObservable(getApplicationContext(), busId, busRouteId).subscribe(new BusObserver(BusMapActivity.this, false, layout));
            return false;
        }));

        Util.setWindowsColor(this, toolbar, TrainLine.NA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        toolbar.setTitle(busRouteId);
        toolbar.setNavigationIcon(arrowBackWhite);
        toolbar.setOnClickListener(v -> finish());
    }

    public void refreshInfoWindow() {
        if (selectedMarker == null) {
            return;
        }
        refreshingInfoWindow = true;
        selectedMarker.showInfoWindow();
        refreshingInfoWindow = false;
    }

    public void centerMapOnBus(@NonNull final List<Bus> result) {
        final Position position;
        final int zoom;
        if (result.size() == 1) {
            position = result.get(0).getPosition();
            zoom = 15;
        } else {
            position = Bus.getBestPosition(result);
            zoom = 11;
        }
        final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void drawBuses(@NonNull final List<Bus> buses) {
        cleanAllMarkers();
        final BitmapDescriptor bitmapDescr = refreshBusesBitmap.getCurrentBitmapDescriptor();
        Stream.of(buses).forEach(bus -> {
            final LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
            final Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title("To " + bus.getDestination()).snippet(bus.getId() + "").icon(bitmapDescr).anchor(0.5f, 0.5f).rotation(bus.getHeading()).flat(true));
            busMarkers.add(marker);

            final LayoutInflater layoutInflater = (LayoutInflater) BusMapActivity.this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(R.layout.marker_train, viewGroup, false);
            final TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(marker.getTitle());

            views.put(marker, view);
        });
    }

    private void cleanAllMarkers() {
        Stream.of(busMarkers).forEach(Marker::remove);
        busMarkers.clear();
    }

    private void drawPattern(@NonNull final List<BusPattern> patterns) {
        j = 0;
        final BitmapDescriptor red = BitmapDescriptorFactory.defaultMarker();
        final BitmapDescriptor blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        Stream.of(patterns).forEach(pattern -> {
            final PolylineOptions poly = new PolylineOptions()
                .color(j == 0 ? Color.RED : (j == 1 ? Color.BLUE : Color.YELLOW))
                .width(7f).geodesic(true);
            Stream.of(pattern.getPoints())
                .map(patternPoint -> {
                    final LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
                    poly.add(point);
                    final Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(patternPoint.getStopName() + " (" + pattern.getDirection() + ")")
                        .snippet("")
                        .icon(j == 0 ? red : blue)
                    );
                    marker.setVisible(false);
                    return marker;
                })
                .forEach(busStationMarkers::add);
            googleMap.addPolyline(poly);
            j++;
        });
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private void enableMyLocationOnMapIfAllowed() {
        if (EasyPermissions.hasPermissions(getApplicationContext(), ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            setLocationOnMap();
        } else {
            EasyPermissions.requestPermissions(this, "Would you like to see your current location on the map?", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        setLocationOnMap();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    public void setLocationOnMap() throws SecurityException {
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        busId = savedInstanceState.getInt(bundleBusId);
        busRouteId = savedInstanceState.getString(bundleBusRouteId);
        bounds = savedInstanceState.getStringArray(bundleBusBounds);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(bundleBusId, busId);
        savedInstanceState.putString(bundleBusRouteId, busRouteId);
        savedInstanceState.putStringArray(bundleBusBounds, bounds);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCameraIdle() {
        refreshBusesBitmap.refresh(googleMap.getCameraPosition(), busMarkers, busStationMarkers);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnCameraIdleListener(this);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Util.CHICAGO, 10));

        enableMyLocationOnMapIfAllowed();
        this.googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(final Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                if (!"".equals(marker.getSnippet())) {
                    final View view = views.get(marker);
                    if (!refreshingInfoWindow) {
                        selectedMarker = marker;
                        final String busId = marker.getSnippet();
                        Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL, 0);
                        ObservableUtil.createFollowBusObservable(getApplicationContext(), busId)
                            .subscribe(new BusFollowObserver(BusMapActivity.this, layout, view, false));
                        status.put(marker, false);
                    }
                    return view;
                } else {
                    return null;
                }
            }
        });

        this.googleMap.setOnInfoWindowClickListener(marker -> {
            if (!"".equals(marker.getSnippet())) {
                final View view = views.get(marker);
                if (!refreshingInfoWindow) {
                    selectedMarker = marker;
                    final String runNumber = marker.getSnippet();
                    final boolean current = status.get(marker);
                    Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL, 0);
                    ObservableUtil.createFollowBusObservable(getApplicationContext(), runNumber)
                        .subscribe(new BusFollowObserver(BusMapActivity.this, layout, view, !current));
                    status.put(marker, !current);
                }
            }
        });
        loadActivityData();
    }

    private void loadActivityData() {
        if (Util.isNetworkAvailable(getApplicationContext())) {
            Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_VEHICLES_URL, 0);
            ObservableUtil.createBusListObservable(getApplicationContext(), busId, busRouteId).subscribe(new BusObserver(BusMapActivity.this, true, layout));
            if (loadPattern) {
                new LoadPattern().execute();
            }
        } else {
            Util.showNetworkErrorMessage(layout);
        }
    }

    private class LoadPattern extends AsyncTask<Void, Void, List<BusPattern>> {
        /**
         * List of bus pattern
         **/
        private List<BusPattern> patterns;

        @Override
        protected final List<BusPattern> doInBackground(final Void... params) {
            this.patterns = new ArrayList<>();
            try {
                if (busId == 0) {
                    // Search for directions
                    final MultiValuedMap<String, String> directionParams = new ArrayListValuedHashMap<>();
                    directionParams.put(requestRt, busRouteId);

                    final InputStream xmlResult = CtaConnect.INSTANCE.connect(BUS_DIRECTION, directionParams, getApplicationContext());
                    final BusDirections busDirections = XmlParser.INSTANCE.parseBusDirections(xmlResult, busRouteId);
                    bounds = new String[busDirections.getLBusDirection().size()];
                    for (int i = 0; i < busDirections.getLBusDirection().size(); i++) {
                        bounds[i] = busDirections.getLBusDirection().get(i).getBusDirectionEnum().toString();
                    }
                    Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_DIRECTION_URL, 0);
                }

                final MultiValuedMap<String, String> routeIdParam = new ArrayListValuedHashMap<>();
                routeIdParam.put(requestRt, busRouteId);
                final InputStream content = CtaConnect.INSTANCE.connect(BUS_PATTERN, routeIdParam, getApplicationContext());
                final List<BusPattern> patterns = XmlParser.INSTANCE.parsePatterns(content);
                Stream.of(patterns)
                    .flatMap(pattern ->
                        Stream.of(bounds)
                            .filter(bound -> pattern.getDirection().equals(bound) || bound.toLowerCase(Locale.US).contains(pattern.getDirection().toLowerCase(Locale.US)))
                            .map(value -> pattern)
                    )
                    .forEach(this.patterns::add);
            } catch (final ConnectException | ParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_PATTERN_URL, 0);
            return this.patterns;
        }

        @Override
        protected final void onPostExecute(final List<BusPattern> result) {
            if (result != null) {
                drawPattern(result);
            } else {
                Util.showNetworkErrorMessage(layout);
            }
        }
    }
}
