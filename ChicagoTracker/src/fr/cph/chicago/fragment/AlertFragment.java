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

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.AlertAdapter;
import fr.cph.chicago.data.DataHolder;

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
	/** The list view **/
	private ListView listView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static final AlertFragment newInstance(final int sectionNumber) {
		AlertFragment fragment = new AlertFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		MainActivity mActivity = (MainActivity) activity;
		mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_alert, container, false);
		listView = (ListView) rootView.findViewById(R.id.alert_list);
		if (DataHolder.getInstance().getAlertData() != null) {
			AlertAdapter ada = new AlertAdapter();
			listView.setAdapter(ada);
		} else {
			loadingLayout = (RelativeLayout) rootView.findViewById(R.id.loading_relativeLayout);
			loadingLayout.setVisibility(RelativeLayout.VISIBLE);
			listView.setVisibility(ListView.INVISIBLE);
			new WaitForRefreshData().execute();
		}
		return rootView;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private final class WaitForRefreshData extends AsyncTask<Void, Void, Boolean> {
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

	private final void loadError() {
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
		RelativeLayout loadingLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
		loadingLayout.setVisibility(RelativeLayout.VISIBLE);
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
	}

	private final void loadList() {
		AlertAdapter ada = new AlertAdapter();
		listView.setAdapter(ada);
		listView.setVisibility(ListView.VISIBLE);
		loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
	}
}
