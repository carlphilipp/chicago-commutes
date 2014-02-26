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

package fr.cph.chicago.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BaseActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.TrackerException;

/**
 * Favorites Fragment
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesFragment extends Fragment {

	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** Tag **/
	private static final String TAG = "FavoritesFragment";
	/** The activity **/
	private MainActivity mActivity;
	/** The adapter of the fragment **/
	private FavoritesAdapter ada;
	/** A refresh task **/
	private RefreshTask refreshTimingTask;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 * @return
	 */
	public static final FavoritesFragment newInstance(final int sectionNumber) {
		FavoritesFragment fragment = new FavoritesFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ada == null) {
			if (ChicagoTracker.getTrainArrivals() == null && ChicagoTracker.getBusArrivals() == null) {
				Intent intent = new Intent(mActivity, BaseActivity.class);
				intent.putExtra("error", true);
				mActivity.finish();
				startActivity(intent);
			}
			ada = new FavoritesAdapter(mActivity);
			ada.setArrivals(ChicagoTracker.getTrainArrivals(), ChicagoTracker.getBusArrivals());
		}

	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		RelativeLayout welcome = (RelativeLayout) rootView.findViewById(R.id.welcome);
		boolean hasFav = Preferences.hasFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		if (!hasFav) {
			welcome.setVisibility(View.VISIBLE);
		}

		ListView listView = (ListView) rootView.findViewById(R.id.favorites_list);
		listView.setAdapter(ada);

		startRefreshTask();

		return rootView;
	}

	@Override
	public final void onPause() {
		super.onPause();
		refreshTimingTask.cancel(true);
	}

	@Override
	public final void onStop() {
		super.onStop();
		refreshTimingTask.cancel(true);
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		refreshTimingTask.cancel(true);
	}

	@Override
	public final void onResume() {
		super.onResume();
		ada.setFavorites();
		ada.notifyDataSetChanged();
		if (refreshTimingTask.getStatus() == Status.FINISHED) {
			startRefreshTask();
		}

	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	/**
	 * Reload data
	 * 
	 * @param trainArrivals
	 *            the train arrivals list
	 * @param busArrivals
	 *            the bus arrivals list
	 */
	public final void reloadData(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals) {
		// startRefreshTask();
		ada.setArrivals(trainArrivals, busArrivals);
		ada.refreshUpdated();
		ada.refreshUpdatedView();
		ada.notifyDataSetChanged();
		((MainActivity) mActivity).stopRefreshAnimation();
	}

	/**
	 * Display error
	 * 
	 * @param trackerException
	 *            the exception
	 */
	public final void displayError(TrackerException trackerException) {
		ChicagoTracker.displayError(mActivity, trackerException);
	}

	/**
	 * Start refresh task
	 */
	private void startRefreshTask() {
		refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		ada.refreshUpdatedView();
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
			ada.refreshUpdatedView();
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