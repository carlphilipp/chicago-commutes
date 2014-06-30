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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.AlertData;
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
 * This class represents the base activity of the application It will load the loading screen and/or the main activity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {

	/** Tag **/
	private static final String TAG = "BaseActivity";
	/** Error state **/
	private Boolean error;

	private SparseArray<TrainArrival> trainArrivals;

	private List<BusArrival> busArrivals;

	private List<BikeStation> bikeStations;

	private TextView trainMessage, busMessage, alertMessage, bikeMessage, favoritesMessage;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loading);

		trainMessage = (TextView) findViewById(R.id.loadingTrainView);

		busMessage = (TextView) findViewById(R.id.loadingBusView);

		alertMessage = (TextView) findViewById(R.id.loadingAlertView);

		bikeMessage = (TextView) findViewById(R.id.loadingBikeView);

		favoritesMessage = (TextView) findViewById(R.id.loadingFavoritesView);

		Bundle extras = getIntent().getExtras();

		if (extras != null && error == null) {
			error = extras.getBoolean("error");
		} else {
			error = false;
		}

		if (error) {
			new LoadData().execute();
		} else if (trainArrivals == null || busArrivals == null || bikeStations == null) {
			new LoadData().execute();
		} else {
			startMainActivity();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		error = savedInstanceState.getBoolean("error");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("error", error);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Called via reflection from CtaConnectTask. It load arrivals data into ChicagoTracker object. Update last update time. Start main activity
	 * 
	 * @param trainArrivals
	 *            list of train arrivals
	 * @param busArrivals
	 *            list of bus arrivals
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations, final Boolean trainBoolean, final Boolean busBoolean, final Boolean bikeBoolean) {
		this.trainArrivals = trainArrivals;
		this.busArrivals = busArrivals;
		if (trainBoolean && busBoolean) {
			favoritesMessage.setText(favoritesMessage.getText() + " - OK");
			favoritesMessage.setTextColor(getResources().getColor(R.color.green));
		} else {
			favoritesMessage.setText(favoritesMessage.getText() + " - FAIL");
			favoritesMessage.setTextColor(getResources().getColor(R.color.red));
		}
		ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
		startMainActivity();
	}

	/**
	 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that if one of the url contacted does not response, we
	 * will still process the other data, and won't throw any exception
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadData extends AsyncTask<Void, String, Void> {
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

			//publishProgress(new String[] { "t", "true" });

			this.busData = BusData.getInstance();
			this.busData.readBusStops();

			// Load bus API data
			/*try {
				this.busData.loadBusRoutes();
				publishProgress(new String[] { "b", "true" });
			} catch (ParserException e) {
				publishProgress(new String[] { "b", "false" });
				Log.e(TAG, e.getMessage(), e);
			} catch (ConnectException e) {
				publishProgress(new String[] { "b", "false" });
				Log.e(TAG, e.getMessage(), e);
			}*/

			// Load alert API data
/*			try {
				this.alertData = AlertData.getInstance();
				this.alertData.loadGeneralAlerts();
				publishProgress(new String[] { "a", "true" });
			} catch (ParserException e) {
				publishProgress(new String[] { "a", "false" });
				Log.e(TAG, e.getMessage(), e);
			} catch (ConnectException e) {
				publishProgress(new String[] { "a", "false" });
				Log.e(TAG, e.getMessage(), e);
			}*/
			// Load divvy
			BaseActivity.this.bikeStations = new ArrayList<BikeStation>();
/*			try {
				Json json = new Json();
				DivvyConnect divvyConnect = DivvyConnect.getInstance();
				String bikeContent = divvyConnect.connect();
				BaseActivity.this.bikeStations = json.parseStations(bikeContent);
				Collections.sort(BaseActivity.this.bikeStations, Util.BIKE_COMPARATOR_NAME);
				publishProgress(new String[] { "d", "true" });
			} catch (ConnectException e) {
				publishProgress(new String[] { "d", "false" });
				Log.e(TAG, e.getMessage(), e);
			} catch (ParserException e) {
				publishProgress(new String[] { "d", "false" });
				Log.e(TAG, e.getMessage(), e);
			}*/
			return null;
		}

		@Override
		protected final void onProgressUpdate(String... progress) {
			String type = progress[0];
			boolean passed = Boolean.valueOf(progress[1]);
			if (type.equals("t")) {
				trainMessage.setVisibility(TextView.VISIBLE);
				if (!passed) {
					trainMessage.setText(trainMessage.getText() + " - FAIL");
					trainMessage.setTextColor(getResources().getColor(R.color.red));
				} else {
					trainMessage.setText(trainMessage.getText() + " - OK");
					trainMessage.setTextColor(getResources().getColor(R.color.green));
				}
				busMessage.setVisibility(TextView.VISIBLE);
			} else if (type.equals("b")) {
				if (!passed) {
					busMessage.setText(busMessage.getText() + " - FAIL");
					busMessage.setTextColor(getResources().getColor(R.color.red));
				} else {
					busMessage.setText(busMessage.getText() + " - OK");
					busMessage.setTextColor(getResources().getColor(R.color.green));
				}
				alertMessage.setVisibility(TextView.VISIBLE);
			} else if (type.equals("a")) {
				if (!passed) {
					alertMessage.setText(alertMessage.getText() + " - FAIL");
					alertMessage.setTextColor(getResources().getColor(R.color.red));
				} else {
					alertMessage.setText(alertMessage.getText() + " - OK");
					alertMessage.setTextColor(getResources().getColor(R.color.green));
				}
				bikeMessage.setVisibility(TextView.VISIBLE);
			} else if (type.equals("d")) {
				if (!passed) {
					bikeMessage.setText(bikeMessage.getText() + " - FAIL");
					bikeMessage.setTextColor(getResources().getColor(R.color.red));
				} else {
					bikeMessage.setText(bikeMessage.getText() + " - OK");
					bikeMessage.setTextColor(getResources().getColor(R.color.green));
				}
			}
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);
			dataHolder.setTrainData(trainData);
			dataHolder.setAlertData(alertData);
			try {
				//favoritesMessage.setVisibility(TextView.VISIBLE);
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
		DataHolder.getInstance().setAlertData(null);
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

		GlobalConnectTask task = new GlobalConnectTask(this, BaseActivity.class, CtaRequestType.TRAIN_ARRIVALS, params, CtaRequestType.BUS_ARRIVALS,
				params2, false);
		task.execute((Void) null);
	}

	/**
	 * Finish current activity and start main activity with custom transition
	 */
	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("busArrivals", (ArrayList<BusArrival>) busArrivals);
		bundle.putSparseParcelableArray("trainArrivals", trainArrivals);
		bundle.putParcelableArrayList("bikeStations", (ArrayList<BikeStation>) bikeStations);
		intent.putExtras(bundle);

		finish();
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
