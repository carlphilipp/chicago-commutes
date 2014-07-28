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

package fr.cph.chicago.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

/**
 * Adapter that will handle buses
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusAdapter extends BaseAdapter {

	/** Main activity **/
	private MainActivity mActivity;
	/** Bus data **/
	private List<BusRoute> busRoutes;
	/** Layout that you can use as a black fade background **/
	private FrameLayout firstLayout;

	/**
	 * Constructor
	 * 
	 * @param activity
	 *            the main activity
	 */
	public BusAdapter(final MainActivity activity) {
		this.mActivity = activity;
		BusData busData = DataHolder.getInstance().getBusData();
		this.busRoutes = busData.getRoutes();
		this.firstLayout = ChicagoTracker.container;
	}

	@Override
	public final int getCount() {
		return busRoutes.size();
	}

	@Override
	public final Object getItem(final int position) {
		return busRoutes.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, ViewGroup parent) {

		final BusRoute route = (BusRoute) getItem(position);

		TextView routeNameView = null;
		TextView routeNumberView = null;
		LinearLayout detailsLayout = null;

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_bus, null);

			final ViewHolder holder = new ViewHolder();
			routeNameView = (TextView) convertView.findViewById(R.id.station_name);
			holder.routeNameView = routeNameView;

			routeNumberView = (TextView) convertView.findViewById(R.id.bike_availability);
			holder.routeNumberView = routeNumberView;

			detailsLayout = (LinearLayout) convertView.findViewById(R.id.route_details);
			holder.detailsLayout = detailsLayout;

			convertView.setTag(holder);

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					holder.detailsLayout.setVisibility(LinearLayout.VISIBLE);
					mActivity.startRefreshAnimation();
					new DirectionAsyncTask().execute(route, holder.detailsLayout);
				}
			});
		} else {
			final ViewHolder holder = (ViewHolder) convertView.getTag();
			routeNameView = holder.routeNameView;
			routeNumberView = holder.routeNumberView;
			detailsLayout = holder.detailsLayout;

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					holder.detailsLayout.setVisibility(LinearLayout.VISIBLE);
					mActivity.startRefreshAnimation();
					new DirectionAsyncTask().execute(route, holder.detailsLayout);
				}
			});
		}

		routeNameView.setText(route.getName());
		routeNumberView.setText(route.getId());

		return convertView;
	}

	public void setRoutes(List<BusRoute> busRoutes) {
		this.busRoutes = busRoutes;
	}

	/**
	 * DP view holder
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private static class ViewHolder {
		TextView routeNameView;
		TextView routeNumberView;
		LinearLayout detailsLayout;
	}

	/**
	 * Task to get the directions of the stop
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

		/** Bus route **/
		private BusRoute busRoute;
		/** **/
		private LinearLayout convertView;
		/** **/
		private TrackerException trackerException;

		@Override
		protected final BusDirections doInBackground(final Object... params) {
			CtaConnect connect = CtaConnect.getInstance();
			BusDirections busDirections = null;
			try {
				MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
				busRoute = (BusRoute) params[0];
				reqParams.put("rt", busRoute.getId());
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
				busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
				convertView = (LinearLayout) params[1];
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			return busDirections;
		}

		@Override
		protected final void onPostExecute(final BusDirections result) {
			mActivity.stopRefreshAnimation();
			if (trackerException == null) {
				List<BusDirection> busDirections = result.getlBusDirection();
				final List<String> data = new ArrayList<String>();
				for(BusDirection busDir : busDirections){
					data.add(busDir.toString());
				}
				
				LayoutInflater layoutInflater = (LayoutInflater) mActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View popupView = layoutInflater.inflate(R.layout.popup_bus, null);

				final int[] screenSize = Util.getScreenSize();
				final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);

				ListView listView = (ListView) popupView.findViewById(R.id.details);
				PopupBusAdapter ada = new PopupBusAdapter(mActivity, data);
				listView.setAdapter(ada);

				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Intent intent = new Intent(mActivity, BusBoundActivity.class);
						Bundle extras = new Bundle();
						extras.putString("busRouteId", busRoute.getId());
						extras.putString("busRouteName", busRoute.getName());
						extras.putString("bound", data.get(position));
						intent.putExtras(extras);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ChicagoTracker.getAppContext().startActivity(intent);
						popup.dismiss();
					}
				});
				popup.setFocusable(true);
				popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
				firstLayout.getForeground().setAlpha(210);

				popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
					@Override
					public void onDismiss() {
						firstLayout.getForeground().setAlpha(0);
						convertView.setVisibility(LinearLayout.GONE);
					}
				});
				popup.showAtLocation(firstLayout, Gravity.CENTER, 0, 0);
			} else {
				ChicagoTracker.displayError(mActivity, trackerException);
			}
		}
	}
}
