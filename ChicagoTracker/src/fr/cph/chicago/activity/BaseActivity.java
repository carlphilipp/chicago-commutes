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
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		new LoadLocalData().execute();
	}

	/**
	 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that if one of
	 * the url contacted does not response, we will still process the other data, and won't throw any
	 * exception
	 *
	 * @author Carl-Philipp Harmant
	 */
	private class LoadLocalData extends AsyncTask<Void, String, Void> {
		/**
		 * Bus data
		 **/
		private BusData busData;
		/**
		 * Train data
		 **/
		private TrainData trainData;

		@Override
		protected final Void doInBackground(final Void... params) {
			// Load local CSV
			trainData = new TrainData();
			trainData.read();

			busData = BusData.getInstance();
			busData.readBusStops();
			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			final DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);
			dataHolder.setTrainData(trainData);
			try {
				// Load favorites data
				loadFavorites();
			} catch (final ParserException e) {
				displayError(e);
			}
		}
	}

	/**
	 * Called via reflection from CtaConnectTask. It load arrivals data into ChicagoTracker object. Update
	 * last update time. Start main activity
	 *
	 * @param trainArrivals list of train arrivals
	 * @param busArrivals   list of bus arrivals
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
	 * @param trainArrivals the train arrivals
	 * @param busArrivals   the bus arrivals
	 */
	private void startMainActivity(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals) {
		if (!isFinishing()) {
			final Intent intent = new Intent(this, MainActivity.class);
			final Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), (ArrayList<BusArrival>) busArrivals);
			bundle.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), trainArrivals);
			intent.putExtras(bundle);

			finish();
			startActivity(intent);
		}
	}

	/**
	 * Display error. Set train and bus data to null before running the error activity
	 *
	 * @param exceptionToBeThrown the exception that has been thrown
	 */
	public void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		ChicagoTracker.displayError(this, exceptionToBeThrown);
	}

	/**
	 * Connect to CTA API and get arrivals trains and buses from favorites
	 *
	 * @throws ParserException the exception
	 */
	private void loadFavorites() throws ParserException {
		final MultiValuedMap<String, String> paramsTrain = getParamsTrain();
		final MultiValuedMap<String, String> paramsBus = getParamsBus();

		// Get preferences to know if trains and buses need to be loaded
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean loadTrain = true;
		boolean loadBus = true;
		if (sharedPref.contains(getString(R.string.preferences_cta_train))) {
			loadTrain = sharedPref.getBoolean(getString(R.string.preferences_cta_train), true);
		} else {
			final SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean(getString(R.string.preferences_cta_train), true);
			editor.apply();
		}
		if (sharedPref.contains(getString(R.string.preferences_cta_bus))) {
			loadBus = sharedPref.getBoolean(getString(R.string.preferences_cta_bus), true);
		} else {
			final SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean(getString(R.string.preferences_cta_bus), true);
			editor.apply();
		}

		final GlobalConnectTask task = new GlobalConnectTask(this, BaseActivity.class, CtaRequestType.TRAIN_ARRIVALS, paramsTrain,
				CtaRequestType.BUS_ARRIVALS, paramsBus, loadTrain, loadBus, false);
		task.execute((Void) null);
		trackWithGoogleAnalytics(loadTrain, loadBus);
	}

	private MultiValuedMap<String, String> getParamsTrain() {
		final MultiValuedMap<String, String> paramsTrain = new ArrayListValuedHashMap<>();
		final List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
		for (final Integer favorite : favorites) {
			paramsTrain.put("mapid", String.valueOf(favorite));
		}
		return paramsTrain;
	}

	private MultiValuedMap<String, String> getParamsBus() {
		final MultiValuedMap<String, String> paramsBus = new ArrayListValuedHashMap<>();
		final List<String> busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		for (final String busFavorite : busFavorites) {
			final String[] fav = Util.decodeBusFavorite(busFavorite);
			paramsBus.put("rt", fav[0]);
			paramsBus.put("stpid", fav[1]);
		}
		return paramsBus;
	}

	private void trackWithGoogleAnalytics(boolean loadTrain, boolean loadBus) {
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
