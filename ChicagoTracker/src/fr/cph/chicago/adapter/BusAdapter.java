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
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.StationActivity;
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

	private BusData data;

	private Map<String, LinearLayout> detailsMap;
	private Map<String, List<TextView>> bouds;

	public BusAdapter() {
		this.data = DataHolder.getInstance().getBusData();
		this.detailsMap = new HashMap<String, LinearLayout>();
		this.bouds = new HashMap<String, List<TextView>>();
	}

	@Override
	public final int getCount() {
		if (data != null) {
			return data.getRouteSize();
		} else {
			return 0;
		}

	}

	@Override
	public final Object getItem(int position) {
		return data.getRoute(position);
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {

		final BusRoute route = data.getRoute(position);

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

			loading.setText((loadingSaved).getText());
			loading.setVisibility(loadingSaved.getVisibility());

			if (bouds.containsKey(route.getId())) {
				List<TextView> tempListView = bouds.get(route.getId());

				for (TextView text : tempListView) {
					TextView derp = new TextView(ChicagoTracker.getAppContext());
					derp.setText(text.getText());
					routeDirections.addView(derp);
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

	public void setBusData() {
		this.data = null;
		this.data = DataHolder.getInstance().getBusData();
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
				TextView currentBound = new TextView(ChicagoTracker.getAppContext());
				currentBound.setText(busDirection.toString());
				currentBound.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.black));
				currentBound.setPadding(10, 0, 0, 0);
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
				if (bouds.containsKey(busRoute.getId())) {
					bouds.get(busRoute.getId()).add(currentBound);
				} else {
					List<TextView> lviews = new ArrayList<TextView>();
					lviews.add(currentBound);
					bouds.put(busRoute.getId(), lviews);
				}
				routeDirections.addView(currentBound);
			}
		}
	}
}
