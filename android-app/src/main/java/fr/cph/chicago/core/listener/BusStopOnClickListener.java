/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.BusActivity;
import fr.cph.chicago.core.adapter.PopupBusDetailsFavoritesAdapter;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.dto.BusDetailsDTO;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;

import static fr.cph.chicago.Constants.BUSES_STOP_URL;

public class BusStopOnClickListener implements View.OnClickListener {

    private static final String TAG = BusStopOnClickListener.class.getSimpleName();

    private final Context context;
    private final ViewGroup parent;
    private final List<BusDetailsDTO> busDetailsDTOs;

    public BusStopOnClickListener(final Context context, final ViewGroup parent, final List<BusDetailsDTO> busDetailsDTOs) {
        this.context = context;
        this.parent = parent;
        this.busDetailsDTOs = busDetailsDTOs;
    }

    @Override
    public void onClick(final View view) {
        if (busDetailsDTOs.size() == 1) {
            final BusDetailsDTO busDetails = busDetailsDTOs.get(0);
            loadBusDetails(view, busDetails);
        } else {
            final PopupBusDetailsFavoritesAdapter ada = new PopupBusDetailsFavoritesAdapter(context, busDetailsDTOs);
            final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = vi.inflate(R.layout.popup_bus, parent, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            listView.setAdapter(ada);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setAdapter(ada, (dialog, position) -> {
                final BusDetailsDTO busDetails = busDetailsDTOs.get(position);
                loadBusDetails(view, busDetails);
                Util.trackAction(context, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_STOP_URL);
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout((int) (App.getScreenWidth() * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private void loadBusDetails(final View view, final BusDetailsDTO busDetails) {
        ObservableUtil.createBusStopBoundObservable(context.getApplicationContext(), busDetails.getBusRouteId(), busDetails.getBoundTitle())
            .subscribe(onNext ->
                    Observable.fromIterable(onNext)
                        .filter(busStop -> Integer.toString(busStop.getId()).equals(busDetails.getStopId()))
                        .firstElement()
                        .subscribe((BusStop busStop) -> {
                                final Intent intent = new Intent(context.getApplicationContext(), BusActivity.class);
                                final Bundle extras = new Bundle();
                                extras.putInt(context.getString(R.string.bundle_bus_stop_id), busStop.getId());
                                extras.putString(context.getString(R.string.bundle_bus_stop_name), busStop.getName());
                                extras.putString(context.getString(R.string.bundle_bus_route_id), busDetails.getBusRouteId());
                                extras.putString(context.getString(R.string.bundle_bus_route_name), busDetails.getRouteName());
                                extras.putString(context.getString(R.string.bundle_bus_bound), busDetails.getBound());
                                extras.putString(context.getString(R.string.bundle_bus_bound_title), busDetails.getBoundTitle());
                                extras.putDouble(context.getString(R.string.bundle_bus_latitude), busStop.getPosition().getLatitude());
                                extras.putDouble(context.getString(R.string.bundle_bus_longitude), busStop.getPosition().getLongitude());

                                intent.putExtras(extras);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            },
                            onError -> {
                                Log.e(TAG, onError.getMessage(), onError);
                                Util.showOopsSomethingWentWrong(parent);
                            }),
                onError -> {
                    Log.e(TAG, onError.getMessage(), onError);
                    Util.showNetworkErrorMessage(view);
                }
            );
    }
}
