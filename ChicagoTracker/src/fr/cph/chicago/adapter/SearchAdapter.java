package fr.cph.chicago.adapter;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusBoundActivity;
import fr.cph.chicago.activity.SearchActivity;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.xml.Xml;

public final class SearchAdapter extends BaseAdapter {

	/** Tag **/
	private static final String TAG = "SearchAdapter";

	private List<Station> trains;
	private List<BusRoute> buses;

	private Context context;
	private SearchActivity activity;
	private FrameLayout container;

	public SearchAdapter(final SearchActivity activity, final FrameLayout container) {
		this.context = ChicagoTracker.getAppContext();
		this.activity = activity;
		this.container = container;
	}

	@Override
	public final int getCount() {
		return trains.size() + buses.size();
	}

	@Override
	public final Object getItem(final int position) {
		Object object = null;
		if (position < trains.size()) {
			object = trains.get(position);
		} else {
			object = buses.get(position - trains.size());
		}
		return object;
	}

	@Override
	public final long getItemId(final int position) {
		return 0;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_search, null);

		TextView rounteName = (TextView) convertView.findViewById(R.id.route_name_value);

		if (position < trains.size()) {
			final Station station = trains.get(position);
			Set<TrainLine> lines = station.getLines();

			rounteName.setText(station.getName());

			LinearLayout stationColorView = (LinearLayout) convertView.findViewById(R.id.station_color);

			int indice = 0;
			for (TrainLine tl : lines) {
				TextView textView = new TextView(context);
				textView.setBackgroundColor(tl.getColor());
				textView.setText(" ");
				textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
				stationColorView.addView(textView);
				if (indice != lines.size()) {
					textView = new TextView(context);
					textView.setText("");
					textView.setPadding(0, 0, (int) context.getResources().getDimension(R.dimen.activity_list_station_colors_space), 0);
					textView.setTextSize(context.getResources().getDimension(R.dimen.activity_list_station_colors));
					stationColorView.addView(textView);
				}
				indice++;
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
					Bundle extras = new Bundle();
					extras.putInt("stationId", station.getId());
					intent.putExtras(extras);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(intent);
					activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
				}
			});
		} else {
			final BusRoute busRoute = buses.get(position - trains.size());

			TextView type = (TextView) convertView.findViewById(R.id.train_bus_type);
			type.setText("B");

			rounteName.setText(busRoute.getId() + " " + busRoute.getName());

			final TextView loadingTextView = (TextView) convertView.findViewById(R.id.loading_text_view);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					loadingTextView.setVisibility(LinearLayout.VISIBLE);
					activity.startRefreshAnimation();
					new DirectionAsyncTask().execute(busRoute, loadingTextView);
				}
			});
		}

		return convertView;
	}

	private class DirectionAsyncTask extends AsyncTask<Object, Void, BusDirections> {

		private BusRoute busRoute;
		private TextView convertView;
		private TrackerException trackerException;

		@Override
		protected final BusDirections doInBackground(final Object... params) {
			CtaConnect connect = CtaConnect.getInstance();
			BusDirections busDirections = null;
			try {
				MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
				busRoute = (BusRoute) params[0];
				reqParams.put("rt", busRoute.getId());
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.BUS_DIRECTION, reqParams);
				busDirections = xml.parseBusDirections(xmlResult, busRoute.getId());
				convertView = (TextView) params[1];
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			return busDirections;
		}

		@Override
		protected final void onPostExecute(final BusDirections result) {
			activity.stopRefreshAnimation();
			if (trackerException == null) {
				PopupMenu popupMenu = new PopupMenu(ChicagoTracker.getAppContext(), convertView);
				final List<BusDirection> lBus = result.getlBusDirection();
				for (int i = 0; i < lBus.size(); i++) {
					popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, lBus.get(i).toString());
				}
				popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Intent intent = new Intent(ChicagoTracker.getAppContext(), BusBoundActivity.class);
						Bundle extras = new Bundle();
						extras.putString("busRouteId", busRoute.getId());
						extras.putString("busRouteName", busRoute.getName());
						extras.putString("bound", lBus.get(item.getItemId()).toString());
						intent.putExtras(extras);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ChicagoTracker.getAppContext().startActivity(intent);
						return false;
					}
				});
				popupMenu.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(PopupMenu menu) {
						container.getForeground().setAlpha(0);
						convertView.setVisibility(LinearLayout.GONE);
						activity.stopRefreshAnimation();
					}
				});
				container.getForeground().setAlpha(210);
				popupMenu.show();
			} else {
				ChicagoTracker.displayError(activity, trackerException);
			}
		}
	}

	public void updateData(List<Station> trains, List<BusRoute> buses) {
		this.trains = trains;
		this.buses = buses;
	}
}
