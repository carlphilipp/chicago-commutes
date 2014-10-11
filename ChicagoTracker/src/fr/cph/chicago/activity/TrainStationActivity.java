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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Activity the list of train stations
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainStationActivity extends ListActivity {
	/** The train data **/
	private TrainData mTrainData;
	/** The line **/
	private TrainLine mLine;
	/** The line param **/
	private String mLineParam;

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
			this.mTrainData = dataHolder.getTrainData();

			if (mLine == null && mLineParam == null) {
				mLineParam = getIntent().getExtras().getString("line");
				mLine = TrainLine.fromString(mLineParam);
			}

			this.setTitle(mLine.toStringWithLine());

			setContentView(R.layout.activity_train_station);

			FrameLayout container = (FrameLayout) findViewById(R.id.container);
			container.getForeground().setAlpha(0);

			TrainAdapter ada = new TrainAdapter(mLine, this, container);
			setListAdapter(ada);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mLineParam = savedInstanceState.getString("line");
		mLine = TrainLine.fromString(mLineParam);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("line", mLineParam);
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
