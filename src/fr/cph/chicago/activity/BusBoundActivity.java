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

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.task.LoadBusPatternTask;
import fr.cph.chicago.util.ObservableUtil;
import fr.cph.chicago.util.Util;

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusBoundActivity extends ListActivity {

    private static final String TAG = BusBoundActivity.class.getSimpleName();

    private MapFragment mapFragment;
    private LinearLayout layout;
    private String busRouteId;
    private String busRouteName;
    private String bound;
    private String boundTitle;
    private BusBoundAdapter busBoundAdapter;
    private List<BusStop> busStops;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus_bound);

            layout = (LinearLayout) findViewById(R.id.bellow);

            if (busRouteId == null || busRouteName == null || bound == null || boundTitle == null) {
                final Bundle extras = getIntent().getExtras();
                busRouteId = extras.getString(getString(R.string.bundle_bus_route_id));
                busRouteName = extras.getString(getString(R.string.bundle_bus_route_name));
                bound = extras.getString(getString(R.string.bundle_bus_bound));
                boundTitle = extras.getString(getString(R.string.bundle_bus_bound_title));
            }
            busBoundAdapter = new BusBoundAdapter();
            setListAdapter(busBoundAdapter);
            getListView().setOnItemClickListener((adapterView, view, position, id) -> {
                final BusStop busStop = (BusStop) busBoundAdapter.getItem(position);
                final Intent intent = new Intent(App.getContext(), BusActivity.class);

                final Bundle extras = new Bundle();
                extras.putInt(getString(R.string.bundle_bus_stop_id), busStop.getId());
                extras.putString(getString(R.string.bundle_bus_stop_name), busStop.getName());
                extras.putString(getString(R.string.bundle_bus_route_id), busRouteId);
                extras.putString(getString(R.string.bundle_bus_route_name), busRouteName);
                extras.putString(getString(R.string.bundle_bus_bound), bound);
                extras.putString(getString(R.string.bundle_bus_bound_title), boundTitle);
                extras.putDouble(getString(R.string.bundle_bus_latitude), busStop.getPosition().getLatitude());
                extras.putDouble(getString(R.string.bundle_bus_longitude), busStop.getPosition().getLongitude());

                intent.putExtras(extras);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });

            final EditText filter = (EditText) findViewById(R.id.bus_filter);
            filter.addTextChangedListener(new TextWatcher() {
                private List<BusStop> busStopsFiltered;

                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                    busStopsFiltered = new ArrayList<>();
                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    if (busStops != null) {
                        for (final BusStop busStop : busStops) {
                            if (StringUtils.containsIgnoreCase(busStop.getName(), s)) {
                                busStopsFiltered.add(busStop);
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(final Editable s) {
                    busBoundAdapter.update(busStopsFiltered);
                    busBoundAdapter.notifyDataSetChanged();
                }
            });

            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            Util.setWindowsColor(this, toolbar, TrainLine.NA);
            toolbar.setTitle(busRouteId + " - " + boundTitle);

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setOnClickListener(v -> finish());

            ObservableUtil.createBusStopBoundObservable(busRouteId, bound)
                .subscribe(onNext -> {
                        BusBoundActivity.this.setBusStops(onNext);
                        busBoundAdapter.update(onNext);
                        busBoundAdapter.notifyDataSetChanged();
                    },
                    onError -> {
                        Log.e(TAG, onError.getMessage(), onError);
                        Util.showOopsSomethingWentWrong(BusBoundActivity.this.getListView());
                    }
                );

            Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_stop, 0);

            // Preventing keyboard from moving background when showing up
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (mapFragment == null) {
            final android.app.FragmentManager fm = getFragmentManager();
            final GoogleMapOptions options = new GoogleMapOptions();
            final CameraPosition camera = new CameraPosition(Util.CHICAGO, 7, 0, 0);
            options.camera(camera);
            mapFragment = MapFragment.newInstance(options);
            mapFragment.setRetainInstance(true);
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        mapFragment.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            if (Util.isNetworkAvailable()) {
                new LoadBusPatternTask(BusBoundActivity.this, mapFragment, busRouteId, boundTitle, false).execute();
            } else {
                Util.showNetworkErrorMessage(layout);
            }
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name));
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound));
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId);
        savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName);
        savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound);
        savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setBusStops(@NonNull final List<BusStop> busStops) {
        this.busStops = busStops;
    }

    public void drawPattern(@NonNull final BusPattern pattern) {
        mapFragment.getMapAsync(googleMap -> {
            final List<Marker> markers = new ArrayList<>();
            final PolylineOptions poly = new PolylineOptions();
            poly.geodesic(true).color(Color.BLACK);
            poly.width(7f);
            Marker marker;
            for (final PatternPoint patternPoint : pattern.getPoints()) {
                final LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
                poly.add(point);
                marker = googleMap.addMarker(new MarkerOptions().position(point).title(patternPoint.getStopName()).snippet(Integer.toString(patternPoint.getSequence())));
                markers.add(marker);
                marker.setVisible(false);
            }
            googleMap.addPolyline(poly);

            googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
                private float currentZoom = -1;

                @Override
                public void onCameraChange(final CameraPosition pos) {
                    if (pos.zoom != currentZoom) {
                        currentZoom = pos.zoom;
                        if (currentZoom >= 14) {
                            for (final Marker marker : markers) {
                                marker.setVisible(true);
                            }
                        } else {
                            for (final Marker marker : markers) {
                                marker.setVisible(false);
                            }
                        }
                    }
                }
            });
        });
    }
}
