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
import android.util.Log;
import android.view.View;
import fr.cph.chicago.R;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;

/**
 * This class represents the base activity of the app
 * 
 * @author Carl-Philipp Harmant
 * 
 */
public class BaseActivity extends Activity {

	/** Tag **/
	private static final String TAG = "BaseActivity";

	private View loginLayout;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "BaseActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		loginLayout = findViewById(R.id.loading_layout);

		if (DataHolder.getInstance().getBusData() == null || DataHolder.getInstance().getTrainData() == null) {
			showProgress(true, null);
			new LoadData().execute();
		} else {
			loadHome();
		}
	}

	private class LoadData extends AsyncTask<Void, Void, BusData> {

		@Override
		protected BusData doInBackground(Void... params) {
			BusData data = BusData.getInstance();
			data.read();
			return data;
		}

		@Override
		protected void onPostExecute(BusData result) {
			DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(result);
			TrainData trainData = new TrainData();
			trainData.read();
			dataHolder.setTrainData(trainData);
			loadHome();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
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
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			loginLayout.setVisibility(View.VISIBLE);
			loginLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					loginLayout.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			loginLayout.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Load home
	 * 
	 * @param portfolio
	 *            the portfolio
	 */
	public void loadHome() {
		Intent intent = new Intent(this, MainActivity.class);
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
