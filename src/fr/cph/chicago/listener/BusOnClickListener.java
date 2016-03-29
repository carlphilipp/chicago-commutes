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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.FavoritesAdapter;
import fr.cph.chicago.adapter.PopupBusFavoritesAdapter;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.util.Util;

/**
 * Favorites bus on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusOnClickListener implements OnClickListener {

    private ViewGroup group;
    private MainActivity activity;
    private BusRoute busRoute;
    private Map<String, List<BusArrival>> mapBusArrivals;

    public BusOnClickListener(@NonNull final MainActivity activity,
                              @NonNull final ViewGroup group,
                              @NonNull final BusRoute busRoute,
                              @NonNull final Map<String, List<BusArrival>> mapBusArrivals) {
        this.activity = activity;
        this.group = group;
        this.busRoute = busRoute;
        this.mapBusArrivals = mapBusArrivals;
    }

    @Override
    public void onClick(final View v) {
        if (!Util.isNetworkAvailable()) {
            Util.showNetworkErrorMessage(activity);
        } else {
            final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, group, false);

            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            final List<String> values = extractValues();

            final PopupBusFavoritesAdapter ada = new PopupBusFavoritesAdapter(activity, values);
            listView.setAdapter(ada);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setAdapter(ada, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int position) {
                    int i = 0;
                    for (final String key : mapBusArrivals.keySet()) {
                        final BusArrival busArrival = mapBusArrivals.get(key).get(0);
                        if (position == i) {
                            // Open details
                            final String boundTitle = busArrival.getRouteDirection();
                            final BusDirection.BusDirectionEnum busDirectionEnum = BusDirection.BusDirectionEnum.fromString(boundTitle);
                            new FavoritesAdapter.BusBoundAsyncTask(activity).execute(busArrival.getRouteId(), busDirectionEnum.getShortUpperCase(), boundTitle,
                                    String.valueOf(busArrival.getStopId()), busRoute.getName());
                        }
                        i++;
                    }
                    for (final Entry<String, List<BusArrival>> entry : mapBusArrivals.entrySet()) {
                        final List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
                        for (final BusArrival arrival : arrivals) {
                            if (position == i) {
                                // Follow a bus in google map view
                                final Intent intent = new Intent(App.getContext(), BusMapActivity.class);
                                final Bundle extras = new Bundle();
                                extras.putInt(activity.getString(R.string.bundle_bus_id), arrival.getBusId());
                                extras.putString(activity.getString(R.string.bundle_bus_route_id), arrival.getRouteId());
                                extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), new String[]{entry.getKey()});
                                intent.putExtras(extras);
                                activity.startActivity(intent);
                            }
                            i++;
                        }
                    }
                    if (position == i) {
                        // Follow all buses on that route in google map view
                        final Set<String> bounds = new HashSet<>();
                        for (final Entry<String, List<BusArrival>> stringListEntry : mapBusArrivals.entrySet()) {
                            bounds.add(stringListEntry.getKey());
                        }
                        final Intent intent = new Intent(App.getContext(), BusMapActivity.class);
                        final Bundle extras = new Bundle();
                        extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.getId());
                        extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), bounds.toArray(new String[bounds.size()]));
                        intent.putExtras(extras);
                        activity.startActivity(intent);
                    }
                }
            });
            final int[] screenSize = Util.getScreenSize();
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private List<String> extractValues() {
        final List<String> values = new ArrayList<>();
        final Set<String> keySet = mapBusArrivals.keySet();
        // In case of several bounds, we put at the top of the list the ability to open details for those bounds. Very not common
        for (final String key : keySet) {
            final StringBuilder openDetails = new StringBuilder("Open details");
            if (keySet.size() > 1) {
                openDetails.append(" (").append(key).append(")");
            }
            values.add(openDetails.toString());
        }
        // Add all the incoming buses to the list.
        final Set<Entry<String, List<BusArrival>>> entrySet = mapBusArrivals.entrySet();
        for (final Entry<String, List<BusArrival>> entry : entrySet) {
            final List<BusArrival> arrivals = BusArrival.getRealBusArrival(entry.getValue());
            for (final BusArrival arrival : arrivals) {
                final StringBuilder sb = new StringBuilder();
                sb.append("See bus - ").append(arrival.getTimeLeftDueDelay());
                if (entrySet.size() > 1) {
                    sb.append(" (").append(entry.getKey()).append(")");
                }
                values.add(sb.toString());
            }
        }
        values.add("See all buses on line " + busRoute.getId());
        return values;
    }
}
