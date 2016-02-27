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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.adapter.PopupBusAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Direction task
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

	private Activity activity;
	private BusRoute busRoute;
	private View convertView;
	private TrackerException trackerException;

	public DirectionAsyncTask(final Activity activity) {
		this.activity = activity;
	}

	@Override
	protected final BusDirections doInBackground(final Object... params) {
		final CtaConnect connect = CtaConnect.getInstance();
		BusDirections busDirections = null;
		try {
			final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
			busRoute = (BusRoute) params[0];
			reqParams.put("rt", busRoute.getId());
			final Xml xml = new Xml();
			final String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
			busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
			convertView = (View) params[1];
		} catch (ParserException | ConnectException e) {
			this.trackerException = e;
		}
		Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus,
				R.string.analytics_action_get_bus_direction, 0);
		return busDirections;
	}

	@Override
	protected final void onPostExecute(final BusDirections result) {
		if (trackerException == null) {
			final List<BusDirection> busDirections = result.getlBusDirection();
			final List<String> data = new ArrayList<>();
			for (BusDirection busDir : busDirections) {
				data.add(busDir.toString());
			}
			data.add("Follow all buses on line " + result.getId());

			final LayoutInflater layoutInflater = (LayoutInflater) activity.getBaseContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View popupView = layoutInflater.inflate(R.layout.popup_bus, null);
			final ListView listView = (ListView) popupView.findViewById(R.id.details);
			final PopupBusAdapter ada = new PopupBusAdapter(activity, data);
			listView.setAdapter(ada);

			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setAdapter(ada, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(final DialogInterface dialog, final int position) {
					if (position != data.size() - 1) {
						final Intent intent = new Intent(activity, BusBoundActivity.class);
						final Bundle extras = new Bundle();
						extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.getId());
						extras.putString(activity.getString(R.string.bundle_bus_route_name), busRoute.getName());
						extras.putString(activity.getString(R.string.bundle_bus_bound), data.get(position));
						intent.putExtras(extras);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ChicagoTracker.getContext().startActivity(intent);
					} else {
						final String[] busDirectionArray = new String[busDirections.size()];
						int i = 0;
						for (final BusDirection busDir : busDirections) {
							busDirectionArray[i++] = busDir.toString();
						}
						final Intent intent = new Intent(ChicagoTracker.getContext(), BusMapActivity.class);
						final Bundle extras = new Bundle();
						extras.putString(activity.getString(R.string.bundle_bus_route_id), result.getId());
						extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), busDirectionArray);
						intent.putExtras(extras);
						activity.startActivity(intent);
					}
					convertView.setVisibility(LinearLayout.GONE);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					convertView.setVisibility(LinearLayout.GONE);
				}
			});

			final AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			ChicagoTracker.displayError(activity, trackerException);
		}
	}
}
