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
import android.content.Context;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.BikeAdapter;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeFragment extends Fragment {

	private static final String TAG = BikeFragment.class.getSimpleName();
	private static final String ARG_SECTION_NUMBER = "section_number";

	private View rootView;
	private RelativeLayout loadingLayout;
	private ListView listView;
	private TextView filterView;

	private MainActivity mainActivity;
	private BikeAdapter bikeAdapter;
	private List<BikeStation> bikeStations;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static BikeFragment newInstance(final int sectionNumber) {
		final BikeFragment fragment = new BikeFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Context context) {
		super.onAttach(context);
		mainActivity = context instanceof Activity ? (MainActivity) context : null;
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.bikeStations = savedInstanceState.getParcelableArrayList("bikeStations");
		} else {
			Bundle bundle = mainActivity.getIntent().getExtras();
			this.bikeStations = bundle.getParcelableArrayList("bikeStations");
		}
		if (this.bikeStations == null) {
			this.bikeStations = new ArrayList<>();
		}
		setHasOptionsMenu(true);

		Util.trackScreen(getResources().getString(R.string.analytics_bike_fragment));
	}

	@Override
	public final void onResume() {
		super.onResume();
		boolean boolTrain = ChicagoTracker.checkTrainData(mainActivity);
		if (boolTrain) {
			ChicagoTracker.checkBusData(mainActivity);
		}

	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_bike, container, false);
		if (!mainActivity.isFinishing()) {
			loadingLayout = (RelativeLayout) rootView.findViewById(R.id.loading_relativeLayout);
			final RelativeLayout desactivatedLayout = (RelativeLayout) rootView.findViewById(R.id.desactivated_layout);
			listView = (ListView) rootView.findViewById(R.id.bike_list);
			filterView = (TextView) rootView.findViewById(R.id.bike_filter);
			if (Util.isNetworkAvailable()) {
				final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
				final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
				if (loadBike) {
					if (bikeStations == null || bikeStations.size() != 0) {
						loadList();
					} else {
						loadingLayout.setVisibility(RelativeLayout.VISIBLE);
						listView.setVisibility(ListView.INVISIBLE);
						filterView.setVisibility(TextView.INVISIBLE);
						new WaitForRefreshData().execute();
					}
				} else {
					desactivatedLayout.setVisibility(RelativeLayout.VISIBLE);
					filterView.setVisibility(TextView.INVISIBLE);
				}
			} else {
				Toast.makeText(ChicagoTracker.getContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
			}
		}
		return rootView;
	}

	private void loadList() {
		final EditText filter = (EditText) rootView.findViewById(R.id.bike_filter);
		if (bikeAdapter == null) {
			bikeAdapter = new BikeAdapter(mainActivity);
		}
		listView.setAdapter(bikeAdapter);
		filter.addTextChangedListener(new TextWatcher() {

			private List<BikeStation> bikeStations = null;

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
				this.bikeStations = new ArrayList<>();
			}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				for (final BikeStation bikeStation : BikeFragment.this.bikeStations) {
					if (StringUtils.containsIgnoreCase(bikeStation.getName(), s.toString().trim())) {
						this.bikeStations.add(bikeStation);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				bikeAdapter.setBikeStations(this.bikeStations);
				bikeAdapter.notifyDataSetChanged();
			}
		});
		listView.setVisibility(ListView.VISIBLE);
		filterView.setVisibility(ListView.VISIBLE);
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		final RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		errorLayout.setVisibility(RelativeLayout.INVISIBLE);
	}

	@Override
	public final void onSaveInstanceState(final Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			final boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
			if (loadBike) {
				item.setActionView(R.layout.progressbar);
				item.expandActionView();

				new DivvyAsyncTask().execute();

				final Bundle bundle = mainActivity.getIntent().getExtras();
				final List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");

				if (bikeStations == null) {
					mainActivity.new LoadBusAndBikeData().execute();
				}
			}
			Util.trackAction(mainActivity, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_bike, 0);
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

	private class WaitForRefreshData extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {
			Bundle bundle = BikeFragment.this.mainActivity.getIntent().getExtras();
			List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");
			int i = 0;
			while ((bikeStations == null || bikeStations.size() == 0) && i < 10) {
				try {
					Thread.sleep(100);
					bundle = BikeFragment.this.mainActivity.getIntent().getExtras();
					bikeStations = bundle.getParcelableArrayList("bikeStations");
					i++;
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			final List<BikeStation> bikeStationsBundle = bundle.getParcelableArrayList("bikeStations");
			return bikeStationsBundle != null && bikeStationsBundle.size() != 0;

		}

		@Override
		protected final void onPostExecute(final Boolean result) {
			if (!result) {
				loadError();
			} else {
				loadList();
			}
		}
	}

	private void loadError() {
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		final RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		errorLayout.setVisibility(RelativeLayout.VISIBLE);
	}

	private class DivvyAsyncTask extends AsyncTask<Void, Void, List<BikeStation>> {
		@Override
		protected List<BikeStation> doInBackground(Void... params) {
			List<BikeStation> bikeStations = new ArrayList<>();
			try {
				final Json json = new Json();
				final DivvyConnect divvyConnect = DivvyConnect.getInstance();
				final String bikeContent = divvyConnect.connect();
				bikeStations = json.parseStations(bikeContent);
				Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				Util.trackAction(BikeFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
						R.string.analytics_action_get_divvy_all, 0);
			} catch (ConnectException e) {
				BikeFragment.this.mainActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getContext(), "Error, try again later!", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e(TAG, "Connect error", e);
			} catch (ParserException e) {
				BikeFragment.this.mainActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getContext(), "Error, try again later!", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e(TAG, "Parser error", e);
			}
			return bikeStations;
		}

		@Override
		protected final void onPostExecute(final List<BikeStation> result) {
			if (result.size() != 0) {
				BikeFragment.this.bikeStations = result;
				if (BikeFragment.this.bikeAdapter == null) {
					BikeFragment.this.loadList();
				}
				BikeFragment.this.bikeAdapter.setBikeStations(result);
				BikeFragment.this.bikeAdapter.notifyDataSetChanged();
				// Put in main activity the new list of bikes
				BikeFragment.this.mainActivity.getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) result);
				//BikeFragment.this.mainActivity.onNewIntent(mainActivity.getIntent());
			}
		}
	}
}
