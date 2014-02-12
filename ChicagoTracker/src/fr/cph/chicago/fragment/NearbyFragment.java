package fr.cph.chicago.fragment;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.NearbyActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.TrackerException;

public class NearbyFragment extends Fragment {

	private static final String TAG = "NearbyFragment";

	private static MainActivity mActivity;
	
	private View loadLayout;
	
	private Menu menu;

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static NearbyFragment newInstance(final int sectionNumber) {
		NearbyFragment fragment = new NearbyFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.loading, container, false);
		loadLayout = rootView.findViewById(R.id.loading_layout);
		

		showProgress(true, null);
		new LoadNearby().execute();
		
		return rootView;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		this.menu = menu;
		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
//			MenuItem menuItem = item;
//			menuItem.setActionView(R.layout.progressbar);
//			menuItem.expandActionView();
			Toast.makeText(this.getActivity(), "Refresh...!", Toast.LENGTH_SHORT).show();
			return true;
		}
		return true;
	}

	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}
	
	/**
	 * Show progress bar
	 * 
	 * @param show
	 *            show the bar or not
	 * @param errorMessage
	 *            the error message
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private final void showProgress(final boolean show, final String errorMessage) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			loadLayout.setVisibility(View.VISIBLE);
			loadLayout.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			loadLayout.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	/**
	 * Load Bus and train data into DataHolder
	 * 
	 * @author Carl-Philipp Harmant
	 * 
	 */
	private final class LoadNearby extends AsyncTask<Void, Void, List<BusStop>> implements LocationListener {
		
		private static final String TAG = "LoadNearby";
		
		private Location location;

		@Override
		protected final List<BusStop> doInBackground(final Void... params) {
			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();
			
			LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, Looper.getMainLooper());
			
			Position positon = new Position();
			while(location == null){
				Log.i(TAG, "location null");
			}
			positon.setLatitude(location.getLatitude());
			positon.setLongitude(location.getLongitude());
			List<BusStop> busStops = busData.readNearbyStops(positon);
			
			return busStops;
		}

		@Override
		protected final void onPostExecute(final List<BusStop> result) {
			for(BusStop busStop : result){
				Log.i(TAG, busStop.toString());
			}
			load(result);
		}

		@Override
		public final void onLocationChanged(final Location location) {
			this.location = location;
		}

		@Override
		public final void onProviderDisabled(final String provider) {
			Log.e(TAG,"disable");
		}

		@Override
		public final void onProviderEnabled(final String provider) {
			Log.e(TAG,"enable");
		}

		@Override
		public final void onStatusChanged(final String provider, final int status, final Bundle extras) {
			Log.d(TAG,"status");
		}
	}
	
	public final void load(List<BusStop> buses) {
		Log.i(TAG, buses.size() + " ");
		ChicagoTracker.setGeoBusStops(buses);
		Intent intent = new Intent(mActivity, NearbyActivity.class);
		showProgress(false, null);
		startActivity(intent);
		mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	/**
	 * Load error
	 * 
	 */
	public final void displayError(final TrackerException exceptionToBeThrown) {
		DataHolder.getInstance().setTrainData(null);
		DataHolder.getInstance().setBusData(null);
		showProgress(false, null);
		ChicagoTracker.displayError(mActivity, exceptionToBeThrown);
		mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

}
