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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.entity.BikeStation;

/**
 * Adapter that will handle bikes
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BikeAdapter extends BaseAdapter {
	/** Main activity **/
	private MainActivity mActivity;
	/** Bike data **/
	private List<BikeStation> mBikeStations;

	/**
	 * Constructor
	 * 
	 * @param activity
	 *            the main activity
	 */
	public BikeAdapter(final MainActivity activity) {
		this.mActivity = activity;
		Bundle bundle = activity.getIntent().getExtras();
		this.mBikeStations = bundle.getParcelableArrayList("bikeStations");
		if(this.mBikeStations == null){
			this.mBikeStations = new ArrayList<BikeStation>();
		}
	}

	@Override
	public final int getCount() {
		return mBikeStations.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mBikeStations.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {

		final BikeStation station = (BikeStation) getItem(position);

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_bike, null);

			holder = new ViewHolder();
			holder.stationNameView = (TextView) convertView.findViewById(R.id.station_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.stationNameView.setText(station.getName());

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChicagoTracker.getAppContext(), BikeStationActivity.class);
				Bundle extras = new Bundle();
				extras.putParcelable("station", station);
				intent.putExtras(extras);
				mActivity.startActivity(intent);
				mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			}
		});
		return convertView;
	}

	public void setBikeStations(List<BikeStation> bikeStations) {
		this.mBikeStations = bikeStations;
	}

	/**
	 * DP view holder
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private static class ViewHolder {
		TextView stationNameView;
	}
}
