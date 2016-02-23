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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import fr.cph.chicago.activity.SearchActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
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
import java.util.List;

/**
 * Favorites Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String TAG = FavoritesFragment.class.getSimpleName();

	private MainActivity mainActivity;

	private FavoritesAdapter favoritesAdapter;

	private RefreshTask refreshTimingTask;

	private List<BusArrival> busArrivals;

	private SparseArray<TrainArrival> trainArrivals;

	private List<BikeStation> bikeStations;

	private RelativeLayout welcomeLayout;

	private View rootView;

	private SwipeRefreshLayout swipeRefreshLayout;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber the section number
	 * @return a favorite fragment
	 */
	public static FavoritesFragment newInstance(final int sectionNumber) {
		final FavoritesFragment fragment = new FavoritesFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			final Bundle bundle = mainActivity.getIntent().getExtras();
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
		Util.trackScreen(getResources().getString(R.string.analytics_favorites_fragment));
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_main, container, false);
		if (!mainActivity.isFinishing()) {
			welcomeLayout = (RelativeLayout) rootView.findViewById(R.id.welcome);
			if (favoritesAdapter == null) {
				favoritesAdapter = new FavoritesAdapter(mainActivity);
				favoritesAdapter.setArrivalsAndBikeStations(trainArrivals, busArrivals, bikeStations);
			}
			final ListView listView = (ListView) rootView.findViewById(R.id.favorites_list);
			listView.setAdapter(favoritesAdapter);
			startRefreshTask();
			final FloatingActionButton floatingButton = (FloatingActionButton) rootView.findViewById(R.id.floating_button);
			floatingButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					final Intent intent = new Intent(mainActivity, SearchActivity.class);
					intent.putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) bikeStations);
					mainActivity.startActivity(intent);
				}
			});
			swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
			swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
					final boolean loadTrain = sharedPref.getBoolean("cta_train", true);
					final boolean loadBus = sharedPref.getBoolean("cta_bus", true);
					final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);

					final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
					final List<Integer> trainFavorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
					for (final Integer fav : trainFavorites) {
						params.put("mapid", String.valueOf(fav));
					}
					final MultiValuedMap<String, String> params2 = new ArrayListValuedHashMap<>();
					final List<String> busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
					for (final String str : busFavorites) {
						final String[] fav = Util.decodeBusFavorite(str);
						params2.put("rt", fav[0]);
						params2.put("stpid", fav[1]);
					}
					try {
						final GlobalConnectTask task = new GlobalConnectTask(FavoritesFragment.this, FavoritesFragment.class,
								CtaRequestType.TRAIN_ARRIVALS, params, CtaRequestType.BUS_ARRIVALS, params2, loadTrain, loadBus, loadBike);
						task.execute((Void) null);
					} catch (ParserException e) {
						ChicagoTracker.displayError(mainActivity, e);
						return;
					}
					// Google analytics
					if (loadTrain) {
						Util.trackAction(mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_train,
								R.string.analytics_action_get_train_arrivals, 0);
					}
					if (loadBus) {
						Util.trackAction(mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_bus,
								R.string.analytics_action_get_bus_arrival, 0);
					}
					if (loadBike) {
						Util.trackAction(mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
								R.string.analytics_action_get_divvy_all, 0);
					}
					// Check if bus or bike data are not loaded. If not, load them.
					// Can happen when the app has been loaded without any data connection
					boolean loadData = false;
					final DataHolder dataHolder = DataHolder.getInstance();

					final BusData busData = dataHolder.getBusData();

					final Bundle bundle = mainActivity.getIntent().getExtras();
					final List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");

					if (loadBus && busData.getRoutes() != null && busData.getRoutes().size() == 0) {
						loadData = true;
					}
					if (!loadData && loadBike && bikeStations == null) {
						loadData = true;
					}
					if (loadData) {
						//startRefreshAnimation();
						//new LoadData().execute();
					}
					Util.trackAction(mainActivity, R.string.analytics_category_ui, R.string.analytics_action_press,
							R.string.analytics_action_refresh_fav, 0);
				}
			});
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
		if (welcomeLayout != null) {
			boolean hasFav = Preferences.hasFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, ChicagoTracker.PREFERENCE_FAVORITES_BUS,
					ChicagoTracker.PREFERENCE_FAVORITES_BIKE);
			if (!hasFav) {
				welcomeLayout.setVisibility(View.VISIBLE);
			} else {
				welcomeLayout.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		//((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
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
	 * @param trainArrivals the train arrivals list
	 * @param busArrivals   the bus arrivals list
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals,
			final List<BikeStation> bikeStations, final Boolean trainBoolean, final Boolean busBoolean, final Boolean bikeBoolean,
			final Boolean networkAvailable) {
		if (!networkAvailable) {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
		} else {
			// Put into intent new bike stations data
			mainActivity.getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) bikeStations);
			//mainActivity.onNewIntent(mainActivity.getIntent());

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
			}
		}, 100);
		stopRefreshing();
	}

	/**
	 * Display error
	 *
	 * @param trackerException the exception
	 */
	public final void displayError(final TrackerException trackerException) {
		ChicagoTracker.displayError(mainActivity, trackerException);
	}

	public final void setBikeStations(final List<BikeStation> bikeStations) {
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

	public void startRefreshing() {
		swipeRefreshLayout.setRefreshing(true);
	}

	public void stopRefreshing() {
		swipeRefreshLayout.setRefreshing(false);
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