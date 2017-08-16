package fr.cph.chicago.core.activity;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.adapter.SearchAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

@SuppressWarnings("WeakerAccess")
public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.search_list)
    ListView listView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.bundle_bike_stations)
    String bundleBikeStations;

    private SearchView searchView;
    private SearchAdapter searchAdapter;
    private List<BikeStation> bikeStations;

    public SearchActivity() {
        this.bikeStations = new ArrayList<>();
    }

    @Override
    protected void onCreate(final Bundle state) {
        super.onCreate(state);
        App.Companion.checkTrainData(this);
        App.Companion.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_search);
            ButterKnife.bind(this);

            setupToolbar();

            container.getForeground().setAlpha(0);

            searchAdapter = new SearchAdapter(this);
            searchAdapter.updateData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            bikeStations = getIntent().getExtras().getParcelableArrayList(bundleBikeStations);
            handleIntent(getIntent());

            listView.setAdapter(searchAdapter);

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
            // FIXME remove clearfocus if possible
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
            final ArrayList<BikeStation> bikeStations = getIntent().getExtras().getParcelableArrayList(bundleBikeStations);
            intent.putParcelableArrayListExtra(bundleBikeStations, bikeStations);
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
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupToolbar() {
        Util.INSTANCE.setWindowsColor(this, toolbar, TrainLine.NA);
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
            final BusData busData = DataHolder.INSTANCE.getBusData();
            final TrainData trainData = DataHolder.INSTANCE.getTrainData();

            final String query = intent.getStringExtra(SearchManager.QUERY).trim();

            final List<Station> foundStations = Stream.of(trainData.getAllStations().entrySet())
                .flatMap(entry -> Stream.of(entry.getValue()))
                .filter(station -> containsIgnoreCase(station.getName(), query))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            final List<BusRoute> foundBusRoutes = Stream.of(busData.getBusRoutes())
                .filter(busRoute -> containsIgnoreCase(busRoute.getId(), query) || containsIgnoreCase(busRoute.getName(), query))
                .distinct()
                .sorted(Util.INSTANCE.getBUS_STOP_COMPARATOR_NAME())
                .collect(Collectors.toList());

            final List<BikeStation> foundBikeStations = Stream.of(bikeStations)
                .filter(bikeStation -> containsIgnoreCase(bikeStation.getName(), query) || containsIgnoreCase(bikeStation.getStAddress1(), query))
                .distinct()
                .sorted(Util.INSTANCE.getBIKE_COMPARATOR_NAME())
                .collect(Collectors.toList());
            searchAdapter.updateData(foundStations, foundBusRoutes, foundBikeStations);
            searchAdapter.notifyDataSetChanged();
        }
    }
}
