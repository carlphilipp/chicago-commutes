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

package fr.cph.chicago.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.fragment.GoogleMapAbility;
import fr.cph.chicago.task.BusBoundAsyncTask;
import fr.cph.chicago.task.LoadBusPatternTask;
import fr.cph.chicago.util.Util;

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusBoundActivity extends ListActivity implements GoogleMapAbility {

    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private String busRouteId;
    private String busRouteName;
    private String bound;
    private String boundTitle;
    private BusBoundAdapter busBoundAdapter;
    private List<BusStop> busStops;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChicagoTracker.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus_bound);

            if (busRouteId == null && busRouteName == null && bound == null && boundTitle == null) {
                busRouteId = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_id));
                busRouteName = getIntent().getExtras().getString(getString(R.string.bundle_bus_route_name));
                bound = getIntent().getExtras().getString(getString(R.string.bundle_bus_bound));
                boundTitle = getIntent().getExtras().getString(getString(R.string.bundle_bus_bound_title));
            }
            busBoundAdapter = new BusBoundAdapter(busRouteId);
            setListAdapter(busBoundAdapter);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                    final BusStop busStop = (BusStop) busBoundAdapter.getItem(position);
                    final Intent intent = new Intent(ChicagoTracker.getContext(), BusActivity.class);

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
                }
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
            Util.setToolbarColor(this, toolbar, TrainLine.NA);
            toolbar.setTitle(busRouteName + " - " + boundTitle);

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            new BusBoundAsyncTask(this, busRouteId, bound, busBoundAdapter).execute();

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
    public final void onStop() {
        super.onStop();
        googleMap = null;
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (googleMap == null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    BusBoundActivity.this.googleMap = googleMap;
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(false);
                    if (Util.isNetworkAvailable()) {
                        new LoadBusPatternTask(BusBoundActivity.this, mapFragment, busRouteId, boundTitle).execute();
                    } else {
                        Toast.makeText(BusBoundActivity.this, "No network connection detected!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

    public void setBusStops(final List<BusStop> busStops) {
        this.busStops = busStops;
    }

    @Override
    public void setGoogleMap(final GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void drawPattern(final BusPattern pattern) {
        if (googleMap != null) {
            final List<Marker> markers = new ArrayList<>();
            final PolylineOptions poly = new PolylineOptions();
            poly.geodesic(true).color(Color.BLACK);
            poly.width(7f);
            Marker marker;
            for (final PatternPoint patternPoint : pattern.getPoints()) {
                final LatLng point = new LatLng(patternPoint.getPosition().getLatitude(), patternPoint.getPosition().getLongitude());
                poly.add(point);
                //if (patternPoint.getStopId() != null) {
                marker = googleMap.addMarker(new MarkerOptions().position(point).title(patternPoint.getStopName()).snippet(String.valueOf(patternPoint.getSequence())));
                markers.add(marker);
                marker.setVisible(false);
                //}
            }
            googleMap.addPolyline(poly);

            googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
                private float currentZoom = -1;

                @Override
                public void onCameraChange(final CameraPosition pos) {
                    if (pos.zoom != currentZoom) {
                        currentZoom = pos.zoom;
                        if (currentZoom >= 14) {
                            for (Marker marker : markers) {
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
        }
    }
}
