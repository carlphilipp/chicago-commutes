package fr.cph.chicago.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.SearchAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private SearchAdapter searchAdapter;
    private List<BikeStation> bikeStations;

    @Override
    protected void onCreate(final Bundle state) {
        super.onCreate(state);
        ChicagoTracker.checkTrainData(this);
        ChicagoTracker.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_search);
            setupToolbar();
            if (Util.isNetworkAvailable()) {
                final FrameLayout container = (FrameLayout) findViewById(R.id.container);
                container.getForeground().setAlpha(0);

                searchAdapter = new SearchAdapter(this);
                searchAdapter.updateData(new ArrayList<Station>(), new ArrayList<BusRoute>(), new ArrayList<BikeStation>());
                bikeStations = getIntent().getExtras().getParcelableArrayList(getString(R.string.bundle_bike_stations));
                handleIntent(getIntent());

                final ListView listView = (ListView) findViewById(R.id.search_list);
                listView.setAdapter(searchAdapter);
            } else {
                Util.showSettingsAlert(this);
            }

            // Associate searchable configuration with the SearchView
            final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = new SearchView(getSupportActionBarNotNull().getThemedContext());
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setIconifiedByDefault(false);
            searchView.setIconified(false);
            searchView.setMaxWidth(1000);
            searchView.setFocusable(true);
            searchView.setFocusableInTouchMode(true);
            searchView.clearFocus();
            searchView.requestFocus();
            searchView.requestFocusFromTouch();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuItem searchItem = menu.add(android.R.string.search_go);
        searchItem.setIcon(R.drawable.ic_search_white_24dp);
        MenuItemCompat.setActionView(searchItem, searchView);
        MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchItem.expandActionView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        // Hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    @Override
    public void startActivity(final Intent intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final ArrayList<BikeStation> bikeStations = getIntent().getExtras().getParcelableArrayList(getString(R.string.bundle_bike_stations));
            intent.putParcelableArrayListExtra(getString(R.string.bundle_bike_stations), bikeStations);
        }
        super.startActivity(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Util.setWindowsColor(this, toolbar, TrainLine.NA);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBarNotNull();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @NonNull
    private ActionBar getSupportActionBarNotNull() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new RuntimeException();
        }
        return actionBar;
    }

    private void handleIntent(@NonNull final Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final BusData busData = DataHolder.getInstance().getBusData();
            final TrainData trainData = DataHolder.getInstance().getTrainData();

            final String query = intent.getStringExtra(SearchManager.QUERY).trim();

            final List<Station> foundStations = new ArrayList<>();
            final List<BusRoute> foundBusRoutes = new ArrayList<>();
            final List<BikeStation> foundBikeStations = new ArrayList<>();

            for (final Entry<TrainLine, List<Station>> e : trainData.getAllStations().entrySet()) {
                for (final Station station : e.getValue()) {
                    if (containsIgnoreCase(station.getName(), query)) {
                        if (!foundStations.contains(station)) {
                            foundStations.add(station);
                        }
                    }
                }
            }

            for (final BusRoute busRoute : busData.getRoutes()) {
                if (containsIgnoreCase(busRoute.getId(), query) || containsIgnoreCase(busRoute.getName(), query)) {
                    if (!foundBusRoutes.contains(busRoute)) {
                        foundBusRoutes.add(busRoute);
                    }
                }
            }

            if (bikeStations != null) {
                for (final BikeStation bikeStation : bikeStations) {
                    if (containsIgnoreCase(bikeStation.getName(), query) || containsIgnoreCase(bikeStation.getStAddress1(), query)) {
                        if (!foundBikeStations.contains(bikeStation)) {
                            foundBikeStations.add(bikeStation);
                        }
                    }
                }
            }
            searchAdapter.updateData(foundStations, foundBusRoutes, foundBikeStations);
            searchAdapter.notifyDataSetChanged();
        }
    }
}
