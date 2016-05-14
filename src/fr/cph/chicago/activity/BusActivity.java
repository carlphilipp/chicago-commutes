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

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.listener.GoogleMapOnClickListener;
import fr.cph.chicago.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.task.DisplayGoogleStreetPictureTask;
import fr.cph.chicago.task.LoadStationDataTask;
import fr.cph.chicago.util.Util;

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusActivity extends Activity {

    private List<BusArrival> busArrivals;
    private String busRouteId, bound, boundTitle;
    private Integer busStopId;
    private String busStopName, busRouteName;
    private double latitude, longitude;
    private boolean isFavorite;

    private SwipeRefreshLayout scrollView;
    private ImageView favoritesImage;
    private LinearLayout stopsView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus);

            scrollView = (SwipeRefreshLayout) findViewById(R.id.activity_bus_stop_swipe_refresh_layout);

            if (busStopId == null || busRouteId == null || bound == null || busStopName == null || busRouteName == null || boundTitle == null) {
                final Bundle extras = getIntent().getExtras();
                busStopId = extras.getInt(getString(R.string.bundle_bus_stop_id));
                busRouteId = extras.getString(getString(R.string.bundle_bus_route_id));
                bound = extras.getString(getString(R.string.bundle_bus_bound));
                boundTitle = extras.getString(getString(R.string.bundle_bus_bound_title));
                busStopName = extras.getString(getString(R.string.bundle_bus_stop_name));
                busRouteName = extras.getString(getString(R.string.bundle_bus_route_name));
                latitude = extras.getDouble(getString(R.string.bundle_bus_latitude));
                longitude = extras.getDouble(getString(R.string.bundle_bus_longitude));
            }

            final Position position = new Position();
            position.setLatitude(latitude);
            position.setLongitude(longitude);

            isFavorite = setupFavorite();

            stopsView = (LinearLayout) findViewById(R.id.activity_bus_stops);
            final ImageView streetViewImage = (ImageView) findViewById(R.id.activity_bus_streetview_image);
            final TextView streetViewText = (TextView) findViewById(R.id.activity_bus_steetview_text);
            final ImageView mapImage = (ImageView) findViewById(R.id.activity_map_image);
            mapImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
            final ImageView directionImage = (ImageView) findViewById(R.id.activity_map_direction);
            directionImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
            final LinearLayout favoritesImageContainer = (LinearLayout) findViewById(R.id.favorites_container);
            favoritesImageContainer.setOnClickListener(v -> BusActivity.this.switchFavorite());
            final LinearLayout walkContainer = (LinearLayout) findViewById(R.id.walk_container);
            final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.map_container);
            favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
            if (isFavorite) {
                favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
            } else {
                favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
            }
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_bus_stop_swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(() -> new LoadStationDataTask(BusActivity.this, swipeRefreshLayout, busRouteId, busStopId).execute());
            streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(this, latitude, longitude));
            mapContainer.setOnClickListener(new GoogleMapOnClickListener(this, latitude, longitude));
            walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(this, latitude, longitude));

            final TextView busRouteNameView2 = (TextView) findViewById(R.id.activity_bus_station_value);
            final String title = busRouteName + " (" + boundTitle + ")";
            busRouteNameView2.setText(title);

            // Load google street picture and data
            new DisplayGoogleStreetPictureTask(this, streetViewImage, streetViewText).execute(position.getLatitude(), position.getLongitude());
            new LoadStationDataTask(this, swipeRefreshLayout, busRouteId, busStopId).execute();

            setToolBar();

            // Google analytics
            Util.trackScreen(getString(R.string.analytics_bus_details));
        }
    }

    private void setToolBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            swipeRefreshLayout.setRefreshing(true);
            new LoadStationDataTask(BusActivity.this, swipeRefreshLayout, busRouteId, busStopId).execute();
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
        busStopId = savedInstanceState.getInt(getString(R.string.bundle_bus_stop_id));
        busRouteId = savedInstanceState.getString(getString(R.string.bundle_bus_route_id));
        bound = savedInstanceState.getString(getString(R.string.bundle_bus_bound));
        boundTitle = savedInstanceState.getString(getString(R.string.bundle_bus_bound_title));
        busStopName = savedInstanceState.getString(getString(R.string.bundle_bus_stop_name));
        busRouteName = savedInstanceState.getString(getString(R.string.bundle_bus_route_name));
        latitude = savedInstanceState.getDouble(getString(R.string.bundle_bus_latitude));
        longitude = savedInstanceState.getDouble(getString(R.string.bundle_bus_longitude));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(getString(R.string.bundle_bus_stop_id), busStopId);
        savedInstanceState.putString(getString(R.string.bundle_bus_route_id), busRouteId);
        savedInstanceState.putString(getString(R.string.bundle_bus_bound), bound);
        savedInstanceState.putString(getString(R.string.bundle_bus_bound_title), boundTitle);
        savedInstanceState.putString(getString(R.string.bundle_bus_stop_name), busStopName);
        savedInstanceState.putString(getString(R.string.bundle_bus_route_name), busRouteName);
        savedInstanceState.putDouble(getString(R.string.bundle_bus_latitude), latitude);
        savedInstanceState.putDouble(getString(R.string.bundle_bus_longitude), longitude);
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
            for (final BusArrival arrival : busArrivals) {
                if (arrival.getRouteDirection().equals(bound) || arrival.getRouteDirection().equals(boundTitle)) {
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
                        final TextView arrivalView = new TextView(App.getContext());
                        if (arrival.isDly()) {
                            arrivalText = arrival.getBusDestination() + ": Delay";
                        } else {
                            arrivalText = arrival.getBusDestination() + ": " + arrival.getTimeLeft();
                        }
                        arrivalView.setText(arrivalText);
                        arrivalView.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
                        tempMap.put(destination, arrivalView);
                    }
                }
            }
        } else {
            final TextView arrivalView = new TextView(App.getContext());
            arrivalView.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
            arrivalView.setText(getString(R.string.bus_activity_no_service));
            tempMap.put("", arrivalView);
        }
        stopsView.removeAllViews();
        for (final Entry<String, TextView> entry : tempMap.entrySet()) {
            stopsView.addView(entry.getValue());
        }
    }

    private boolean setupFavorite() {
        final List<String> favorites = Preferences.getBusFavorites(App.PREFERENCE_FAVORITES_BUS);
        for (final String favorite : favorites) {
            if (favorite.equals(busRouteId + "_" + busStopId + "_" + boundTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add or remove from favorites
     */
    private void switchFavorite() {
        if (isFavorite) {
            Util.removeFromBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, App.PREFERENCE_FAVORITES_BUS, scrollView);
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
            isFavorite = false;
        } else {
            Util.addToBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, App.PREFERENCE_FAVORITES_BUS, scrollView);
            Preferences.addBusRouteNameMapping(String.valueOf(busStopId), busRouteName);
            Preferences.addBusStopNameMapping(String.valueOf(busStopId), busStopName);
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
            isFavorite = true;
        }
    }
}
