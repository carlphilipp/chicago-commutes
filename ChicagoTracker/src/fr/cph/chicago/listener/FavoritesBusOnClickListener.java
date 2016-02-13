/**
 * Copyright 2016 Carl-Philipp Harmant
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
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

/**
 * Favorites bus on click listener
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesBusOnClickListener implements OnClickListener {
	/** The main activity **/
	private MainActivity mActivity;
	/** The layout that is used to display a fade black background **/
	private FrameLayout mFirstLayout;
	/** Bus route **/
	private BusRoute mBusRoute;
	/** Map bus arrivals **/
	private Map<String, List<BusArrival>> mMapBusArrivals;

	/**
	 * @param activity
	 * @param firstLayout
	 * @param busRoute
	 * @param mapBusArrivals
	 */
	public FavoritesBusOnClickListener(final MainActivity activity, final FrameLayout firstLayout, final BusRoute busRoute,
			final Map<String, List<BusArrival>> mapBusArrivals) {
		this.mActivity = activity;
		this.mFirstLayout = firstLayout;
		this.mBusRoute = busRoute;
		this.mMapBusArrivals = mapBusArrivals;
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
			final List<BusArrival> busArrivals = new ArrayList<BusArrival>();

			ListView listView = (ListView) popupView.findViewById(R.id.details);
			final List<String> values = new ArrayList<String>();
			Set<Entry<String, List<BusArrival>>> entrySet = mMapBusArrivals.entrySet();
			for (Entry<String, List<BusArrival>> entry : entrySet) {
				StringBuilder sb = new StringBuilder();
				sb.append("Open details");
				if (entrySet.size() > 1) {
					sb.append(" (" + entry.getKey() + ")");
				}
				values.add(sb.toString());
			}
			for (Entry<String, List<BusArrival>> entry : entrySet) {
				List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
				busArrivals.addAll(arrivals);
				for (BusArrival arrival : arrivals) {
					StringBuilder sb = new StringBuilder();
					sb.append("Follow bus - " + arrival.getTimeLeftDueDelay());
					if (entrySet.size() > 1) {
						sb.append(" (" + entry.getKey() + ")");
					}
					values.add(sb.toString());
				}
			}
			values.add("Follow all buses on line " + mBusRoute.getId());

			PopupBusAdapter ada = new PopupBusAdapter(mActivity, values);
			listView.setAdapter(ada);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					int i = 0;
					for (Entry<String, List<BusArrival>> entry : mMapBusArrivals.entrySet()) {
						BusArrival busArrival = entry.getValue().get(0);
						if (position == i) {
							mActivity.startRefreshAnimation();
							new FavoritesAdapter.BusBoundAsyncTask(mActivity).execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
									String.valueOf(busArrival.getStopId()), mBusRoute.getName());
							popup.dismiss();
						}
						i++;
					}
					for (Entry<String, List<BusArrival>> entry : mMapBusArrivals.entrySet()) {
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
								popup.dismiss();
							}
							i++;
						}
					}
					if (position == i) {
						Set<String> bounds = new HashSet<String>();
						Iterator<Entry<String, List<BusArrival>>> itr = mMapBusArrivals.entrySet().iterator();
						while (itr.hasNext()) {
							String derp = itr.next().getKey();
							bounds.add(derp);
						}
						Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
						Bundle extras = new Bundle();
						extras.putString("busRouteId", mBusRoute.getId());
						extras.putStringArray("bounds", bounds.toArray(new String[bounds.size()]));
						intent.putExtras(extras);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
						popup.dismiss();
					}
				}
			});
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
			mFirstLayout.getForeground().setAlpha(210);

			popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					mFirstLayout.getForeground().setAlpha(0);
				}
			});

			popup.showAtLocation(mFirstLayout, Gravity.CENTER, 0, 0);
		}
	}
}
