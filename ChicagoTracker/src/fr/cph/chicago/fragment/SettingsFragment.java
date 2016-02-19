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
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.util.Util;

public class SettingsFragment extends PreferenceFragmentCompat {

	/**
	 * The fragment argument representing the section number for this fragment.
	 **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/**
	 * The main activity
	 **/
	private MainActivity mainActivity;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 *
	 * @param sectionNumber the section number
	 * @return the fragment
	 */
	public static SettingsFragment newInstance(final int sectionNumber) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
		mainActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setHasOptionsMenu(true);

		Util.trackScreen(mainActivity, R.string.analytics_settings_fragment);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
//		for (int i = 0; i <= menu.size(); i++) {
//			int id = menu.getItem(0).getItemId();
//			menu.removeItem(id);
//		}
		super.onPrepareOptionsMenu(menu);
	}
}
