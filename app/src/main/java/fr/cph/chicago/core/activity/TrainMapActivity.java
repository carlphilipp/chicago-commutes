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

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter;
import fr.cph.chicago.core.listener.TrainMapOnCameraChangeListener;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.util.Util;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static fr.cph.chicago.Constants.GPS_ACCESS;
import static fr.cph.chicago.Constants.TRAINS_FOLLOW_URL;
import static fr.cph.chicago.Constants.TRAINS_LOCATION_URL;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_FOLLOW;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_LOCATION;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    @BindView(android.R.id.content)
    ViewGroup viewGroup;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.map)
    RelativeLayout layout;

    @BindString(R.string.bundle_train_line)
    String bundleTrainLine;
    @BindString(R.string.analytics_train_map)
    String analyticsTrainMap;
    @BindString(R.string.request_runnumber)
    String requestRunNumber;
    @BindString(R.string.bus_all_results)
    String busAllResults;
    @BindString(R.string.request_rt)
    String requestRt;

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    Drawable arrowBackWhite;

    private MapFragment mapFragment;
    private Marker selectedMarker;
    private Map<Marker, View> views;

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
            ButterKnife.bind(this);

            if (savedInstanceState != null) {
                line = savedInstanceState.getString(bundleTrainLine);
            } else {
                line = getIntent().getExtras().getString(bundleTrainLine);
            }

            // Init data
            initData();

            // Init toolbar
            setToolbar();

            // Google analytics
            Util.trackScreen(getApplicationContext(), analyticsTrainMap);
        }
    }

    private void initData() {
        // Load data
        trainData = DataHolder.INSTANCE.getTrainData();
        markers = new ArrayList<>();
        status = new HashMap<>();
        trainListener = new TrainMapOnCameraChangeListener(getApplicationContext());
    }

    private void setToolbar() {

        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            new LoadTrainPositionTask(line, trainData).execute(false, true);
            return false;
        }));
        final TrainLine trainLine = TrainLine.fromXmlString(line);
        Util.setWindowsColor(this, toolbar, trainLine);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setNavigationIcon(arrowBackWhite);
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
        enableMyLocationOnMapIfAllowed();
        mapFragment.getMapAsync(googleMap -> {
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(final Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(final Marker marker) {
                    if (!"".equals(marker.getSnippet())) {
                        // View can be null
                        final View view = views.get(marker);
                        if (!refreshingInfoWindow) {
                            selectedMarker = marker;
                            final String runNumber = marker.getSnippet();
                            new LoadTrainFollowTask(view, false, trainData).execute(runNumber);
                            status.put(marker, false);
                        }
                        return view;
                    } else {
                        return null;
                    }
                }
            });

            googleMap.setOnInfoWindowClickListener(marker -> {
                if (!"".equals(marker.getSnippet())) {
                    final View view = views.get(marker);
                    if (!refreshingInfoWindow) {
                        selectedMarker = marker;
                        final String runNumber = marker.getSnippet();
                        final Boolean current = status.get(marker);
                        new LoadTrainFollowTask(view, !current, trainData).execute(runNumber);
                        status.put(marker, !current);
                    }
                }
            });
            if (Util.isNetworkAvailable(getApplicationContext())) {
                new LoadTrainPositionTask(line, trainData).execute(centerMap, true);
            } else {
                Util.showNetworkErrorMessage(layout);
            }
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        line = savedInstanceState.getString(bundleTrainLine);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString(bundleTrainLine, line);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void refreshInfoWindow() {
        if (selectedMarker == null) {
            return;
        }
        refreshingInfoWindow = true;
        selectedMarker.showInfoWindow();
        refreshingInfoWindow = false;
    }

    private void centerMapOnTrain(@NonNull final List<Train> result) {
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

    private void drawTrains(@NonNull final List<Train> trains) {
        mapFragment.getMapAsync(googleMap -> {
            // TODO see if views can actually be null.
            if (views == null) {
                views = new HashMap<>();
            } else {
                views.clear();
            }
            Stream.of(markers).forEach(Marker::remove);
            markers.clear();
            final BitmapDescriptor bitmapDescr = trainListener.getCurrentBitmapDescriptor();
            Stream.of(trains).forEach(train -> {
                final LatLng point = new LatLng(train.getPosition().getLatitude(), train.getPosition().getLongitude());
                final String title = "To " + train.getDestName();
                final String snippet = Integer.toString(train.getRouteNumber());

                final Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet).icon(bitmapDescr).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
                markers.add(marker);

                final View view = getLayoutInflater().inflate(R.layout.marker_train, viewGroup, false);
                final TextView title2 = (TextView) view.findViewById(R.id.title);
                title2.setText(title);

                final TextView color = (TextView) view.findViewById(R.id.route_color_value);
                color.setBackgroundColor(TrainLine.fromXmlString(line).getColor());

                views.put(marker, view);
            });
            trainListener.setTrainMarkers(markers);

            googleMap.setOnCameraChangeListener(trainListener);
        });
    }

    private void drawLine(@NonNull final List<Position> positions) {
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

    @AfterPermissionGranted(GPS_ACCESS)
    private void enableMyLocationOnMapIfAllowed() {
        Log.e("DRP", "enableMyLocationOnMapIfAllowed");
        if (EasyPermissions.hasPermissions(getApplicationContext(), ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            setLocationOnMap();
        } else {
            EasyPermissions.requestPermissions(this, "Would you like to see your current location on the map?", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.e("DRP", "onRequestPermissionsResult");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.e("DRP", "onPermissionsGranted");
        setLocationOnMap();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.e("DRP", "onPermissionsDenied");
    }

    public void setLocationOnMap() throws SecurityException {
        mapFragment.getMapAsync(googleMap -> {
            googleMap.setMyLocationEnabled(true);
        });
    }

    private class LoadTrainFollowTask extends AsyncTask<String, Void, List<Eta>> {

        private final String TAG = LoadTrainFollowTask.class.getSimpleName();

        private final TrainData trainData;
        private final View view;
        private final boolean loadAll;

        /**
         * Constructor
         *
         * @param view    the view
         * @param loadAll a boolean to load everything
         */
        private LoadTrainFollowTask(@NonNull final View view, final boolean loadAll, @NonNull final TrainData trainData) {
            this.trainData = trainData;
            this.view = view;
            this.loadAll = loadAll;
        }

        @Override
        protected final List<Eta> doInBackground(final String... params) {
            final String runNumber = params[0];
            List<Eta> etas = new ArrayList<>();
            try {
                final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
                connectParam.put(requestRunNumber, runNumber);
                final InputStream content = CtaConnect.INSTANCE.connect(TRAIN_FOLLOW, connectParam, getApplicationContext());
                etas = XmlParser.INSTANCE.parseTrainsFollow(content, trainData);
            } catch (final ConnectException | ParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_FOLLOW_URL, 0);
            if (!loadAll && etas.size() > 7) {
                etas = etas.subList(0, 6);

                // Add a fake Eta cell to alert the user about the fact that only a part of the result is displayed
                final Eta eta = new Eta();
                eta.setDly(false);
                eta.setApp(false);
                final Date currentDate = Calendar.getInstance().getTime();
                eta.setArrivalDepartureDate(currentDate);
                eta.setPredictionDate(currentDate);
                final Station fakeStation = Station.builder().id(0).name(busAllResults).stops(Collections.emptyList()).build();
                eta.setStation(fakeStation);
                etas.add(eta);
            }
            return etas;
        }

        @Override
        protected final void onPostExecute(final List<Eta> result) {
            // View can be null
            final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
            final TextView error = (TextView) view.findViewById(R.id.error);
            if (result.size() != 0) {
                final TrainMapSnippetAdapter ada = new TrainMapSnippetAdapter(result);
                arrivals.setAdapter(ada);
                arrivals.setVisibility(ListView.VISIBLE);
                error.setVisibility(TextView.GONE);
            } else {
                arrivals.setVisibility(ListView.GONE);
                error.setVisibility(TextView.VISIBLE);
            }
            refreshInfoWindow();
        }
    }

    private class LoadTrainPositionTask extends AsyncTask<Boolean, Void, List<Train>> {

        private final String TAG = LoadTrainPositionTask.class.getSimpleName();

        private final String line;
        private TrainData trainData;

        private boolean centerMap;
        private List<Position> positions;

        private LoadTrainPositionTask(@NonNull final String line, @NonNull final TrainData trainData) {
            this.line = line;
            this.trainData = trainData;
        }

        @Override
        protected List<Train> doInBackground(final Boolean... params) {
            // Make sure that trainData is not null
            if (trainData == null) {
                trainData = DataHolder.INSTANCE.getTrainData();
            }
            centerMap = params[0];


            final List<Train> trains = getTrainData();
            positions = trainData.readPattern(getApplicationContext(), TrainLine.fromXmlString(line));
            return trains;
        }

        @Override
        protected final void onPostExecute(final List<Train> trains) {
            if (trains != null) {
                drawTrains(trains);
                drawLine(positions);
                if (trains.size() != 0) {
                    if (centerMap) {
                        centerMapOnTrain(trains);
                    }
                } else {
                    Util.showMessage(TrainMapActivity.this, R.string.message_no_train_found);
                }
            } else {
                Util.showMessage(TrainMapActivity.this, R.string.message_error_while_loading_data);
            }
        }

        private List<Train> getTrainData() {
            List<Train> trains = null;
            try {
                final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
                connectParam.put(requestRt, line);
                final InputStream content = CtaConnect.INSTANCE.connect(TRAIN_LOCATION, connectParam, getApplicationContext());
                trains = XmlParser.INSTANCE.parseTrainsLocation(content);
                Util.trackAction(TrainMapActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_LOCATION_URL, 0);
            } catch (final ConnectException | ParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return trains;
        }
    }
}
