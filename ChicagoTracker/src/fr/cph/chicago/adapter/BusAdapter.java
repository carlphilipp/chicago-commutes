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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import fr.cph.chicago.xml.Xml;

public final class BusAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "BusAdapter";

	private BusData busData;

	private Map<String, LinearLayout> detailsMap;
	private Map<String, List<TextView>> bounds;

	public BusAdapter() {
		this.busData = DataHolder.getInstance().getBusData();
		this.detailsMap = new HashMap<String, LinearLayout>();
		this.bounds = new HashMap<String, List<TextView>>();
	}

	@Override
	public final int getCount() {
		return busData.getRouteSize();
	}

	@Override
	public final Object getItem(int position) {
		return busData.getRoute(position);
	}

	@Override
	public final long getItemId(int position) {
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
					new DirectionAsyncTask().execute(route, loading, routeDirections);
				}
				detailsLayout.setVisibility(LinearLayout.VISIBLE);
			}
		});
		
		return convertView;
	}

	private class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

		private BusRoute busRoute;
		private TextView loading;
		private LinearLayout routeDirections;

		@Override
		protected BusDirections doInBackground(Object... params) {
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
				routeDirections = (LinearLayout) params[2];
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			return busDirections;
		}

		@Override
		protected void onPostExecute(BusDirections result) {
			loading.setVisibility(TextView.GONE);
			for (final BusDirection busDirection : result.getlBusDirection()) {
				Button currentBound = new Button(ChicagoTracker.getAppContext(), null, android.R.attr.buttonStyleSmall);
				currentBound.setText(busDirection.toString());
//				currentBound.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey_M_B));
				
//				currentBound.setTextSize(ChicagoTracker.getAppContext().getResources().getDimension(R.dimen.bus_adapter_button_text_size));
				currentBound.setWidth((int) ChicagoTracker.getAppContext().getResources().getDimension(R.dimen.bus_adapter_button_width));
				currentBound.setHeight((int) ChicagoTracker.getAppContext().getResources().getDimension(R.dimen.bus_adapter_button_height));
				
//				currentBound.setBackground(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.buttonshape));
//				currentBound.setShadowLayer(5, 0, 0, R.color.grey_again);
//				currentBound.setPadding(10, 0, 0, 0);
				currentBound.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ChicagoTracker.getAppContext(), BusBoundActivity.class);
						Bundle extras = new Bundle();
						extras.putString("busRouteId", busRoute.getId());
						extras.putString("busRouteName", busRoute.getName());
						extras.putString("bound", busDirection.toString());
						intent.putExtras(extras);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ChicagoTracker.getAppContext().startActivity(intent);
					}
				});
				if (bounds.containsKey(busRoute.getId())) {
					bounds.get(busRoute.getId()).add(currentBound);
				} else {
					List<TextView> lviews = new ArrayList<TextView>();
					lviews.add(currentBound);
					bounds.put(busRoute.getId(), lviews);
				}
				routeDirections.addView(currentBound);
			}
		}
	}
}
