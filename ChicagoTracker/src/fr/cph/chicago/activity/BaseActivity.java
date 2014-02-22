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

import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.AlertData;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.task.CtaConnectTask;
import fr.cph.chicago.util.Util;

/**
 * This class represents the base activity of the application It will load the loading screen and/or
 * the main activity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {

	/** Tag **/
	private static final String TAG = "BaseActivity";

	/** Layout loaded **/
	private View loadLayout;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loading);
		loadLayout = findViewById(R.id.loading_layout);

		Bundle extras = getIntent().getExtras();
		boolean error = false;
		if (extras != null) {
			error = extras.getBoolean("error");
		}

		if (error) {
			showProgress(true);
			new LoadData().execute();
		} else if (DataHolder.getInstance().getBusData() == null || DataHolder.getInstance().getTrainData() == null) {
			showProgress(true);
			new LoadData().execute();
		} else {
			startMainActivity();
		}

	}

	/**
	 * Called via reflection from CtaConnectTask. It load arrivals data into ChicagoTracker object.
	 * Update last update time. Start main activity
	 * 
	 * @param trainArrivals
	 *            list of train arrivals
	 * @param busArrivals
	 *            list of bus arrivals
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals) {
		ChicagoTracker.setBusArrivals(busArrivals);
		ChicagoTracker.setTrainArrivals(trainArrivals);
		ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
		startMainActivity();
	}

	/**
	 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that
	 * if one of the url contacted does not response, we will still process the other data, and
	 * won't throw any exception
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadData extends AsyncTask<Void, Void, Void> {
		/** Bus data **/
		private BusData busData;
		/** Train data **/
		private TrainData trainData;
		/** Alert data **/
		private AlertData alertData;

		@Override
		protected final Void doInBackground(final Void... params) {

			// Load local CSV
			this.trainData = new TrainData();
			this.trainData.read();

			// Load bus API data
			this.busData = BusData.getInstance();
			this.busData.readBusStops();

			try {
				this.busData.loadBusRoutes();
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			}

			// Load alert API data
			this.alertData = AlertData.getInstance();
			try {
				this.alertData.loadGeneralAlerts();
			} catch (ParserException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ConnectException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);
			dataHolder.setTrainData(trainData);
			dataHolder.setAlertData(alertData);
			try {
				// Load favorites data
				loadData();
			} catch (ParserException e) {
				displayError(e);
			}
		}
	}

	/**
	 * Show progress bar
	 * 
	 * @param show
	 *            show the bar or not
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private final void showProgress(final boolean show) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
				// loadLayout.setVisibility(View.VISIBLE);
				loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
					}
				});
			} else {
				loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		} catch (IllegalStateException e) {
			Log.i(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Display error. Set train and bus data to null before running the error activity
	 * 
	 * @param exceptionToBeThrown
	 *            the exception that has been thrown
	 */
	private void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		// Stop of the laoding
		showProgress(false);
		ChicagoTracker.displayError(this, exceptionToBeThrown);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * Connect to CTA API and get arrivals trains and buses from favorites
	 * 
	 * @throws ParserException
	 *             the exception
	 */
	private void loadData() throws ParserException {
		MultiMap<String, String> params = new MultiValueMap<String, String>();
		List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		for (Integer fav : favorites) {
			params.put("mapid", String.valueOf(fav));
		}

		MultiMap<String, String> params2 = new MultiValueMap<String, String>();
		List<String> busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		for (String str : busFavorites) {
			String[] fav = Util.decodeBusFavorite(str);
			params2.put("rt", fav[0]);
			params2.put("stpid", fav[1]);
		}

		CtaConnectTask task = new CtaConnectTask(this, BaseActivity.class, CtaRequestType.TRAIN_ARRIVALS, params, CtaRequestType.BUS_ARRIVALS,
				params2);
		task.execute((Void) null);
	}

	/**
	 * Finish current activity and start main activity with custom transition
	 */
	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		showProgress(false);
		finish();
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
