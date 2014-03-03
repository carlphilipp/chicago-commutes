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

import org.apache.commons.lang3.StringUtils;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

/**
 * Activity that represents the bus bound activity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusBoundActivity extends ListActivity {
	/** Bus route id **/
	private String busRouteId;
	/** Bus route name **/
	private String busRouteName;
	/** Bound **/
	private String bound;
	/** Adapter **/
	private BusBoundAdapter ada;
	/** List of bus stop get via API **/
	private List<BusStop> busStops;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_bound);
		if (busRouteId == null && busRouteName == null && bound == null) {
			busRouteId = getIntent().getExtras().getString("busRouteId");
			busRouteName = getIntent().getExtras().getString("busRouteName");
			bound = getIntent().getExtras().getString("bound");
		}
		ada = new BusBoundAdapter(busRouteId);
		setListAdapter(ada);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				BusStop busStop = (BusStop) ada.getItem(position);
				Intent intent = new Intent(ChicagoTracker.getAppContext(), BusActivity.class);

				Bundle extras = new Bundle();
				extras.putInt("busStopId", busStop.getId());
				extras.putString("busStopName", busStop.getName());
				extras.putString("busRouteId", busRouteId);
				extras.putString("busRouteName", busRouteName);
				extras.putString("bound", bound);
				extras.putDouble("latitude", busStop.getPosition().getLatitude());
				extras.putDouble("longitude", busStop.getPosition().getLongitude());

				intent.putExtras(extras);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			}
		});

		EditText filter = (EditText) findViewById(R.id.bus_filter);
		filter.addTextChangedListener(new TextWatcher() {
			List<BusStop> busStopsFiltered;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				busStopsFiltered = new ArrayList<BusStop>();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				for (BusStop busStop : busStops) {
					if (StringUtils.containsIgnoreCase(busStop.getName(), s)) {
						this.busStopsFiltered.add(busStop);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				ada.update(busStopsFiltered);
				ada.notifyDataSetChanged();
			}
		});

		getActionBar().setDisplayHomeAsUpEnabled(true);
		new BusBoundAsyncTask().execute();

		// Preventing keyboard from moving background when showing up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		busRouteId = savedInstanceState.getString("busRouteId");
		busRouteName = savedInstanceState.getString("busRouteName");
		bound = savedInstanceState.getString("bound");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("busRouteId", busRouteId);
		savedInstanceState.putString("busRouteName", busRouteName);
		savedInstanceState.putString("bound", bound);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(this.busRouteName + " (" + this.bound + ")");
		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Task that connect to API to get the bound of the selected stop
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class BusBoundAsyncTask extends AsyncTask<Void, Void, List<BusStop>> {

		/** The exception that could potentially been thrown during request **/
		private TrackerException trackerException;

		@Override
		protected final List<BusStop> doInBackground(final Void... params) {
			List<BusStop> lBuses = null;
			try {
				lBuses = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, bound);
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			return lBuses;
		}

		@Override
		protected final void onPostExecute(final List<BusStop> result) {
			BusBoundActivity.this.busStops = result;
			if (trackerException == null) {
				ada.update(result);
				ada.notifyDataSetChanged();
			} else {
				ChicagoTracker.displayError(BusBoundActivity.this, trackerException);
			}
		}
	}
}
