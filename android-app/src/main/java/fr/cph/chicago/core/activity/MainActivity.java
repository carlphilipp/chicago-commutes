/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
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
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.fragment.BikeFragment;
import fr.cph.chicago.core.fragment.BusFragment;
import fr.cph.chicago.core.fragment.CtaMapFragment;
import fr.cph.chicago.core.fragment.FavoritesFragment;
import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.core.fragment.SettingsFragment;
import fr.cph.chicago.core.fragment.TrainFragment;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.BUSES_ROUTES_URL;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SELECTED_ID = "SELECTED_ID";
    private static final int POSITION_BUS = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.container)
    FrameLayout frameLayout;
    @BindView(R.id.main_drawer)
    NavigationView drawer;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindString(R.string.bundle_bike_stations)
    String bundleBikeStations;
    @BindString(R.string.bundle_title)
    String bundleTitle;
    @BindString(R.string.favorites)
    String favorites;
    @BindString(R.string.train)
    String train;
    @BindString(R.string.bus)
    String bus;
    @BindString(R.string.divvy)
    String divvy;
    @BindString(R.string.nearby)
    String nearby;
    @BindString(R.string.cta_map)
    String ctaMap;
    @BindString(R.string.settings)
    String settings;

    @BindColor(R.color.primaryColorDarker)
    int primaryColorDarker;

    private int currentPosition;

    private ActionBarDrawerToggle drawerToggle;
    private MenuItem menuItem;

    private FavoritesFragment favoritesFragment;
    private TrainFragment trainFragment;
    private BusFragment busFragment;
    private BikeFragment bikeFragment;
    private NearbyFragment nearbyFragment;
    private CtaMapFragment ctaMapFragment;
    private SettingsFragment settingsFragment;

    private String title;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isFinishing()) {
            if (savedInstanceState != null) {
                reloadData();
            }
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);

            loadFirstData();

            frameLayout.getForeground().setAlpha(0);

            initView();
            setToolbar();

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            currentPosition = savedInstanceState == null ? R.id.navigation_favorites : savedInstanceState.getInt(SELECTED_ID);
            itemSelection(currentPosition);

            checkForErrorInBundle();
        }
    }

    private void checkForErrorInBundle() {
        final boolean isTrainError = getIntent().getBooleanExtra(getString(R.string.bundle_train_error), false);
        final boolean isBusError = getIntent().getBooleanExtra(getString(R.string.bundle_bus_error), false);
        // FIXME The snackbar does not move up the search button
        if (isTrainError && isBusError) {
            Util.showSnackBar(this, R.string.message_something_went_wrong);
        } else {
            if (isTrainError) {
                Util.showSnackBar(this, R.string.message_error_train_favorites);
            } else if (isBusError) {
                Util.showSnackBar(this, R.string.message_error_bus_favorites);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPosition == R.id.navigation_favorites) {
            finish();
        } else {
            onNavigationItemSelected(menuItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBarTitle(title);
    }

    private void initView() {
        drawer.setNavigationItemSelectedListener(this);
        menuItem = drawer.getMenu().getItem(0);
    }

    private void setToolbar() {
        toolbar.setOnMenuItemClickListener(item -> {
/*            if (nearby.equals(toolbar.getTitle())) {
                nearbyFragment.reloadData();
            } else {*/
                // Favorite fragment
                favoritesFragment.startRefreshing();

                Util.trackAction(getApplicationContext(), R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
                Util.trackAction(getApplicationContext(), R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
                Util.trackAction(getApplicationContext(), R.string.analytics_category_req, R.string.analytics_action_get_divvy, getApplicationContext().getString(R.string.analytics_action_get_divvy_all));
                Util.trackAction(getApplicationContext(), R.string.analytics_category_ui, R.string.analytics_action_press, getApplicationContext().getString(R.string.analytics_action_refresh_fav));

                if (Util.isNetworkAvailable(getApplicationContext())) {
                    final DataHolder dataHolder = DataHolder.INSTANCE;
                    if (dataHolder.getBusData() == null
                        || dataHolder.getBusData().getBusRoutes() == null
                        || dataHolder.getBusData().getBusRoutes().size() == 0
                        || getIntent().getParcelableArrayListExtra(bundleBikeStations) == null
                        || getIntent().getParcelableArrayListExtra(bundleBikeStations).size() == 0) {
                        loadFirstData();
                    }
                    final Observable<FavoritesDTO> zipped = ObservableUtil.createAllDataObservable(getApplicationContext());
                    zipped.subscribe(
                        favoritesResult -> favoritesFragment.reloadData(favoritesResult),
                        onError -> {
                            Log.e(TAG, onError.getMessage(), onError);
                            favoritesFragment.displayError(R.string.message_something_went_wrong);
                        }
                    );
                } else {
                    favoritesFragment.displayError(R.string.message_network_error);
                }
            //}
            return true;
        });
        toolbar.inflateMenu(R.menu.main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
            getWindow().setNavigationBarColor(primaryColorDarker);
        }
    }

    public void loadFirstData() {
        ObservableUtil.createOnFirstLoadObservable().subscribe(
            onNext -> {
                final DataHolder dataHolder = DataHolder.INSTANCE;
                dataHolder.getBusData().setBusRoutes(onNext.getBusRoutes());
                refreshFirstLoadData(dataHolder.getBusData(), onNext.getBikeStations());
                if (onNext.isBikeStationsError() || onNext.isBusRoutesError()) {
                    Util.showSnackBar(this, R.string.message_something_went_wrong);
                }
            },
            onError -> Util.showSnackBar(this, R.string.message_something_went_wrong),
            () -> {
                Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ROUTES_URL);
                Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_divvy, getApplicationContext().getString(R.string.analytics_action_get_divvy_all));
            }
        );
    }

    private void refreshFirstLoadData(@NonNull final BusData busData, @NonNull final List<BikeStation> bikeStations) {
        // Put data into data holder
        final DataHolder dataHolder = DataHolder.INSTANCE;
        dataHolder.setBusData(busData);

        getIntent().putParcelableArrayListExtra(bundleBikeStations, (ArrayList<BikeStation>) bikeStations);
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

    private void reloadData() {
        final DataHolder dataHolder = DataHolder.INSTANCE;
        final TrainData trainData = TrainData.INSTANCE;
        if (trainData.isStationNull() || trainData.isStopsNull()) {
            trainData.read(getApplicationContext());
            dataHolder.setTrainData(trainData);
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
                setBarTitle(favorites);
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
                setBarTitle(train);
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
                setBarTitle(bus);
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
                setBarTitle(divvy);
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
                setBarTitle(nearby);
                if (nearbyFragment == null) {
                    nearbyFragment = NearbyFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    Observable.create(
                        subscriber -> {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            subscriber.onNext(new Object());
                            subscriber.onComplete();
                        })
                        .delay(320, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> Log.e(TAG, throwable.getMessage(), throwable))
                        .subscribe(o -> fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commit());
                }
                hideActionBarMenu();
                break;
            case R.id.navigation_cta_map:
                setBarTitle(ctaMap);
                if (ctaMapFragment == null) {
                    ctaMapFragment = CtaMapFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, ctaMapFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                hideActionBarMenu();
                break;
            case R.id.rate_this_app:
                Util.rateThisApp(this);
                break;
            case R.id.settings:
                setBarTitle(settings);
                if (settingsFragment == null) {
                    settingsFragment = SettingsFragment.newInstance(position + 1);
                }
                if (!this.isFinishing()) {
                    fragmentManager.beginTransaction().replace(R.id.container, settingsFragment).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                hideActionBarMenu();
                break;
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        menuItem.setChecked(true);
        currentPosition = menuItem.getItemId();
        itemSelection(currentPosition);
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SELECTED_ID, currentPosition);
        savedInstanceState.putString(bundleTitle, title);
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        title = savedInstanceState.getString(bundleTitle);
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
