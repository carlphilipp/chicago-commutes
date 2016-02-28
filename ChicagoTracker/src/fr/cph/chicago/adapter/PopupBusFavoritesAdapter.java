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
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.cph.chicago.R;

import java.util.List;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class PopupBusFavoritesAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final List<String> values;

	/**
	 * @param context
	 * @param values
	 */
	public PopupBusFavoritesAdapter(final Context context, final List<String> values) {
		super(context, R.layout.popup_bus_cell, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView;
		if (position == 0) {
			rowView = inflater.inflate(R.layout.popup_bus_cell_0, parent, false);
		} else {
			rowView = inflater.inflate(R.layout.popup_bus_cell, parent, false);
			final ImageView imageView = (ImageView) rowView.findViewById(R.id.popup_map_map);
			imageView.setColorFilter(ContextCompat.getColor(context, R.color.grey_5));
		}
		final TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(values.get(position));
		return rowView;
	}
}
