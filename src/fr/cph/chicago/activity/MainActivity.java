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

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.fragment.BikeFragment;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.web.FavoritesResult;
import rx.Observable;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SELECTED_ID = "SELECTED_ID";
    private static final int POSITION_BUS = 2;

    private int currentPosition;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem menuItem;

    private FavoritesFragment favoritesFragment;
    private TrainFragment trainFragment;
    private BusFragment busFragment;
    private BikeFragment bikeFragment;
    private NearbyFragment nearbyFragment;

    private String title;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isFinishing()) {
            if (savedInstanceState != null) {
                App.reloadData();
            }
            setContentView(R.layout.activity_main);

            ObservableUtil.createOnFirstLoadObservable().subscribe(
                onNext -> {
                    final DataHolder dataHolder = DataHolder.getInstance();
                    dataHolder.getBusData().setBusRoutes(onNext.getBusRoutes());
                    MainActivity.this.refresh(dataHolder.getBusData(), onNext.getBikeStations());
                    if (onNext.isBikeStationsError() || onNext.isBusRoutesError()) {
                        Util.showOopsSomethingWentWrong(drawerLayout);
                    }
                },
                onError -> Util.showOopsSomethingWentWrong(drawerLayout),
                () -> {
                    Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_routes, 0);
                    Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
                }
            );

            App.container = (FrameLayout) findViewById(R.id.container);
            App.container.getForeground().setAlpha(0);

            initView();
            setToolbar();

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            currentPosition = savedInstanceState == null ? R.id.navigation_favorites : savedInstanceState.getInt(SELECTED_ID);
            itemSelection(currentPosition);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPosition == R.id.navigation_favorites) {
            finish();
        } else {
            this.onNavigationItemSelected(menuItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBarTitle(this.title);
    }

    private void initView() {
        final NavigationView drawer = (NavigationView) findViewById(R.id.main_drawer);
        drawer.setNavigationItemSelectedListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuItem = drawer.getMenu().getItem(0);
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (getString(R.string.nearby).equals(toolbar.getTitle())) {
                nearbyFragment.reloadData();
            } else {
                // Favorite fragment
                favoritesFragment.startRefreshing();

                Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_arrival, 0);
                Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.url_train_arrivals, 0);
                Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
                Util.trackAction(MainActivity.this, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_fav, 0);

                if (Util.isNetworkAvailable()) {
                    final Observable<FavoritesResult> zipped = ObservableUtil.createAllDataObservable();
                    zipped.subscribe(favoritesResult -> MainActivity.this.favoritesFragment.reloadData(favoritesResult),
                        onError -> {
                            Log.e(TAG, onError.getMessage(), onError);
                            MainActivity.this.favoritesFragment.displayError(R.string.message_something_went_wrong);
                        }
                    );
                } else {
                    MainActivity.this.favoritesFragment.displayError(R.string.message_network_error);
                }
            }
            return true;
        });
        toolbar.inflateMenu(R.menu.main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.primaryColorDarker));
        }
    }

    public void refresh(@NonNull final BusData busData, @NonNull final List<BikeStation> bikeStations) {
        // Put data into data holder
        final DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.setBusData(busData);

        getIntent().putParcelableArrayListExtra(getString(R.string.bundle_bike_stations), (ArrayList<BikeStation>) bikeStations);
        onNewIntent(getIntent());
        if (favoritesFragment != null) {
            favoritesFragment.setBikeStations(bikeStations);
        }
        if (bikeFragment != null) {
            bikeFragment.setBikeStations(bikeStations);
        }
        if (currentPosition == POSITION_BUS && busFragment != null) {
            busFragment.update();
        }
    }

    private void setBarTitle(@NonNull final String title) {
        this.title = title;
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    private void itemSelection(final int position) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        currentPosition = position;
        switch (position) {
            case R.id.navigation_favorites:
                setBarTitle(getString(R.string.favorites));
                if (favoritesFragment == null) {
                    favoritesFragment = FavoritesFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, favoritesFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                showActionBarMenu();
                break;
            case R.id.navigation_train:
                setBarTitle(getString(R.string.train));
                if (trainFragment == null) {
                    trainFragment = TrainFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, trainFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                hideActionBarMenu();
                break;
            case R.id.navigation_bus:
                setBarTitle(getString(R.string.bus));
                if (busFragment == null) {
                    busFragment = BusFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, busFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                hideActionBarMenu();
                break;
            case R.id.navigation_bike:
                setBarTitle(getString(R.string.divvy));
                if (bikeFragment == null) {
                    bikeFragment = BikeFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, bikeFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                hideActionBarMenu();
                break;
            case R.id.navigation_nearby:
                setBarTitle(getString(R.string.nearby));
                if (nearbyFragment == null) {
                    nearbyFragment = NearbyFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                showActionBarMenu();
                break;
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        menuItem.setChecked(true);
        currentPosition = menuItem.getItemId();
        itemSelection(currentPosition);
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SELECTED_ID, currentPosition);
        savedInstanceState.putString(getString(R.string.bundle_title), title);
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        title = savedInstanceState.getString(getString(R.string.bundle_title));
        currentPosition = savedInstanceState.getInt(SELECTED_ID);
    }

    private void hideActionBarMenu() {
        if (toolbar.getMenu().getItem(0).isVisible()) {
            showHideActionBarMenu(false);
        }
    }

    private void showActionBarMenu() {
        if (!toolbar.getMenu().getItem(0).isVisible()) {
            showHideActionBarMenu(true);
        }
    }

    private void showHideActionBarMenu(final boolean bool) {
        toolbar.getMenu().getItem(0).setVisible(bool);
    }
}
