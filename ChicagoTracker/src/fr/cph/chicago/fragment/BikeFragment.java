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

/**
 * Created by carl on 11/15/13.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.Fragment;
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

/**
 * Bike Fragment
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeFragment extends Fragment {
	/** Tag **/
	private static final String TAG = "BikeFragment";
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** The main actvity **/
	private MainActivity mActivity;
	/** Adapter **/
	private BikeAdapter ada;
	/** Bike data **/
	private List<BikeStation> bikeStations;
	/** Root view **/
	private View rootView;
	/** Loading layout **/
	private RelativeLayout loadingLayout;
	/** Desactivated layout **/
	private RelativeLayout desactivatedLayout;
	/** The list view **/
	private ListView listView;
	/** The filter text view **/
	private TextView filterView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static BikeFragment newInstance(final int sectionNumber) {
		BikeFragment fragment = new BikeFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.bikeStations = savedInstanceState.getParcelableArrayList("bikeStations");
		} else {
			Bundle bundle = mActivity.getIntent().getExtras();
			this.bikeStations = bundle.getParcelableArrayList("bikeStations");
		}
		if (this.bikeStations == null) {
			this.bikeStations = new ArrayList<BikeStation>();
		}
		setHasOptionsMenu(true);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_bus, container, false);
		if (!mActivity.isFinishing()) {
			loadingLayout = (RelativeLayout) rootView.findViewById(R.id.loading_relativeLayout);
			desactivatedLayout = (RelativeLayout) rootView.findViewById(R.id.desactivated_layout);
			listView = (ListView) rootView.findViewById(R.id.bus_list);
			filterView = (TextView) rootView.findViewById(R.id.bus_filter);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
			boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
			if (loadBike) {
				if (bikeStations == null && bikeStations.size() != 0) {
					loadList();
				} else {
					loadingLayout.setVisibility(RelativeLayout.VISIBLE);
					listView.setVisibility(ListView.INVISIBLE);
					filterView.setVisibility(TextView.INVISIBLE);
					new WaitForRefreshData().execute();
				}
			}else{
				desactivatedLayout.setVisibility(RelativeLayout.VISIBLE);
				filterView.setVisibility(TextView.INVISIBLE);
			}
		}
		return rootView;
	}

	private final void loadList() {
		EditText filter = (EditText) rootView.findViewById(R.id.bus_filter);
		if (ada == null) {
			ada = new BikeAdapter(mActivity);
		}
		listView.setAdapter(ada);
		filter.addTextChangedListener(new TextWatcher() {

			private List<BikeStation> bikeStations = null;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				this.bikeStations = new ArrayList<BikeStation>();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				for (BikeStation bikeStation : BikeFragment.this.bikeStations) {
					if (StringUtils.containsIgnoreCase(bikeStation.getName(), s.toString().trim())) {
						this.bikeStations.add(bikeStation);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				ada.setBikeStations(this.bikeStations);
				ada.notifyDataSetChanged();
			}
		});
		listView.setVisibility(ListView.VISIBLE);
		filterView.setVisibility(ListView.VISIBLE);
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
	}

	@Override
	public final void onSaveInstanceState(final Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			MenuItem menuItem = item;
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();

			new DivvyAsyncTask().execute();
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

	private final class WaitForRefreshData extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {
			Bundle bundle = BikeFragment.this.mActivity.getIntent().getExtras();
			List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");
			int i = 0;
			while ((bikeStations == null || bikeStations.size() == 0) && i < 10) {
				try {
					Thread.sleep(100);
					bundle = BikeFragment.this.mActivity.getIntent().getExtras();
					bikeStations = bundle.getParcelableArrayList("bikeStations");
					i++;
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return bundle.getParcelableArrayList("bikeStations").size() == 0;
		}

		@Override
		protected final void onPostExecute(final Boolean result) {
			if (result) {
				loadError();
			} else {
				loadList();
			}
		}
	}

	private final void loadError() {
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		RelativeLayout loadingLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		loadingLayout.setVisibility(RelativeLayout.VISIBLE);
	}

	private final class DivvyAsyncTask extends AsyncTask<Void, Void, List<BikeStation>> {

		@Override
		protected List<BikeStation> doInBackground(Void... params) {
			List<BikeStation> bikeStations = new ArrayList<BikeStation>();
			try {
				Json json = new Json();
				DivvyConnect divvyConnect = DivvyConnect.getInstance();
				String bikeContent = divvyConnect.connect();
				bikeStations = json.parseStations(bikeContent);
				Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				bikeStations = new ArrayList<BikeStation>();
				throw new ConnectException("derp");
			} catch (ConnectException e) {
				BikeFragment.this.mActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "Error, try again later!", Toast.LENGTH_LONG).show();
					}
				});
				Log.e(TAG, "Connect error", e);
			} catch (ParserException e) {
				BikeFragment.this.mActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChicagoTracker.getAppContext(), "Error, try again later!", Toast.LENGTH_LONG).show();
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
				BikeFragment.this.ada.setBikeStations(result);
				BikeFragment.this.ada.notifyDataSetChanged();
				// Put in main activity the new list of bikes
				BikeFragment.this.mActivity.getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) result);
				BikeFragment.this.mActivity.onNewIntent(mActivity.getIntent());
			}
			BikeFragment.this.mActivity.stopRefreshAnimation();
		}
	}

}
