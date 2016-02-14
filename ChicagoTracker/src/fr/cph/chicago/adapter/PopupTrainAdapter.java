/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;

import java.util.List;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class PopupTrainAdapter extends ArrayAdapter<String> {
	/** The context **/
	private final Context context;
	/** The values **/
	private final List<String> values;
	/** The colors **/
	private final List<Integer> colors;

	/**
	 * @param context
	 * @param values
	 * @param colors
	 */
	public PopupTrainAdapter(final Context context, final List<String> values, final List<Integer> colors) {
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
			colorView.setBackgroundColor(colors.get(position - 1));
		}
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(values.get(position));
		return rowView;
	}

}
