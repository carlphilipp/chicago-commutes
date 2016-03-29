/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.listener.BusMapOnCameraChangeListener;
import fr.cph.chicago.task.LoadBusFollowTask;
import fr.cph.chicago.task.LoadBusPositionTask;
import fr.cph.chicago.task.LoadCurrentPositionTask;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.connection.CtaRequestType.BUS_PATTERN;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapActivity extends Activity {

    private static final String TAG = BusMapActivity.class.getSimpleName();

    private ViewGroup viewGroup;
    private MapFragment mapFragment;
    private Marker selectedMarker;
    private RelativeLayout layout;

    private List<Marker> busMarkers;
    private List<Marker> busStationMarkers;
    private Map<Marker, View> views;
    private Map<Marker, Boolean> status;

    private Integer busId;
    private String busRouteId;
    private String[] bounds;
    private BusMapOnCameraChangeListener busListener;

    private boolean refreshingInfoWindow = false;
    private boolean centerMap = true;
    private boolean loadPattern = true;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_map);
            layout = (RelativeLayout) findViewById(R.id.map);
            viewGroup = (ViewGroup) findViewById(android.R.id.content);
            if (savedInstanceState != null) {
                busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id));
                busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
                bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds));
            } else {
                busId = getIntent().getExtras().getInt(getString(R.string.bundle_bus_id));
                busRouteId = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_id));
                bounds = getIntent().getExtras().getStringArray(getString(R.string.bundle_bus_bounds));
            }

            busMarkers = new ArrayList<>();
            busStationMarkers = new ArrayList<>();
            views = new HashMap<>();
            status = new HashMap<>();
            busListener = new BusMapOnCameraChangeListener();

            setToolbar();

            Util.trackScreen(getString(R.string.analytics_bus_map));
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (mapFragment == null) {
            final FragmentManager fm = getFragmentManager();
            mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
            final GoogleMapOptions options = new GoogleMapOptions();
            final CameraPosition camera = new CameraPosition(Util.CHICAGO, 10, 0, 0);
            options.camera(camera);
            mapFragment = MapFragment.newInstance(options);
            mapFragment.setRetainInstance(true);
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
    }

    @Override
    public final void onStop() {
        super.onStop();
        centerMap = false;
        loadPattern = false;
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
    }

    @Override
    public final void onResume() {
        super.onResume();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(final Marker marker) {
                        if (!"".equals(marker.getSnippet())) {
                            final View view = views.get(marker);
                            if (!refreshingInfoWindow) {
                                selectedMarker = marker;
                                final String busId = marker.getSnippet();
                                new LoadBusFollowTask(BusMapActivity.this, view, false).execute(busId);
                                status.put(marker, false);
                            }
                            return view;
                        } else {
                            return null;
                        }
                    }
                });

                googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (!"".equals(marker.getSnippet())) {
                            final View view = views.get(marker);
                            if (!refreshingInfoWindow) {
                                selectedMarker = marker;
                                final String runNumber = marker.getSnippet();
                                final boolean current = status.get(marker);
                                new LoadBusFollowTask(BusMapActivity.this, view, !current).execute(runNumber);
                                status.put(marker, !current);
                            }
                        }
                    }
                });
                if (Util.isNetworkAvailable()) {
                    new LoadCurrentPositionTask(BusMapActivity.this, mapFragment).execute();
                    new LoadBusPositionTask(BusMapActivity.this, busId, busRouteId).execute(centerMap, !loadPattern);
                    if (loadPattern) {
                        new LoadPattern().execute();
                    }
                } else {
                    Util.showNetworkErrorMessage(layout);
                }
            }
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        busId = savedInstanceState.getInt(getString(R.string.bundle_bus_id));
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
        bounds = savedInstanceState.getStringArray(getString(R.string.bundle_bus_bounds));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(getString(R.string.bundle_bus_id), busId);
        savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId);
        savedInstanceState.putStringArray(getString(R.string.bundle_bus_bounds), bounds);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new LoadCurrentPositionTask(BusMapActivity.this, mapFragment).execute();
                new LoadBusPositionTask(BusMapActivity.this, busId, busRouteId).execute(false, true);
                return false;
            }
        }));

        Util.setWindowsColor(this, toolbar, TrainLine.NA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        toolbar.setTitle(busRouteId);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
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
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
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
        });
    }

    public void drawBuses(@NonNull final List<Bus> buses) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                for (final Marker marker : busMarkers) {
                    marker.remove();
                }
                busMarkers.clear();
                final Bitmap bitmap = busListener.getCurrentBitmap();
                for (final Bus bus : buses) {
                    final LatLng point = new LatLng(bus.getPosition().getLatitude(), bus.getPosition().getLongitude());
                    final Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(point).title("To " + bus.getDestination()).snippet(bus.getId() + "").icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f)
                                    .rotation(bus.getHeading()).flat(true));
                    busMarkers.add(marker);

                    final LayoutInflater layoutInflater = (LayoutInflater) BusMapActivity.this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = layoutInflater.inflate(R.layout.marker_train, viewGroup, false);
                    final TextView title = (TextView) view.findViewById(R.id.title);
                    title.setText(marker.getTitle());

                    views.put(marker, view);
                }

                busListener.setBusMarkers(busMarkers);
                // TODO Reactivate to see if when we zoom the bug of the info windows disappear
                //googleMap.setOnCameraChangeListener(busListener);
            }
        });
    }

    private void drawPattern(@NonNull final List<BusPattern> patterns) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                int j = 0;
                final BitmapDescriptor red = BitmapDescriptorFactory.defaultMarker();
                final BitmapDescriptor blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                for (final BusPattern pattern : patterns) {
                    final PolylineOptions poly = new PolylineOptions();
                    if (j == 0) {
                        poly.geodesic(true).color(Color.RED);
                    } else if (j == 1) {
                        poly.geodesic(true).color(Color.BLUE);
                    } else {
                        poly.geodesic(true).color(Color.YELLOW);
                    }
                    poly.width(7f);
                    for (final PatternPoint patternPoint : pattern.getPoints()) {
                        final LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
                        poly.add(point);
                        final MarkerOptions options = new MarkerOptions();
                        options.position(point).title(patternPoint.getStopName() + " (" + pattern.getDirection() + ")").snippet("");
                        if (j == 0) {
                            options.icon(red);
                        } else {
                            options.icon(blue);
                        }

                        final Marker marker = googleMap.addMarker(options);
                        busStationMarkers.add(marker);
                        marker.setVisible(false);
                    }
                    googleMap.addPolyline(poly);
                    j++;
                }
                busListener.setBusStationMarkers(busStationMarkers);

                // TODO Reactivate to see if when we zoom the bug of the info windows disappear
                googleMap.setOnCameraChangeListener(busListener);
            }
        });
    }

    private class LoadPattern extends AsyncTask<Void, Void, List<BusPattern>> {
        /**
         * List of bus pattern
         **/
        private List<BusPattern> patterns;

        @Override
        protected final List<BusPattern> doInBackground(final Void... params) {
            this.patterns = new ArrayList<>();
            final CtaConnect connect = CtaConnect.getInstance();
            try {
                if (busId == 0) {
                    // Search for directions
                    final MultiValuedMap<String, String> directionParams = new ArrayListValuedHashMap<>();
                    directionParams.put(getString(R.string.request_rt), busRouteId);
                    final XmlParser xml = XmlParser.getInstance();
                    final InputStream xmlResult = connect.connect(BUS_DIRECTION, directionParams);
                    final BusDirections busDirections = xml.parseBusDirections(xmlResult, busRouteId);
                    bounds = new String[busDirections.getLBusDirection().size()];
                    for (int i = 0; i < busDirections.getLBusDirection().size(); i++) {
                        bounds[i] = busDirections.getLBusDirection().get(i).getBusDirectionEnum().toString();
                    }
                    Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_direction, 0);
                }

                final MultiValuedMap<String, String> routeIdParam = new ArrayListValuedHashMap<>();
                routeIdParam.put(getResources().getString(R.string.request_rt), busRouteId);
                final InputStream content = connect.connect(BUS_PATTERN, routeIdParam);
                final XmlParser xml = XmlParser.getInstance();
                final List<BusPattern> patterns = xml.parsePatterns(content);
                for (final BusPattern pattern : patterns) {
                    final String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
                    for (final String bound : bounds) {
                        if (pattern.getDirection().equals(bound) || bound.toLowerCase(Locale.US).contains(directionIgnoreCase)) {
                            this.patterns.add(pattern);
                        }
                    }
                }
            } catch (final ConnectException | ParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            Util.trackAction(BusMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_pattern, 0);
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
