package fr.cph.chicago.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;

public final class SearchAdapter extends BaseAdapter {
	
	/** Tag **/
	private static final String TAG = "SearchAdapter";

	private List<Station> trains;
	
	private Context context;
	private Activity activity;
	
	public SearchAdapter(Activity activity){
		this.context = ChicagoTracker.getAppContext();
		this.activity = activity;
	}

	@Override
	public final int getCount() {
		return trains.size();
	}

	@Override
	public final Object getItem(final int position) {
		return null;
	}

	@Override
	public final long getItemId(final int position) {
		return 0;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		final Station station = trains.get(position);
		Set<TrainLine> lines = station.getLines();

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_search, null);
		
		TextView rounteName = (TextView) convertView.findViewById(R.id.route_name_value);
		rounteName.setText(station.getName());
		
		LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

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
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("stationId", station.getId());
				intent.putExtras(extras);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			}
		});
		
		return convertView;
	}
	
	public void setTrains(List<Station> trains){
		this.trains = trains;
	}
}
