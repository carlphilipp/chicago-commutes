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

package fr.cph.chicago.core.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.List;
import java.util.Set;

import fr.cph.chicago.R;
import fr.cph.chicago.core.listener.TrainOnClickListener;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;

/**
 * Adapter that will handle trains
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class TrainAdapter extends BaseAdapter {

    private final Activity activity;
    private final List<Station> stations;

    /**
     * Constructor
     *
     * @param line the train line
     */
    public TrainAdapter(@NonNull final TrainLine line, @NonNull final Activity activity) {
        // Load data
        final TrainData data = DataHolder.INSTANCE.getTrainData();
        this.stations = data.getStationsForLine(line);
        this.activity = activity;
    }

    @Override
    public final int getCount() {
        return stations.size();
    }

    @Override
    public final Object getItem(final int position) {
        return stations.get(position);
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_train, parent, false);

            final TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name_value);
            final LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);
            holder.stationNameView = stationNameView;
            holder.stationColorView = stationColorView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Station station = (Station) getItem(position);
        final Set<TrainLine> lines = station.getLines();

        holder.stationNameView.setText(station.getName());

        holder.stationColorView.removeAllViews();
        Stream.of(lines)
            .map(line -> LayoutUtil.createColoredRoundForMultiple(activity.getApplicationContext(), line))
            .forEach(layout -> holder.stationColorView.addView(layout));
        convertView.setOnClickListener(new TrainOnClickListener(parent.getContext(), station.getId(), lines));
        return convertView;
    }

    private static class ViewHolder {
        TextView stationNameView;
        LinearLayout stationColorView;
    }
}
