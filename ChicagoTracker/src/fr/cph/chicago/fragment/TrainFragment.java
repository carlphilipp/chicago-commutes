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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.TrainStationActivity;
import fr.cph.chicago.adapter.TrainStationAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class TrainFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static TrainFragment newInstance(int sectionNumber) {
		TrainFragment fragment = new TrainFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public TrainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_train, container, false);
		
		TrainStationAdapter ada = new TrainStationAdapter();
		ListView listView = (ListView) rootView.findViewById(R.id.train_list);
		listView.setAdapter(ada);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				Intent intent = new Intent(TrainFragment.this.getView().getContext(), TrainStationActivity.class);
				Bundle extras = new Bundle();
				String line = TrainLine.values()[position].toString();
				extras.putString("line", line);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}
}
