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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.BusAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusRoute;

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
	private MainActivity mActivity;
	/** Adapter **/
	private BusAdapter ada;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static BusFragment newInstance(final int sectionNumber) {
		BusFragment fragment = new BusFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(mActivity);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_bus, container, false);
		if (!mActivity.isFinishing()) {
			EditText filter = (EditText) rootView.findViewById(R.id.bus_filter);
			ListView listView = (ListView) rootView.findViewById(R.id.bus_list);
			if (ada == null) {
				ada = new BusAdapter(mActivity);
			}
			listView.setAdapter(ada);
			filter.addTextChangedListener(new TextWatcher() {

				private BusData busData = DataHolder.getInstance().getBusData();
				private List<BusRoute> busRoutes = null;

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					busRoutes = new ArrayList<BusRoute>();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					List<BusRoute> busRoutes = busData.getRoutes();
					for (BusRoute busRoute : busRoutes) {
						if (StringUtils.containsIgnoreCase(busRoute.getId(), s.toString().trim())
								|| StringUtils.containsIgnoreCase(busRoute.getName(), s.toString().trim())) {
							this.busRoutes.add(busRoute);
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					ada.setRoutes(busRoutes);
					ada.notifyDataSetChanged();
				}
			});
		}
		return rootView;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
