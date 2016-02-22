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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.activity.SearchActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.listener.FavoritesTrainOnClickListener;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter that will handle search
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class SearchAdapter extends BaseAdapter {

	private List<Station> trains;

	private List<BusRoute> busRoutes;

	private List<BikeStation> bikeStations;

	private Context context;

	private SearchActivity searchActivity;

	private FrameLayout container;

	/**
	 * Constructor
	 *
	 * @param activity  the search activity
	 * @param container the container
	 */
	public SearchAdapter(final SearchActivity activity, final FrameLayout container) {
		this.context = ChicagoTracker.getAppContext();
		this.searchActivity = activity;
		this.container = container;
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
			if (lines != null) {
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
			}
			convertView.setOnClickListener(new FavoritesTrainOnClickListener(searchActivity, container, station.getId(), lines));
		} else if (position < trains.size() + busRoutes.size()) {
			final BusRoute busRoute = (BusRoute) getItem(position);

			final TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText("B");

			final String name = busRoute.getId() + " " + busRoute.getName();
			routeName.setText(name);

			final TextView loadingTextView = (TextView) convertView.findViewById(R.id.loading_text_view);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					loadingTextView.setVisibility(LinearLayout.VISIBLE);
					new DirectionAsyncTask().execute(busRoute, loadingTextView);
				}
			});
		} else {
			final BikeStation bikeStation = (BikeStation) getItem(position);

			final TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText("D");

			routeName.setText(bikeStation.getName());

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(ChicagoTracker.getAppContext(), BikeStationActivity.class);
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
	 * Direction task
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

		private BusRoute busRoute;
		private TextView convertView;
		private TrackerException trackerException;

		@Override
		protected final BusDirections doInBackground(final Object... params) {
			busRoute = (BusRoute) params[0];
			convertView = (TextView) params[1];

			final CtaConnect connect = CtaConnect.getInstance();
			BusDirections busDirections = null;
			try {
				final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
				reqParams.put("rt", busRoute.getId());

				final Xml xml = new Xml();
				final String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
				busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
			} catch (ParserException | ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(SearchAdapter.this.searchActivity, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_direction, 0);
			return busDirections;
		}

		@Override
		protected final void onPostExecute(final BusDirections result) {
			if (trackerException == null) {
				final List<BusDirection> busDirections = result.getlBusDirection();
				final List<String> data = new ArrayList<>();
				for (final BusDirection busDirection : busDirections) {
					data.add(busDirection.toString());
				}
				data.add("Follow all buses on line " + result.getId());

				final LayoutInflater layoutInflater = (LayoutInflater) searchActivity.getBaseContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View popupView = layoutInflater.inflate(R.layout.popup_bus, null);

				final int[] screenSize = Util.getScreenSize();
				final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);

				final ListView listView = (ListView) popupView.findViewById(R.id.details);
				final PopupBusAdapter ada = new PopupBusAdapter(searchActivity, data);
				listView.setAdapter(ada);

				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
						if (position != data.size() - 1) {
							final Intent intent = new Intent(searchActivity, BusBoundActivity.class);
							final Bundle extras = new Bundle();
							extras.putString("busRouteId", busRoute.getId());
							extras.putString("busRouteName", busRoute.getName());
							extras.putString("bound", data.get(position));
							intent.putExtras(extras);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
						} else {
							final String[] busDirectionArray = new String[busDirections.size()];
							int i = 0;
							for (final BusDirection busDirection : busDirections) {
								busDirectionArray[i++] = busDirection.toString();
							}
							final Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
							final Bundle extras = new Bundle();
							extras.putString("busRouteId", result.getId());
							extras.putStringArray("bounds", busDirectionArray);
							intent.putExtras(extras);
							searchActivity.startActivity(intent);
						}
						popup.dismiss();
					}
				});
				popup.setFocusable(true);
				popup.setBackgroundDrawable(ContextCompat.getDrawable(ChicagoTracker.getAppContext(), R.drawable.any_selector));
				container.getForeground().setAlpha(210);

				popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
					@Override
					public void onDismiss() {
						container.getForeground().setAlpha(0);
						convertView.setVisibility(LinearLayout.GONE);
					}
				});
				popup.showAtLocation(container, Gravity.CENTER, 0, 0);
			} else {
				ChicagoTracker.displayError(searchActivity, trackerException);
			}
		}
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
