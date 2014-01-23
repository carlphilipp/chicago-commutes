package fr.cph.chicago.activity;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.xml.Xml;

public class BusActivity extends Activity {

	/** Tag **/
	private static final String TAG = "BusActivity";

	List<BusArrival> busArrivals;

	private String busRouteName;
	private String busRouteId;
	private String bound;
	private Integer busStopId;
	private ImageView streetViewImage, mapImage, directionImage;
	private Position position;
	private Menu menu;
	private LinearLayout stopsView;
	private boolean firstLoad = true;
	private boolean isFavorite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load right xml
		setContentView(R.layout.activity_bus);

		this.isFavorite = isFavorite();

		busStopId = getIntent().getExtras().getInt("busStopId");
		String busStopName = getIntent().getExtras().getString("busStopName");
		busRouteId = getIntent().getExtras().getString("busRouteId");
		busRouteName = getIntent().getExtras().getString("busRouteName");
		bound = getIntent().getExtras().getString("bound");
		position = new Position();
		position.setLatitude(getIntent().getExtras().getDouble("latitude"));
		position.setLongitude(getIntent().getExtras().getDouble("longitude"));

		stopsView = (LinearLayout) findViewById(R.id.activity_bus_stops);

		TextView busRouteNameView = (TextView) findViewById(R.id.activity_bus_station_name);
		busRouteNameView.setText(busStopName);

		TextView busRouteNameView2 = (TextView) findViewById(R.id.activity_bus_value);
		busRouteNameView2.setText(busRouteName + " (" + bound + ")");

		streetViewImage = (ImageView) findViewById(R.id.activity_bus_streetview_image);

		mapImage = (ImageView) findViewById(R.id.activity_bus_map_image);

		directionImage = (ImageView) findViewById(R.id.activity_bus_map_direction);

		new DisplayGoogleStreetPicture().execute(position);

		(new LoadData()).execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_no_search, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Bus");

		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();

		return true;
	}

	private class LoadData extends AsyncTask<Void, Void, List<BusArrival>> {

		@Override
		protected List<BusArrival> doInBackground(Void... params) {
			MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
			reqParams.put("rt", busRouteId);
			reqParams.put("stpid", String.valueOf(busStopId));
			CtaConnect connect = CtaConnect.getInstance();
			try {
				Xml xml = new Xml();
				String xmlResult = connect.connect(CtaRequestType.BUS_ARRIVALS, reqParams);
				return xml.parseBusArrivals(xmlResult);
			} catch (IOException e) {
				e.printStackTrace();
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<BusArrival> result) {
			BusActivity.this.busArrivals = result;
			BusActivity.this.buildArrivals();
		}

	}

	private class DisplayGoogleStreetPicture extends AsyncTask<Position, Void, Drawable> {
		private Position position;

		@Override
		protected Drawable doInBackground(Position... params) {
			GStreetViewConnect connect = GStreetViewConnect.getInstance();
			try {
				this.position = params[0];
				return connect.connect(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Drawable result) {
			int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) BusActivity.this.streetViewImage
					.getLayoutParams();
			ViewGroup.LayoutParams params2 = BusActivity.this.streetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BusActivity.this.streetViewImage.setLayoutParams(params2);
			BusActivity.this.streetViewImage.setImageDrawable(result);
			BusActivity.this.streetViewImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", position.getLatitude(),
							position.getLongitude());
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0",
								position.getLatitude(), position.getLongitude());
						Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			BusActivity.this.mapImage.setImageDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.da_turn_arrive));
			BusActivity.this.mapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});
			BusActivity.this.directionImage.setImageDrawable(ChicagoTracker.getAppContext().getResources()
					.getDrawable(R.drawable.ic_directions_walking));
			BusActivity.this.directionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
			firstLoad = false;
		}
	}

	public void buildArrivals() {
		if (busArrivals != null) {
			Map<String, TextView> mapRes = new HashMap<String, TextView>();
			for (BusArrival arrival : this.busArrivals) {
				if (arrival.getRouteDirection().equals(bound)) {
					String destination = arrival.getBusDestination();
					if (mapRes.containsKey(destination)) {
						TextView arrivalView = mapRes.get(destination);
						arrivalView.setText(arrivalView.getText() + " " + arrival.getTimeLeft());
					} else {
						TextView arrivalView = new TextView(ChicagoTracker.getAppContext());
						arrivalView.setText(arrival.getBusDestination() + ": " + arrival.getTimeLeft());
						arrivalView.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey));
						mapRes.put(destination, arrivalView);
					}
				}
			}
			for (Entry<String, TextView> entry : mapRes.entrySet()) {
				stopsView.addView(entry.getValue());
			}
		}
	}

	public boolean isFavorite() {
		// boolean isFavorite = false;
		// List<Integer> favorites = Preferences.getFavorites(ChicagoTracker.PREFERENCE_FAVORITES);
		// for (Integer fav : favorites) {
		// if (fav.intValue() == stationId.intValue()) {
		// isFavorite = true;
		// }
		// }
		// return isFavorite;
		return false;
	}
}
