package fr.cph.chicago.adapter;

import java.io.IOException;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
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

	public BusAdapter() {
		data = DataHolder.getInstance().getBusData();
	}

	@Override
	public final int getCount() {
		if (data != null) {
			return data.getSize();
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

		BusRoute route = data.getRoute(position);

		ViewHolderItem viewHolder;

		if (convertView == null) {
			viewHolder = new ViewHolderItem();
			
			LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_bus, null);
			
			viewHolder.routeNameView = (TextView) convertView.findViewById(R.id.route_name_value);

			viewHolder.routeNumberView = (TextView) convertView.findViewById(R.id.route_number_value);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		viewHolder.routeNameView.setText(route.getName());
		
		viewHolder.routeNumberView.setText(route.getId());
		
//		TextView loading = (TextView) convertView.findViewById(R.id.loading_text_view);
//		
//		Log.i(TAG, "Loading id: " + loading.getId() + " - " + loading.getText());
		
		return convertView;
	}

	private static class ViewHolderItem {
		TextView routeNameView;
		TextView routeNumberView;
	}
	
	public void setBusData() {
		this.data = null;
		this.data = DataHolder.getInstance().getBusData();
	}
	
	LinearLayout detailsLayout;
	TextView loading;
	
	public void updateDetails(View childView, int position){
		RelativeLayout view = (RelativeLayout) childView;
		detailsLayout = (LinearLayout) view.findViewById(R.id.route_details);
		detailsLayout.setVisibility(LinearLayout.VISIBLE);
		loading = (TextView) detailsLayout.findViewById(R.id.loading_text_view);
		
		BusRoute busRoute = (BusRoute) data.getRoute(position);
		String stopId = busRoute.getId();
		new DirectionAsyncTask().execute(stopId);
	}
	
	class DirectionAsyncTask extends AsyncTask<String, Void, BusDirections> {
		@Override
		protected BusDirections doInBackground(String... params) {
			Log.i(TAG, "doInBackground");
			CtaConnect connect = CtaConnect.getInstance();
			BusDirections busDirections = null;
			try {
				MultiMap<String,String> reqParams = new MultiValueMap<String, String>();
				reqParams.put("rt", params[0]);
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
				busDirections = xml.parseBusDirections(xmlResult, params[0]);
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
			for(BusDirection busDirection :  result.getlBusDirection()){
				TextView textView = new TextView(ChicagoTracker.getAppContext());
				textView.setText(busDirection.toString()+ " ");
				Log.i(TAG, "Loading view "+ loading.getId() +" Update view " + detailsLayout.getId() + " with " + textView.getText());
				detailsLayout.addView(textView);
			}
		}
	}

}
