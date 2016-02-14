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
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

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
	/** List of train stations **/
	private List<Station> mTrains;
	/** List of buses route **/
	private List<BusRoute> mBuses;
	/** List of bikes stations **/
	private List<BikeStation> mBikes;
	/** The context **/
	private Context mContext;
	/** The search activity **/
	private SearchActivity mActivity;
	/** The layout that is used to display a fade black background **/
	private FrameLayout mContainer;

	/** The layout that is used to display a fade black background **/
	// private FrameLayout mFirstLayout;

	/**
	 * Constructor
	 *
	 * @param activity
	 *            the search activity
	 * @param container
	 *            the layout container
	 */
	public SearchAdapter(final SearchActivity activity, final FrameLayout container) {
		this.mContext = ChicagoTracker.getAppContext();
		this.mActivity = activity;
		this.mContainer = container;
		// this.mFirstLayout = container2;
	}

	@Override
	public final int getCount() {
		return mTrains.size() + mBuses.size() + mBikes.size();
	}

	@Override
	public final Object getItem(final int position) {
		Object object = null;
		if (position < mTrains.size()) {
			object = mTrains.get(position);
		} else if (position < mTrains.size() + mBuses.size()) {
			object = mBuses.get(position - mTrains.size());
		} else {
			object = mBikes.get(position - (mTrains.size() + mBuses.size()));
		}
		return object;
	}

	@Override
	public final long getItemId(final int position) {
		return 0;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_search, null);

		TextView rounteName = (TextView) convertView.findViewById(R.id.station_name);

		if (position < mTrains.size()) {
			final Station station = (Station) getItem(position);
			Set<TrainLine> lines = station.getLines();

			rounteName.setText(station.getName());

			LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

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
			convertView.setOnClickListener(new FavoritesTrainOnClickListener(mActivity, mContainer, station.getId(), lines));
		} else if (position < mTrains.size() + mBuses.size()) {
			final BusRoute busRoute = (BusRoute) getItem(position);

			TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText("B");

			rounteName.setText(busRoute.getId() + " " + busRoute.getName());

			final TextView loadingTextView = (TextView) convertView.findViewById(R.id.loading_text_view);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					loadingTextView.setVisibility(LinearLayout.VISIBLE);
					mActivity.startRefreshAnimation();
					new DirectionAsyncTask().execute(busRoute, loadingTextView);
				}
			});
		} else {
			final BikeStation bikeStation = (BikeStation) getItem(position);

			TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText("D");

			rounteName.setText(bikeStation.getName());

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ChicagoTracker.getAppContext(), BikeStationActivity.class);
					Bundle extras = new Bundle();
					extras.putParcelable("station", bikeStation);
					intent.putExtras(extras);
					mActivity.startActivity(intent);
					mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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

		/** **/
		private BusRoute busRoute;
		/** **/
		private TextView convertView;
		/** **/
		private TrackerException trackerException;

		@Override
		protected final BusDirections doInBackground(final Object... params) {
			CtaConnect connect = CtaConnect.getInstance();
			BusDirections busDirections = null;
			try {
				MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
				busRoute = (BusRoute) params[0];
				reqParams.put("rt", busRoute.getId());
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
				busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
				convertView = (TextView) params[1];
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(SearchAdapter.this.mActivity, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_direction, 0);
			return busDirections;
		}

		@Override
		protected final void onPostExecute(final BusDirections result) {
			mActivity.stopRefreshAnimation();
			if (trackerException == null) {
				// PopupMenu popupMenu = new PopupMenu(ChicagoTracker.getAppContext(), convertView);
				final List<BusDirection> lBus = result.getlBusDirection();
				final List<String> data = new ArrayList<String>();
				for (BusDirection busDir : lBus) {
					data.add(busDir.toString());
				}
				data.add("Follow all buses on line " + result.getId());

				LayoutInflater layoutInflater = (LayoutInflater) mActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View popupView = layoutInflater.inflate(R.layout.popup_bus, null);

				final int[] screenSize = Util.getScreenSize();
				final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);

				ListView listView = (ListView) popupView.findViewById(R.id.details);
				PopupBusAdapter ada = new PopupBusAdapter(mActivity, data);
				listView.setAdapter(ada);

				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (position != data.size() - 1) {
							Intent intent = new Intent(mActivity, BusBoundActivity.class);
							Bundle extras = new Bundle();
							extras.putString("busRouteId", busRoute.getId());
							extras.putString("busRouteName", busRoute.getName());
							extras.putString("bound", data.get(position));
							intent.putExtras(extras);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ChicagoTracker.getAppContext().startActivity(intent);
						} else {
							String[] busDirectionArray = new String[lBus.size()];
							int i = 0;
							for (BusDirection busDir : lBus) {
								busDirectionArray[i++] = busDir.toString();
							}
							Intent intent = new Intent(ChicagoTracker.getAppContext(), BusMapActivity.class);
							Bundle extras = new Bundle();
							extras.putString("busRouteId", result.getId());
							extras.putStringArray("bounds", busDirectionArray);
							intent.putExtras(extras);
							mActivity.startActivity(intent);
							mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
						}
						popup.dismiss();
					}
				});
				popup.setFocusable(true);
				popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
				mContainer.getForeground().setAlpha(210);

				popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
					@Override
					public void onDismiss() {
						mContainer.getForeground().setAlpha(0);
						convertView.setVisibility(LinearLayout.GONE);
					}
				});
				popup.showAtLocation(mContainer, Gravity.CENTER, 0, 0);
			} else {
				ChicagoTracker.displayError(mActivity, trackerException);
			}
		}
	}

	/**
	 * Update data
	 *
	 * @param trains
	 *            the list of train stations
	 * @param buses
	 *            the list of bus routes
	 */
	public void updateData(List<Station> trains, List<BusRoute> buses, List<BikeStation> bikes) {
		this.mTrains = trains;
		this.mBuses = buses;
		this.mBikes = bikes;
	}
}
