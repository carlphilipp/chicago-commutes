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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class TrainStationActivity extends ListActivity {

	private TrainData data;

	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load data
		DataHolder dataHolder = DataHolder.getInstance();
		this.data = dataHolder.getTrainData();

		final TrainLine line = TrainLine.fromString(getIntent().getExtras().getString("line"));

		this.setTitle(line.toStringWithLine());

		setContentView(R.layout.activity_train_station);

		TrainAdapter ada = new TrainAdapter(line);
		setListAdapter(ada);
		ListView listView = getListView();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("stationId", data.getStationsForLine(line).get(position).getId());
				intent.putExtras(extras);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			}
		});
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
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
}
