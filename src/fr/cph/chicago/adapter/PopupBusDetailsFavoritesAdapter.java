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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.entity.BusDetailsDTO;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class PopupBusDetailsFavoritesAdapter extends ArrayAdapter<BusDetailsDTO> {

    private final Activity activity;
    private final List<BusDetailsDTO> values;

    public PopupBusDetailsFavoritesAdapter(@NonNull final Activity activity, @NonNull final List<BusDetailsDTO> values) {
        super(activity, R.layout.popup_bus_cell, values);
        this.activity = activity;
        this.values = values;
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        final View rowView = activity.getLayoutInflater().inflate(R.layout.popup_bus_cell_0, parent, false);
        final TextView textView = (TextView) rowView.findViewById(R.id.label);
        final String toDisplay = values.get(position).getStopName() + " (" + WordUtils.capitalize(values.get(position).getBound().toLowerCase()) + ")";
        textView.setText(toDisplay);
        return rowView;
    }
}
