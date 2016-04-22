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
import android.content.Context;
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

import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.listener.GoogleMapOnClickListener;
import fr.cph.chicago.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.task.DisplayGoogleStreetPictureTask;
import fr.cph.chicago.task.DivvyAsyncTask;
import fr.cph.chicago.util.Util;

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeStationActivity extends Activity {

    private static final String TAG = BikeStationActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView favoritesImage;

    private BikeStation bikeStation;
    private boolean isFavorite;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bike_station);
            bikeStation = getIntent().getExtras().getParcelable(getString(R.string.bundle_bike_station));
            if (bikeStation != null) {
                final double latitude = bikeStation.getLatitude();
                final double longitude = bikeStation.getLongitude();

                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new DivvyAsyncTask(BikeStationActivity.this, bikeStation.getId(), swipeRefreshLayout).execute();
                    }
                });

                isFavorite = isFavorite();

                final ImageView streetViewImage = (ImageView) findViewById(R.id.activity_bike_station_streetview_image);
                final TextView streetViewText = (TextView) findViewById(R.id.activity_bike_station_steetview_text);

                // Call google street api to load image
                new DisplayGoogleStreetPictureTask(this, streetViewImage, streetViewText).execute(latitude, longitude);

                final ImageView mapImage = (ImageView) findViewById(R.id.activity_map_image);
                mapImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                final ImageView directionImage = (ImageView) findViewById(R.id.activity_map_direction);
                directionImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
                final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.map_container);
                final LinearLayout walkContainer = (LinearLayout) findViewById(R.id.walk_container);
                if (isFavorite) {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
                } else {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                }
                final LinearLayout favoritesImageContainer = (LinearLayout) findViewById(R.id.favorites_container);
                favoritesImageContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        BikeStationActivity.this.switchFavorite();
                    }
                });

                final TextView bikeStationValue = (TextView) findViewById(R.id.activity_bike_station_value);
                bikeStationValue.setText(bikeStation.getStAddress1());

                streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(this, latitude, longitude));
                mapContainer.setOnClickListener(new GoogleMapOnClickListener(this, latitude, longitude));
                walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(this, latitude, longitude));

                drawData();
            }
            setToolBar();
        }
    }

    private void setToolBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                swipeRefreshLayout.setRefreshing(true);
                new DivvyAsyncTask(BikeStationActivity.this, bikeStation.getId(), swipeRefreshLayout).execute();
                return false;
            }
        }));
        Util.setWindowsColor(this, toolbar, TrainLine.NA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setTitle(bikeStation.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void drawData() {
        final Context context = App.getContext();

        final LinearLayout container = (LinearLayout) findViewById(R.id.favorites_bikes_list);
        final LinearLayout availableLayout = new LinearLayout(context);
        final LinearLayout availableBikes = new LinearLayout(context);
        final LinearLayout availableDocks = new LinearLayout(context);

        final TextView availableBike = new TextView(context);
        final TextView availableDock = new TextView(context);
        final TextView amountBike = new TextView(context);
        final TextView amountDock = new TextView(context);

        container.removeAllViews();
        container.setOrientation(LinearLayout.HORIZONTAL);
        availableLayout.setOrientation(LinearLayout.VERTICAL);
        availableBikes.setOrientation(LinearLayout.HORIZONTAL);
        availableBike.setText(getResources().getText(R.string.bike_available_bikes));
        availableBike.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableBike.setWidth(300);
        availableBikes.addView(availableBike);
        amountBike.setText(String.valueOf(bikeStation.getAvailableBikes()));
        if (bikeStation.getAvailableBikes() == 0) {
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        availableBikes.addView(amountBike);
        availableLayout.addView(availableBikes);
        availableDocks.setOrientation(LinearLayout.HORIZONTAL);
        availableDock.setText(getResources().getText(R.string.bike_available_docks));
        availableDock.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
        availableDock.setWidth(300);
        availableDocks.addView(availableDock);
        amountDock.setText(String.valueOf(bikeStation.getAvailableDocks()));
        if (bikeStation.getAvailableDocks() == 0) {
            amountDock.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            amountDock.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        availableDocks.addView(amountDock);
        availableLayout.addView(availableDocks);
        container.addView(availableLayout);
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bikeStation = savedInstanceState.getParcelable(getString(R.string.bundle_bike_station));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putParcelable(getString(R.string.bundle_bike_station), bikeStation);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    private boolean isFavorite() {
        final List<String> favorites = Preferences.getBikeFavorites(App.PREFERENCE_FAVORITES_BIKE);
        for (final String favorite : favorites) {
            if (Integer.valueOf(favorite) == bikeStation.getId()) {
                return true;
            }
        }
        return false;
    }

    public void refreshStation(@NonNull final BikeStation station) {
        this.bikeStation = station;
        drawData();
    }

    /**
     * Add/remove favorites
     */
    private void switchFavorite() {
        if (isFavorite) {
            Util.removeFromBikeFavorites(bikeStation.getId(), App.PREFERENCE_FAVORITES_BIKE, swipeRefreshLayout);
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
            isFavorite = false;
        } else {
            Util.addToBikeFavorites(bikeStation.getId(), App.PREFERENCE_FAVORITES_BIKE, swipeRefreshLayout);
            Preferences.addBikeRouteNameMapping(Integer.toString(bikeStation.getId()), bikeStation.getName());
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
            isFavorite = true;
        }
    }
}
