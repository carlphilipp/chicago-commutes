package fr.cph.chicago.fragment;

/**
 * Created by carl on 11/15/13.
 */

import java.io.IOException;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.BusAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.xml.Xml;

/**
 * A placeholder fragment containing a simple view.
 */
public class BusFragment extends Fragment {
	
	private static final String TAG = "BusFragment";
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private BusAdapter ada;

	private Menu menu;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static BusFragment newInstance(int sectionNumber) {
		BusFragment fragment = new BusFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public BusFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_bus, container, false);
		ada = new BusAdapter();
		ListView listView = (ListView) rootView.findViewById(R.id.bus_list);
		listView.setAdapter(ada);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				ada.updateDetails(childView, position);
			}
			
		});
			
//			private TextView loading;
//			private LinearLayout detailsLayout;
//			
//			@Override
//			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
//				RelativeLayout view = (RelativeLayout) childView;
//				detailsLayout = (LinearLayout) view.findViewById(R.id.route_details);
//				detailsLayout.setVisibility(LinearLayout.VISIBLE);
//				loading = (TextView) detailsLayout.findViewById(R.id.loading_text_view);
//				
//				BusRoute busRoute = (BusRoute) ada.getItem(position);
//				
//				String stopId = busRoute.getId();
//				
//				new DirectionAsyncTask().execute(stopId);
//				Log.i(TAG, "Click");
//			}
//			
//			class DirectionAsyncTask extends AsyncTask<String, Void, BusDirections> {
//				@Override
//				protected BusDirections doInBackground(String... params) {
//					Log.i(TAG, "doInBackground");
//					CtaConnect connect = CtaConnect.getInstance();
//					BusDirections busDirections = null;
//					try {
//						MultiMap<String,String> reqParams = new MultiValueMap<String, String>();
//						reqParams.put("rt", params[0]);
//						Xml xml = new Xml();
//						String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
//						busDirections = xml.parseBusDirections(xmlResult, params[0]);
//					} catch (IOException e) {
//						e.printStackTrace();
//					} catch (XmlPullParserException e) {
//						e.printStackTrace();
//					}
//					return busDirections;
//				}
//				@Override
//				protected void onPostExecute(BusDirections result) {
//					loading.setVisibility(TextView.GONE);
//					for(BusDirection busDirection :  result.getlBusDirection()){
//						TextView textView = new TextView(TrainTracker.getAppContext());
//						textView.setText(busDirection.toString()+ " ");
//						Log.i(TAG, "Loading view "+ loading.getId() +" Update view " + detailsLayout.getId() + " with " + textView.getText());
//						detailsLayout.addView(textView);
//					}
//				}
//			}
//		}


		new LoadBusRoutes().execute();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.action_refresh:
			MenuItem menuItem = item;
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();
			
			new LoadBusRoutes().execute();

			Toast.makeText(this.getActivity(), "Refresh...!", Toast.LENGTH_SHORT).show();
			return true;
		}
		return true;
	}

	public void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	private class LoadBusRoutes extends AsyncTask<Void, Void, BusData> {

		@Override
		protected BusData doInBackground(Void... params) {
			BusData data = BusData.getInstance();
			data.read();
			return data;
		}

		@Override
		protected void onPostExecute(BusData result) {
			DataHolder.getInstance().setBusData(result);
			ada.setBusData();
			ada.notifyDataSetChanged();
			stopRefreshAnimation();
		}
	}
}
