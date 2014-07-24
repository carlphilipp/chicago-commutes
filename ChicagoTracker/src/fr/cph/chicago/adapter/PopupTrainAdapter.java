package fr.cph.chicago.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;

public class PopupTrainAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final List<String> values;
	private final List<Integer> colors;

	public PopupTrainAdapter(Context context, List<String> values, List<Integer> colors) {
		super(context, R.layout.popup_train_cell, values);
		this.context = context;
		this.values = values;
		this.colors = colors;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (position == 0) {
			rowView = inflater.inflate(R.layout.popup_train_cell_0, parent, false);
		} else {
			rowView = inflater.inflate(R.layout.popup_train_cell, parent, false);
			TextView colorView = (TextView) rowView.findViewById(R.id.line_color);
			colorView.setBackgroundColor(colors.get(position-1));
		}
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(values.get(position));
		return rowView;
	}

}
