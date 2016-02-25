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

package fr.cph.chicago.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Favorites bus on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesBusOnClickListener implements OnClickListener {
	/**
	 * The main activity
	 **/
	private MainActivity mainActivity;
	/**
	 * The layout that is used to display a fade black background
	 **/
	private FrameLayout firstLayout;
	/**
	 * Bus route
	 **/
	private BusRoute busRoute;
	/**
	 * Map bus arrivals
	 **/
	private Map<String, List<BusArrival>> mapBusArrivals;

	/**
	 * @param activity
	 * @param firstLayout
	 * @param busRoute
	 * @param mapBusArrivals
	 */
	public FavoritesBusOnClickListener(final MainActivity activity, final FrameLayout firstLayout, final BusRoute busRoute,
			final Map<String, List<BusArrival>> mapBusArrivals) {
		this.mainActivity = activity;
		this.firstLayout = firstLayout;
		this.busRoute = busRoute;
		this.mapBusArrivals = mapBusArrivals;
	}

	@Override
	public void onClick(final View v) {
		if (!Util.isNetworkAvailable()) {
			Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
		} else {
			final LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View popupView = layoutInflater.inflate(R.layout.popup_bus, null);

			final int[] screenSize = Util.getScreenSize();
			final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);
			// TODO see why this is never used. Refactor
			final List<BusArrival> busArrivals = new ArrayList<>();

			final ListView listView = (ListView) popupView.findViewById(R.id.details);
			final List<String> values = new ArrayList<>();
			final Set<Entry<String, List<BusArrival>>> entrySet = mapBusArrivals.entrySet();
			for (final Entry<String, List<BusArrival>> entry : entrySet) {
				final StringBuilder sb = new StringBuilder();
				sb.append("Open details");
				if (entrySet.size() > 1) {
					sb.append(" (").append(entry.getKey()).append(")");
				}
				values.add(sb.toString());
			}
			for (final Entry<String, List<BusArrival>> entry : entrySet) {
				final List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
				busArrivals.addAll(arrivals);
				for (final BusArrival arrival : arrivals) {
					final StringBuilder sb = new StringBuilder();
					sb.append("Follow bus - ").append(arrival.getTimeLeftDueDelay());
					if (entrySet.size() > 1) {
						sb.append(" (").append(entry.getKey()).append(")");
					}
					values.add(sb.toString());
				}
			}
			values.add("Follow all buses on line " + busRoute.getId());

			final PopupBusAdapter ada = new PopupBusAdapter(mainActivity, values);
			listView.setAdapter(ada);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					int i = 0;
					for (final Entry<String, List<BusArrival>> entry : mapBusArrivals.entrySet()) {
						final BusArrival busArrival = entry.getValue().get(0);
						if (position == i) {
							//mainActivity.startRefreshAnimation();
							new FavoritesAdapter.BusBoundAsyncTask(mainActivity).execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
									String.valueOf(busArrival.getStopId()), busRoute.getName());
							popup.dismiss();
						}
						i++;
					}
					for (final Entry<String, List<BusArrival>> entry : mapBusArrivals.entrySet()) {
						final List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
						for (final BusArrival arrival : arrivals) {
							if (position == i) {
								final Intent intent = new Intent(ChicagoTracker.getContext(), BusMapActivity.class);
								final Bundle extras = new Bundle();
								extras.putInt("busId", arrival.getBusId());
								extras.putString("busRouteId", arrival.getRouteId());
								extras.putStringArray("bounds", new String[] { entry.getKey() });
								intent.putExtras(extras);
								mainActivity.startActivity(intent);
								//mainActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
								popup.dismiss();
							}
							i++;
						}
					}
					if (position == i) {
						final Set<String> bounds = new HashSet<>();
						for (final Entry<String, List<BusArrival>> stringListEntry : mapBusArrivals.entrySet()) {
							bounds.add(stringListEntry.getKey());
						}
						final Intent intent = new Intent(ChicagoTracker.getContext(), BusMapActivity.class);
						final Bundle extras = new Bundle();
						extras.putString("busRouteId", busRoute.getId());
						extras.putStringArray("bounds", bounds.toArray(new String[bounds.size()]));
						intent.putExtras(extras);
						mainActivity.startActivity(intent);
						popup.dismiss();
					}
				}
			});
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.any_selector));
			firstLayout.getForeground().setAlpha(210);

			popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					firstLayout.getForeground().setAlpha(0);
				}
			});
			popup.setAnimationStyle(R.style.popupAnimation);
			popup.showAtLocation(firstLayout, Gravity.CENTER, 0, 0);
		}
	}
}
