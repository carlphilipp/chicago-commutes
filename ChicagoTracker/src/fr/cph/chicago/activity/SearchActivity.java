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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Activity that display search result
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class SearchActivity extends ListActivity {
	/** The menu **/
	private Menu mMenu;
	/** The adapter **/
	private SearchAdapter mAdapter;
	/** Bike stations **/
	private List<BikeStation> mBikeStations;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_search);
			FrameLayout container = (FrameLayout) findViewById(R.id.container);
			container.getForeground().setAlpha(0);

			if (Util.isNetworkAvailable()) {
				mAdapter = new SearchAdapter(this, container);
				mBikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
				handleIntent(getIntent());
				setListAdapter(mAdapter);
			} else {
				Toast.makeText(ChicagoTracker.getAppContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
			}

			// Preventing keyboard from moving background when showing up
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.mMenu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Load animation in menu
	 */
	public final void startRefreshAnimation() {
		if (mMenu != null) {
			MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
			refreshMenuItem.setActionView(R.layout.progressbar);
			refreshMenuItem.expandActionView();
		}
	}

	/**
	 * Stop animation in menu
	 */
	public final void stopRefreshAnimation() {
		if (mMenu != null) {
			MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Reload adapter with correct data
	 *
	 * @param intent
	 *            the intent
	 */
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();
			TrainData trainData = dataHolder.getTrainData();

			List<Station> foundStations = new ArrayList<Station>();

			for (Entry<TrainLine, List<Station>> e : trainData.getAllStations().entrySet()) {
				for (Station station : e.getValue()) {
					boolean res = StringUtils.containsIgnoreCase(station.getName(), query.trim());
					if (res) {
						if (!foundStations.contains(station)) {
							foundStations.add(station);
						}
					}
				}
			}

			List<BusRoute> foundBusRoutes = new ArrayList<BusRoute>();

			for (BusRoute busRoute : busData.getRoutes()) {
				boolean res = StringUtils.containsIgnoreCase(busRoute.getId(), query.trim())
						|| StringUtils.containsIgnoreCase(busRoute.getName(), query.trim());
				if (res) {
					if (!foundBusRoutes.contains(busRoute)) {
						foundBusRoutes.add(busRoute);
					}
				}
			}

			List<BikeStation> foundBikeStations = new ArrayList<BikeStation>();
			if (mBikeStations != null) {
				for (BikeStation bikeStation : mBikeStations) {
					boolean res = StringUtils.containsIgnoreCase(bikeStation.getName(), query.trim())
							|| StringUtils.containsIgnoreCase(bikeStation.getStAddress1(), query.trim());
					if (res) {
						if (!foundBikeStations.contains(bikeStation)) {
							foundBikeStations.add(bikeStation);
						}
					}
				}
			}
			mAdapter.updateData(foundStations, foundBusRoutes, foundBikeStations);
			mAdapter.notifyDataSetChanged();
		}
	}
}
