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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
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
import fr.cph.chicago.xml.Xml;

public final class BusAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "BusAdapter";

	private Activity activity;
	
	private BusData busData;
	private FrameLayout firstLayout;

	private Map<String, LinearLayout> detailsMap;
	private Map<String, List<TextView>> bounds;

	public BusAdapter(final Activity activity) {
		this.activity = activity;
		this.busData = DataHolder.getInstance().getBusData();
		this.detailsMap = new HashMap<String, LinearLayout>();
		this.bounds = new HashMap<String, List<TextView>>();
		this.firstLayout = ChicagoTracker.container;
	}

	@Override
	public final int getCount() {
		return busData.getRouteSize();
	}

	@Override
	public final Object getItem(final int position) {
		return busData.getRoute(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {

		final BusRoute route = busData.getRoute(position);

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_bus, null);

		TextView routeNameView = (TextView) convertView.findViewById(R.id.route_name_value);
		routeNameView.setText(route.getName());

		TextView routeNumberView = (TextView) convertView.findViewById(R.id.route_number_value);
		routeNumberView.setText(route.getId());

		final TextView loading = (TextView) convertView.findViewById(R.id.loading_text_view);

		final LinearLayout routeDirections = (LinearLayout) convertView.findViewById(R.id.route_directions);

		final LinearLayout detailsLayout = (LinearLayout) convertView.findViewById(R.id.route_details);
		if (detailsMap.containsKey(route.getId())) {
			LinearLayout detailsLayoutSaved = detailsMap.get(route.getId());
			detailsLayout.setVisibility(detailsLayoutSaved.getVisibility());

			TextView loadingSaved = (TextView) detailsLayoutSaved.findViewById(R.id.loading_text_view);

			loading.setText(loadingSaved.getText());
			loading.setVisibility(loadingSaved.getVisibility());

			if (bounds.containsKey(route.getId())) {
				List<TextView> tempListView = bounds.get(route.getId());

				for (TextView text : tempListView) {
					TextView text2 = new TextView(ChicagoTracker.getAppContext());
					text2.setText(text.getText());
					routeDirections.addView(text2);
				}
			}
		}
		detailsMap.put(route.getId(), detailsLayout);

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (detailsLayout.getVisibility() == LinearLayout.GONE) {
					new DirectionAsyncTask().execute(route, loading, detailsLayout);
				}
				detailsLayout.setVisibility(LinearLayout.VISIBLE);
			}
		});

		return convertView;
	}

	private class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

		private BusRoute busRoute;
		private TextView loading;
		private View convertView;
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
				loading = (TextView) params[1];
				convertView = (View) ((LinearLayout) params[2]).getParent();
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			return busDirections;
		}

		@Override
		protected final void onPostExecute(final BusDirections result) {
			if (trackerException == null) {
				loading.setVisibility(TextView.GONE);
				PopupMenu popupMenu = new PopupMenu(ChicagoTracker.getAppContext(), convertView);
				List<BusDirection> lBus = result.getlBusDirection();
				for (int i = 0; i < lBus.size(); i++) {
					popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, lBus.get(i).toString());
				}
				popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						for (final BusDirection busDirection : result.getlBusDirection()) {
							Intent intent = new Intent(ChicagoTracker.getAppContext(), BusBoundActivity.class);
							Bundle extras = new Bundle();
							extras.putString("busRouteId", busRoute.getId());
							extras.putString("busRouteName", busRoute.getName());
							extras.putString("bound", busDirection.toString());
							intent.putExtras(extras);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ChicagoTracker.getAppContext().startActivity(intent);
							return false;
						}
						return false;
					}

				});
				popupMenu.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(PopupMenu menu) {
						firstLayout.getForeground().setAlpha(0);
					}
				});
				firstLayout.getForeground().setAlpha(210);
				popupMenu.show();
			} else {
				ChicagoTracker.displayError(activity, trackerException);
			}
		}
	}
}
