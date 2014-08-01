package fr.cph.chicago.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.Eta;

/**
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapSnippetAdapter extends BaseAdapter {
	/** Eta list **/
	private List<Eta> mEtas;

	/**
	 * @param etas
	 */
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

		if (!(position == mEtas.size() - 1 && eta.getTimeLeftDueDelay().equals("0 min"))) {
			TextView time = (TextView) convertView.findViewById(R.id.time);
			time.setText(eta.getTimeLeftDueDelay());
		} else {
			name.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey));
			name.setTypeface(null, Typeface.BOLD);
			name.setGravity(Gravity.CENTER);
		}

		return convertView;
	}

}
