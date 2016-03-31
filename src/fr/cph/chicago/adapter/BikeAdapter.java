/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

    private MainActivity mainActivity;
    private List<BikeStation> bikeStations;

    /**
     * Constructor
     *
     * @param activity the main activity
     */
    public BikeAdapter(final MainActivity activity) {
        this.mainActivity = activity;
        final Bundle bundle = activity.getIntent().getExtras();
        this.bikeStations = bundle.getParcelableArrayList(mainActivity.getString(R.string.bundle_bike_stations));
        if (this.bikeStations == null) {
            this.bikeStations = new ArrayList<>();
        }

    }

    @Override
    public final int getCount() {
        return bikeStations.size();
    }

    @Override
    public final Object getItem(final int position) {
        return bikeStations.get(position);
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final View getView(final int position, View convertView, final ViewGroup parent) {

        final BikeStation station = (BikeStation) getItem(position);

        final ViewHolder holder;

        if (convertView == null) {
            final LayoutInflater vi = (LayoutInflater) ChicagoTracker.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_bike, parent, false);

            holder = new ViewHolder();
            holder.stationNameView = (TextView) convertView.findViewById(R.id.station_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.stationNameView.setText(station.getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
                final Bundle extras = new Bundle();
                extras.putParcelable(mainActivity.getString(R.string.bundle_bike_station), station);
                intent.putExtras(extras);
                mainActivity.startActivity(intent);
            }
        });
        return convertView;
    }

    public void setBikeStations(final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
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
