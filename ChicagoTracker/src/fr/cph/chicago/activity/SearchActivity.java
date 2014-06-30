/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

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

/**
 * Activity that display search result
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class SearchActivity extends ListActivity {

	/** The menu **/
	private Menu menu;
	/** The adapter **/
	private SearchAdapter ada;
	
	List<BikeStation> bikeStations;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkTrainData(this);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.activity_search);

			FrameLayout container = (FrameLayout) findViewById(R.id.container);
			container.getForeground().setAlpha(0);
			ada = new SearchAdapter(this, container);
			bikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
			handleIntent(getIntent());
			setListAdapter(ada);

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
		this.menu = menu;
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
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.setActionView(R.layout.progressbar);
			refreshMenuItem.expandActionView();
		}
	}

	/**
	 * Stop animation in menu
	 */
	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
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

			//List<BikeStation> bikeStations = intent.getExtras().getParcelableArrayList("bikeStations");
			List<BikeStation> foundBikeStations = new ArrayList<BikeStation>();
			for (BikeStation bikeStation : bikeStations) {
				boolean res = StringUtils.containsIgnoreCase(bikeStation.getName(), query.trim())
						|| StringUtils.containsIgnoreCase(bikeStation.getStAddress1(), query.trim());
				if (res) {
					if (!foundBikeStations.contains(bikeStation)) {
						foundBikeStations.add(bikeStation);
					}
				}
			}

			ada.updateData(foundStations, foundBusRoutes, foundBikeStations);
			ada.notifyDataSetChanged();
		}
	}
}
