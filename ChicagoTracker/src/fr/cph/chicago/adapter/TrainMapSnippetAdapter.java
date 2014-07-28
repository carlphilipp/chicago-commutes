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
import fr.cph.chicago.entity.Eta;

public class TrainMapSnippetAdapter extends BaseAdapter {

	private List<Eta> mEtas;

	public TrainMapSnippetAdapter(final List<Eta> etas) {
		this.mEtas = etas;
	}

	@Override
	public final int getCount() {
		return mEtas.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mEtas.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		Eta eta = (Eta) getItem(position);
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_map_train, null);
		TextView name = (TextView) convertView.findViewById(R.id.station_name);
		name.setText(eta.getStation().getName());
		
		TextView time = (TextView) convertView.findViewById(R.id.time);
		time.setText(eta.getTimeLeftDueDelay());

		return convertView;
	}

}
