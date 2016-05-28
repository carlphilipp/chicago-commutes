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

package fr.cph.chicago.app.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.app.listener.TrainMapOnCameraChangeListener;
import fr.cph.chicago.app.task.LoadTrainFollowTask;
import fr.cph.chicago.app.task.LoadTrainPositionTask;
import fr.cph.chicago.util.Util;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapActivity extends Activity {

    private ViewGroup viewGroup;
    private MapFragment mapFragment;
    private Marker selectedMarker;
    private Map<Marker, View> views;
    private RelativeLayout layout;
    private String line;
    private Map<Marker, Boolean> status;
    private List<Marker> markers;
    private TrainData trainData;
    private TrainMapOnCameraChangeListener trainListener;

    private boolean centerMap = true;
    private boolean refreshingInfoWindow = false;
    private boolean drawLine = true;

    public TrainMapActivity() {
        this.views = new HashMap<>();
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkTrainData(this);
        if (!this.isFinishing()) {
            MapsInitializer.initialize(getApplicationContext());
            setContentView(R.layout.activity_map);
            layout = (RelativeLayout) findViewById(R.id.map);
            viewGroup = (ViewGroup) findViewById(android.R.id.content);
            if (savedInstanceState != null) {
                line = savedInstanceState.getString(getString(R.string.bundle_train_line));
            } else {
                line = getIntent().getExtras().getString(getString(R.string.bundle_train_line));
            }

            // Init data
            initData();

            // Init toolbar
            setToolbar();

            // Google analytics
            Util.trackScreen(getApplicationContext(), getString(R.string.analytics_train_map));
        }
    }

    private void initData() {
        // Load data
        final DataHolder dataHolder = DataHolder.getInstance();
        trainData = dataHolder.getTrainData();
        markers = new ArrayList<>();
        status = new HashMap<>();
        trainListener = new TrainMapOnCameraChangeListener(getApplicationContext());
    }

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            new LoadTrainPositionTask(TrainMapActivity.this, line, trainData).execute(false, true);
            return false;
        }));
        final TrainLine trainLine = TrainLine.fromXmlString(line);
        Util.setWindowsColor(this, toolbar, trainLine);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(v -> finish());

        toolbar.setTitle(trainLine.toStringWithLine());
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
    public final void onStop() {
        super.onStop();
        centerMap = false;
    }

    @Override
    public final void onResume() {
        super.onResume();
        mapFragment.getMapAsync(googleMap -> {
            Util.setLocationOnMap(this, googleMap);
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
                            final String runNumber = marker.getSnippet();
                            new LoadTrainFollowTask(TrainMapActivity.this, view, false, trainData).execute(runNumber);
                            status.put(marker, false);
                        }
                        return view;
                    } else {
                        return null;
                    }
                }
            });

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(final Marker marker) {
                    if (!"".equals(marker.getSnippet())) {
                        final View view = views.get(marker);
                        if (!refreshingInfoWindow) {
                            selectedMarker = marker;
                            final String runNumber = marker.getSnippet();
                            final Boolean current = status.get(marker);
                            new LoadTrainFollowTask(TrainMapActivity.this, view, !current, trainData).execute(runNumber);
                            status.put(marker, !current);
                        }
                    }
                }
            });
            if (Util.isNetworkAvailable(getApplicationContext())) {
                new LoadTrainPositionTask(TrainMapActivity.this, line, trainData).execute(centerMap, true);
            } else {
                Util.showNetworkErrorMessage(layout);
            }
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        line = savedInstanceState.getString(getString(R.string.bundle_train_line));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString(getString(R.string.bundle_train_line), line);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void refreshInfoWindow() {
        if (selectedMarker == null) {
            return;
        }
        refreshingInfoWindow = true;
        selectedMarker.showInfoWindow();
        refreshingInfoWindow = false;
    }

    public void centerMapOnTrain(@NonNull final List<Train> result) {
        mapFragment.getMapAsync(googleMap -> {
            final Position position;
            final int zoom;
            if (result.size() == 1) {
                position = result.get(0).getPosition();
                zoom = 15;
            } else {
                position = Train.getBestPosition(result);
                zoom = 11;
            }
            final LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        });
    }

    public void drawTrains(@NonNull final List<Train> trains) {
        mapFragment.getMapAsync(googleMap -> {
            if (views == null) {
                views = new HashMap<>();
            } else {
                views.clear();
            }
            for (final Marker marker : markers) {
                marker.remove();
            }
            markers.clear();
            final BitmapDescriptor bitmapDescr = trainListener.getCurrentBitmapDescriptor();
            for (final Train train : trains) {
                final LatLng point = new LatLng(train.getPosition().getLatitude(), train.getPosition().getLongitude());
                final String title = "To " + train.getDestName();
                final String snippet = Integer.toString(train.getRouteNumber());

                final Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet).icon(bitmapDescr).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
                markers.add(marker);

                final View view = TrainMapActivity.this.getLayoutInflater().inflate(R.layout.marker_train, viewGroup, false);
                final TextView title2 = (TextView) view.findViewById(R.id.title);
                title2.setText(title);

                final TextView color = (TextView) view.findViewById(R.id.route_color_value);
                color.setBackgroundColor(TrainLine.fromXmlString(line).getColor());

                views.put(marker, view);
            }

            trainListener.setTrainMarkers(markers);

            googleMap.setOnCameraChangeListener(trainListener);
        });
    }

    public void drawLine(@NonNull final List<Position> positions) {
        if (drawLine) {
            mapFragment.getMapAsync(googleMap -> {
                final PolylineOptions poly = new PolylineOptions();
                poly.width(7f);
                poly.geodesic(true).color(TrainLine.fromXmlString(line).getColor());
                Stream.of(positions)
                    .map(position -> new LatLng(position.getLatitude(), position.getLongitude()))
                    .forEach(poly::add);

                googleMap.addPolyline(poly);
            });
            drawLine = false;
        }
    }
}
