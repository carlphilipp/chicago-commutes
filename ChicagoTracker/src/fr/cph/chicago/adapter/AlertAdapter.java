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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.Alert;
import fr.cph.chicago.entity.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that will handle alerts
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class AlertAdapter extends BaseAdapter {
	/** The list of alerts **/
	private List<Alert> mAlerts;

	/** The constructor **/
	public AlertAdapter() {
		if (DataHolder.getInstance().getAlertData() != null) {
			mAlerts = DataHolder.getInstance().getAlertData().getAlerts();
		} else {
			mAlerts = new ArrayList<Alert>();
		}
	}

	@Override
	public final int getCount() {
		return mAlerts.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mAlerts.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return mAlerts.get(position).getId();
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		Alert alert = (Alert) getItem(position);

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_alert, null);

			ViewHolder holder = new ViewHolder();
			TextView lineView = (TextView) convertView.findViewById(R.id.alert_line);
			StringBuilder stb = new StringBuilder();
			for (Service service : alert.getImpactedServices()) {
				if (service.getType().equals("T")) {
					stb.append(service.getName() + ", ");
				} else {
					stb.append(service.getId() + ", ");
				}
			}
			String res = stb.toString().substring(0, stb.toString().length() - 2);
			lineView.setText(res);
			holder.lineView = lineView;

			TextView headLineView = (TextView) convertView.findViewById(R.id.alert_headline);
			headLineView.setText(alert.getHeadline());
			holder.headLineView = headLineView;

			TextView descrView = (TextView) convertView.findViewById(R.id.alert_short_description);
			descrView.setText(alert.getShortDescription());
			holder.descrView = descrView;

			convertView.setTag(holder);
		}

		return convertView;
	}

	/**
	 * DP View holder
	 *
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	static class ViewHolder {
		TextView lineView;
		TextView headLineView;
		TextView descrView;
	}

	public final void setAlerts(final List<Alert> alerts) {
		this.mAlerts = alerts;
	}

}
