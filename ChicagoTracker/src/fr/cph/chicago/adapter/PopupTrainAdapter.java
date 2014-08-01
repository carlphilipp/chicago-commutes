package fr.cph.chicago.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class PopupTrainAdapter extends ArrayAdapter<String> {
	/** The context **/
	private final Context mContext;
	/** The values **/
	private final List<String> mValues;
	/** The colors **/
	private final List<Integer> mColors;

	/**
	 * @param context
	 * @param values
	 * @param colors
	 */
	public PopupTrainAdapter(final Context context, final List<String> values, final List<Integer> colors) {
		super(context, R.layout.popup_train_cell, values);
		this.mContext = context;
		this.mValues = values;
		this.mColors = colors;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (position == 0) {
			rowView = inflater.inflate(R.layout.popup_train_cell_0, parent, false);
		} else {
			rowView = inflater.inflate(R.layout.popup_train_cell, parent, false);
			TextView colorView = (TextView) rowView.findViewById(R.id.line_color);
			colorView.setBackgroundColor(mColors.get(position - 1));
		}
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(mValues.get(position));
		return rowView;
	}

}
