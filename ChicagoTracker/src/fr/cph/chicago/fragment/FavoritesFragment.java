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

package fr.cph.chicago.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Favorites Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesFragment extends Fragment {
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_nfumber";
	/** Tag **/
	private static final String TAG = "FavoritesFragment";
	/** The activity **/
	private MainActivity mainActivity;
	/** The adapter of the fragment **/
	private FavoritesAdapter favoritesAdapter;
	/** A refresh task **/
	private RefreshTask refreshTimingTask;
	/** List of bus arrivals **/
	private List<BusArrival> busArrivals;
	/** Train arrivals **/
	private SparseArray<TrainArrival> trainArrivals;
	/** List of bus arrivals **/
	private List<BikeStation> bikeStations;
	/** Welcome layout **/
	private RelativeLayout welcomelayout;
	/** Root view **/
	private View rootView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 * @return
	 */
	public static FavoritesFragment newInstance(final int sectionNumber) {
		FavoritesFragment fragment = new FavoritesFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			Bundle bundle = mainActivity.getIntent().getExtras();
			busArrivals = bundle.getParcelableArrayList("busArrivals");
			trainArrivals = bundle.getSparseParcelableArray("trainArrivals");
			bikeStations = bundle.getParcelableArrayList("bikeStations");
		} else {
			busArrivals = savedInstanceState.getParcelableArrayList("busArrivals");
			trainArrivals = savedInstanceState.getSparseParcelableArray("trainArrivals");
			bikeStations = savedInstanceState.getParcelableArrayList("bikeStations");
			boolean boolTrain = ChicagoTracker.checkTrainData(mainActivity);
			if (boolTrain) {
				ChicagoTracker.checkBusData(mainActivity);
			}
		}
		if (bikeStations == null) {
			bikeStations = new ArrayList<>();
		}

		Util.trackScreen(mainActivity, R.string.analytics_favorites_fragment);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_main, container, false);
		if (!mainActivity.isFinishing()) {
			welcomelayout = (RelativeLayout) rootView.findViewById(R.id.welcome);
			if (favoritesAdapter == null) {
				favoritesAdapter = new FavoritesAdapter(mainActivity);
				favoritesAdapter.setArrivalsAndBikeStations(trainArrivals, busArrivals, bikeStations);
			}
			ListView listView = (ListView) rootView.findViewById(R.id.favorites_list);
			listView.setAdapter(favoritesAdapter);
			startRefreshTask();
		}
		return rootView;
	}

	@Override
	public final void onPause() {
		super.onPause();
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
	}

	@Override
	public final void onStop() {
		super.onStop();
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		if (refreshTimingTask != null) {
			refreshTimingTask.cancel(true);
		}
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public final void onResume() {
		super.onResume();
		favoritesAdapter.setFavorites();
		favoritesAdapter.notifyDataSetChanged();
		if (refreshTimingTask.getStatus() == Status.FINISHED) {
			startRefreshTask();
		}
		if (welcomelayout != null) {
			boolean hasFav = Preferences.hasFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, ChicagoTracker.PREFERENCE_FAVORITES_BUS,
					ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			if (!hasFav) {
				welcomelayout.setVisibility(View.VISIBLE);
			} else {
				welcomelayout.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("busArrivals", (ArrayList<BusArrival>) busArrivals);
		outState.putSparseParcelableArray("trainArrivals", trainArrivals);
		outState.putParcelableArrayList("bikeStations", (ArrayList<BikeStation>) bikeStations);
	}

	/**
	 * Reload data
	 *
	 * @param trainArrivals
	 *            the train arrivals list
	 * @param busArrivals
	 *            the bus arrivals list
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations, final Boolean trainBoolean, final Boolean busBoolean, final Boolean bikeBoolean,
			final Boolean networkAvailable) {
		if (!networkAvailable) {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
		} else {
			// Put into intent new bike stations data
			mainActivity.getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) bikeStations);
			mainActivity.onNewIntent(mainActivity.getIntent());

			favoritesAdapter.setArrivalsAndBikeStations(trainArrivals, busArrivals, bikeStations);
			favoritesAdapter.refreshUpdated();
			favoritesAdapter.refreshUpdatedView();
			favoritesAdapter.notifyDataSetChanged();
		}
		// Highlight background
		rootView.setBackgroundResource(R.drawable.highlight_selector);
		rootView.postDelayed(new Runnable() {
			public void run() {
				rootView.setBackgroundResource(R.drawable.bg_selector);
				mainActivity.stopRefreshAnimation();
			}
		}, 100);
	}

	/**
	 * Display error
	 *
	 * @param trackerException
	 *            the exception
	 */
	public final void displayError(TrackerException trackerException) {
		ChicagoTracker.displayError(mainActivity, trackerException);
	}

	public final void setBikeStations(List<BikeStation> bikeStations) {
		this.bikeStations = bikeStations;
		favoritesAdapter.setBikeStations(bikeStations);
		favoritesAdapter.notifyDataSetChanged();
	}

	/**
	 * Start refresh task
	 */
	private void startRefreshTask() {
		refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		favoritesAdapter.refreshUpdatedView();
	}

	/**
	 * RefreshTask
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class RefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected final void onProgressUpdate(Void... values) {
			super.onProgressUpdate();
			favoritesAdapter.refreshUpdatedView();
		}

		@Override
		protected final Void doInBackground(final Void... params) {
			while (!this.isCancelled()) {
				Log.v(TAG, "Updated of time " + Thread.currentThread().getId());
				try {
					publishProgress();
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Log.v(TAG, "Stopping thread. Normal Behavior");
				}
			}
			return null;
		}
	}
}