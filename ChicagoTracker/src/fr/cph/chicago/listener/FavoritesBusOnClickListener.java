package fr.cph.chicago.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.adapter.PopupBusAdapter;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.util.Util;

public class FavoritesBusOnClickListener implements OnClickListener {
	/** Tag **/
	private static final String TAG = "FavoritesBusOnClickListener";
	/** The main activity **/
	private MainActivity mActivity;
	/** The layout that is used to display a fade black background **/
	private FrameLayout firstLayout;
	/** Bus route **/
	private BusRoute busRoute;
	/** Map bus arrivals **/
	private Map<String, List<BusArrival>> mapBusArrivals;

	public FavoritesBusOnClickListener(final MainActivity activity, final FrameLayout firstLayout, final BusRoute busRoute,
			final Map<String, List<BusArrival>> mapBusArrivals) {
		this.mActivity = activity;
		this.firstLayout = firstLayout;
		this.busRoute = busRoute;
		this.mapBusArrivals = mapBusArrivals;
	}

	@Override
	public void onClick(final View v) {
		if (!Util.isNetworkAvailable()) {
			Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
		} else {
			LayoutInflater layoutInflater = (LayoutInflater) mActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = layoutInflater.inflate(R.layout.popup_bus, null);
			
			int[] screenSize = Util.getScreenSize();
			final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);
			
/*			if (mapBusArrivals.entrySet().size() == 1) {
				final List<BusArrival> busArrivals = BusArrival.getRealBusArrival(mapBusArrivals.entrySet().iterator().next().getValue());
				
				ListView listView = (ListView) popupView.findViewById(R.id.details);
				final List<String> values = new ArrayList<String>();
				values.add("Open details");
				for (BusArrival arrival : busArrivals) {
					values.add("Follow bus - " + arrival.getTimeLeftDueDelay() + "");
				}
				values.add("Follow all buses on line " + busRoute.getId());
				
				PopupBusAdapter ada = new PopupBusAdapter(mActivity, values);
				listView.setAdapter(ada);
				
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (position == 0) {
							BusArrival busArrival = mapBusArrivals.entrySet().iterator().next().getValue().get(0);
							mActivity.startRefreshAnimation();
							new FavoritesAdapter.BusBoundAsyncTask(mActivity).execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
									String.valueOf(busArrival.getStopId()), busRoute.getName());
							popup.dismiss();
						} else if (position == values.size() - 1) {
							Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
							Bundle extras = new Bundle();
							Set<String> bounds = new HashSet<String>();
							Iterator<Entry<String, List<BusArrival>>> itr = mapBusArrivals.entrySet().iterator();
							while (itr.hasNext()) {
								String temp = itr.next().getKey();
								Log.i(TAG, "Add: " + temp);
								bounds.add(temp);
							}
							if (busArrivals.size() == 0) {
								extras.putString("busRouteId", busRoute.getId());
								extras.putStringArray("bounds", bounds.toArray(new String[bounds.size()]));
							} else {
								extras.putString("busRouteId", busArrivals.get(0).getRouteId());
								extras.putStringArray("bounds", bounds.toArray(new String[bounds.size()]));
							}

							intent.putExtras(extras);
							mActivity.startActivity(intent);
							mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
							popup.dismiss();
						}else{
							for (int i = 0; i < busArrivals.size(); i++) {
								if (position == i + 1) {
									Toast.makeText(mActivity, "" + busArrivals.get(i).getTimeLeftDueDelay() + " " + busArrivals.get(i).getBusId(),
											Toast.LENGTH_SHORT).show();
									Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
									Bundle extras = new Bundle();
									extras.putInt("busId", busArrivals.get(i).getBusId());
									extras.putString("busRouteId", busArrivals.get(i).getRouteId());
									extras.putStringArray("bounds", new String[] { busArrivals.get(i).getRouteDirection() });
									intent.putExtras(extras);
									mActivity.startActivity(intent);
									mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
								}
							}
							popup.dismiss();
						}
					}
				});
			} else {*/
				final List<BusArrival> busArrivals = new ArrayList<BusArrival>();
				
				ListView listView = (ListView) popupView.findViewById(R.id.details);
				final List<String> values = new ArrayList<String>();
				Set<Entry<String, List<BusArrival>>> entrySet = mapBusArrivals.entrySet();
				for (Entry<String, List<BusArrival>> entry : entrySet) {
					StringBuilder sb = new StringBuilder();
					sb.append("Open details");
					if(entrySet.size() > 1){
						sb.append(" " + entry.getKey() + ")");
					}
					values.add(sb.toString());
				}
				for (Entry<String, List<BusArrival>> entry : entrySet) {
					List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
					busArrivals.addAll(arrivals);
					for (BusArrival arrival : arrivals) {
						StringBuilder sb = new StringBuilder();
						sb.append("Follow bus - " + arrival.getTimeLeftDueDelay());
						if(entrySet.size() > 1){
							sb.append(" (" + entry.getKey() + ")");
						}
						values.add(sb.toString());
					}
				}
				values.add("Follow all buses on line " + busRoute.getId());
				
				PopupBusAdapter ada = new PopupBusAdapter(mActivity, values);
				listView.setAdapter(ada);
				
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
						int i = 0;
						for (Entry<String, List<BusArrival>> entry : mapBusArrivals.entrySet()) {
							BusArrival busArrival = entry.getValue().get(0);
							if (position == i) {
								mActivity.startRefreshAnimation();
								new FavoritesAdapter.BusBoundAsyncTask(mActivity).execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
										String.valueOf(busArrival.getStopId()), busRoute.getName());
								popup.dismiss();
							}
							i++;
						}
						for (Entry<String, List<BusArrival>> entry : mapBusArrivals.entrySet()) {
							List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
							for (BusArrival arrival : arrivals) {
								if (position == i) {
									Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
									Bundle extras = new Bundle();
									extras.putInt("busId", arrival.getBusId());
									extras.putString("busRouteId", arrival.getRouteId());
									extras.putStringArray("bounds", new String[] { entry.getKey() });
									intent.putExtras(extras);
									mActivity.startActivity(intent);
									mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
									Toast.makeText(mActivity, "" + arrival.getTimeLeftDueDelay() + " " + arrival.getBusId(), Toast.LENGTH_SHORT)
											.show();
									popup.dismiss();
								}
								i++;
							}
						}
						if (position == i) {
							Set<String> bounds = new HashSet<String>();
							Iterator<Entry<String, List<BusArrival>>> itr = mapBusArrivals.entrySet().iterator();
							while (itr.hasNext()) {
								String derp = itr.next().getKey();
								bounds.add(derp);
							}
							Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
							Bundle extras = new Bundle();
							extras.putString("busRouteId", busRoute.getId());
							extras.putStringArray("bounds", bounds.toArray(new String[bounds.size()]));
							intent.putExtras(extras);
							mActivity.startActivity(intent);
							mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
							popup.dismiss();
						}
					}
				});
			//}
			
			
			
			
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
			firstLayout.getForeground().setAlpha(210);
			
			popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					firstLayout.getForeground().setAlpha(0);
				}
			});

			popup.showAtLocation(firstLayout, Gravity.CENTER, 0, 0);
		}
	}

}
