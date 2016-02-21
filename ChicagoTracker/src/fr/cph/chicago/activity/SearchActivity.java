package fr.cph.chicago.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

	private static final String TAG = "SearchActivity";

	private MenuItem searchItem;

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

			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			Util.setToolbarColor(this, toolbar, TrainLine.NA);
			setSupportActionBar(toolbar);
			//getSupportActionBar().setLogo(R.drawable.ic_arrow_back_white_24dp);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);


			if (Util.isNetworkAvailable()) {
				FrameLayout container = (FrameLayout) findViewById(R.id.container);
				ListView listView = (ListView) findViewById(R.id.search_list);
				container.getForeground().setAlpha(0);
				searchAdapter = new SearchAdapter(this, container);
				DataHolder.getInstance().getTrainData();
				searchAdapter.updateData(new ArrayList<Station>(), new ArrayList<BusRoute>(), new ArrayList<BikeStation>());
				// FIXME possible bug without that.
				bikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
				//bikeStations = new ArrayList<>();
				handleIntent(getIntent());
				listView.setAdapter(searchAdapter);
			} else {
				Toast.makeText(ChicagoTracker.getAppContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
			}

			// Associate searchable configuration with the SearchView
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

			searchView = new SearchView(getSupportActionBar().getThemedContext());
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



			SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView
					.findViewById(android.support.v7.appcompat.R.id.search_src_text);

			// Collapse the search menu when the user hits the back key
			searchAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus)
						showSearch(false);
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		searchItem = menu.add(android.R.string.search_go);
		searchItem.setIcon(R.drawable.ic_search_white_24dp);
		MenuItemCompat.setActionView(searchItem, searchView);
		MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		searchItem.expandActionView();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void startActivity(final Intent intent) {
		// check if search intent
		Log.e(TAG, "Start activity with action: " + intent.getAction());
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			ArrayList<BikeStation> bikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
			intent.putParcelableArrayListExtra("bikeStations", bikeStations);
//			intent.putParcelableArrayListExtra("bikeStations", new ArrayList<Parcelable>());
		}
		super.startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showSearch(false);
		Bundle extras = intent.getExtras();
		String userQuery = String.valueOf(extras.get(SearchManager.USER_QUERY));
		String query = String.valueOf(extras.get(SearchManager.QUERY));

		Toast.makeText(this, "query: " + query + " user_query: " + userQuery, Toast.LENGTH_SHORT).show();
		handleIntent(intent);
	}

	protected void showSearch(boolean visible) {
		if (visible)
			MenuItemCompat.expandActionView(searchItem);
		else
			MenuItemCompat.collapseActionView(searchItem);
	}

	@Override
	public boolean onSearchRequested() {
		showSearch(true);
		// dont show the built-in search dialog
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();
			TrainData trainData = dataHolder.getTrainData();

			List<Station> foundStations = new ArrayList<>();

			for (Map.Entry<TrainLine, List<Station>> e : trainData.getAllStations().entrySet()) {
				for (Station station : e.getValue()) {
					boolean res = StringUtils.containsIgnoreCase(station.getName(), query.trim());
					if (res) {
						if (!foundStations.contains(station)) {
							foundStations.add(station);
						}
					}
				}
			}

			List<BusRoute> foundBusRoutes = new ArrayList<>();

			for (BusRoute busRoute : busData.getRoutes()) {
				boolean res = StringUtils.containsIgnoreCase(busRoute.getId(), query.trim())
						|| StringUtils.containsIgnoreCase(busRoute.getName(), query.trim());
				if (res) {
					if (!foundBusRoutes.contains(busRoute)) {
						foundBusRoutes.add(busRoute);
					}
				}
			}

			List<BikeStation> foundBikeStations = new ArrayList<>();
			if (bikeStations != null) {
				for (BikeStation bikeStation : bikeStations) {
					boolean res = StringUtils.containsIgnoreCase(bikeStation.getName(), query.trim())
							|| StringUtils.containsIgnoreCase(bikeStation.getStAddress1(), query.trim());
					if (res) {
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
