/**
 * Copyright 2017 Carl-Philipp Harmant
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.entity.BusArrival;

/**
 * Adapter that will handle bus map
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapSnippetAdapter extends BaseAdapter {

    private final Activity activity;
    private final List<BusArrival> arrivals;

    public BusMapSnippetAdapter(@NonNull final Activity activity, @NonNull final List<BusArrival> arrivals) {
        this.activity = activity;
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

    @SuppressLint("ViewHolder")
    @Override
    public final View getView(final int position, View convertView, final ViewGroup parent) {
        final BusArrival arrival = (BusArrival) getItem(position);
        convertView = activity.getLayoutInflater().inflate(R.layout.list_map_train, parent, false);
        final TextView stationNameTextView = (TextView) convertView.findViewById(R.id.station_name);
        stationNameTextView.setText(arrival.getStopName());

        if (!(position == arrivals.size() - 1 && "No service".equals(arrival.getTimeLeftDueDelay()))) {
            final TextView timeTextView = (TextView) convertView.findViewById(R.id.time);
            timeTextView.setText(arrival.getTimeLeftDueDelay());
        } else {
            stationNameTextView.setText(arrival.getStopName());
            stationNameTextView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.grey));
            stationNameTextView.setGravity(Gravity.CENTER);
        }
        return convertView;
    }
}
