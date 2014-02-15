package fr.cph.chicago.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusStop;

public final class NearbyAdapter extends BaseAdapter {

	private Context context;
	private MainActivity activity;
	private List<BusStop> busStops;
	private SparseArray<Map<String, List<BusArrival>>> busArrivals;

	public NearbyAdapter(final MainActivity activity) {
		this.context = ChicagoTracker.getAppContext();
		this.activity = activity;
		this.busStops = new ArrayList<BusStop>();
		this.busArrivals = new SparseArray<Map<String, List<BusArrival>>>();
	}

	@Override
	public final int getCount() {
		return busStops.size();
	}

	@Override
	public final Object getItem(int position) {
		return busStops.get(position);
	}

	@Override
	public final long getItemId(int position) {
		return busStops.get(position).getId();
	}

	@SuppressLint("NewApi")
	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_nearby, null);

		BusStop busStop = busStops.get(position);

		TextView typeView = (TextView) convertView.findViewById(R.id.train_bus_type);
		typeView.setText("B");

		TextView routeView = (TextView) convertView.findViewById(R.id.route_name_value);
		routeView.setText(busStop.getName());

		LinearLayout resultLayout = (LinearLayout) convertView.findViewById(R.id.nearby_bus_result);
		LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		int line1PaddingColor = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
		int stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);

		if (busArrivals.size() > 0) {
			for (Entry<String, List<BusArrival>> entry : busArrivals.get(busStop.getId()).entrySet()) {
				LinearLayout llh = new LinearLayout(context);
				llh.setLayoutParams(paramsLayout);
				llh.setOrientation(LinearLayout.HORIZONTAL);
				llh.setPadding(line1PaddingColor, stopsPaddingTop, 0, 0);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					llh.setBackground(context.getResources().getDrawable(R.drawable.any_selector));
				}

				TextView tlView = new TextView(context);
				tlView.setBackgroundColor(context.getResources().getColor(R.color.black));
				tlView.setText("   ");
				tlView.setLayoutParams(paramsTextView);
				llh.addView(tlView);

				final String key = entry.getKey();
				final List<BusArrival> value = entry.getValue();

//				llh.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (value.entrySet().size() == 1) {
//							BusArrival busArrival = value.entrySet().iterator().next().getValue().get(0);
//							activity.startRefreshAnimation();
//							// new BusBoundAsyncTask().execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
//							// String.valueOf(busArrival.getStopId()), key);
//						} else {
//							List<String> menuTitles = new ArrayList<String>();
//							for (Entry<String, List<BusArrival>> entry : value.entrySet()) {
//								menuTitles.add(entry.getKey());
//							}
//							PopupMenu popupMenu = new PopupMenu(context, v);
//							for (int i = 0; i < menuTitles.size(); i++) {
//								popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuTitles.get(i));
//							}
//							popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//								@Override
//								public boolean onMenuItemClick(MenuItem item) {
//									Iterator<Entry<String, List<BusArrival>>> iterator = value.entrySet().iterator();
//									if (item.getItemId() == 1) {
//										iterator.next();
//									} else if (item.getItemId() == 2) {
//										iterator.next();
//										iterator.next();
//									} else if (item.getItemId() == 3) {
//										iterator.next();
//										iterator.next();
//										iterator.next();
//									}
//									BusArrival busArrival = iterator.next().getValue().get(0);
//									// new BusBoundAsyncTask().execute(busArrival.getRouteId(), busArrival.getRouteDirection(),
//									// String.valueOf(busArrival.getStopId()), key);
//									activity.startRefreshAnimation();
//									return false;
//								}
//							});
//							popupMenu.setOnDismissListener(new OnDismissListener() {
//								@Override
//								public void onDismiss(PopupMenu menu) {
//									// firstLayout.getForeground().setAlpha(0);
//								}
//							});
//							// firstLayout.getForeground().setAlpha(210);
//							popupMenu.show();
//						}
//					}
//				});

				LinearLayout stopLayout = new LinearLayout(context);
				stopLayout.setOrientation(LinearLayout.VERTICAL);
				stopLayout.setPadding(line1PaddingColor, 0, 0, 0);

//				TextView stopName = new TextView(context);
//				stopName.setText(String.valueOf(key));
//				stopName.setTextColor(context.getResources().getColor(R.color.grey_5));
//				stopName.setTypeface(Typeface.DEFAULT_BOLD);
//
//				stopLayout.addView(stopName);

//				for (Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
					String key2 = key;
					List<BusArrival> buses = value;

					LinearLayout boundLayout = new LinearLayout(context);
					boundLayout.setOrientation(LinearLayout.HORIZONTAL);

					TextView bound = new TextView(context);
					bound.setText(key2 + ": ");
					bound.setTextColor(context.getResources().getColor(R.color.grey_5));
					boundLayout.addView(bound);

					for (BusArrival arri : buses) {
						TextView timeView = new TextView(context);
						timeView.setText(arri.getTimeLeftDueDelay() + " ");
						timeView.setTextColor(context.getResources().getColor(R.color.grey));
						timeView.setLines(1);
						timeView.setEllipsize(TruncateAt.END);
						boundLayout.addView(timeView);
					}
					stopLayout.addView(boundLayout);
//				}
				llh.addView(stopLayout);
				resultLayout.addView(llh);
			}
		}
		return convertView;
	}

	public final void setBusStops(final List<BusStop> busStops, final SparseArray<Map<String, List<BusArrival>>> busArrivals) {
		this.busStops = busStops;
		this.busArrivals = busArrivals;
	}

}
