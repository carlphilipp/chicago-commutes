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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.TrainStationActivity;
import fr.cph.chicago.adapter.TrainStationAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class TrainFragment extends Fragment {
	private static final String ARG_SECTION_NUMBER = "section_number";
	private MainActivity mainActivity;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 * @return
	 */
	public static TrainFragment newInstance(final int sectionNumber) {
		final TrainFragment fragment = new TrainFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.trackScreen(getResources().getString(R.string.analytics_train_fragment));
	}

	@Override
	public final void onAttach(final Context context) {
		super.onAttach(context);
		mainActivity = context instanceof Activity ? (MainActivity) context : null;
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_train, container, false);
		final TrainStationAdapter ada = new TrainStationAdapter();
		final ListView listView = (ListView) rootView.findViewById(R.id.train_list);
		listView.setAdapter(ada);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				if (Util.isNetworkAvailable()) {
					final Intent intent = new Intent(mainActivity, TrainStationActivity.class);
					final Bundle extras = new Bundle();
					final String line = TrainLine.values()[position].toString();
					extras.putString("line", line);
					intent.putExtras(extras);
					startActivity(intent);
				} else {
					Toast.makeText(ChicagoTracker.getContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		return rootView;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}