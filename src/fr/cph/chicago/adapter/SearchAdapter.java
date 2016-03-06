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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.SearchActivity;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.listener.FavoritesTrainOnClickListener;
import fr.cph.chicago.task.DirectionAsyncTask;

import java.util.List;
import java.util.Set;

/**
 * Adapter that will handle search
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class SearchAdapter extends BaseAdapter {

	private Context context;

	private List<Station> trains;
	private List<BusRoute> busRoutes;
	private List<BikeStation> bikeStations;
	private SearchActivity searchActivity;

	/**
	 * Constructor
	 *
	 * @param activity the search activity
	 */
	public SearchAdapter(final SearchActivity activity) {
		this.context = ChicagoTracker.getContext();
		this.searchActivity = activity;
	}

	@Override
	public final int getCount() {
		return trains.size() + busRoutes.size() + bikeStations.size();
	}

	@Override
	public final Object getItem(final int position) {
		Object object;
		if (position < trains.size()) {
			object = trains.get(position);
		} else if (position < trains.size() + busRoutes.size()) {
			object = busRoutes.get(position - trains.size());
		} else {
			object = bikeStations.get(position - (trains.size() + busRoutes.size()));
		}
		return object;
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		final LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_search, parent, false);

		final TextView routeName = (TextView) convertView.findViewById(R.id.station_name);

		if (position < trains.size()) {
			final Station station = (Station) getItem(position);
			routeName.setText(station.getName());

			final LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

			final Set<TrainLine> lines = station.getLines();
			int index = 0;
			for (final TrainLine trainLine : lines) {
				TextView textView = new TextView(context);
				textView.setBackgroundColor(trainLine.getColor());
				textView.setText(" ");
				textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
				stationColorView.addView(textView);
				if (index != lines.size()) {
					textView = new TextView(context);
					textView.setText("");
					textView.setPadding(0, 0, (int) context.getResources().getDimension(R.dimen.activity_list_station_colors_space), 0);
					textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
					stationColorView.addView(textView);
				}
				index++;
			}
			convertView.setOnClickListener(new FavoritesTrainOnClickListener(searchActivity, station.getId(), lines));
		} else if (position < trains.size() + busRoutes.size()) {
			final BusRoute busRoute = (BusRoute) getItem(position);

			final TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText(searchActivity.getString(R.string.B));

			final String name = busRoute.getId() + " " + busRoute.getName();
			routeName.setText(name);

			final TextView loadingTextView = (TextView) convertView.findViewById(R.id.loading_text_view);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					loadingTextView.setVisibility(LinearLayout.VISIBLE);
					new DirectionAsyncTask(searchActivity, parent).execute(busRoute, loadingTextView);
				}
			});
		} else {
			final BikeStation bikeStation = (BikeStation) getItem(position);

			final TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText(searchActivity.getString(R.string.D));

			routeName.setText(bikeStation.getName());

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
					final Bundle extras = new Bundle();
					extras.putParcelable("station", bikeStation);
					intent.putExtras(extras);
					searchActivity.startActivity(intent);
				}
			});
		}
		return convertView;
	}

	/**
	 * Update data
	 *
	 * @param trains the list of train stations
	 * @param buses  the list of bus routes
	 * @param bikes  the list of bikes
	 */
	public void updateData(final List<Station> trains, final List<BusRoute> buses, final List<BikeStation> bikes) {
		this.trains = trains;
		this.busRoutes = buses;
		this.bikeStations = bikes;
	}
}
