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

package fr.cph.chicago.core.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.listener.BikeStationOnClickListener;
import fr.cph.chicago.entity.BikeStation;

import java.util.List;

/**
 * Adapter that will handle bikes
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BikeAdapter extends BaseAdapter {

    private List<BikeStation> bikeStations;

    /**
     * Constructor
     *
     * @param activity the main activity
     */
    public BikeAdapter(@NonNull final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
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
            final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_bike, parent, false);

            holder = new ViewHolder();
            holder.stationNameView = (TextView) convertView.findViewById(R.id.station_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.stationNameView.setText(station.getName());

        convertView.setOnClickListener(new BikeStationOnClickListener(station));
        return convertView;
    }

    public void setBikeStations(@NonNull final List<BikeStation> bikeStations) {
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
