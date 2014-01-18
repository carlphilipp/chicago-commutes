package fr.cph.chicago.fragment;

/**
 * Created by carl on 11/15/13.
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.task.CtaConnectTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavoritesFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/** Tag **/
	private static final String TAG = "FavoritesFragment";

	private static SparseArray<TrainArrival> arrivals;

	private ListView listView;

	private View rootView;

	private List<Integer> favorites = new ArrayList<Integer>();

	private static Activity mActivity;

	private static FavoritesAdapter ada;

	private DataHolder dataHolder;

	private TrainData data;

	private Menu menu;

	private RefreshTask refreshTimingTask;

	private boolean firstLoad = true;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FavoritesFragment newInstance(int sectionNumber, TrainData data) {
		FavoritesFragment fragment = new FavoritesFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public FavoritesFragment() {
		arrivals = new SparseArray<TrainArrival>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.dataHolder = DataHolder.getInstance();
		this.data = dataHolder.getTrainData();

		rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ada = new FavoritesAdapter(this.getActivity(), arrivals, favorites);
		listView = (ListView) rootView.findViewById(R.id.favorites_list);
		listView.setAdapter(ada);

		// Force onCreateOptionsMenu being called
		setHasOptionsMenu(true);

		refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		loadData();
		super.onCreateOptionsMenu(menu, inflater);
	}

	public void loadData() {
		if (firstLoad) {
			MenuItem menuItem = menu.getItem(1);
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();

			MultiMap<String, String> params = new MultiValueMap<String, String>();
			List<Integer> favorites = Preferences.getFavorites(ChicagoTracker.PREFERENCE_FAVORITES);
			for (Integer fav : favorites) {
				params.put("mapid", String.valueOf(fav));
			}
			try {
				CtaConnectTask task = new CtaConnectTask(FavoritesFragment.class, CtaRequestType.TRAIN_ARRIVALS, params, data, mActivity);
				task.execute((Void) null);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			firstLoad = false;
		}
	}

	private class RefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			ada.refreshUpdatedView();
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (!this.isCancelled()) {
				Log.i(TAG, "Updated of time " + Thread.currentThread().getId());
				try {
					publishProgress();
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Log.v(TAG, "Stopping thread. Normal Behavior");
				}
			}
			return null;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "On start");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, "On stop");
		refreshTimingTask.cancel(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "On pause");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "On resume");
		this.favorites = Preferences.getFavorites(ChicagoTracker.PREFERENCE_FAVORITES);
		ada.setFavorites();
		ada.notifyDataSetChanged();
		if (refreshTimingTask.getStatus() == Status.FINISHED) {
			refreshTimingTask = (RefreshTask) new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		this.favorites = Preferences.getFavorites(ChicagoTracker.PREFERENCE_FAVORITES);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	public static void reloadData(SparseArray<TrainArrival> arri) {
		arrivals.clear();
		arrivals = arri;
		ada.setArrivals(arrivals);
		ada.refreshUpdated();
		ada.notifyDataSetChanged();
		((MainActivity) mActivity).stopRefreshAnimation();
	}

	public static void updateFavorites() {
		ada.setFavorites();
		// ada.notifyDataSetChanged();
	}
}
