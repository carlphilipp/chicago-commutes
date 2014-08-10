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
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.task.GlobalConnectTask;
import fr.cph.chicago.util.Util;

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {
	/** Error state **/
	private Boolean mError;
	/** Train arrivals **/
	private SparseArray<TrainArrival> mTrainArrivals;
	/** Bus arrivals **/
	private List<BusArrival> mBusArrivals;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		Bundle extras = getIntent().getExtras();
		if (extras != null && mError == null) {
			mError = extras.getBoolean("error");
		} else {
			mError = false;
		}

		if (mError) {
			new LoadData().execute();
		} else if (mTrainArrivals == null || mBusArrivals == null) {
			new LoadData().execute();
		} else {
			startMainActivity(mTrainArrivals, mBusArrivals);
		}
	}

	@Override
	public final void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mError = savedInstanceState.getBoolean("error");
	}

	@Override
	public final void onSaveInstanceState(final Bundle savedInstanceState) {
		savedInstanceState.putBoolean("error", mError);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Called via reflection from CtaConnectTask. It load arrivals data into ChicagoTracker object. Update
	 * last update time. Start main activity
	 * 
	 * @param trainArrivals
	 *            list of train arrivals
	 * @param busArrivals
	 *            list of bus arrivals
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations, final Boolean trainBoolean, final Boolean busBoolean, final Boolean bikeBoolean,
			final Boolean networkAvailable) {
		if (!networkAvailable) {
			Toast.makeText(this, "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
		ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
		startMainActivity(trainArrivals, busArrivals);
	}

	/**
	 * Finish current activity and start main activity with custom transition
	 * 
	 * @param trainArrivals
	 *            the train arrivals
	 * @param busArrivals
	 *            the bus arrivals
	 */
	private void startMainActivity(SparseArray<TrainArrival> trainArrivals, List<BusArrival> busArrivals) {
		if (!isFinishing()) {
			Intent intent = new Intent(this, MainActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("busArrivals", (ArrayList<BusArrival>) busArrivals);
			bundle.putSparseParcelableArray("trainArrivals", trainArrivals);
			intent.putExtras(bundle);

			finish();
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}

	/**
	 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that if one of
	 * the url contacted does not response, we will still process the other data, and won't throw any
	 * exception
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadData extends AsyncTask<Void, String, Void> {
		/** Bus data **/
		private BusData busData;
		/** Train data **/
		private TrainData trainData;

		@Override
		protected final Void doInBackground(final Void... params) {
			// Load local CSV
			this.trainData = new TrainData();
			this.trainData.read();

			this.busData = BusData.getInstance();
			this.busData.readBusStops();
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);
			dataHolder.setTrainData(trainData);
			try {
				// Load favorites data
				loadData();
			} catch (ParserException e) {
				displayError(e);
			}
		}
	}

	/**
	 * Display error. Set train and bus data to null before running the error activity
	 * 
	 * @param exceptionToBeThrown
	 *            the exception that has been thrown
	 */
	public void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
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

		// Get preferences to know if trains and buses need to be loaded
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean loadTrain = true;
		boolean loadBus = true;
		if (sharedPref.contains("cta_train")) {
			loadTrain = sharedPref.getBoolean("cta_train", true);
		} else {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("cta_train", true);
			editor.commit();
		}
		if (sharedPref.contains("cta_bus")) {
			loadBus = sharedPref.getBoolean("cta_bus", true);
		} else {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("cta_bus", true);
			editor.commit();
		}

		GlobalConnectTask task = new GlobalConnectTask(this, BaseActivity.class, CtaRequestType.TRAIN_ARRIVALS, params, CtaRequestType.BUS_ARRIVALS,
				params2, loadTrain, loadBus, false);
		task.execute((Void) null);
		if (loadTrain) {
			Util.trackAction(BaseActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
					R.string.analytics_action_get_train_arrivals, 0);
		}
		if (loadBus) {
			Util.trackAction(BaseActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_arrival, 0);
		}
	}
}
