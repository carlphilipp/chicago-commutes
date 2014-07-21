package fr.cph.chicago.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;

public class PopupBusAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final List<String> values;

	public PopupBusAdapter(Context context, List<String> values) {
		super(context, R.layout.popup_bus_cell, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.popup_bus_cell, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(values.get(position));
		return rowView;
	}

}
