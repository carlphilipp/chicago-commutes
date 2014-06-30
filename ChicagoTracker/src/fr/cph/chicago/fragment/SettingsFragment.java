package fr.cph.chicago.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;

public class SettingsFragment extends PreferenceFragment {
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	
	private MainActivity mActivity;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static final SettingsFragment newInstance(final int sectionNumber) {
		SettingsFragment fragment = new SettingsFragment();
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
/*		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
		boolean loadTrain = sharedPref.getBoolean("cta_train", true);
		boolean loadBus = sharedPref.getBoolean("cta_bus", true);
		boolean loadAlert = sharedPref.getBoolean("cta_alert", true);
		boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
		Log.i(TAG, "loadTrain:" + loadTrain + " loadBus:" + loadBus +" loadAlert:" + loadAlert+" loadBike:" + loadBike);*/
	}
}
