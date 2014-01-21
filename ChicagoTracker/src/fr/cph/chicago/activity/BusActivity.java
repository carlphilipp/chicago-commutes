package fr.cph.chicago.activity;

import java.io.IOException;
import java.util.Locale;

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
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.entity.Position;

public class BusActivity extends Activity {
	
	private String busRouteName;
	private ImageView streetViewImage, mapImage, directionImage;
	private Position position;
	private Menu menu;
	private boolean firstLoad = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load right xml
		setContentView(R.layout.activity_bus);

		Integer busStopId = getIntent().getExtras().getInt("busStopId");
		String busStopName = getIntent().getExtras().getString("busStopName");
		String busRouteId = getIntent().getExtras().getString("busRouteId");
		busRouteName = getIntent().getExtras().getString("busRouteName");
		position = new Position();
		position.setLatitude(getIntent().getExtras().getDouble("latitude"));
		position.setLongitude(getIntent().getExtras().getDouble("longitude"));
		
		TextView busRouteNameView = (TextView) findViewById(R.id.activity_bus_station_name);
		busRouteNameView.setText(busStopName);
		
		streetViewImage = (ImageView) findViewById(R.id.activity_bus_streetview_image);
		
		mapImage = (ImageView) findViewById(R.id.activity_bus_map_image);
		
		directionImage = (ImageView) findViewById(R.id.activity_bus_map_direction);
		
		new DisplayGoogleStreetPicture().execute(position);
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
		actionBar.setTitle(busRouteName);

		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();

		return true;
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

						// To get direction
						// String uri = "http://maps.google.com/?f=d&daddr="+position.getLatitude() + "," + position.getLongitude();
						// Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						// i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						// startActivity(i);
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

}
