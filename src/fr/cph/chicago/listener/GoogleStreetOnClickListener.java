package fr.cph.chicago.listener;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.util.Locale;

public class GoogleStreetOnClickListener implements View.OnClickListener {

	private Activity activity;
	private double latitude;
	private double longitude;

	public GoogleStreetOnClickListener(final Activity activity, final double latitude, final double longitude) {
		this.activity = activity;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public void onClick(final View v) {
		String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", latitude, longitude);
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		try {
			activity.startActivity(intent);
		} catch (final ActivityNotFoundException ex) {
			uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0", latitude, longitude);
			final Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			activity.startActivity(unrestrictedIntent);
		}
	}
}
