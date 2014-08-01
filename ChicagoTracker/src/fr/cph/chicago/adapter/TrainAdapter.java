/**
 * Copyright 2014 Carl-Philipp Harmant
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
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Adapter that will handle trains
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class TrainAdapter extends BaseAdapter {
	/** List of stations **/
	private List<Station> mStations;
	/** The context **/
	private Context mContext;

	/**
	 * Constructor
	 * 
	 * @param line
	 *            the train line
	 */
	public TrainAdapter(final TrainLine line) {
		// Load data
		DataHolder dataHolder = DataHolder.getInstance();
		TrainData data = dataHolder.getTrainData();
		this.mStations = data.getStationsForLine(line);
		this.mContext = ChicagoTracker.getAppContext();
	}

	@Override
	public final int getCount() {
		return mStations.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mStations.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		Station station = mStations.get(position);
		Set<TrainLine> lines = station.getLines();

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_train, null);

		TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
		LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

		stationNameView.setText(station.getName());

		int indice = 0;
		for (TrainLine tl : lines) {
			TextView textView = new TextView(mContext);
			textView.setBackgroundColor(tl.getColor());
			textView.setText(" ");
			textView.setTextSize(mContext.getResources().getDimension(R.dimen.activity_list_station_colors));
			stationColorView.addView(textView);
			if (indice != lines.size()) {
				textView = new TextView(mContext);
				textView.setText("");
				textView.setPadding(0, 0, (int) mContext.getResources().getDimension(R.dimen.activity_list_station_colors_space), 0);
				textView.setTextSize(mContext.getResources().getDimension(R.dimen.activity_list_station_colors));
				stationColorView.addView(textView);
			}
			indice++;
		}
		return convertView;
	}
}
