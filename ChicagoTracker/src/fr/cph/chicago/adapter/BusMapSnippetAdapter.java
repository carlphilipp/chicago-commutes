package fr.cph.chicago.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.BusArrival;

public class BusMapSnippetAdapter extends BaseAdapter {

	private List<BusArrival> mArrivals;

	public BusMapSnippetAdapter(final List<BusArrival> arrivals) {
		this.mArrivals = arrivals;
	}

	@Override
	public final int getCount() {
		return mArrivals.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mArrivals.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		BusArrival arrival = (BusArrival) getItem(position);
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_map_train, null);
		TextView name = (TextView) convertView.findViewById(R.id.station_name);
		name.setText(arrival.getStopName());

		TextView time = (TextView) convertView.findViewById(R.id.time);
		time.setText(arrival.getTimeLeftDueDelay());

		return convertView;
	}

}
