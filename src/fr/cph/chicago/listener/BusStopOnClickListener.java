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

package fr.cph.chicago.listener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.adapter.PopupBusDetailsFavoritesAdapter;
import fr.cph.chicago.entity.BusDetailsDTO;
import fr.cph.chicago.util.Util;

// TODO find a solution when stop name are too long (54A stop name for example)
public class BusStopOnClickListener implements View.OnClickListener {

    private final MainActivity activity;
    private final ViewGroup parent;
    private final List<BusDetailsDTO> busDetailsDTOs;

    public BusStopOnClickListener(final MainActivity activity, final ViewGroup parent, final List<BusDetailsDTO> busDetailsDTOs){
        this.activity = activity;
        this.parent = parent;
        this.busDetailsDTOs = busDetailsDTOs;
    }

    @Override
    public void onClick(final View v) {
        if (busDetailsDTOs.size() == 1) {
            final BusDetailsDTO busDetails = busDetailsDTOs.get(0);
            new FavoritesAdapter.BusBoundAsyncTask(activity).execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
        } else {
            final PopupBusDetailsFavoritesAdapter ada = new PopupBusDetailsFavoritesAdapter(activity, busDetailsDTOs);
            final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, parent, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            listView.setAdapter(ada);
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setAdapter(ada, (dialog, position) -> {
                final BusDetailsDTO busDetails = busDetailsDTOs.get(position);
                new FavoritesAdapter.BusBoundAsyncTask(activity).execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
            });
            final int[] screenSize = Util.getScreenSize();
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
