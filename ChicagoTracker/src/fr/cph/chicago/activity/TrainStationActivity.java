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
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainStationActivity extends ListActivity {
	/** The train data **/
	private TrainData trainData;
	/** The line **/
	private TrainLine trainLine;
	/** The line param **/
	private String lineParam;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!this.isFinishing()) {
			// Load data
			DataHolder dataHolder = DataHolder.getInstance();
			this.trainData = dataHolder.getTrainData();

			if (trainLine == null && lineParam == null) {
				lineParam = getIntent().getExtras().getString("line");
				trainLine = TrainLine.fromString(lineParam);
			}

			this.setTitle(trainLine.toStringWithLine());

			setContentView(R.layout.activity_train_station);

			FrameLayout container = (FrameLayout) findViewById(R.id.container);
			container.getForeground().setAlpha(0);

			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			Util.setToolbarColor(this, toolbar, trainLine);
			toolbar.setTitle(trainLine.toString() + " Line");

			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			TrainAdapter ada = new TrainAdapter(trainLine, this, container);
			setListAdapter(ada);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		lineParam = savedInstanceState.getString("line");
		trainLine = TrainLine.fromString(lineParam);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("line", lineParam);
		super.onSaveInstanceState(savedInstanceState);
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
