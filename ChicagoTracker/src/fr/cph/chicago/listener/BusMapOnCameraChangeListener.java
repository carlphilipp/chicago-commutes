package fr.cph.chicago.listener;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;

public class BusMapOnCameraChangeListener implements OnCameraChangeListener {

	private float currentZoom = -1;
	private float oldZoom = -1;
	private Bitmap bhalfsize1;
	private Bitmap bhalfsize2;
	private Bitmap bhalfsize3;
	private List<Marker> mBusMarkers;
	private List<Marker> mBusStationMarkers;

	public BusMapOnCameraChangeListener() {
		Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getAppContext().getResources(), R.drawable.bus_gta);
		this.bhalfsize1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 11, icon.getHeight() / 11, false);
		this.bhalfsize2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 6, icon.getHeight() / 6, false);
		this.bhalfsize3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 4, icon.getHeight() / 4, false);
	}

	public void setBusMarkers(List<Marker> busMarkers) {
		this.mBusMarkers = busMarkers;
	}

	public void setBusStationMarkers(List<Marker> busStationMarkers) {
		this.mBusStationMarkers = busStationMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom != currentZoom) {
			oldZoom = currentZoom;
			currentZoom = position.zoom;
			if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
				for (Marker marker : mBusMarkers) {
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize1));
				}
			} else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
				for (Marker marker : mBusMarkers) {
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize2));
				}
			} else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
				for (Marker marker : mBusMarkers) {
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize3));
				}
			}
			if (isIn(currentZoom, 21f, 16f) && !isIn(oldZoom, 21f, 16f)) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(true);
				}
			} else if (!isIn(currentZoom, 21f, 16f) && isIn(oldZoom, 21f, 16f)) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(false);
				}
			}
		}
	}

	public boolean isIn(float num, float sup, float inf) {
		return num >= inf && num <= sup;
	}

}
