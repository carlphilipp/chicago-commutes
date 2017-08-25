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

package fr.cph.chicago.core.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.marker.RefreshTrainMarkers;
import fr.cph.chicago.rx.ObservableUtil;
import fr.cph.chicago.service.TrainService;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static fr.cph.chicago.Constants.TRAINS_FOLLOW_URL;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class TrainMapActivity extends AbstractMapActivity {

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

    private final TrainService trainService;
    private final ObservableUtil observableUtil;

    private Map<Marker, View> views;
    private String line;
    private Map<Marker, Boolean> status;
    private List<Marker> markers;
    private RefreshTrainMarkers refreshTrainMarkers;

    private boolean centerMap = true;
    private boolean drawLine = true;

    public TrainMapActivity() {
        this.views = new HashMap<>();
        trainService = TrainService.INSTANCE;
        observableUtil = ObservableUtil.INSTANCE;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.Companion.checkTrainData(this);
        if (!this.isFinishing()) {
            MapsInitializer.initialize(getApplicationContext());
            setContentView(R.layout.activity_map);
            ButterKnife.bind(this);

            line = savedInstanceState != null
                ? savedInstanceState.getString(bundleTrainLine)
                : getIntent().getExtras().getString(bundleTrainLine);

            // Init data
            initData();

            // Init toolbar
            setToolbar();

            // Google analytics
            util.trackScreen(analyticsTrainMap);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        markers = new ArrayList<>();
        status = new HashMap<>();
        refreshTrainMarkers = new RefreshTrainMarkers();
    }

    @Override
    protected void setToolbar() {
        super.setToolbar();
        toolbar.setOnMenuItemClickListener((item -> {
            centerMap = false;
            loadActivityData();
            return false;
        }));

        final TrainLine trainLine = TrainLine.Companion.fromXmlString(line);
        util.setWindowsColor(this, toolbar, trainLine);
        toolbar.setTitle(trainLine.toStringWithLine());
    }

    @Override
    public final void onStop() {
        super.onStop();
        centerMap = false;
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

    private void centerMapOnTrain(@NonNull final List<Train> result) {
        final Position position;
        final int zoom;
        if (result.size() == 1) {
            position = result.get(0).getPosition();
            zoom = 15;
        } else {
            position = Train.Companion.getBestPosition(result);
            zoom = 11;
        }
        centerMapOn(position.getLatitude(), position.getLongitude(), zoom);
    }

    private void drawTrains(@NonNull final List<Train> trains) {
        // TODO see if views can actually be null.
        if (views == null) {
            views = new HashMap<>();
        } else {
            views.clear();
        }
        cleanAllMarkers();
        final BitmapDescriptor bitmapDescr = refreshTrainMarkers.getCurrentDescriptor();
        Stream.of(trains).forEach(train -> {
            final LatLng point = new LatLng(train.getPosition().getLatitude(), train.getPosition().getLongitude());
            final String title = "To " + train.getDestName();
            final String snippet = Integer.toString(train.getRouteNumber());

            final Marker marker = getGoogleMap().addMarker(new MarkerOptions().position(point).title(title).snippet(snippet).icon(bitmapDescr).anchor(0.5f, 0.5f).rotation(train.getHeading()).flat(true));
            markers.add(marker);

            final View view = getLayoutInflater().inflate(R.layout.marker, viewGroup, false);
            final TextView title2 = view.findViewById(R.id.title);
            title2.setText(title);

            views.put(marker, view);
        });
    }

    private void cleanAllMarkers() {
        Stream.of(markers).forEach(Marker::remove);
        markers.clear();
    }

    private void drawLine(@NonNull final List<Position> positions) {
        final PolylineOptions poly = new PolylineOptions();
        poly.width(((App) getApplication()).getLineWidth());
        poly.geodesic(true).color(TrainLine.Companion.fromXmlString(line).getColor());
        Stream.of(positions)
            .map(position -> new LatLng(position.getLatitude(), position.getLongitude()))
            .forEach(poly::add);

        getGoogleMap().addPolyline(poly);
        drawLine = false;
    }

    @Override
    public void onCameraIdle() {
        refreshTrainMarkers.refresh(getGoogleMap().getCameraPosition(), markers);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);
        getGoogleMap().setInfoWindowAdapter(new InfoWindowAdapter() {
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
                        setSelectedMarker(marker);
                        final String runNumber = marker.getSnippet();
                        new LoadTrainFollowTask(view, false).execute(runNumber);
                        status.put(marker, false);
                    }
                    return view;
                } else {
                    return null;
                }
            }
        });
        getGoogleMap().setOnInfoWindowClickListener(marker -> {
            if (!"".equals(marker.getSnippet())) {
                final View view = views.get(marker);
                if (!refreshingInfoWindow) {
                    setSelectedMarker(marker);
                    final String runNumber = marker.getSnippet();
                    final Boolean current = status.get(marker);
                    new LoadTrainFollowTask(view, !current).execute(runNumber);
                    status.put(marker, !current);
                }
            }
        });
        loadActivityData();
    }

    private void loadActivityData() {
        if (util.isNetworkAvailable()) {
            // Load train location
            final Observable<List<Train>> trainsObservable = observableUtil.createTrainLocationObservable(line);
            // Load pattern from local file
            final Observable<List<Position>> positionsObservable = observableUtil.createTrainPatternObservable(line);

            if (drawLine) {
                Observable.zip(trainsObservable, positionsObservable, (trains, positions) -> {
                    if (trains != null) {
                        drawTrains(trains);
                        drawLine(positions);
                        if (trains.size() != 0) {
                            if (centerMap) {
                                centerMapOnTrain(trains);
                            }
                        } else {
                            util.showMessage(TrainMapActivity.this, R.string.message_no_train_found);
                        }
                    } else {
                        util.showMessage(TrainMapActivity.this, R.string.message_error_while_loading_data);
                    }
                    return new Object();
                }).subscribe();
            } else {
                trainsObservable.subscribe(trains -> {
                    if (trains != null) {
                        drawTrains(trains);
                        if (trains.size() == 0) {
                            util.showMessage(TrainMapActivity.this, R.string.message_no_train_found);
                        }
                    } else {
                        util.showMessage(TrainMapActivity.this, R.string.message_error_while_loading_data);
                    }
                });
            }
        } else {
            util.showNetworkErrorMessage(layout);
        }
    }

    private class LoadTrainFollowTask extends AsyncTask<String, Void, List<Eta>> {

        private final View view;
        private final boolean loadAll;

        /**
         * Constructor
         *
         * @param view    the view
         * @param loadAll a boolean to load everything
         */
        private LoadTrainFollowTask(@NonNull final View view, final boolean loadAll) {
            this.view = view;
            this.loadAll = loadAll;
        }

        @Override
        protected final List<Eta> doInBackground(final String... params) {
            final String runNumber = params[0];
            List<Eta> etas = trainService.loadTrainEta(runNumber);
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_FOLLOW_URL);
            if (!loadAll && etas.size() > 7) {
                etas = etas.subList(0, 6);
                final Date currentDate = Calendar.getInstance().getTime();
                final Station fakeStation = new Station(0, busAllResults, Collections.emptyList());
                // Add a fake Eta cell to alert the user about the fact that only a part of the result is displayed
                final Eta eta = Eta.Companion.buildFakeEtaWith(fakeStation, currentDate, currentDate, false, false);
                etas.add(eta);
            }
            return etas;
        }

        @Override
        protected final void onPostExecute(final List<Eta> result) {
            // View can be null
            final ListView arrivals = view.findViewById(R.id.arrivals);
            final TextView error = view.findViewById(R.id.error);
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
}
