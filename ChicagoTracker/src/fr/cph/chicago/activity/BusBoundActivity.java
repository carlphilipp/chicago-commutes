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

import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.BusBoundAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusStop;

public class BusBoundActivity extends ListActivity {
	
	private static final String TAG = "BusBoundActivity";
	private String busRouteId;
	private String busRouteName;
	private String bound;
	private BusBoundAdapter ada;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_bound);
		busRouteId = getIntent().getExtras().getString("busRouteId");
		busRouteName = getIntent().getExtras().getString("busRouteName");
		bound = getIntent().getExtras().getString("bound");
		
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
				ChicagoTracker.getAppContext().startActivity(intent);
			}
		});
		new BusBoundAsyncTask().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(this.busRouteName + " (" + this.bound + ")");
		return super.onCreateOptionsMenu(menu);
	}

	private class BusBoundAsyncTask extends AsyncTask<Void, Void, List<BusStop>> {

		@Override
		protected List<BusStop> doInBackground(Void... params) {
			return DataHolder.getInstance().getBusData().readBusStop(busRouteId, bound);
		}
		@Override
		protected void onPostExecute(List<BusStop> result) {
			ada.update(result);
			ada.notifyDataSetChanged();
		}
	}
}
