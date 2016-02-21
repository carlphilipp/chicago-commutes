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
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.AlertAdapter;
import fr.cph.chicago.data.AlertData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;

/**
 * Alert Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class AlertFragment extends Fragment {
	/** Tag **/
	private static final String TAG = "AlertFragment";
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** Root view **/
	private View rootView;
	/** Loading layout **/
	private RelativeLayout loadingLayout;
	/** Desactivated layout **/
	private RelativeLayout desactivatedLayout;
	/** The list view **/
	private ListView listView;
	/** The main activity **/
	private MainActivity mainActivity;
	/** The adapter **/
	private AlertAdapter alertAdapter;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static AlertFragment newInstance(final int sectionNumber) {
		AlertFragment fragment = new AlertFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		//mainActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.trackScreen(getResources().getString(R.string.analytics_alert_fragment));
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_alert, container, false);
		listView = (ListView) rootView.findViewById(R.id.alert_list);
		desactivatedLayout = (RelativeLayout) rootView.findViewById(R.id.desactivated_layout);
		loadingLayout = (RelativeLayout) rootView.findViewById(R.id.loading_relativeLayout);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
		boolean loadAlert = sharedPref.getBoolean("cta_alert", true);
		if (Util.isNetworkAvailable()) {
			if (loadAlert) {
				if (DataHolder.getInstance().getAlertData() != null) {
					alertAdapter = new AlertAdapter();
					listView.setAdapter(alertAdapter);
				} else {
					loadingLayout.setVisibility(RelativeLayout.VISIBLE);
					listView.setVisibility(ListView.INVISIBLE);
					new WaitForRefreshData().execute();
				}
			} else {
				desactivatedLayout.setVisibility(RelativeLayout.VISIBLE);
			}
		} else {
			Toast.makeText(ChicagoTracker.getAppContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
		}
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			boolean loadAlerts = sharedPref.getBoolean("cta_alert", true);
			if (loadAlerts) {
				DataHolder dataHolder = DataHolder.getInstance();
				AlertData alertData = dataHolder.getAlertData();
				if (alertData == null || alertData.getAlerts().size() == 0) {
					//mainActivity.startRefreshAnimation();
					mainActivity.new LoadData().execute();
				} else {
					//AlertFragment.this.mainActivity.startRefreshAnimation();
					new LoadData().execute();
				}
			}
			Util.trackAction(mainActivity, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_alert, 0);
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

	private class LoadData extends AsyncTask<Void, Void, AlertData> {

		@Override
		protected AlertData doInBackground(Void... params) {
			AlertData alertData = AlertData.getInstance();
			if (Util.isNetworkAvailable()) {
				try {
					alertData.loadGeneralAlerts();
					Util.trackAction(AlertFragment.this.mainActivity, R.string.analytics_category_req, R.string.analytics_action_get_alert,
							R.string.analytics_action_get_alert_general, 0);
				} catch (ParserException | ConnectException e) {
					Log.e(TAG, "Parser error", e);
					AlertFragment.this.mainActivity.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(mainActivity, "A surprising error has occurred. Try again!", Toast.LENGTH_SHORT).show();
						}
					});

				}
			} else {
				AlertFragment.this.mainActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_SHORT).show();
					}
				});
			}
			return alertData;
		}

		@Override
		protected final void onPostExecute(final AlertData result) {
			if (alertAdapter != null) {
				alertAdapter.setAlerts(result.getAlerts());
				alertAdapter.notifyDataSetChanged();
			}
			//AlertFragment.this.mainActivity.stopRefreshAnimation();
		}
	}

	private class WaitForRefreshData extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {
			int i = 0;
			while (DataHolder.getInstance().getAlertData() == null && i < 10) {
				try {
					Thread.sleep(100);
					i++;
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return DataHolder.getInstance().getAlertData() == null;
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

	private void loadError() {
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		errorLayout.setVisibility(RelativeLayout.VISIBLE);
	}

	public final void loadList() {
		AlertAdapter ada = new AlertAdapter();
		listView.setAdapter(ada);
		listView.setVisibility(ListView.VISIBLE);
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		errorLayout.setVisibility(RelativeLayout.INVISIBLE);
	}
}
