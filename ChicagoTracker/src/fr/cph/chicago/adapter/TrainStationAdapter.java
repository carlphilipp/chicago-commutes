package fr.cph.chicago.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.enumeration.TrainLine;

public final class TrainStationAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "TrainStationAdapter";

	public TrainStationAdapter() {
	}

	@Override
	public final int getCount() {
		return TrainLine.values().length;
	}

	@Override
	public final Object getItem(int position) {
		return TrainLine.values()[position];
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.list_train_line, null);
		
		TextView textView = (TextView) v.findViewById(R.id.station_color_value);
		textView.setBackgroundColor(TrainLine.values()[position].getColor());
		textView.setText("  ");
		
		textView = (TextView) v.findViewById(R.id.station_name_value);
		textView.setText(TrainLine.values()[position].toString());
		return v;
	}

}
