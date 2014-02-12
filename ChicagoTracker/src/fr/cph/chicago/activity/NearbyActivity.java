package fr.cph.chicago.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.cph.chicago.R;

public class NearbyActivity extends Activity {

	static final LatLng CHICAGO = new LatLng(41.8819,-87.6278);
	static final LatLng SKOKIE = new LatLng(42.0333, -87.7428);
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		Marker chicago = map.addMarker(new MarkerOptions().position(CHICAGO).title("Chicago"));
		Marker skokie = map.addMarker(new MarkerOptions().position(SKOKIE).title("Skokie").snippet("Cameleon software (:"));
//				.icon(BitmapDescriptorFactory.fromResource(R.drawable.left_arrow)));

		// Move the camera instantly to hamburg with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CHICAGO, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.global, menu);
		return true;
	}

}
