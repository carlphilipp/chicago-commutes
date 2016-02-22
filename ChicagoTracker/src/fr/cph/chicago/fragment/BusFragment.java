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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.BusAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusFragment extends Fragment {
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** The main actvity **/
	private MainActivity mainActivity;
	/** Adapter **/
	private BusAdapter busAdapter;

	private EditText textFilter;

	private ListView listView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static BusFragment newInstance(final int sectionNumber) {
		final BusFragment fragment = new BusFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Context context){
		super.onAttach(context);
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		//((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(mainActivity);
		Util.trackScreen(getResources().getString(R.string.analytics_bus_fragment));
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_bus, container, false);
		if (!mainActivity.isFinishing()) {
			textFilter = (EditText) rootView.findViewById(R.id.bus_filter);
			listView = (ListView) rootView.findViewById(R.id.bus_list);
			if (Util.isNetworkAvailable()) {
				addView();
			} else {
				Toast.makeText(ChicagoTracker.getAppContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
			}
		}
		return rootView;
	}

	public final void update() {
		addView();
	}

	private void addView() {
		busAdapter = new BusAdapter(mainActivity);
		listView.setAdapter(busAdapter);
		textFilter.setVisibility(TextView.VISIBLE);
		textFilter.addTextChangedListener(new TextWatcher() {

			final BusData busData = DataHolder.getInstance().getBusData();
			List<BusRoute> busRoutes = null;

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
				busRoutes = new ArrayList<>();
			}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before,final  int count) {
				final List<BusRoute> busRoutes = busData.getRoutes();
				for (final BusRoute busRoute : busRoutes) {
					if (StringUtils.containsIgnoreCase(busRoute.getId(), s.toString().trim())
							|| StringUtils.containsIgnoreCase(busRoute.getName(), s.toString().trim())) {
						this.busRoutes.add(busRoute);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				busAdapter.setRoutes(busRoutes);
				busAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
