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

package fr.cph.chicago.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.adapter.PopupBusAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;

/**
 * Direction task
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

    private final Activity activity;
    private final ViewGroup viewGroup;

    private BusRoute busRoute;
    private View convertView;
    private TrackerException trackerException;

    public DirectionAsyncTask(@NonNull final Activity activity, @NonNull final ViewGroup viewGroup) {
        this.activity = activity;
        this.viewGroup = viewGroup;
    }

    @Override
    protected final BusDirections doInBackground(final Object... params) {
        final CtaConnect connect = CtaConnect.getInstance();
        BusDirections busDirections = new BusDirections();
        try {
            final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
            busRoute = (BusRoute) params[0];
            convertView = (View) params[1];
            reqParams.put(activity.getString(R.string.request_rt), busRoute.getId());
            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = connect.connect(BUS_DIRECTION, reqParams);
            busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
        } catch (ParserException | ConnectException e) {
            this.trackerException = e;
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_direction, 0);
        return busDirections;
    }

    @Override
    protected final void onPostExecute(final BusDirections result) {
        if (trackerException == null) {
            final List<BusDirection> busDirections = result.getLBusDirection();
            final List<String> data = new ArrayList<>();
            for (final BusDirection busDir : busDirections) {
                data.add(busDir.getBusDirectionEnum().toString());
            }
            data.add(activity.getString(R.string.message_see_all_buses_on_line) + result.getId());

            final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, viewGroup, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            final PopupBusAdapter ada = new PopupBusAdapter(activity, data);
            listView.setAdapter(ada);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setAdapter(ada, (dialog, position) -> {
                final Bundle extras = new Bundle();
                if (position != data.size() - 1) {
                    final Intent intent = new Intent(activity, BusBoundActivity.class);
                    extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.getId());
                    extras.putString(activity.getString(R.string.bundle_bus_route_name), busRoute.getName());
                    extras.putString(activity.getString(R.string.bundle_bus_bound), busDirections.get(position).getBusTextReceived());
                    extras.putString(activity.getString(R.string.bundle_bus_bound_title), busDirections.get(position).getBusDirectionEnum().toString());
                    intent.putExtras(extras);
                    activity.startActivity(intent);
                } else {
                    final String[] busDirectionArray = new String[busDirections.size()];
                    int i = 0;
                    for (final BusDirection busDir : busDirections) {
                        busDirectionArray[i++] = busDir.getBusDirectionEnum().toString();
                    }
                    final Intent intent = new Intent(activity, BusMapActivity.class);
                    extras.putString(activity.getString(R.string.bundle_bus_route_id), result.getId());
                    extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), busDirectionArray);
                    intent.putExtras(extras);
                    activity.startActivity(intent);
                }
                convertView.setVisibility(LinearLayout.GONE);
            });
            builder.setOnCancelListener(dialog -> convertView.setVisibility(LinearLayout.GONE));
            final int[] screenSize = Util.getScreenSize();
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            Util.showNetworkErrorMessage(activity);
            convertView.setVisibility(LinearLayout.GONE);
        }
    }
}
