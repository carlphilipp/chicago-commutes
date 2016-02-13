/**
 * Copyright 2016 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class PopupBusAdapter extends ArrayAdapter<String> {
	/** Context **/
	private final Context mContext;
	/** Values **/
	private final List<String> mValues;

	/**
	 * @param context
	 * @param values
	 */
	public PopupBusAdapter(Context context, List<String> values) {
		super(context, R.layout.popup_bus_cell, values);
		this.mContext = context;
		this.mValues = values;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.popup_bus_cell, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(mValues.get(position));
		return rowView;
	}

}
