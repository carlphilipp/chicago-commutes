package fr.cph.chicago.adapter;

import java.util.List;

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

/**
 * 
 * @author carl
 * 
 */
public final class AlertAdapter extends BaseAdapter {

	/** **/
	private List<Alert> alerts;

	/**
	 * 
	 */
	public AlertAdapter() {
		alerts = DataHolder.getInstance().getAlertData().getAlerts();
	}

	@Override
	public final int getCount() {
		return alerts.size();
	}

	@Override
	public final Object getItem(final int position) {
		return alerts.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return alerts.get(position).getId();
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
	 * 
	 * @author carl
	 * 
	 */
	static class ViewHolder {
		TextView lineView;
		TextView headLineView;
		TextView descrView;
	}

}
