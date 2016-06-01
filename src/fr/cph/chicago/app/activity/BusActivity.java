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

package fr.cph.chicago.app.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.app.App;
import fr.cph.chicago.app.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.app.listener.GoogleMapOnClickListener;
import fr.cph.chicago.app.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.app.task.LoadStationDataTask;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusActivity extends AbstractStationActivity {

    @BindView(R.id.activity_bus_stop_swipe_refresh_layout) SwipeRefreshLayout scrollView;
    @BindView(R.id.activity_favorite_star) ImageView favoritesImage;
    @BindView(R.id.activity_bus_stops) LinearLayout stopsView;
    @BindView(R.id.activity_bus_streetview_image) ImageView streetViewImage;
    @BindView(R.id.activity_bus_steetview_text) TextView streetViewText;
    @BindView(R.id.activity_map_image) ImageView mapImage;
    @BindView(R.id.activity_map_direction) ImageView directionImage;
    @BindView(R.id.favorites_container) LinearLayout favoritesImageContainer;
    @BindView(R.id.walk_container) LinearLayout walkContainer;
    @BindView(R.id.map_container) LinearLayout mapContainer;
    @BindView(R.id.activity_bus_station_value) TextView busRouteNameView2;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindString(R.string.bundle_bus_stop_id) String bundleBusStopId;
    @BindString(R.string.bundle_bus_route_id) String bundleBusRouteId;
    @BindString(R.string.bundle_bus_bound) String bundleBusBound;
    @BindString(R.string.bundle_bus_bound_title) String bundleBusBoundTitle;
    @BindString(R.string.bundle_bus_stop_name) String bundleBusStopName;
    @BindString(R.string.bundle_bus_route_name) String bundleBusRouteName;
    @BindString(R.string.bundle_bus_latitude) String bundleBusLatitude;
    @BindString(R.string.bundle_bus_longitude) String bundleBusLongitude;
    @BindString(R.string.bus_activity_no_service) String busActivityNoService;
    @BindString(R.string.analytics_bus_details) String analyticsBusDetails;

    @BindColor(R.color.grey_5) int grey_5;
    @BindColor(R.color.grey) int grey;
    @BindColor(R.color.yellowLineDark) int yellowLineDark;

    private List<BusArrival> busArrivals;
    private String busRouteId, bound, boundTitle;
    private Integer busStopId;
    private String busStopName, busRouteName;
    private double latitude, longitude;
    private boolean isFavorite;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus);
            ButterKnife.bind(this);

            if (busStopId == null || busRouteId == null || bound == null || busStopName == null || busRouteName == null || boundTitle == null) {
                final Bundle extras = getIntent().getExtras();
                busStopId = extras.getInt(bundleBusStopId);
                busRouteId = extras.getString(bundleBusRouteId);
                bound = extras.getString(bundleBusBound);
                boundTitle = extras.getString(bundleBusBoundTitle);
                busStopName = extras.getString(bundleBusStopName);
                busRouteName = extras.getString(bundleBusRouteName);
                latitude = extras.getDouble(bundleBusLatitude);
                longitude = extras.getDouble(bundleBusLongitude);
            }

            final Position position = new Position();
            position.setLatitude(latitude);
            position.setLongitude(longitude);

            isFavorite = isFavorite();

            mapImage.setColorFilter(grey_5);
            directionImage.setColorFilter(grey_5);
            favoritesImageContainer.setOnClickListener(v -> switchFavorite());

            if (isFavorite) {
                favoritesImage.setColorFilter(yellowLineDark);
            } else {
                favoritesImage.setColorFilter(grey_5);
            }
            scrollView.setOnRefreshListener(() -> new LoadStationDataTask(this, scrollView, busRouteId, busStopId).execute());
            streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(latitude, longitude));
            mapContainer.setOnClickListener(new GoogleMapOnClickListener(latitude, longitude));
            walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(latitude, longitude));

            final String title = busRouteName + " (" + boundTitle + ")";
            busRouteNameView2.setText(title);

            // Load google street picture and data
            createGoogleStreetObservable(position.getLatitude(), position.getLongitude());
            subscribeToGoogleStreet(streetViewImage, streetViewText);
            new LoadStationDataTask(this, scrollView, busRouteId, busStopId).execute();

            setToolBar();

            // Google analytics
            Util.trackScreen(getApplicationContext(), analyticsBusDetails);
        }
    }

    private void setToolBar() {
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            scrollView.setRefreshing(true);
            new LoadStationDataTask(this, scrollView, busRouteId, busStopId).execute();
            return false;
        }));
        Util.setWindowsColor(this, toolbar, TrainLine.NA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setTitle(busRouteId + " - " + busStopName);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(v -> finish());
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        busStopId = savedInstanceState.getInt(bundleBusStopId);
        busRouteId = savedInstanceState.getString(bundleBusRouteId);
        bound = savedInstanceState.getString(bundleBusBound);
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle);
        busStopName = savedInstanceState.getString(bundleBusStopName);
        busRouteName = savedInstanceState.getString(bundleBusRouteName);
        latitude = savedInstanceState.getDouble(bundleBusLatitude);
        longitude = savedInstanceState.getDouble(bundleBusLongitude);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(bundleBusStopId, busStopId);
        savedInstanceState.putString(bundleBusRouteId, busRouteId);
        savedInstanceState.putString(bundleBusBound, bound);
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle);
        savedInstanceState.putString(bundleBusStopName, busStopName);
        savedInstanceState.putString(bundleBusRouteName, busRouteName);
        savedInstanceState.putDouble(bundleBusLatitude, latitude);
        savedInstanceState.putDouble(bundleBusLongitude, longitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setBusArrivals(@NonNull final List<BusArrival> busArrivals) {
        this.busArrivals = busArrivals;
    }

    /**
     * Draw arrivals in current layout
     */
    public void drawArrivals() {
        final Map<String, TextView> tempMap = new HashMap<>();
        if (busArrivals.size() != 0) {
            Stream.of(busArrivals)
                .filter(arrival -> arrival.getRouteDirection().equals(bound) || arrival.getRouteDirection().equals(boundTitle))
                .forEach(arrival -> {
                    final String destination = arrival.getBusDestination();
                    final String arrivalText;
                    if (tempMap.containsKey(destination)) {
                        final TextView arrivalView = tempMap.get(destination);
                        if (arrival.isDly()) {
                            arrivalText = arrivalView.getText() + " Delay";
                        } else {
                            arrivalText = arrivalView.getText() + " " + arrival.getTimeLeft();
                        }
                        arrivalView.setText(arrivalText);
                    } else {
                        final TextView arrivalView = new TextView(getApplicationContext());
                        if (arrival.isDly()) {
                            arrivalText = arrival.getBusDestination() + ": Delay";
                        } else {
                            arrivalText = arrival.getBusDestination() + ": " + arrival.getTimeLeft();
                        }
                        arrivalView.setText(arrivalText);
                        arrivalView.setTextColor(grey);
                        tempMap.put(destination, arrivalView);
                    }
                });
        } else {
            final TextView arrivalView = new TextView(getApplicationContext());
            arrivalView.setTextColor(grey);
            arrivalView.setText(busActivityNoService);
            tempMap.put("", arrivalView);
        }
        stopsView.removeAllViews();
        Stream.of(tempMap.entrySet()).forEach(entry -> stopsView.addView(entry.getValue()));
    }

    @Override
    protected boolean isFavorite() {
        final List<String> favorites = Preferences.getBusFavorites(getApplicationContext(), App.PREFERENCE_FAVORITES_BUS);
        return Stream.of(favorites)
            .filter(favorite -> favorite.equals(busRouteId + "_" + busStopId + "_" + boundTitle))
            .findFirst()
            .isPresent();
    }

    /**
     * Add or remove from favorites
     */
    private void switchFavorite() {
        if (isFavorite) {
            Util.removeFromBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, scrollView);
            favoritesImage.setColorFilter(grey_5);
            isFavorite = false;
        } else {
            Util.addToBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, scrollView);
            Preferences.addBusRouteNameMapping(getApplicationContext(), String.valueOf(busStopId), busRouteName);
            Preferences.addBusStopNameMapping(getApplicationContext(), String.valueOf(busStopId), busStopName);
            favoritesImage.setColorFilter(yellowLineDark);
            isFavorite = true;
        }
    }
}
