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

package fr.cph.chicago.app.listener;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.BusActivity;
import fr.cph.chicago.app.activity.MainActivity;
import fr.cph.chicago.app.adapter.PopupBusDetailsFavoritesAdapter;
import fr.cph.chicago.entity.BusDetailsDTO;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import rx.Observable;

public class BusStopOnClickListener implements View.OnClickListener {

    private static final String TAG = BusStopOnClickListener.class.getSimpleName();

    private final MainActivity activity;
    private final ViewGroup parent;
    private final List<BusDetailsDTO> busDetailsDTOs;

    public BusStopOnClickListener(final MainActivity activity, final ViewGroup parent, final List<BusDetailsDTO> busDetailsDTOs) {
        this.activity = activity;
        this.parent = parent;
        this.busDetailsDTOs = busDetailsDTOs;
    }

    @Override
    public void onClick(final View v) {
        if (busDetailsDTOs.size() == 1) {
            final BusDetailsDTO busDetails = busDetailsDTOs.get(0);
            loadBusDetails(busDetails);
        } else {
            final PopupBusDetailsFavoritesAdapter ada = new PopupBusDetailsFavoritesAdapter(activity, busDetailsDTOs);
            final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, parent, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            listView.setAdapter(ada);
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setAdapter(ada, (dialog, position) -> {
                final BusDetailsDTO busDetails = busDetailsDTOs.get(position);
                loadBusDetails(busDetails);
                Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_stop, 0);
            });
            final int[] screenSize = Util.getScreenSize();
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void loadBusDetails(final BusDetailsDTO busDetails) {
        ObservableUtil.createBusStopBoundObservable(busDetails.getBusRouteId(), busDetails.getBoundTitle())
            .subscribe(onNext -> {
                    Observable.from(onNext)
                        .filter(busStop -> Integer.toString(busStop.getId()).equals(busDetails.getStopId()))
                        .first()
                        .subscribe((BusStop busStop) -> {
                                final Intent intent = new Intent(App.getContext(), BusActivity.class);
                                final Bundle extras = new Bundle();
                                extras.putInt(activity.getString(R.string.bundle_bus_stop_id), busStop.getId());
                                extras.putString(activity.getString(R.string.bundle_bus_stop_name), busStop.getName());
                                extras.putString(activity.getString(R.string.bundle_bus_route_id), busDetails.getBusRouteId());
                                extras.putString(activity.getString(R.string.bundle_bus_route_name), busDetails.getRouteName());
                                extras.putString(activity.getString(R.string.bundle_bus_bound), busDetails.getBound());
                                extras.putString(activity.getString(R.string.bundle_bus_bound_title), busDetails.getBoundTitle());
                                extras.putDouble(activity.getString(R.string.bundle_bus_latitude), busStop.getPosition().getLatitude());
                                extras.putDouble(activity.getString(R.string.bundle_bus_longitude), busStop.getPosition().getLongitude());

                                intent.putExtras(extras);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            },
                            onError -> {
                                Log.e(TAG, onError.getMessage(), onError);
                                Util.showOopsSomethingWentWrong(parent);
                            });
                },
                onError -> {
                    Log.e(TAG, onError.getMessage(), onError);
                    Util.showNetworkErrorMessage(activity);
                }
            );
    }
}
