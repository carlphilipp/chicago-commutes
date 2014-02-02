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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import fr.cph.chicago.R;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

/**
 * This class represents the base activity of the app
 * 
 * @author Carl-Philipp Harmant
 * 
 */
public class BaseActivity extends Activity {

	/** Tag **/
	private static final String TAG = "BaseActivity";

	/** Layout loaded **/
	private View loadLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loading);
		loadLayout = findViewById(R.id.loading_layout);

		if (DataHolder.getInstance().getBusData() == null || DataHolder.getInstance().getTrainData() == null) {
			showProgress(true, null);
			new LoadData().execute();
		} else {
			loadHome();
		}
	}

	/**
	 * Load Bus and train data into DataHolder
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private class LoadData extends AsyncTask<Void, Void, Void> {
		/** Bus data **/
		private BusData busData;
		/** Train data **/
		private TrainData trainData;
		/** Tracker exception **/
		private TrackerException exceptionToBeThrown;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				this.busData = BusData.getInstance();
				this.busData.read();
				this.trainData = new TrainData();
				this.trainData.read();
			} catch (ParserException e) {
				this.exceptionToBeThrown = e;
			} catch (ConnectException e) {
				this.exceptionToBeThrown = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (exceptionToBeThrown == null) {
				DataHolder dataHolder = DataHolder.getInstance();
				dataHolder.setBusData(busData);
				dataHolder.setTrainData(trainData);

				// Load home when finished
				loadHome();
			} else {
				loadError(exceptionToBeThrown);
			}
		}
	}

	/**
	 * Show progress bar
	 * 
	 * @param show
	 *            show the bar or not
	 * @param errorMessage
	 *            the error message
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show, String errorMessage) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			loadLayout.setVisibility(View.VISIBLE);
			loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Load home
	 * 
	 */
	private void loadHome() {
		Intent intent = new Intent(this, MainActivity.class);
		showProgress(false, null);
		finish();
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	/**
	 * Load home
	 * 
	 */
	private void loadError(TrackerException exceptionToBeThrown) {
		Intent intent = new Intent(this, ErrorActivity.class);
		showProgress(false, null);
		finish();
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * Display error
	 * 
	 * @param jsonObject
	 *            the json object
	 */
	// public void displayError(JSONObject jsonObject) {
	// Intent intent = new Intent(this, ErrorActivity.class);
	// intent.putExtra("data", jsonObject.toString());
	// intent.putExtra("login", login);
	// intent.putExtra("password", password);
	// startActivity(intent);
	// finish();
	// }
}
