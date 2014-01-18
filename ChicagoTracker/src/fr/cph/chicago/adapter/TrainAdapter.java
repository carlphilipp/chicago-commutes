package fr.cph.chicago.adapter;

import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;

public final class TrainAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "TrainAdapter";

	private List<Station> stations;

	private Context context;

	public TrainAdapter(TrainLine line) {
		// Load data
		DataHolder dataHolder = DataHolder.getInstance();
		TrainData data = dataHolder.getTrainData();
		this.stations = data.getStationsForLine(line);
		this.context = ChicagoTracker.getAppContext();
	}

	@Override
	public final int getCount() {
		return stations.size();
	}

	@Override
	public final Object getItem(int position) {
		return stations.get(position);
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		Station station = stations.get(position);
		Set<TrainLine> lines = station.getLines();

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_train, null);

		TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
		LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

		stationNameView.setText(station.getName());

		int indice = 0;
		for (TrainLine tl : lines) {
			TextView textView = new TextView(context);
			textView.setBackgroundColor(tl.getColor());
			textView.setText(" ");
			textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
			stationColorView.addView(textView);
			if (indice != lines.size()) {
				textView = new TextView(context);
				textView.setText("");
				textView.setPadding(0, 0, (int) context.getResources().getDimension(R.dimen.activity_list_station_colors_space), 0);
				textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
				stationColorView.addView(textView);
			}
			indice++;
		}
		return convertView;
	}
}
