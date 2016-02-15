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
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.BusArrival;

import java.util.List;

/**
 * Adapter that will handle bus map
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapSnippetAdapter extends BaseAdapter {
	/** List of bus arrivals **/
	private List<BusArrival> arrivals;

	/**
	 * @param arrivals
	 */
	public BusMapSnippetAdapter(final List<BusArrival> arrivals) {
		this.arrivals = arrivals;
	}

	@Override
	public final int getCount() {
		return arrivals.size();
	}

	@Override
	public final Object getItem(final int position) {
		return arrivals.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		BusArrival arrival = (BusArrival) getItem(position);
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_map_train, parent);
		TextView name = (TextView) convertView.findViewById(R.id.station_name);
		name.setText(arrival.getStopName());

		if (!(position == arrivals.size() - 1 && arrival.getTimeLeftDueDelay().equals("No service scheduled"))) {
			TextView time = (TextView) convertView.findViewById(R.id.time);
			time.setText(arrival.getTimeLeftDueDelay());
		} else {
			name.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey));
			name.setTypeface(null, Typeface.BOLD);
			name.setGravity(Gravity.CENTER);
		}

		return convertView;
	}

}
